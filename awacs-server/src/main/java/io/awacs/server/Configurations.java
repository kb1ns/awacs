/**
 * Copyright 2016-2017 AWACS Project.
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

package io.awacs.server;

import io.awacs.common.Configuration;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 * Created by pixyonly on 16/9/30.
 */
public class Configurations {

    public static final String CFG_BIND_HOST = "bind_host";

    public static final String DEFAULT_BIND_HOST = "0.0.0.0";

    public static final String CFG_BIND_PORT = "bind_port";

    public static final int DEFAULT_BIND_PORT = 7200;

    public static final String CFG_BOSS_CORE = "boss_core";

    public static final int DEFAULT_BOSS_CORE = 1;

    public static final String CFG_WORKER_CORE = "worker_core";

    public static final int DEFAULT_WORKER_CORE = Runtime.getRuntime().availableProcessors() * 2;

    public static final String COMPONENT_PREFIX = "components";

    public static final String COMPONENT_CLASS = "class";

    public static Configuration loadConfigurations() {
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        Enumeration<String> keys = bundle.getKeys();
        Map<String, String> map = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key.trim(), bundle.getString(key).trim());
        }
        return new Configuration(map);
    }
}
