package io.awacs.agent;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * We don't use root classloader to load plugin
 * Created by pixyonly on 02/09/2017.
 */
class PluginClassLoader extends URLClassLoader {

    public PluginClassLoader(List<PluginDescriptor> descriptors) {
        this(new URL[]{});
        for (PluginDescriptor descriptor : descriptors) {
            addJar(descriptor.getJarFile().getName());
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
            URL url = new URL("file://" + jar);
            super.addURL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
