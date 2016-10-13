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

package io.awacs.agent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.awacs.core.Agent;
import io.awacs.core.Plugin;
import io.awacs.core.PluginDescriptor;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Created by pixyonly on 16/9/3.
 */
public class PluggableAgent implements Agent {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(Agent.class);

    private Instrumentation inst;

    private List<PluginDescriptor> descriptors;

    private String agentLocation;

    private String pluginDownloadUrl;

    private List<InetSocketAddress> addresses;

    public PluggableAgent(Instrumentation inst, String pluginDownloadUrl) {
        this.inst = inst;
        //TODO multi server
        this.pluginDownloadUrl = pluginDownloadUrl;
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        this.agentLocation = path.substring(0, path.lastIndexOf('/')) + "/";
        this.descriptors = new LinkedList<>();
        this.addresses = new LinkedList<>();
    }

    @Override
    public List<PluginDescriptor> pullConfiguration() throws IOException {
        URLConnection urlConnection = new URL(pluginDownloadUrl).openConnection();
        urlConnection.connect();
        InputStream is = urlConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String strRead;
        StringBuilder result = new StringBuilder();
        while ((strRead = reader.readLine()) != null)
            result.append(strRead);
        reader.close();
        JSONObject body = JSONObject.parseObject(result.toString());
        JSONArray plugins = body.getJSONArray("plugins");
        for (int i = 0; i < plugins.size(); i++) {
            JSONObject plugin = plugins.getJSONObject(i);
            PluginDescriptor descriptor = plugin.toJavaObject(PluginDescriptor.class);
            if (descriptor.getDownloadUrl().startsWith("/")) {
                descriptor.setDownloadUrl(pluginDownloadUrl + descriptor.getDownloadUrl());
            }
            //TODO compare hash to decide whether download
            descriptors.add(descriptor);
            logger.info(String.format("Plugin definition found: %s->%s@%s", descriptor.getName(),
                    descriptor.getPluginClass(),
                    descriptor.getDownloadUrl()));
        }
        JSONArray servers = body.getJSONArray("serverAddrs");
        for (int i = 0; i < servers.size(); i++) {
            String[] split = servers.getString(i).split(":");
            addresses.add(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        }
        return descriptors;
    }

    @Override
    public void fetchPlugins() throws Exception {
        for (PluginDescriptor descriptor : descriptors) {
            try {
                URL url = new URL(descriptor.getDownloadUrl());
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();
                String resourceName = url.getFile();
                String writeToPath = agentLocation + (!resourceName.contains("/") ?
                        resourceName :
                        resourceName.substring(resourceName.lastIndexOf("/") + 1));
                OutputStream os = new FileOutputStream(writeToPath);
                byte[] buf = new byte[4096];
                int read;
                while ((read = is.read(buf)) != -1) {
                    os.write(buf, 0, read);
                }
                os.flush();
                os.close();
                is.close();
                logger.info("Plugin downloaded " + writeToPath);
                JarFile jarFile = new JarFile(writeToPath);
                inst.appendToSystemClassLoaderSearch(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        NettyClient nettyClient = new NettyClient.Builder().setAddresses(addresses).build();
        MessageHub.instance.register(nettyClient);
        for (PluginDescriptor descriptor : descriptors) {
            try {
                Class<?> clazz = Class.forName(descriptor.getPluginClass());
                Plugin p = (Plugin) clazz.newInstance();
                p.setDescriptor(descriptor);
                p.setInstrumentation(inst);
                p.boot();
                logger.info("Load plugin " + descriptor.getName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        MessageHub.instance.unregister();
    }
}
