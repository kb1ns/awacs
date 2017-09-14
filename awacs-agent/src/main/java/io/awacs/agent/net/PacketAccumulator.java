package io.awacs.agent.net;

import io.awacs.agent.AWACS;
import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.net.Packet;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketAccumulator implements Configurable {

    private static Logger log = Logger.getLogger("AWACS");

    private volatile boolean closed;

    private Channels channels;

    private ArrayBlockingQueue<Packet> queue;

    private ConcurrentLinkedDeque<ByteBuffer> buffers;

    private int maxBatchBytes;

    private int maxBatchNumbers;

    private int maxAppendMs;

    private int batchLingerMs;

    private int timeout;

    @Override
    public void init(Configuration configuration) {
        maxBatchBytes = configuration.getInteger(AWACS.CONFIG_MAX_BATCH_BYTES, AWACS.DEFAULT_MAX_BATCH_BYTES);
        maxBatchNumbers = configuration.getInteger(AWACS.CONFIG_MAX_BATCH_NUMBERS, AWACS.DEFAULT_MAX_BATCH_NUMBERS);
        maxAppendMs = configuration.getInteger(AWACS.CONFIG_MAX_APPEND_MS, AWACS.DEFAULT_MAX_APPEND_MS);
        batchLingerMs = configuration.getInteger(AWACS.CONFIG_BATCH_LINGER_MS, AWACS.DEFAULT_BATCH_LINGER_MS);
        timeout = configuration.getInteger(AWACS.CONFIG_TIMEOUT_MS, AWACS.DEFAULT_TIMEOUT_MS);
        queue = new ArrayBlockingQueue<>(configuration.getInteger(AWACS.CONFIG_MAX_WAITING_MESSAGE, AWACS.DEFAULT_MAX_WAITING_MESSAGE));
        channels = new Channels(configuration.getArray(AWACS.CONFIG_SERVER), timeout);
        buffers = new ConcurrentLinkedDeque<>();
        start();
    }

    private void start() {
        int leastBuffers = 0;
        do {
            buffers.add(ByteBuffer.allocate(maxBatchBytes));
        } while (leastBuffers++ < maxBatchNumbers / 2);
        //make sure only this thread can access ByteBuffer
        Thread t = new Thread(new Runnable() {
            long newBatchCreated = 0l;

            @Override
            public void run() {
                while (!closed) {
                    try {
                        Packet p = queue.take();
                        ByteBuffer batch = buffers.peek();
                        boolean readyToFlush = false;
                        if (batch != null) {
                            if (batch.remaining() == batch.capacity() && batch.capacity() >= p.size()) {
                                batch.put(p.serialize());
                                newBatchCreated = System.currentTimeMillis();
                            } else if (batch.remaining() >= p.size()) {
                                batch.put(p.serialize());
                                if (System.currentTimeMillis() - newBatchCreated > batchLingerMs) {
                                    readyToFlush = true;
                                }
                            } else {
                                //TODO
                                readyToFlush = true;
                            }
                            if (readyToFlush) {
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
                        } else {
                            //TODO allocating new buffer
                            log.warning("All buffers are full, allocating a new buffer.");
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
            log.warning("Out of buffer limit, message abandoned.");
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
