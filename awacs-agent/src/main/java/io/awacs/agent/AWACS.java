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

import io.awacs.agent.net.PacketQueue;
import io.awacs.agent.net.Remote;
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

    static final Logger log = Logger.getLogger("AWACS");

    static final String CONFIG_FILE_NAME = "awacs.properties";

    static final String CONFIG_SERVER_KEY = "server";

    static final String CONFIG_NAMESPACE_KEY = "namespace";

    static final String CONFIG_PLUGINS_KEY = "plugins";

    static final String CONFIG_PLUGINS_CONFIG_PATTERN = "plugins.%s.conf.";

    static final String CONFIG_PLUGIN_CLASS_PATTERN = "plugins.%s.class";

    static final String CONFIG_LOGLEVEL_KEY = "log_level";

    static final String DEFAULT_LOGLEVEL_VALUE = "info";

    static final String DEFAULT_NAMESPACE_VALUE = "defaultapp";

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
            properties.load(new FileInputStream(home + CONFIG_FILE_NAME));
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
        String loglevel = config.getString(CONFIG_LOGLEVEL_KEY, DEFAULT_LOGLEVEL_VALUE);
        log.setLevel(Level.parse(loglevel));
        String[] addr = config.getArray(CONFIG_SERVER_KEY);
        List<Remote> hosts = new ArrayList<>(addr.length);
        for (String a : addr) {
            hosts.add(new Remote(a));
        }
        PacketQueue queue = new PacketQueue(hosts);
        Sender.I.init(config.getString(CONFIG_NAMESPACE_KEY, DEFAULT_NAMESPACE_VALUE), queue);
        String[] pluginList = config.getArray(CONFIG_PLUGINS_KEY);
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
            log.log(Level.INFO, "Load plugin {0}.", p);
        }
        classLoader = new PluginClassLoader(descriptors);
        log.info("AWACS prepared.");
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
