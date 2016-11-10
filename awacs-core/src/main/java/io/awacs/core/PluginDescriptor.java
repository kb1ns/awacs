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

package io.awacs.core;

import java.util.Map;

/**
 * Created by pixyonly on 7/14/16.
 */
public final class PluginDescriptor {

    private String name;

    private String downloadUrl;

    private Map<String, String> properties;

    private String pluginClass;

    private String keyValue;

    private String keyClass;

    private String hash;

    public PluginDescriptor setName(String name) {
        this.name = name;
        return this;
    }

    public PluginDescriptor setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public PluginDescriptor setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public PluginDescriptor setPluginClass(String pluginClass) {
        this.pluginClass = pluginClass;
        return this;
    }

    public PluginDescriptor setKeyValue(String keyValue) {
        this.keyValue = keyValue;
        return this;
    }

    public PluginDescriptor setKeyClass(String keyClass) {
        this.keyClass = keyClass;
        return this;
    }

    public PluginDescriptor setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getPluginClass() {
        return pluginClass;
    }

    public String getKeyClass() {
        return keyClass;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public String getHash() {
        return hash;
    }
}
