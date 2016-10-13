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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.awacs.core.*;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by pixyonly on 16/9/29.
 */
public class DefaultRepositories implements Repositories {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(Repositories.class);

    private ConcurrentMap<String, Object> repositories = new ConcurrentHashMap<>();

    @Override
    public void init(Configuration configuration) throws InitializationException {
        String[] repoNames = configuration.getString(Configurations.REPOSITORY_PREFIX).trim().split(",");
        for (String repoName : repoNames) {
            logger.debug("Repository {} configuration found.", repoName);
            ImmutableMap<String, String> resourceConfig = configuration.getSubProperties(Configurations.REPOSITORY_PREFIX + "." + repoName + ".");
            String className = resourceConfig.get(Configurations.REPOSITORY_CLASS);
            if (Strings.isNullOrEmpty(className))
                throw new InitializationException();
            try {
                Class<?> clazz = Class.forName(className);
                Object repository = clazz.newInstance();
                if (repository instanceof Configurable) {
                    ((Configurable) repository).init(new Configuration(resourceConfig));
                }
                repositories.put(repoName, repository);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
                throw new InitializationException();
            }
        }
    }

    @Override
    public Object lookup(String id, Class<?> clazz) throws RepositoryNotFoundException {
        if (!Strings.isNullOrEmpty(id)) {
            Object r = repositories.get(id);
            if (r == null || !clazz.isInstance(r)) {
                logger.warn("Repository {}@{} not found.", clazz.getName(), id);
                throw new RepositoryNotFoundException(id);
            }
            return r;
        } else {
            return repositories.values().stream().filter(clazz::isInstance).findAny()
                    .orElseThrow(RepositoryNotFoundException::new);
        }
    }

}
