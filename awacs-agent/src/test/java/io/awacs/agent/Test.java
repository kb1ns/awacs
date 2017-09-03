package io.awacs.agent;

import io.awacs.agent.net.AgentClient;
import io.awacs.agent.net.Callback;
import io.awacs.common.Packet;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class Test {

    @org.junit.Test
    public void test() {
        List<InetSocketAddress> addresses = new LinkedList<>();
        addresses.add(new InetSocketAddress("127.0.0.1", 7200));
        AgentClient client = new AgentClient(addresses);
        client.start();
        for (int i = 0; i < 10000; i++) {
            client.send(new Packet("hello", (byte) 1, "hello, world.......").serialize(),
                    new Callback() {
                        @Override
                        public void onCompelete() {

                        }

                        @Override
                        public void onException(Throwable t) {
                            t.printStackTrace();
                        }
                    });
        }
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
