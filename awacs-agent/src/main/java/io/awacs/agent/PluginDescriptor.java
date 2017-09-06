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
