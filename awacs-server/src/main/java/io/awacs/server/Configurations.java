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

    public static final String SERVER_PREFIX = "awacs.servers";

    public static final String TCP_BIND_HOST = "tcp_bind_host";

    public static final String DEFAULT_TCP_BIND_HOST = "0.0.0.0";

    public static final String TCP_BIND_PORT = "tcp_bind_port";

    public static final int DEFAULT_TCP_BIND_PORT = 7200;

    public static final String TCP_BOSS_CORE = "tcp_boss_core";

    public static final int DEFAULT_TCP_BOSS_CORE = 1;

    public static final String TCP_WORKER_CORE = "tcp_worker_core";

    public static final int DEFAULT_TCP_WORKER_CORE = Runtime.getRuntime().availableProcessors() * 2;

//    public static final String PLUGIN_PREFIX = "awacs.plugins";
//
//    public static final String PLUGIN_CLASS = "pluginClass";
//
//    public static final String PLUGIN_PROPERTIES = "pluginProperties";
//
//    public static final String HANDLER_PROPERTIES = "handlerProperties";
//
//    public static final String HANDLER_CLASS = "handlerClass";
//
//    public static final String KEY_CLASS = "keyClass";
//
//    public static final String KEY_VALUE = "keyValue";

    public static final String COMPONENT_PREFIX = "awacs.components";

    public static final String COMPONENT_CLASS = "class";

    public static Configuration loadConfigurations() {
        //TODO
        ResourceBundle bundle = ResourceBundle.getBundle("server");
        Enumeration<String> keys = bundle.getKeys();
        Map<String, String> map = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key.trim(), bundle.getString(key).trim());
        }
        return new Configuration(map);
    }

//    public static List<String> exportServerAddr(Configuration configuration) {
//        ImmutableMap<String, String> map = configuration.getSubProperties(SERVER_PREFIX + ".");
//        Set<String> serverNames = map.keySet().stream().map(key -> key.substring(0, key.indexOf("."))).collect(Collectors.toSet());
//        List<String> addrs = new ArrayList<>(serverNames.size());
//        addrs.addAll(serverNames.stream().map(serverName -> map.getOrDefault(serverName + "." + TCP_BIND_HOST, DEFAULT_TCP_BIND_HOST) + ":" +
//                map.getOrDefault(serverName + "." + TCP_BIND_PORT, DEFAULT_TCP_BIND_PORT)).collect(Collectors.toList()));
//        return addrs;
//    }
//
//    public static String getPluginPath() {
//        return System.getProperty("awacs.home", System.getProperty("user.dir")) + "/plugins/";
//    }
//
//    public static void dump(String conf) throws IOException {
//        FileWriter fw = new FileWriter(new File(getPluginPath() + "/config.json"));
//        fw.write(conf);
//        fw.flush();
//        fw.close();
//    }
}
