/**
 * Copyright 2016-2017 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.awacs.plugin.stacktrace;

import io.awacs.agent.AWACS;
import io.awacs.agent.Sender;
import io.awacs.common.format.Influx;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 2/7/17.
 */
public class CallStack {

    private static final Logger log = Logger.getLogger("AWACS");

    private static Map<Long, Deque<CallElement>> roots = new HashMap<>();

    public static void methodEnter(String clazz, String method) {
        Deque<CallElement> stack = roots.get(Thread.currentThread().getId());
        if (stack != null) {
            CallElement head = new CallElement(clazz, method);
            if (stack.isEmpty()) {
                stack.push(head);
            } else {
                stack.push(stack.element().callSub(head));
            }
        }
    }

    public static void methodQuit() {
        Deque<CallElement> stack = roots.get(Thread.currentThread().getId());
        if (stack != null && !stack.isEmpty()) {
            if (stack.size() > 1) {
                CallElement completed = stack.pop().end();
                if (completed.isNoneCost()) {
                    stack.element().removeSub(completed.id());
                }
            } else {
                //stack over
                stack.element().end();
            }
        }
    }

    public static void initStack() {
        roots.put(Thread.currentThread().getId(), new ArrayDeque<CallElement>());
    }

    public static CallElement reset() {
        Deque<CallElement> stack = roots.remove(Thread.currentThread().getId());
        if (stack != null && !stack.isEmpty()) {
            if (stack.size() > 1)
                return new CallElement(stack.pollLast().id(), "(error)");
            else
                return stack.pop();
        }
        return null;
    }

    //发送线程的堆栈信息
    public static void incrAccess() {
        CallElement root = CallStack.reset();
        if (root != null && root.getElapsedTime() >= StackTracePlugin.Config.F.responseTimeThreshold) {
            String s = Influx.measurement(AWACS.M.namespace()).time(System.nanoTime(), TimeUnit.NANOSECONDS)
                    .addField("thread", Thread.currentThread().getName())
                    .addField("stack", root.toString())
                    .addField("elapsed_time", root.getElapsedTime())
                    .tag("entry", root.id())
                    .tag("namespace", AWACS.M.namespace())
                    .tag("hostname", AWACS.M.hostname())
                    .build()
                    .lineProtocol();
            log.log(Level.FINE, "Request completed: {0}", s);
            Sender.I.send((byte) 0x01, s);
        }
    }

    //发送异常信息
    public static void incrFailure(Throwable e) {
        CallStack.reset();
        if (StackTracePlugin.Config.F.isValid(e.getClass())) {
            String s = buildErrReport(e);
            log.log(Level.FINE, "Exception catched: {0}", s);
            Sender.I.send((byte) 0x01, s);
        }
    }

    private static String buildErrReport(Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        int level = StackTracePlugin.Config.F.maxExceptionLevel;
        List<StackTraceElement> reducedStack = new ArrayList<>(level);
        for (StackTraceElement element : stack) {
            if (level-- < 1) {
                break;
            }
            reducedStack.add(element);
        }
        return Influx.measurement(AWACS.M.namespace()).time(System.nanoTime(), TimeUnit.NANOSECONDS)
                .addField("thread", Thread.currentThread().getName())
                .addField("stack", reducedStack.toString())
                .addField("message", e.getMessage())
                .tag("entry", e.getClass().getCanonicalName())
                .tag("namespace", AWACS.M.namespace())
                .tag("hostname", AWACS.M.hostname())
                .build()
                .lineProtocol();
    }
}
