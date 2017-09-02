package io.awacs.agent;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * Created by pixyonly on 02/09/2017.
 */
class PluginClassLoader extends URLClassLoader {

    final String PLUGIN_NAME_PATTERN = "awacs-%s-plugin.jar";

    public PluginClassLoader(String baseDir, String[] pluginNames) {
        this(new URL[]{});
        for(String name : pluginNames) {
            addJar(baseDir + String.format(PLUGIN_NAME_PATTERN, name));
        }
    }

    PluginClassLoader(URL[] urls) {
        super(urls);
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    private void addJar(String jar) {
        try {
            URL url = new URL(jar);
            super.addURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
