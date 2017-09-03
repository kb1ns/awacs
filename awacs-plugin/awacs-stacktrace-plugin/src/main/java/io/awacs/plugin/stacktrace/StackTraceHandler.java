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

package io.awacs.plugin.stacktrace;

//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import io.awacs.core.Configuration;
//import io.awacs.core.EnableInjection;
//import io.awacs.core.InitializationException;
//import io.awacs.core.PluginHandler;
//import io.awacs.core.transport.Message;
//import io.awacs.core.util.LoggerPlus;
//import io.awacs.core.util.LoggerPlusFactory;
//import io.awacs.repository.EmailRepository;
//import io.awacs.repository.MailForm;
//import io.awacs.repository.influx.InfluxRepository;
//import org.influxdb.dto.Point;

/**
 * Created by pixyonly on 16/10/26.
 */
public class StackTraceHandler {

//    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(PluginHandler.class);
//
//    @Resource
//    private EmailRepository emailRepository;
//
//    @Resource
//    private InfluxRepository influxRepository;
//
//    private List<String> toList = Collections.emptyList();
//
//    private List<String> ccList = Collections.emptyList();
//
//    private boolean enableNotification = false;
//
//    private List<String> excludeExceptionPrefixes = Collections.emptyList();
//
//    private List<String> includeExceptionPrefixes = Collections.emptyList();
//
//    private static final String ENABLE_NOTIFICATION = "notification.enable";
//
//    private static final String NOTIFICATION_RECIPIENTS_TO = "notification.recipients.to";
//
//    private static final String NOTIFICATION_RECIPIENTS_CC = "notification.recipients.cc";
//
//    private static final String NOTIFICATION_EXCEPTION_EXCLUDE_PREFIX = "notification.excludeExceptionPrefix";
//
//    private static final String NOTIFICATION_EXCEPTION_INCLUDE_PREFIX = "notification.includeExceptionPrefix";
//
//    private static final boolean DEFAULT_ENABLE_NOTIFICATION = false;
//
//    @Override
//    public Message handle(Message message, InetSocketAddress address) {
//        String content = new String(message.body());
//        JSONObject json = JSONObject.parseObject(content);
//
//        if (json.containsKey("exception")) {
//            String pretty = prettify(json.getJSONArray("stack"));
//            Point err = Point.measurement("").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                    .tag("ip", address.getAddress().getHostAddress())
//                    .tag("exception", json.getString("exception"))
//                    .addField("message", json.getString("message"))
//                    .addField("thread", json.getString("thread"))
//                    .addField("pid", message.getPid())
//                    .addField("stack", pretty)
//                    .addField("execution_time", -1)
//                    .build();
//            influxRepository.write(err);
//            if (enableNotification) {
//                boolean valid = false;
//                for (String prefix : excludeExceptionPrefixes) {
//                    if (json.getString("exception").startsWith(prefix)) {
//                        valid = true;
//                        break;
//                    }
//                }
//                for (String prefix : includeExceptionPrefixes) {
//                    if (json.getString("exception").startsWith(prefix)) {
//                        valid = false;
//                        break;
//                    }
//                }
//                if (!valid) {
//                    String template = String.format("Host: %s\nPID: %d\nThread: %s\nthrows %s:%s\n%s",
//                            address.getAddress().getHostAddress(),
//                            message.getPid(),
//                            json.getString("thread"),
//                            json.getString("exception"),
//                            json.getString("message"),
//                            pretty);
//                    MailForm mail = new MailForm().setSubject("Server warning")
//                            .setText(template)
//                            .setTo(toList)
//                            .setCc(ccList);
//                    emailRepository.send(mail);
//                }
//            }
//            return null;
//        }
//        JSONObject stack = json.getJSONObject("stack");
//        //TODO config measurement
//        Point p = Point.measurement("").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .tag("ip", address.getAddress().getHostAddress())
//                .tag("entry", stack.getString("caller").replaceAll("/", "."))
//                .addField("thread", json.getString("thread"))
//                .addField("pid", message.getPid())
//                .addField("stack", prettify(stack))
//                .addField("execution_time", stack.getIntValue("elapsedTime"))
//                .build();
//        influxRepository.write(p);
//        return null;
//    }
//
//
//    @Override
//    public void init(Configuration configuration) throws InitializationException {
//        enableNotification = configuration.getBoolean(ENABLE_NOTIFICATION, DEFAULT_ENABLE_NOTIFICATION);
//        if (enableNotification) {
//            String to = configuration.getString(NOTIFICATION_RECIPIENTS_TO);
//            if (to != null) {
//                String[] r = to.split(",");
//                for (String t : r) {
//                    toList.add(t.trim());
//                }
//            }
//
//            String cc = configuration.getString(NOTIFICATION_RECIPIENTS_CC);
//            if (cc != null) {
//                String[] r = cc.split(",");
//                for (String t : r) {
//                    ccList.add(t.trim());
//                }
//            }
//
//            String excludes = configuration.getString(NOTIFICATION_EXCEPTION_EXCLUDE_PREFIX);
//            if (excludes != null) {
//                String[] r = excludes.split(",");
//                for (String t : r) {
//                    excludeExceptionPrefixes.add(t.trim());
//                }
//            }
//
//            String includes = configuration.getString(NOTIFICATION_EXCEPTION_INCLUDE_PREFIX);
//            if (includes != null) {
//                String[] r = includes.split(",");
//                for (String t : r) {
//                    includeExceptionPrefixes.add(t.trim());
//                }
//            }
//        }
//    }
//
//    String prettify(JSONObject json) {
//        return tabLevel(json, 0).toString();
//    }
//
//    String prettify(JSONArray json) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < json.size(); i++) {
//            JSONObject span = json.getJSONObject(i);
//            if (span.getBooleanValue("nativeMethod"))
//                break;
//            sb.append('-').append(span.getString("className"))
//                    .append('#')
//                    .append(span.getString("methodName"))
//                    .append('@')
//                    .append(span.getIntValue("lineNumber"))
//                    .append('\n');
//        }
//        return sb.toString();
//    }
//
//    private StringBuilder tabLevel(JSONObject sub, int tab) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < tab * 2; i++)
//            sb.append("+");
//        sb.append(sub.getString("caller").replaceAll("/", "."))
//                .append(":")
//                .append(sub.getIntValue("elapsedTime"))
//                .append("|")
//                .append(sub.getIntValue("callCount"));
//        JSONArray subMethods = sub.getJSONArray("subMethods");
//        if (!subMethods.isEmpty() && sub.getIntValue("elapsedTime") > 0) {
//            for (int i = 0; i < subMethods.size(); i++) {
//                sb.append('\n').append(tabLevel(subMethods.getJSONObject(i), tab + 1));
//            }
//        }
//        return sb;
//    }
}
