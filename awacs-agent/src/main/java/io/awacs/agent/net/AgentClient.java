package io.awacs.agent.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class AgentClient {

    private List<InetSocketAddress> addresses;

    private Map<Integer, SocketChannel> channels;

    private Selector selector;

    private ExecutorService boss;

    private volatile boolean closed = false;

    private Random random;

    public AgentClient(List<InetSocketAddress> addresses) {
        this.addresses = addresses;
        this.channels = new TreeMap<>();
        this.random = new Random();
        this.boss = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }

    public void start() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < addresses.size(); i++) {
            channels.put(i, ready(addresses.get(i)));
        }
    }

    private SocketChannel ready(InetSocketAddress address) {
        try {
            SocketChannel channel = SocketChannel.open(address);
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void send(final byte[] msg, final int retry, final Callback cb, final int remoteIndex) {
        if (closed) {
            throw new IllegalStateException("Cannot send data after closed.");
        }
        boss.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    channels.get(remoteIndex).write(ByteBuffer.wrap(msg));
                } catch (Exception e) {
                    if (e instanceof NotYetConnectedException) {
                        try {
                            channels.get(remoteIndex).connect(addresses.get(remoteIndex));
                        } catch (Exception ignore) {
                        }
                    } else if (e instanceof NullPointerException) {
                        channels.put(remoteIndex, ready(addresses.get(remoteIndex)));
                    }
                    if (retry > 0) {
                        send(msg, retry - 1, cb);
                    } else if (cb != null) {
                        cb.onException(e);
                    }
                }
            }
        });
    }

    public void send(byte[] msg, Callback cb) {
        send(msg, 2, cb, random.nextInt(addresses.size()));
    }

    public void send(byte[] msg, int retry, Callback cb) {
        send(msg, retry, cb, random.nextInt(addresses.size()));
    }

    public void stop() {
        closed = true;
        try {
            for (SocketChannel socketChannel : channels.values()) {
                socketChannel.close();
            }
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boss.shutdownNow();
    }
}
