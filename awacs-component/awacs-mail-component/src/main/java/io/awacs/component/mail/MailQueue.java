package io.awacs.component.mail;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

class MailQueue {

    private ArrayDeque<String> fifo;

    private HashSet<String> container;

    private int size;

    private final int capacity;

    private final ReentrantReadWriteLock lock;

    public MailQueue(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.container = new HashSet<>();
        this.fifo = new ArrayDeque<>(capacity);
        this.lock = new ReentrantReadWriteLock();
    }

    public void checkOnFire(String content, Consumer<String> consumer) {
        try {
            lock.writeLock().lock();
            if (!container.contains(content)) {
                if (size >= capacity) {
                    container.remove(fifo.removeFirst());
                } else {
                    size++;
                }
                fifo.addLast(content);
                container.add(content);
                try {
                    consumer.accept(content);
                } catch (Exception ignore) {
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int size() {
        return size;
    }

    public void expire() {
        try {
            lock.writeLock().lock();
            if (size > 0) {
                container.remove(fifo.removeFirst());
                size--;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clear() {
        try {
            lock.writeLock().lock();
            container.clear();
            fifo.clear();
            size = 0;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
