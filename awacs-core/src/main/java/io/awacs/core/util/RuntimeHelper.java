/**
 * Copyright 2016 AWACS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.awacs.core.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Created by pixyonly on 8/23/16.
 */
public enum RuntimeHelper {

    instance;

    RuntimeHelper() {
        try {
            host = Inet4Address.getLocalHost().getHostAddress();
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String name = runtime.getName();
            int index = name.indexOf("@");
            if (index != -1) {
                pid = Integer.parseInt(name.substring(0, index));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private String host;

    private int pid;

    public String getHost() {
        return host;
    }

    public int getPid() {
        return pid;
    }
}
