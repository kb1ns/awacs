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

import com.alibaba.fastjson.JSONObject;
import io.awacs.agent.MessageHub;
import io.awacs.core.NoSuchKeyTypeException;
import io.awacs.core.Plugin;
import io.awacs.core.PluginDescriptor;
import io.awacs.core.transport.Key;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;
import io.awacs.protocol.binary.BinaryMessage;
import io.awacs.protocol.binary.ByteKey;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Created by pixyonly on 16/10/25.
 * 方法调用堆栈的追踪插件
 */
public class StackTracePlugin implements Plugin {

    private static LoggerPlus logger = LoggerPlusFactory.getLogger(StackTracePlugin.class);

    private static PluginDescriptor descriptor;

    private static Key<?> key;

    private static ClassFilter classFilter;

    private Instrumentation inst;

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    //发送线程的堆栈信息
    public static void incrAccess() {
        logger.debug("Request complete, event fired.");
        String report = buildAccessReport(CallStack.reset());
        MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
                .setKey(key)
                .setVersion(BinaryMessage.C_VERSION)
                .setBody(report.getBytes())
                .build());
    }

    private static String buildAccessReport(CallElement root) {
        return "{\"thread\":\""
                + Thread.currentThread().getName()
                + "\",\"stack\":"
                + root
                + "}";
    }

    //发送异常信息
    public static void incrFailure(Throwable e) {
        logger.info("Exception occur, event fired.");
        CallStack.reset();
        JSONObject report = new JSONObject();
        report.put("thread", Thread.currentThread().getName());
        report.put("stack", e.getStackTrace());
        report.put("exception", e.getClass().getCanonicalName());
        report.put("message", e.getMessage());
        MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
                .setKey(key)
                .setVersion(BinaryMessage.C_VERSION)
                .setBody(report.toJSONString().getBytes())
                .build());
    }

    @Override
    public void setDescriptor(PluginDescriptor descriptor) {
        StackTracePlugin.descriptor = descriptor;
        try {
            key = ByteKey.getKey(descriptor.getKeyClass(), descriptor.getKeyValue());
            String prefixes = descriptor.getProperties().get("packagePrefix");
            String[] filters = prefixes == null ? null : prefixes.split(",");
            classFilter = new ClassFilter(filters);
        } catch (NoSuchKeyTypeException e) {
            e.printStackTrace();
        }
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
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    if (!classFilter.doFilter(className)) {
                        return classfileBuffer;
                    }
                    ClassWriter cw = new ClassWriter(0);
                    ClassVisitor cv = new StackTraceClassAdaptor(cw);
                    ClassReader cr = new ClassReader(classfileBuffer);
                    cr.accept(cv, 0);
                    return cw.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        });
    }
}
