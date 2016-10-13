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

package io.awacs.agent;

import io.awacs.core.Agent;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;

import java.lang.instrument.Instrumentation;

/**
 * JavaAgent启动类
 * Created by pixyonly on 7/12/16.
 */
public class Bootstrap {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger("agent");

    public static void premain(String preArgs, Instrumentation inst) {
        logger.info("AWACS Rocks.\nConnecting to server: {}", preArgs);
        final Agent agent = new PluggableAgent(inst, preArgs);
        try {
            agent.pullConfiguration();
            agent.fetchPlugins();
        } catch (Exception e) {
            e.printStackTrace();
        }
        agent.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                agent.stop();
            }
        });
    }
}
