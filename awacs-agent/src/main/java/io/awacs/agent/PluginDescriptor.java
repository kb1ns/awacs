package io.awacs.agent;

import io.awacs.common.Configuration;

/**
 *
 * Created by pixyonly on 02/09/2017.
 */
final class PluginDescriptor {

    private String pluginName;

    private String className;

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
}
