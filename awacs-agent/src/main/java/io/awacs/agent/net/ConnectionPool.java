package io.awacs.agent.net;

import io.awacs.common.net.Packet;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 01/10/2017.
 */
final class ConnectionPool {

    private final Logger log = Logger.getLogger("AWACS");

    private final LinkedBlockingDeque<Connection> connections;

    ConnectionPool(String[] serverAddrs,
                   int maxBatchBytes,
                   int timeout) {
        connections = new LinkedBlockingDeque<>();
        for (String a : serverAddrs) {
            connections.add(new Connection(new Remote(a), maxBatchBytes, timeout));
            if (serverAddrs.length <= 2) {
                connections.add(new Connection(new Remote(a), maxBatchBytes, timeout));
            }
        }
    }

    void send(final Packet packet) {
        try {
            final Connection head = connections.take();
            log.log(Level.FINE, "Taking connection {0}", head.remote.getAddress());
            final byte[] data = packet.serialize();
            head.writeWrapper(ByteBuffer.wrap(data), new Callback() {
                @Override
                public void onComplete() {
                    log.log(Level.FINE, "Flushed single. Using {0}", head.remote.getAddress());
                    connections.offer(head);
                }

                @Override
                public void onException(Throwable t) {
                    connections.offer(head);
                }
            });
        } catch (Exception ignored) {
        }
    }

    void commit(final Packet packet) {
        try {
            final Connection head = connections.take();
            log.log(Level.FINE, "Taking connection {0}", head.remote.getAddress());
            if (packet != null) {
                final byte[] data = packet.serialize();
                if (!head.put(data)) {
                    //非严格时序
                    head.writeInner(new Callback() {
                        @Override
                        public void onComplete() {
                            head.put(data);
                            connections.offer(head);
                            log.log(Level.FINE, "Flushed batch. Using {0}", head.remote.getAddress());
                        }

                        @Override
                        public void onException(Throwable t) {
                            head.put(data);
                            connections.offer(head);
                        }
                    });
                } else {
                    connections.addFirst(head);
                }
            } else {
                head.writeInner(new Callback() {
                    @Override
                    public void onComplete() {
                        log.log(Level.FINE, "Flushed batch. Using {0}", head.remote.getAddress());
                        connections.offer(head);
                    }

                    @Override
                    public void onException(Throwable t) {
                        connections.offer(head);
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }
}
