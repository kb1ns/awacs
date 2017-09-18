package io.awacs.agent;

import io.awacs.agent.net.PacketAccumulator;
import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.net.Packet;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared by all plugins
 * Created by pixyonly on 02/09/2017.
 */
public enum Sender implements Configurable {

    I;

    private PacketAccumulator queue;

    private static final Logger log = Logger.getLogger("AWACS");

    @Override
    public void init(Configuration configuration) {
        queue = new PacketAccumulator();
        queue.init(configuration);
    }

    public void send(byte key, String body) {
        doSend(new Packet(AWACS.M.namespace(), key, body));
    }

    void doSend(Packet packet) {
        queue.enqueue(packet);
    }

    void close() {
        queue.close();
        log.log(Level.INFO, "Sender closed.");
    }
}
