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

package io.awacs.server;

import com.google.common.base.Strings;
import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.InitializationException;
import io.awacs.common.Releasable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class Components implements Configurable, Releasable {

    private static final Logger log = LoggerFactory.getLogger(Components.class);

    final Map<String, Configurable> holder = new HashMap<>();

    @Override
    public void init(Configuration configuration) {
        String[] names = configuration.getArray(Configurations.COMPONENT_PREFIX);
        for (String name : names) {
            log.debug("Component {} configuration found.", name);
            Configuration selfConfig = configuration.getSubConfig(Configurations.COMPONENT_PREFIX + "." + name + ".");
            String className = selfConfig.getString(Configurations.COMPONENT_CLASS);
            if (Strings.isNullOrEmpty(className))
                throw new InitializationException("Config `class` is necessary for components. e.g. awacs.components." + name + ".class = ?");
            try {
                Class<?> clazz = Class.forName(className);
                Configurable component = (Configurable) clazz.newInstance();
                component.init(selfConfig);
                holder.put(name, component);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error("Cannot initialized component " + name, e);
                throw new InitializationException(e);
            }
        }
    }

    public Object lookup(String name, Class<?> clazz) {
        if (!Strings.isNullOrEmpty(name)) {
            Object r = holder.get(name);
            if (r == null || !clazz.isInstance(r)) {
                throw new ComponentException(String.format("Component %s#%s not found.", clazz.getName(), name));
            }
            return r;
        }
        return holder.values().stream().filter(clazz::isInstance).findAny().orElseThrow(ComponentException::new);
    }

    @Override
    public void release() {
        holder.values().stream().filter(c -> c instanceof Releasable).map(c -> (Releasable) c).forEach(Releasable::release);
    }
}
