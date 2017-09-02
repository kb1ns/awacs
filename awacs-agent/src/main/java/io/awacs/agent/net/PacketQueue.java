package io.awacs.agent.net;

import java.util.List;

/**
 * Created by pixyonly on 03/09/2017.
 */
public final class PacketQueue {

    private volatile boolean closed;

    private AgentClient client;

    private List<Remote> remotes;

    enum Batch {
        FIRST, SECOND;


    }
}
