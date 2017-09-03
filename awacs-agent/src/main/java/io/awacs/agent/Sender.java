package io.awacs.agent;

import io.awacs.agent.net.Callback;
import io.awacs.agent.net.PacketQueue;
import io.awacs.common.Packet;

import java.util.concurrent.Future;

/**
 *
 * Created by pixyonly on 02/09/2017.
 */
public enum Sender {

    I;

    private String namespace;

    private PacketQueue queue;

    void init(String namespace, PacketQueue queue) {
        this.namespace = namespace;
        this.queue = queue;
    }

    public Future<byte[]> send(byte key, String body, Callback cb) {
        return doSend(new Packet(namespace, key, body), cb);
    }

    private Future<byte[]> doSend(Packet packet, Callback cb) {
        queue.enqueue(packet, cb);
        return null;
    }

    public void close() {
        queue.close();
    }
}
