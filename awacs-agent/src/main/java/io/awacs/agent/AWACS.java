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

package io.awacs.agent;

import io.awacs.common.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AWACS primary class
 * Created by pixyonly on 16/9/3.
 */
public enum AWACS {

    M;

    private static final Logger log = Logger.getLogger("AWACS");

    private static final String CONFIG_FILE = "awacs.properties";

    public static final String CONFIG_SERVER = "server";

    public static final String CONFIG_NAMESPACE = "namespace";

    public static final String DEFAULT_NAMESPACE = "default_jvm";

    public static final String CONFIG_LOGLEVEL = "log_level";

    public static final String DEFAULT_LOGLEVEL = "INFO";

    public static final String CONFIG_MAX_BATCH_BYTES = "max_batch_bytes";

    public static final int DEFAULT_MAX_BATCH_BYTES = 1048576;

    public static final String CONFIG_MAX_WAITING_MESSAGE = "max_waiting_message";

    public static final int DEFAULT_MAX_WAITING_MESSAGE = 100;

    public static final String CONFIG_TIMEOUT_MS = "server_timeout_ms";

    public static final int DEFAULT_TIMEOUT_MS = 1000;

    public static final String CONFIG_MAX_BATCH_NUMBERS = "max_batch_numbers";

    public static final int DEFAULT_MAX_BATCH_NUMBERS = 10;

    public static final String CONFIG_MAX_APPEND_MS = "max_append_ms";

    public static final int DEFAULT_MAX_APPEND_MS = 50;

    public static final String CONFIG_BATCH_LINGER_MS = "batch_linger_ms";

    public static final int DEFAULT_BATCH_LINGER_MS = 3000;

    public static final String CONFIG_PLUGINS = "plugins";

    public static final String CONFIG_PLUGINS_CONFIG_PATTERN = "plugins.%s.conf.";

    public static final String CONFIG_PLUGIN_CLASS_PATTERN = "plugins.%s.class";

    Instrumentation inst;

    PluginClassLoader classLoader;

    List<PluginDescriptor> descriptors;

    List<Plugin> plugins;

    String home;

    public void prepare(Instrumentation inst) {
        M.inst = inst;
        home = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        home = home.substring(0, home.lastIndexOf('/')) + "/";
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(home + CONFIG_FILE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Enumeration<Object> keys = properties.keys();
        Map<String, String> map = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            map.put(key.trim(), properties.getProperty(key).trim());
        }
        Configuration config = new Configuration(map);
        try {
            String loglevel = config.getString(CONFIG_LOGLEVEL, DEFAULT_LOGLEVEL);
            log.setLevel(Level.parse(loglevel));
            log.log(Level.INFO, "Setting log level {0}", loglevel);
        } catch (Exception e) {
            log.setLevel(Level.parse(DEFAULT_LOGLEVEL));
        }
        Sender.I.init(config);
        String[] pluginList = config.getArray(CONFIG_PLUGINS);
        plugins = new ArrayList<>(pluginList.length);
        descriptors = new ArrayList<>(pluginList.length);
        for (String p : pluginList) {
            try {
                descriptors.add(new PluginDescriptor(p)
                        .setPluginProperties(config.getSubConfig(String.format(CONFIG_PLUGINS_CONFIG_PATTERN, p)))
                        .setClassName(config.getString(String.format(CONFIG_PLUGIN_CLASS_PATTERN, p)))
                        .setJarFile(new JarFile(home + String.format("plugins/awacs-%s-plugin.jar", p))));
            } catch (IOException e) {
                log.log(Level.SEVERE, "Cannot read jar file: awacs-{0}-plugin.jar", p);
            }
            log.log(Level.INFO, "Plugin {0} loaded.", p);
        }
        classLoader = new PluginClassLoader(descriptors);
        log.info("AWACS ready.");
    }

    public void run() {
        for (PluginDescriptor descriptor : descriptors) {
            try {
                inst.appendToSystemClassLoaderSearch(descriptor.getJarFile());
//                Class<?> clazz = classLoader.findClass(descriptor.getClassName());
                Class<?> clazz = Class.forName(descriptor.getClassName());
                Plugin plugin = (Plugin) clazz.newInstance();
                plugin.init(descriptor.getPluginProperties());
                plugin.rock();
                plugins.add(plugin);
                log.log(Level.INFO, "AWACS plugin {0} initialized.", descriptor.getClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                log.log(Level.SEVERE, "Cannot launch plugin {0}.", descriptor.getPluginName());
                e.printStackTrace();
            }
        }
    }

    public Instrumentation getInstrumentation() {
        return inst;
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                for (Plugin plugin : plugins) {
                    plugin.over();
                }
                Sender.I.close();
            }
        });
    }
}
