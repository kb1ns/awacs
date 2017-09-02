package io.awacs.agent;

import io.awacs.agent.net.Callback;
import io.awacs.common.Packet;

import java.util.concurrent.Future;

/**
 *
 * Created by pixyonly on 02/09/2017.
 */
public enum Sender {

    I;

    private String namespace;

    void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Future<byte[]> send(byte key, String body, Callback cb) {
        return doSend(new Packet(namespace, key, body), cb);
    }

    private Future<byte[]> doSend(Packet packet, Callback cb) {
        return null;
    }

    public void close() {

    }
}
