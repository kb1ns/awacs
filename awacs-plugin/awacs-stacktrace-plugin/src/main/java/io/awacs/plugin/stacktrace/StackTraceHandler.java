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

package io.awacs.plugin.stacktrace;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.awacs.core.Configuration;
import io.awacs.core.EnableInjection;
import io.awacs.core.InitializationException;
import io.awacs.core.PluginHandler;
import io.awacs.core.transport.Message;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;
import io.awacs.repository.EmailRepository;
import io.awacs.repository.MailForm;
import io.awacs.repository.MongoRepository;
import org.bson.Document;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Created by pixyonly on 16/10/26.
 */
@EnableInjection
public class StackTraceHandler implements PluginHandler {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(PluginHandler.class);

    @Resource
    private EmailRepository emailRepository;

    @Resource
    private MongoRepository mongoRepository;

    private List<String> toList = Collections.emptyList();

    private List<String> ccList = Collections.emptyList();

    private boolean enableNotification = false;

    private List<String> excludeExceptionPrefixes = Collections.emptyList();

    private static final String ENABLE_NOTIFICATION = "notification.enable";

    private static final String NOTIFICATION_RECIPIENTS_TO = "notification.recipients.to";

    private static final String NOTIFICATION_RECIPIENTS_CC = "notification.recipients.cc";

    private static final String NOTIFICATION_EXCEPTION_PREFIX = "notification.excludeExceptionPrefix";

    private static final boolean DEFAULT_ENABLE_NOTIFICATION = false;

    @Override
    public Message handle(Message message, InetSocketAddress address) {
        String content = new String(message.body());
        logger.debug(content);
        JSONObject json = JSONObject.parseObject(content);
        json.put("host", address.getAddress().getHostAddress());
        json.put("pid", message.getPid());
        json.put("timestamp", message.getTimestamp());
        if (json.containsKey("exception")) {
            if (enableNotification) {
                boolean valid = true;
                for (String prefix : excludeExceptionPrefixes) {
                    if (json.getString("exception").startsWith(prefix)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    MailForm mail = new MailForm().setText(JSON.toJSONString(json, true))
                            .setTo(toList)
                            .setCc(ccList)
                            .setSubject(String.format("Warning from %s", address.getAddress().getHostAddress()));
                    emailRepository.send(mail);
                }
            }
        } else {
            JSONArray methodCallChain = json.getJSONArray("stack");
            json.put("stack", buildCallerStack(methodCallChain));
        }
        try {
            mongoRepository.save("stacktrace_plugin", Document.parse(json.toJSONString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject buildCallerStack(JSONArray seq) {
        try {
            Stack<JSONObject> stack = new Stack<>();
            JSONObject head = seq.getJSONObject(0);
            seq.remove(0);
            stack.push(head);
            JSONObject rec = null;
            while (!stack.isEmpty()) {
                JSONObject _head = seq.getJSONObject(0);
                seq.remove(0);
                JSONObject top = stack.peek();
                top.putIfAbsent("methods", new JSONArray());
                if (_head.getIntValue("flag") == 0) {
                    top.getJSONArray("methods").add(_head);
                    stack.push(_head);
                } else {
                    top.remove("flag");
                    top.put("elapsedTime", _head.getLongValue("timestamp") - top.getLongValue("timestamp"));
                    top.remove("timestamp");
                    rec = stack.pop();
                }
            }
            return rec;
        } catch (Exception e) {
            throw new IllegalArgumentException(seq.toJSONString());
        }
    }

    @Override
    public void init(Configuration configuration) throws InitializationException {
        enableNotification = configuration.getBoolean(ENABLE_NOTIFICATION, DEFAULT_ENABLE_NOTIFICATION);
        if (enableNotification) {
            String to = configuration.getString(NOTIFICATION_RECIPIENTS_TO);
            if (to != null)
                toList = Arrays.asList(to.split(","));

            String cc = configuration.getString(NOTIFICATION_RECIPIENTS_CC);
            if (cc != null)
                ccList = Arrays.asList(cc.split(","));

            String excludes = configuration.getString(NOTIFICATION_EXCEPTION_PREFIX);
            if (excludes != null)
                excludeExceptionPrefixes = Arrays.asList(excludes.split(","));
        }
    }

}
