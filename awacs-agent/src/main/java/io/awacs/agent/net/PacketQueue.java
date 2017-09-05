package io.awacs.agent.net;

import io.awacs.common.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketQueue {

    private static Logger log = LoggerFactory.getLogger(PacketQueue.class);

    private volatile boolean closed;

    private final Deque<Connection> batches;

    private Selector selector;

    private ExecutorService boss;

    private ArrayBlockingQueue<Packet> queue;

    private List<Remote> remotes;

    private int maxBatchMemory = 1 << 10;

    public PacketQueue(List<Remote> remotes) {
        if (remotes.size() <= 2) {
            this.remotes = new ArrayList<>(remotes.size() * 2);
            this.remotes.addAll(remotes);
            this.remotes.addAll(remotes);
        } else {
            this.remotes = remotes;
        }
        this.batches = new ArrayDeque<>(this.remotes.size());
        init();
    }

    private void init() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO config
        this.queue = new ArrayBlockingQueue<>(50);
        for (Remote r : remotes) {
            this.batches.add(new Connection(selector, r, maxBatchMemory));
        }
        this.boss = Executors.newFixedThreadPool(remotes.size(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (!closed) {
                    try {
                        Packet packet = queue.take();
                        if (packet.size() > maxBatchMemory) {
                            //TODO
                            continue;
                        }
                        if (batches.isEmpty()) {
                            //wait until a connection becomes avaliable
                            synchronized (batches) {
                                if (batches.isEmpty()) {
                                    batches.wait();
                                }
                            }
                        }
                        if (!batches.peek().append(packet)) {
                            shift();
                        }
                    } catch (Exception e) {
                        //protect our consumer
                        log.error("", e);
                    }
                }
            }
        };
        Thread consumer = new Thread(task);
        consumer.setDaemon(true);
        consumer.start();
    }

    public void enqueue(final Packet packet) {
        if (closed)
            return;
        try {
            queue.offer(packet, 10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private void shift() {
        final Connection c = batches.poll();
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
                        batches.add(c);
                        batches.notifyAll();
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
    }
}
