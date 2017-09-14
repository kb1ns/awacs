package io.awacs.agent.net;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pixyonly on 14/09/2017.
 */
public final class Channels {

    private final Set<Connection> readyConnections;

    Channels(String[] serverAddrs, int timeout) {
        readyConnections = new HashSet<>();
        for (String a : serverAddrs) {
            readyConnections.add(new Connection(new Remote(a), timeout));
        }
    }

    void flush(final ByteBuffer buffer, final Callback cb) {
        Connection c = readyConnections.iterator().next();
        c.flush(buffer, cb);
    }
}
