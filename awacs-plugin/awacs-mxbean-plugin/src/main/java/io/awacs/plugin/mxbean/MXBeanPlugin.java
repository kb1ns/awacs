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

package io.awacs.plugin.mxbean;

import io.awacs.agent.Plugin;
import io.awacs.common.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by antong on 16/9/28.
 */
public class MXBeanPlugin implements Plugin {

    private static Logger log = LoggerFactory.getLogger("agent");

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @Override
    public void init(Configuration properties) {

    }

    @Override
    public void rock() {
//        executor.scheduleAtFixedRate(new Command(), 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void over() {

    }

//    class Command implements Runnable {

//        @Override
//        public void run() {
//            try {
//                MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
//                        .setKey(ByteKey.getKey(descriptor.getKeyClass(), descriptor.getKeyValue()))
//                        .setBody(new MXBeanReport().toString().getBytes())
//                        .build());
//            } catch (NoSuchKeyTypeException e) {
//                logger.log(Level.SEVERE, "Round failed.");
//            }
//        }
//    }

    public static void main(String[] args) {
        new MXBeanPlugin().rock();
    }

}
