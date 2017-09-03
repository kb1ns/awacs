package io.awacs.agent;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class Test {

    @org.junit.Test
    public void test() {
        Object obj = new Object();
        synchronized (obj) {
            synchronized (obj) {
                System.out.println("hello, world.");
            }
        }
    }
}
