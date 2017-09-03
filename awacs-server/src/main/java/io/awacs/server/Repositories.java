/**
 * Copyright 2016 AWACS Project.
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
import io.awacs.common.Repository;
import io.awacs.common.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 *
 * Created by pixyonly on 16/9/27.
 */
public class Repositories implements Configurable {

    private static final Logger log = LoggerFactory.getLogger(Repositories.class);

    final ArrayList<Repository> holder = new ArrayList<>(256);

    @Override
    public void init(Configuration configuration) throws InitializationException {
        String[] repoNames = configuration.getArray(Configurations.REPOSITORY_PREFIX);
        for (String repoName : repoNames) {
            log.debug("Repository {} configuration found.", repoName);
            Configuration selfConfig = configuration.getSubConfig(Configurations.REPOSITORY_PREFIX + "." + repoName + ".");
            String className = selfConfig.getString(Configurations.REPOSITORY_CLASS);
            if (Strings.isNullOrEmpty(className))
                throw new InitializationException();
            try {
                Class<?> clazz = Class.forName(className);
                Repository repo = (Repository) clazz.newInstance();
                repo.init(selfConfig);
                holder.set(selfConfig.getInteger("key"), repo);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error("Cannot initialized repository " + repoName, e);
                throw new InitializationException();
            }
        }
    }
}
