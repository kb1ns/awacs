package io.awacs.agent;

import io.awacs.common.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 12/09/2017.
 */
public class SenderTest {

    @BeforeClass
    public static void init() {
        ResourceBundle bundle = ResourceBundle.getBundle("awacs");
        Enumeration<String> keys = bundle.getKeys();
        Map<String, String> map = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key.trim(), bundle.getString(key).trim());
        }
        Configuration config = new Configuration(map);
        Logger.getLogger("AWACS").setLevel(Level.FINE);
        Sender.I.init(config);
    }

    @Test
    public void test() {
        System.out.println(System.currentTimeMillis());
//        new Thread() {
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                }
//                System.out.println(Thread.currentThread().getName() + ":" + System.currentTimeMillis());
//            }
//        }.start();
//        new Thread() {
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                }
//                System.out.println(Thread.currentThread().getName() + ":" + System.currentTimeMillis());
//            }
//        }.start();
//        new Thread() {
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                }
//                System.out.println(Thread.currentThread().getName() + ":" + System.currentTimeMillis());
//            }
//        }.start();
//        new Thread() {
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                    Sender.I.send((byte) 1, "hello, world 000>>>-?,=V>4-F/.$#@_--->>><<<---++");
//                }
//                System.out.println(Thread.currentThread().getName() + ":" + System.currentTimeMillis());
//            }
//        }.start();
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 1002; i++) {
//                Sender.I.send((byte) 1, "00000000000000");
                Sender.I.send((byte) 1, "000000000000000001111111111111111");
                Sender.I.send((byte) 1, "11111111111111111100000000000000000");
//                Sender.I.send((byte) 1, "0000000011111111");
//                Sender.I.send((byte) 1, "1111111100000000");
                Sender.I.send((byte) 1, "1111111111111111111111111111111111111");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + ":" + System.currentTimeMillis());


        try {
            Thread.currentThread().join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
