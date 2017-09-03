package io.awacs.server;

import io.awacs.common.Packet;

import java.net.InetSocketAddress;

/**
 * Created by pixyonly on 03/09/2017.
 */
public interface Handler {

    Packet onReceive(Packet recieve, InetSocketAddress remote);

    byte key();
}
