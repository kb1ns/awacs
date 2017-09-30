/**
 * Copyright 2016-2017 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.awacs.agent.net;

import io.awacs.agent.AWACS;
import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.net.Packet;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketAccumulator implements Configurable {

    private static Logger log = Logger.getLogger("AWACS");

    private volatile boolean closed = false;

    private AtomicInteger bufferNumbers = new AtomicInteger(0);

    private Channels channels;

    private ArrayBlockingQueue<Packet> queue;

    private ConcurrentLinkedDeque<ByteBuffer> buffers;

    private int maxBatchBytes;

    private int maxBatchNumbers;

    private int maxAppendMs;

    private int batchLingerMs;

    @Override
    public void init(Configuration configuration) {
        maxBatchBytes = configuration.getInteger(AWACS.CONFIG_MAX_BATCH_BYTES, AWACS.DEFAULT_MAX_BATCH_BYTES);
        maxBatchNumbers = configuration.getInteger(AWACS.CONFIG_MAX_BATCH_NUMBERS, AWACS.DEFAULT_MAX_BATCH_NUMBERS);
        maxAppendMs = configuration.getInteger(AWACS.CONFIG_MAX_APPEND_MS, AWACS.DEFAULT_MAX_APPEND_MS);
        batchLingerMs = configuration.getInteger(AWACS.CONFIG_BATCH_LINGER_MS, AWACS.DEFAULT_BATCH_LINGER_MS);
        queue = new ArrayBlockingQueue<>(configuration.getInteger(AWACS.CONFIG_MAX_WAITING_MESSAGE, AWACS.DEFAULT_MAX_WAITING_MESSAGE));
        channels = new Channels(configuration.getArray(AWACS.CONFIG_SERVER), configuration.getInteger(AWACS.CONFIG_TIMEOUT_MS, AWACS.DEFAULT_TIMEOUT_MS));
        buffers = new ConcurrentLinkedDeque<>();
        start();
    }

    private void writeBatch() {
        final ByteBuffer buf = buffers.poll();
        channels.flush(buf, new Callback() {
            @Override
            public void onComplete() {
                buf.clear();
                buffers.add(buf);
            }

            @Override
            public void onException(Throwable t) {
                buf.clear();
                buffers.add(buf);
            }
        });
    }

    private void write(Packet packet) {
        ByteBuffer buf = ByteBuffer.wrap(packet.serialize());
        channels.flush(buf, null);
    }

    private boolean allocateNewBuffer(Packet init) {
        if (bufferNumbers.get() >= maxBatchNumbers) {
            log.log(Level.WARNING, "Couldn't allocate more buffers.");
            return false;
        }
        ByteBuffer buf = ByteBuffer.allocate(maxBatchBytes);
        buf.put(init.serialize());
        buffers.push(buf);
        return true;
    }

    private void start() {
        do {
            buffers.add(ByteBuffer.allocate(maxBatchBytes));
        } while (bufferNumbers.getAndIncrement() < maxBatchNumbers / 2);
        log.log(Level.INFO, "{0}x{1} bytes memory allocated.", new Integer[]{maxBatchBytes, bufferNumbers.get()});
        //make sure only this thread can access buffers
        Thread t = new Thread(new Runnable() {
            long newBatchCreated = 0l;

            @Override
            public void run() {
                while (!closed) {
                    try {
                        Packet p = queue.take();
                        if (!buffers.isEmpty()) {
                            ByteBuffer batch = buffers.peek();
                            if (batch.remaining() == batch.capacity()) {
                                batch.put(p.serialize());
                                newBatchCreated = System.currentTimeMillis();
                            } else if (batch.remaining() >= p.size()) {
                                batch.put(p.serialize());
                                if (System.currentTimeMillis() - newBatchCreated > batchLingerMs) {
                                    writeBatch();
                                }
                            } else {
                                writeBatch();
                                ByteBuffer free = buffers.peek();
                                if (free != null) {
                                    free.put(p.serialize());
                                    newBatchCreated = System.currentTimeMillis();
                                } else if (allocateNewBuffer(p)) {
                                    newBatchCreated = System.currentTimeMillis();
                                }
                            }
                        } else {
                            log.warning("All buffers are full, try to allocate a new buffer.");
                            if (allocateNewBuffer(p)) {
                                newBatchCreated = System.currentTimeMillis();
                            } else {
                                write(p);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void enqueue(final Packet packet) {
        if (closed) {
            log.warning("Sender has been closed.");
            return;
        }
        if (packet.size() > maxBatchBytes) {
            write(packet);
            return;
        }
        try {
            queue.offer(packet, maxAppendMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
    }

    public void close() {
        closed = true;
        //TODO drainTo channel
    }
}
