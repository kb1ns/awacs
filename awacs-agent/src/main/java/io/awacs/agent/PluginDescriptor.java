/**
 * Copyright 2016-2017 AWACS Project.
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

import java.util.jar.JarFile;

/**
 * Plugins' simple descriptor
 * Created by pixyonly on 02/09/2017.
 */
final class PluginDescriptor {

    private String pluginName;

    private String className;

    private JarFile jarFile;

    private Configuration pluginProperties;

    public PluginDescriptor(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getClassName() {
        return className;
    }

    public PluginDescriptor setClassName(String className) {
        this.className = className;
        return this;
    }

    public PluginDescriptor setPluginProperties(Configuration pluginProperties) {
        this.pluginProperties = pluginProperties;
        return this;
    }

    public Configuration getPluginProperties() {
        return pluginProperties;
    }

    public PluginDescriptor setJarFile(JarFile jarFile) {
        this.jarFile = jarFile;
        return this;
    }

    public JarFile getJarFile() {
        return jarFile;
    }
}
