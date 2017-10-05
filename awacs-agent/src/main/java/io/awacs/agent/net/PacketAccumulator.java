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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketAccumulator implements Configurable {

    private static Logger log = Logger.getLogger("AWACS");

    private volatile boolean closed = false;

    private ArrayBlockingQueue<Packet> queue;

    private ConnectionPool connectionPool;

    private int maxBatchBytes;

    private int maxAppendMs;

    private int batchLingerMs;

    @Override
    public void init(Configuration configuration) {
        maxBatchBytes = configuration.getInteger(AWACS.CONFIG_MAX_BATCH_BYTES, AWACS.DEFAULT_MAX_BATCH_BYTES);
        maxAppendMs = configuration.getInteger(AWACS.CONFIG_MAX_APPEND_MS, AWACS.DEFAULT_MAX_APPEND_MS);
        batchLingerMs = configuration.getInteger(AWACS.CONFIG_BATCH_LINGER_MS, AWACS.DEFAULT_BATCH_LINGER_MS);
        queue = new ArrayBlockingQueue<>(configuration.getInteger(AWACS.CONFIG_MAX_WAITING_MESSAGE, AWACS.DEFAULT_MAX_WAITING_MESSAGE));
        connectionPool = new ConnectionPool(configuration.getArray(AWACS.CONFIG_SERVER),
                maxBatchBytes,
                configuration.getInteger(AWACS.CONFIG_TIMEOUT_MS, AWACS.DEFAULT_TIMEOUT_MS));
        start();
    }

    private void start() {
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!closed) {
                    try {
                        Packet packet = queue.take();
                        log.log(Level.FINE, "Preparing packet: {0}", packet.getNamespace());
                        if (packet.size() > maxBatchBytes) {
                            log.log(Level.FINE, "Packet({0} bytes) exceed the maxBatchBytes {1}.",
                                    new int[]{packet.size(), maxBatchBytes});
                            connectionPool.send(packet);
                        } else {
                            connectionPool.commit(packet);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.start();
        Timer evil = new Timer();
        evil.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    log.fine("Flush timer triggered, ready to flush.");
                    connectionPool.commit(null);
                } catch (Exception ignored) {
                }
            }
        }, 1000, batchLingerMs);
    }

    public void enqueue(final Packet packet) {
        if (closed) {
            log.warning("Sender has been closed.");
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
