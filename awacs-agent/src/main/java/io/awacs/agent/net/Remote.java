package io.awacs.agent.net;

import java.net.InetSocketAddress;

/**
 * Created by pixyonly on 04/09/2017.
 */
public class Remote {

    private InetSocketAddress address;

    public Remote(String str) {
        int m = str.indexOf(':');
        address = new InetSocketAddress(str.substring(0, m), Integer.parseInt(str.substring(m + 1)));
    }

    public InetSocketAddress getAddress() {
        return address;
    }
}
