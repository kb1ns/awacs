/**
 * Copyright 2016 AWACS Project.
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
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
                stack.push(stack.element().callSub(head));
            }
        }
    }

    public static void methodQuit(String clazz, String method) {
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
}
