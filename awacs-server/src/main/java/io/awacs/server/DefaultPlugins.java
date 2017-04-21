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
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import io.awacs.core.*;
import io.awacs.core.transport.Key;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by antong on 16/9/20.
 */
public class DefaultPlugins implements Plugins {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(Plugins.class);

    private final Map<Key<?>, PluginHandler> handlers = new HashMap<>();

    private final Map<Key<?>, PluginDescriptor> descriptors = new HashMap<>();

    private Repositories repositories;

    @Override
    public Set<Key<?>> keySet() {
        return handlers.keySet();
    }

    @Override
    public PluginHandler getPluginHandler(Key<?> key) {
        return handlers.get(key);
    }

    @Override
    public PluginDescriptor getPluginDescriptor(Key<?> key) {
        return descriptors.get(key);
    }

    @Override
    public void setRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

    @Override
    public void init(Configuration configuration) throws InitializationException {
        String[] pluginNames = configuration.getString(Configurations.PLUGIN_PREFIX).trim().split(",");
        for (String pluginName : pluginNames) {
            try {
                logger.info("Plugin {} configuration found.", pluginName);
                ImmutableMap<String, String> pluginConfig = configuration.getSubProperties(Configurations.PLUGIN_PREFIX + "." + pluginName + ".");
                String pluginClassName = pluginConfig.get(Configurations.PLUGIN_CLASS);
                String keyType = pluginConfig.get(Configurations.KEY_CLASS);
                String keyValue = pluginConfig.get(Configurations.KEY_VALUE);
                String pluginPathRoot = Configurations.getPluginPath();
                String relativePath = "/awacs-" + pluginName + "-plugin.jar";
                String pluginPath = pluginPathRoot + relativePath;
                String fileHash = Files.hash(new File(pluginPath), Hashing.sha1()).toString();
                ImmutableMap<String, String> pluginProperties = configuration.getSubProperties(Configurations.PLUGIN_PREFIX + "." + pluginName + "." + Configurations.PLUGIN_PROPERTIES + ".");
                Map<String, String> props = new HashMap<>();
                for (String key : pluginProperties.keySet())
                    props.put(key, pluginProperties.get(key));
                PluginDescriptor descriptor = new PluginDescriptor()
                        .setPluginClass(pluginClassName)
                        .setHash(fileHash)
                        .setName(pluginName)
                        .setKeyClass(keyType)
                        .setKeyValue(keyValue)
                        .setProperties(props)
                        .setDownloadUrl(relativePath);

                String handlerClassName = pluginConfig.get(Configurations.HANDLER_CLASS);
                Class<?> clazz = Class.forName(handlerClassName);
                PluginHandler handler = (PluginHandler) clazz.newInstance();
                ImmutableMap<String, String> handlerProperties = configuration.getSubProperties(Configurations.PLUGIN_PREFIX + "." + pluginName + "." + Configurations.HANDLER_PROPERTIES + ".");
                handler.init(new Configuration(handlerProperties));

                if (handler instanceof RepositoriesAware) {
                    ((RepositoriesAware) handler).setContext(repositories);
                }
                if (clazz.isAnnotationPresent(EnableInjection.class)) {
                    List<Field> waitForInject = Stream.of(clazz.getDeclaredFields())
                            .filter(f -> f.isAnnotationPresent(Resource.class) || f.isAnnotationPresent(Injection.class))
                            .collect(Collectors.toList());
                    for (Field f : waitForInject) {
                        f.setAccessible(true);
                        if (f.get(handler) == null) {
                            String name;
                            if (f.isAnnotationPresent(Resource.class)) {
                                Resource r = f.getDeclaredAnnotation(Resource.class);
                                name = r.name();
                            } else {
                                Injection i = f.getDeclaredAnnotation(Injection.class);
                                name = i.name();
                            }
                            Object repo = repositories.lookup(name, f.getType());
                            f.set(handler, repo);
                            logger.debug("Inject repository {} to plugin handler {}", repo, handler);
                        }
                    }
                }
                Key<?> k = Key.getKey(keyType, keyValue);
                handlers.put(k, handler);
                descriptors.put(k, descriptor);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IOException
                    | NoSuchKeyTypeException | RepositoryNotFoundException e) {
                e.printStackTrace();
                throw new InitializationException();
            }
        }
    }
}
