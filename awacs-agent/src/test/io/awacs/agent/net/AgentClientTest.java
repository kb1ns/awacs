package io.awacs.agent.net;

import io.awacs.common.Packet;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by pixyonly on 04/09/2017.
 */
public class AgentClientTest {

    @Test
    public void testBatch() {
        List<Remote> remotes = new LinkedList<>();
        remotes.add(new Remote("127.0.0.1:7200"));
        final PacketQueue queue = new PacketQueue(remotes);

        String bs = "good boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boygood boy";
        final Packet p1 = new Packet("myapp", (byte) 1, bs);
//        Packet p2 = new Packet("myapp", (byte) 1, "good boy");
//        Packet p3 = new Packet("myapp", (byte) 1, "good boy");
//        Packet p4 = new Packet("myapp", (byte) 1, "good boy");
//        Packet p5 = new Packet("myapp", (byte) 1, "good boy");
        ExecutorService es = Executors.newCachedThreadPool();
        long t = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            es.submit(new Runnable() {
                @Override
                public void run() {
                    queue.enqueue(p1);
                }
            });
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(System.currentTimeMillis() - t);

        queue.enqueue(p1);
        queue.enqueue(p1);
        queue.enqueue(p1);
//        queue.enqueue(p1);
//        queue.enqueue(p1);
//        Selector selector = null;
//        try {
//            selector = Selector.open();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Connection c = new Connection(selector, new Remote("127.0.0.1:7200"));
//        if (c.append(p1)) {
//            c.flush(null);
//        }

        try {
            Thread.currentThread().join(30000);
        } catch (InterruptedException err) {
            err.printStackTrace();
        }
    }
}
