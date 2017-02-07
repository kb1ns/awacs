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

/**
 * Created by pixyonly on 16/10/25.
 * 线程栈信息
 */
public class StackFrame {

    private final static int METHOD_ENTER = 0;

    private final static int METHOD_LEFT = 1;

    private String caller;

    private long timestamp;

    private int flag;

    StackFrame(String clazz, String method, int flag) {
        this.caller = clazz + "#" + method;
        this.timestamp = System.currentTimeMillis();
        this.flag = flag;
    }

    String id() {
        return caller + "_" + flag;
    }

    public String getCaller() {
        return caller;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getFlag() {
        return flag;
    }

    @Override
    public String toString() {
        return '{' +
                "\"caller\":\"" + caller + "\"," +
                "\"timestamp\":" + timestamp + "," +
                "\"flag\":" + flag +
                '}';
    }
}
