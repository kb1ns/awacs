package io.awacs.server.handler;

import io.awacs.common.net.Packet;
import io.awacs.server.Handler;

import java.net.InetSocketAddress;

/**
 * Created by pixyonly on 30/09/2017.
 */
public class BytecodeHandler implements Handler {

    @Override
    public Packet onReceive(Packet recieve, InetSocketAddress remote) {
        return null;
    }

    @Override
    public byte key() {
        return 0x02;
    }
}
