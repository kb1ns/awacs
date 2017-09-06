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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.*;

/**
 * AWACS primary class
 * Created by pixyonly on 16/9/3.
 */
public enum AWACS {

    M;

    private final static Logger log = LoggerFactory.getLogger(AWACS.class);

    Instrumentation inst;

    PluginClassLoader classLoader;

    List<PluginDescriptor> descriptors;

    List<Plugin> plugins;

    PacketQueue queue;

    String home;

    public void prepare(Instrumentation inst) {
        M.inst = inst;
        log.info("AWACS attached.");

        home = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        home = home.substring(0, home.lastIndexOf('/')) + "/";
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(home + "awacs.properties"));
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
        String[] addr = config.getArray("server");
        List<Remote> hosts = new ArrayList<>(addr.length);
        for (String a : addr) {
            hosts.add(new Remote(a));
        }
        queue = new PacketQueue(hosts);
        Sender.I.init(config.getString("namespace", "defaultapp"), queue);
        String[] pluginList = config.getArray("plugins");

        classLoader = new PluginClassLoader(home + "plugin", pluginList);
        for (String p : pluginList) {
            descriptors.add(new PluginDescriptor(p)
                    .setClassName(config.getString("plugins." + p + ".class"))
                    .setPluginProperties(new Configuration(config.getSubProperties("plugins." + p + ".conf."))));
            log.info("Load plugin {}", p);
        }
        log.info("AWACS prepared.");
    }

    public void run() {
        for (PluginDescriptor descriptor : descriptors) {
            try {
                Class<?> clazz = classLoader.findClass(descriptor.getClassName());
                Plugin plugin = (Plugin) clazz.newInstance();
                plugin.init(descriptor.getPluginProperties());
                plugin.rock();
                plugins.add(plugin);
                log.info("AWACS plugin {} initialized.", descriptor.getClassName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                log.error("Cannot start " + descriptor.getPluginName(), e);
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
                queue.close();
            }
        });
    }
}
