package io.awacs.agent;

import java.util.Map;

/**
 *
 * Created by pixyonly on 02/09/2017.
 */
final class PluginDescriptor {

    private String pluginName;

    private String className;

    private Map<String, String> pluginProperties;

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

    public Map<String, String> getPluginProperties() {
        return pluginProperties;
    }

    public PluginDescriptor setPluginProperties(Map<String, String> pluginProperties) {
        this.pluginProperties = pluginProperties;
        return this;
    }
}
