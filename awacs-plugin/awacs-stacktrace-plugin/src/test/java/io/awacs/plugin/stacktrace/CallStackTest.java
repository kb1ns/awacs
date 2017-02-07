package io.awacs.plugin.stacktrace;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by pixyonly on 2/7/17.
 */
public class CallStackTest {

    Stub1 stub1;

    @Before
    public void init() {
        stub1 = new Stub1();
    }

    @Test
    public void test() {
        CallStack.initStack();
        CallStack.methodEnter("io/awacs/plugin/stacktrace/CallStackTest", "test");
        stub1.stubMethod0();
        CallStack.methodQuit("io/awacs/plugin/stacktrace/CallStackTest", "test");
        System.out.println(CallStack.reset());
    }


    public static class Stub1 {

        public void stubMethod0() {
            CallStack.methodEnter("io/awacs/plugin/stacktrace/CallStackTest$Stub0", "stubMethod0");
            stubMethod1();
            stubMethod3();
            stubMethod1();
            stubMethod3();
            CallStack.methodQuit("io/awacs/plugin/stacktrace/CallStackTest$Stub0", "stubMethod0");
        }

        public void stubMethod1() {
            CallStack.methodEnter("io/awacs/plugin/stacktrace/CallStackTest$Stub1", "stubMethod1");
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0)
                    stubMethod2();
            }
            CallStack.methodQuit("io/awacs/plugin/stacktrace/CallStackTest$Stub1", "stubMethod1");
        }

        public void stubMethod2() {
            CallStack.methodEnter("io/awacs/plugin/stacktrace/CallStackTest$Stub2", "stubMethod2");
            try {
                stubMethod3();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CallStack.methodQuit("io/awacs/plugin/stacktrace/CallStackTest$Stub2", "stubMethod2");
        }

        public void stubMethod3() {
            CallStack.methodEnter("io/awacs/plugin/stacktrace/CallStackTest$Stub3", "stubMethod3");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            CallStack.methodQuit("io/awacs/plugin/stacktrace/CallStackTest$Stub3", "stubMethod3");
        }
    }
}
