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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by pixyonly on 16/10/25.
 * 线程的线程栈的处理工具类
 */
public class StackFrames {

    //为每一个线程保存一个线程栈信息列表
    private static Map<Long, List<StackFrame>> entry = new HashMap<>();

    //初始化当前线程的线程栈信息列表：在起始跟踪方法的开始调用
    public static void init() {
        entry.put(Thread.currentThread().getId(), new LinkedList<StackFrame>());
    }

    //保存当前线程的线程栈信息(包含结束和开始信息，通过flag字段来区分)到线程栈信息列表
    public static void push(String clazz, String method, int flag) {
        Long id = Thread.currentThread().getId();
        if (entry.get(id) != null) {
            entry.get(id).add(new StackFrame(clazz, method, flag));
        }
    }

    //清除当前线程的线程栈信息列表：在起始跟踪方法的结束调用
    public static List dump() {
        return entry.remove(Thread.currentThread().getId());
    }

}
