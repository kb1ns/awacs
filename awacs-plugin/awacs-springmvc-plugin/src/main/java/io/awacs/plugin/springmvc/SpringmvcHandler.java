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

package io.awacs.plugin.springmvc;

import com.google.common.base.Strings;
import io.awacs.core.Configuration;
import io.awacs.core.EnableInjection;
import io.awacs.core.InitializationException;
import io.awacs.core.PluginHandler;
import io.awacs.core.transport.Message;
import io.awacs.repository.EmailRepository;
import io.awacs.repository.MailForm;
import io.awacs.repository.MongoRepository;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Created by antong on 16/9/20.
 */
@EnableInjection
public class SpringmvcHandler implements PluginHandler {

    @Resource
    private EmailRepository emailRepository;

    @Resource
    private MongoRepository mongoRepository;

    private List<String> toList;

    private List<String> ccList;

    private boolean enableNotification;

    private static String ENABLE_NOTIFICATION = "enableNotification";

    private static String NOTIFICATION_RECIPIENTS_TO = "notification.recipients.to";

    private static String NOTIFICATION_RECIPIENTS_CC = "notification.recipients.cc";

    private static boolean DEFAULT_ENABLE_NOTIFICATION = false;

    private static JsonWriterSettings jsonWriterSettings = new JsonWriterSettings(true);

    @Override
    public void init(Configuration configuration) throws InitializationException {
        enableNotification = configuration.getBoolean(ENABLE_NOTIFICATION, DEFAULT_ENABLE_NOTIFICATION);
        if (enableNotification) {
            toList = Arrays.asList(configuration.getString(NOTIFICATION_RECIPIENTS_TO, "").split(","));
            if (Strings.isNullOrEmpty(toList.get(0)))
                toList.clear();
            ccList = Arrays.asList(configuration.getString(NOTIFICATION_RECIPIENTS_CC, "").split(","));
            if (Strings.isNullOrEmpty(ccList.get(0)))
                ccList.clear();
        }
    }

    @Override
    public Message handle(Message message, InetSocketAddress address) {
        try {
            String collection = "key_" + message.getKey();
            Document doc = Document.parse(new String(message.body()));
            doc.put("ip", address.getAddress().getHostAddress());
            doc.put("pid", message.getPid());
            if (enableNotification && !Strings.isNullOrEmpty(doc.getString("exception"))) {
                MailForm mail = new MailForm().setText(doc.toJson(jsonWriterSettings))
                        .setTo(toList)
                        .setCc(ccList)
                        .setSubject(String.format("Warning from %s", address.getAddress().getHostAddress()));
                emailRepository.send(mail);
            }
            mongoRepository.save(collection, doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
