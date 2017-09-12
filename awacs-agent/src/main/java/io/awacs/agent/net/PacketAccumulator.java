package io.awacs.agent.net;

import io.awacs.agent.AWACS;
import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.net.Packet;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketAccumulator implements Configurable {

    private static Logger log = Logger.getLogger("AWACS");

    private volatile boolean closed;

    private final Deque<Connection> batches = new ArrayDeque<>();

    private Selector selector;

    private ExecutorService boss;

    private ScheduledExecutorService flusher;

    private ArrayBlockingQueue<Packet> queue;

    private List<Remote> remotes;

    private int maxBatchBytes;

    private int maxWaitingRequests;

    private int maxAppendMs;

    private int batchLingerMs;

    @Override
    public void init(Configuration configuration) {
        String[] addr = configuration.getArray(AWACS.CONFIG_SERVER);
        remotes = new ArrayList<>();
        for (String a : addr) {
            remotes.add(new Remote(a));
//            if (addr.length <= 2) {
//                remotes.add(new Remote(a));
//            }
        }
        maxBatchBytes = configuration.getInteger(AWACS.CONFIG_MAX_BATCH_BYTES, AWACS.DEFAULT_MAX_BATCH_BYTES);
        maxWaitingRequests = configuration.getInteger(AWACS.CONFIG_MAX_WAITING_REQUESTS, AWACS.DEFAULT_MAX_WAITING_REQUESTS);
        maxAppendMs = configuration.getInteger(AWACS.CONFIG_MAX_APPEND_MS, AWACS.DEFAULT_MAX_APPEND_MS);
        batchLingerMs = configuration.getInteger(AWACS.CONFIG_BATCH_LINGER_MS, AWACS.DEFAULT_BATCH_LINGER_MS);
        start();
    }

    private void start() {
        queue = new ArrayBlockingQueue<>(maxWaitingRequests);
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Remote r : remotes) {
            batches.add(new Connection(selector, r, maxBatchBytes));
            log.log(Level.INFO, "Connected to server {0}", r);
        }
        ThreadFactory daemon = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        };
        boss = Executors.newFixedThreadPool(remotes.size(), daemon);
        flusher = Executors.newSingleThreadScheduledExecutor(daemon);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while (!closed) {
                    try {
                        Packet packet = queue.take();
                        //wait until a connection becomes avaliable
                        synchronized (batches) {
                            if (batches.isEmpty()) {
                                log.info("All connection are flush in progress, wait...");
                                batches.wait();
                            }
                            if (!batches.peek().append(packet)) {
                                shift();
                                queue.offer(packet);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
        flusher.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (batches) {
                    shift();
                }
            }
        }, batchLingerMs, batchLingerMs, TimeUnit.MILLISECONDS);
    }

    public boolean enqueue(final Packet packet) {
        if (closed || packet.size() > maxBatchBytes) {
            log.warning("Out of buffer limit, packet abandoned.");
            return false;
        }
        try {
            return queue.offer(packet, maxAppendMs, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void shift() {
        final Connection c = batches.poll();
        if (c == null) {
            return;
        }
        log.log(Level.FINE, "{0} ready to flush.", c.remote);
        boss.submit(new Runnable() {
            @Override
            public void run() {
                c.flush(new Callback() {
                    @Override
                    public void onComplete() {
                        synchronized (batches) {
                            batches.add(c);
                            batches.notifyAll();
                        }
                    }

                    @Override
                    public void onException(Throwable t) {
                        synchronized (batches) {
                            batches.add(c);
                            batches.notifyAll();
                        }
                    }
                });
            }
        });
    }

    public void close() {
        closed = true;
        //TODO drainTo channel
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boss.shutdownNow();
        flusher.shutdown();
    }
}
