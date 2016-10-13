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

package io.awacs.plugin.mxbean;

import io.awacs.core.NoSuchKeyTypeException;
import io.awacs.core.Plugin;
import io.awacs.core.PluginDescriptor;
import io.awacs.agent.MessageHub;
import io.awacs.protocol.binary.BinaryMessage;
import io.awacs.protocol.binary.ByteKey;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by antong on 16/9/28.
 */
public class MXBeanPlugin implements Plugin {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);

    private static Logger logger = java.util.logging.Logger.getLogger("agent");

    private static PluginDescriptor descriptor;

    private Instrumentation inst;

    public MXBeanPlugin() {

    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void setDescriptor(PluginDescriptor descriptor) {
        MXBeanPlugin.descriptor = descriptor;
    }

    @Override
    public Instrumentation getInstrumentation() {
        return inst;
    }

    @Override
    public void setInstrumentation(Instrumentation inst) {
        this.inst = inst;
    }

    @Override
    public void boot() {
        executor.scheduleAtFixedRate(new Command(), 0, 1, TimeUnit.MINUTES);
    }

    class Command implements Runnable {

        @Override
        public void run() {
            try {
                MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
                        .setKey(ByteKey.getKey(descriptor.getKeyClass(), descriptor.getKeyValue()))
                        .setBody(new MXBeanReport().toString().getBytes())
                        .build());
            } catch (NoSuchKeyTypeException e) {
                logger.log(Level.SEVERE, "Round failed.");
            }
        }
    }

    public static void main(String[] args) {
        new MXBeanPlugin().boot();
    }

}
