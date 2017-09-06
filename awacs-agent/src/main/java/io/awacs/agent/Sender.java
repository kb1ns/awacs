package io.awacs.agent;

import io.awacs.agent.net.PacketQueue;
import io.awacs.common.Packet;

/**
 * Shared by all plugins
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

    public void send(byte key, String body) {
        doSend(new Packet(namespace, key, body));
    }

    void doSend(Packet packet) {
        queue.enqueue(packet);
    }

    void close() {
        queue.close();
    }
}
