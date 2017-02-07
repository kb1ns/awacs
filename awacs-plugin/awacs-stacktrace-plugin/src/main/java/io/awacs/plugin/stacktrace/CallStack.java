package io.awacs.plugin.stacktrace;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pixyonly on 2/7/17.
 */
public class CallStack {

    private static Map<Long, Deque<CallElement>> roots = new HashMap<>();

    public static void methodEnter(String clazz, String method) {
        Deque<CallElement> stack = roots.get(Thread.currentThread().getId());
        if (stack != null) {
            CallElement head = new CallElement(clazz, method);
            if (stack.isEmpty()) {
                stack.push(head);
            } else {
                stack.element().callSub(head);
                stack.push(head);
            }
        }
    }

    public static void methodQuit(String clazz, String method) {
        Deque<CallElement> stack = roots.get(Thread.currentThread().getId());
        if (stack != null && !stack.isEmpty()) {
            stack.element().end();
            if (stack.size() > 1)
                stack.pop();
        }
    }

    public static void initStack() {
        roots.put(Thread.currentThread().getId(), new ArrayDeque<CallElement>());
    }

    public static CallElement reset() {
        Deque<CallElement> stack = roots.remove(Thread.currentThread().getId());
        if (stack != null && !stack.isEmpty()) {
            return stack.poll();
        }
        return null;
    }
}
