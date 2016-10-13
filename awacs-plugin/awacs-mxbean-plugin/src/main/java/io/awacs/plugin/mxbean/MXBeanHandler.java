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

import io.awacs.core.Configuration;
import io.awacs.core.EnableInjection;
import io.awacs.core.InitializationException;
import io.awacs.core.PluginHandler;
import io.awacs.core.transport.Message;
import io.awacs.repository.MongoRepository;
import org.bson.Document;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * Created by antong on 16/9/20.
 */
@EnableInjection
public class MXBeanHandler implements PluginHandler {

    @Resource
    private MongoRepository mongoRepository;

    @Override
    public Message handle(Message message, InetSocketAddress address) {
        try {
            Document doc = Document.parse(new String(message.body()));
            String collection = "key_" + message.getKey();
            doc.put("host", address.getAddress().getHostAddress());
            doc.put("port", address.getPort());
            mongoRepository.save(collection, doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void init(Configuration configuration) throws InitializationException {

    }
}
