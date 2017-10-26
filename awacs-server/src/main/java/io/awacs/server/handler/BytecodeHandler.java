package io.awacs.server.handler;

import io.awacs.common.net.Packet;
import io.awacs.component.fernflower.FernflowerComponent;
import io.awacs.server.Handler;
import io.awacs.server.Inject;

import java.net.InetSocketAddress;

/**
 * Created by pixyonly on 30/09/2017.
 */
public class BytecodeHandler implements Handler {

    @Inject("fernflower")
    private FernflowerComponent fernflower;

    @Override
    public Packet onReceive(Packet recieve, InetSocketAddress remote) {
        fernflower.record(recieve.getNamespace(), recieve.getBody());
        return null;
    }

    @Override
    public byte key() {
        return 0x02;
    }

    @Override
    public void release() {
    }
}
