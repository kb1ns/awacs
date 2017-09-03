package io.awacs.agent.net;

import io.awacs.common.Packet;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketQueue {

    private volatile boolean closed;

    private AgentClient client;

    private List<Remote> remotes;

    private final Object arbitrator = new Object();

    public void enqueue(Packet packet, Callback cb) {
        if (closed) {
            throw new IllegalStateException("AWACS is closed.");
        }
        synchronized (arbitrator) {
            if (Batch.FIRST.progressed.get()) {
                if (Batch.SECOND.progressed.get()) {
                    return;
                }
                if (!Batch.SECOND.append(packet)) {
                    Batch.SECOND.flush();
                    enqueue(packet, cb);
                }
            } else {
                if (!Batch.FIRST.append(packet)) {
                    Batch.FIRST.flush();
                    enqueue(packet, cb);
                }
            }
        }
    }

    //TODO
    public void close() {
        closed = true;
        Batch.FIRST.flush();
        Batch.SECOND.flush();
    }

    enum Batch {

        FIRST, SECOND;

        private AtomicBoolean progressed = new AtomicBoolean(false);

        private final ByteBuffer buffer = ByteBuffer.allocate(1 << 22);

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        private void flush() {
            progressed.set(true);
            //TODO awake Client
            new Thread() {
                public void run() {
                    try {
                        lock.writeLock().lock();
                        buffer.flip();
                        buffer.array();
                        System.out.println("consumed");
                        buffer.clear();
                    } finally {
                        done();
                        lock.writeLock().unlock();
                    }
                }
            };
        }

        boolean append(Packet packet) {
            try {
                lock.readLock().lock();
                if (packet.size() + buffer.remaining() > buffer.capacity()) {
                    return false;
                }
                buffer.put(packet.serialize());
                return true;
            } finally {
                lock.readLock().unlock();
            }
        }

        void done() {
            progressed.set(false);
        }
    }
}
