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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.awacs.core.Configuration;
import io.awacs.core.PluginDescriptor;
import io.awacs.core.Repositories;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by antong on 16/9/12.
 */
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        Configuration configuration = Configurations.loadConfigurations();

        Repositories repositories = new DefaultRepositories();
        repositories.init(configuration);

        DefaultPlugins plugins = new DefaultPlugins();
        plugins.setRepositories(repositories);
        plugins.init(configuration);

        List<String> addrs = Configurations.exportServerAddr(configuration);
        List<PluginDescriptor> descriptors = plugins.keySet().parallelStream().map(plugins::getPluginDescriptor).collect(Collectors.toList());
        JSONObject json = new JSONObject();
        json.put("plugins", descriptors);
        json.put("serverAddrs", addrs);
        Configurations.dump(JSON.toJSONString(json, true));

        MessageReportServer tcpServer = new MessageReportServer();
        tcpServer.init(configuration);
        tcpServer.setPlugins(plugins);

        StaticResourceServer httpServer = new StaticResourceServer();
        httpServer.init(configuration);

        httpServer.start();
        tcpServer.start();
    }
}
