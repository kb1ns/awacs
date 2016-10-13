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

import com.google.common.collect.ImmutableMap;
import io.awacs.core.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by pixyonly on 16/9/30.
 */
public class Configurations {

    public static String SERVER_PREFIX = "awacs.servers";

    public static String HTTP_BIND_HOST = "http_bind_host";

    public static String DEFAULT_HTTP_BIND_HOST = "0.0.0.0";

    public static String HTTP_BIND_PORT = "http_bind_port";

    public static String DEFAULT_HTTP_BIND_PORT = "7100";

    public static String HTTP_BOSS_CORE = "http_boss_core";

    public static String DEFAULT_HTTP_BOSS_CORE = "1";

    public static String HTTP_WORKER_CORE = "http_worker_core";

    public static String DEFAULT_HTTP_WORKER_CORE = String.valueOf(Runtime.getRuntime().availableProcessors() * 2);

    public static String TCP_BIND_HOST = "tcp_bind_host";

    public static String DEFAULT_TCP_BIND_HOST = "0.0.0.0";

    public static String TCP_BIND_PORT = "tcp_bind_port";

    public static String DEFAULT_TCP_BIND_PORT = "7200";

    public static String TCP_BOSS_CORE = "tcp_boss_core";

    public static String DEFAULT_TCP_BOSS_CORE = "1";

    public static String TCP_WORKER_CORE = "tcp_worker_core";

    public static String DEFAULT_TCP_WORKER_CORE = String.valueOf(Runtime.getRuntime().availableProcessors() * 2);

    public static String PLUGIN_PREFIX = "awacs.plugins";

    public static String PLUGIN_CLASS = "pluginClass";

    public static String HANDLER_CLASS = "handlerClass";

    public static String KEY_CLASS = "keyClass";

    public static String KEY_VALUE = "keyValue";

    public static String REPOSITORY_PREFIX = "awacs.repositories";

    public static String REPOSITORY_CLASS = "class";

    public static Configuration loadConfigurations() {
        ResourceBundle bundle = ResourceBundle.getBundle("awacs");
        Enumeration<String> keys = bundle.getKeys();
        Map<String, String> map = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key.trim(), bundle.getString(key).trim());
        }
        return new Configuration(map);
    }

    public static List<String> exportServerAddr(Configuration configuration) {
        ImmutableMap<String, String> map = configuration.getSubProperties(SERVER_PREFIX + ".");
        Set<String> serverNames = map.keySet().stream().map(key -> key.substring(0, key.indexOf("."))).collect(Collectors.toSet());
        List<String> addrs = new ArrayList<>(serverNames.size());
        addrs.addAll(serverNames.stream().map(serverName -> map.getOrDefault(serverName + "." + TCP_BIND_HOST, DEFAULT_TCP_BIND_HOST) + ":" +
                map.getOrDefault(serverName + "." + TCP_BIND_PORT, DEFAULT_TCP_BIND_PORT)).collect(Collectors.toList()));
        return addrs;
    }

    public static String getPluginPath() {
        return System.getProperty("awacs.home", System.getProperty("user.dir")) + "/plugins/";
    }

    public static void dump(String conf) throws IOException {
        FileWriter fw = new FileWriter(new File(getPluginPath() + "/config.json"));
        fw.write(conf);
        fw.flush();
        fw.close();
    }
}
