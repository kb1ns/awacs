package io.awacs.common;

import io.awacs.common.Configurable;
import io.awacs.common.Packet;

import java.net.InetSocketAddress;

/**
 * Created by pixyonly on 03/09/2017.
 */
public interface Repository extends Configurable {

    Packet confirm(Packet recieve, InetSocketAddress remote);
}
