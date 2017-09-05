package io.awacs.agent.net;

import io.awacs.common.Packet;

import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by pixyonly on 04/09/2017.
 */
class Connection {

    Remote remote;

    SocketChannel channel;

    ByteBuffer buffer;

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    Selector selector;

    int bufSize;

    Connection(Selector selector, Remote remote, int bufSize) {
        this.selector = selector;
        this.remote = remote;
        this.bufSize = bufSize;
        this.buffer = ByteBuffer.allocate(bufSize);
        ready();
    }

    boolean ready() {
        try {
            if (channel == null) {
                channel = SocketChannel.open(remote.getAddress());
                channel.configureBlocking(false);
                channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
//            channel.register(selector, SelectionKey.OP_READ);
                return channel.finishConnect();
            } else if (!channel.isConnected()) {
                channel.connect(remote.getAddress());
                return channel.finishConnect();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    void flush(Callback cb) {
        flush(cb, 2);
    }

    void flush(Callback cb, int retry) {
        try {
            lock.writeLock().lock();
            buffer.flip();
            channel.write(buffer);
            if (cb != null) {
                cb.onComplete();
            }
        } catch (Exception e) {
            if (e instanceof NotYetConnectedException) {
                ready();
            }
            if (retry > 0) {
                flush(cb, retry - 1);
            } else if (cb != null) {
                cb.onException(e);
            }
        } finally {
            buffer.clear();
            lock.writeLock().unlock();
        }
    }

    boolean append(Packet packet) {
        try {
            lock.writeLock().lock();
            if (buffer.remaining() < packet.size()) {
                return false;
            }
            buffer.put(packet.serialize());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
