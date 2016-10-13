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

import io.awacs.agent.MessageHub;
import io.awacs.core.NoSuchKeyTypeException;
import io.awacs.core.Plugin;
import io.awacs.core.PluginDescriptor;
import io.awacs.core.transport.Key;
import io.awacs.core.transport.Message;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;
import io.awacs.protocol.binary.BinaryMessage;
import io.awacs.protocol.binary.ByteKey;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Created by pixyonly on 7/5/16.
 */
public class SpringmvcPlugin implements Plugin {

    private static LoggerPlus logger = LoggerPlusFactory.getLogger(SpringmvcPlugin.class);

    private static PluginDescriptor descriptor;

    private static Key<?> key;

    private Instrumentation inst;

    public SpringmvcPlugin() {
    }

    public static void incrAccess(String name, long duration) {
        String content = generateReport(name, null, duration);
        logger.trace("Event fired.");
        Message m = new BinaryMessage.BinaryMessageBuilder().setKey(key).setBody(content.getBytes()).build();
        MessageHub.instance.publish(m);
    }

    public static void incrFailure(String name, Throwable t) {
        String content = generateReport(name, t, 0l);
        logger.trace("Event fired.");
        Message m = new BinaryMessage.BinaryMessageBuilder().setKey(key).setBody(content.getBytes()).build();
        MessageHub.instance.publish(m);
    }

    private static String generateReport(String name, Throwable t, long duration) {
        boolean successful = t == null;
        SpringmvcReport report = new SpringmvcReport().setElapsedTime((int) duration)
                .setMessage(!successful ? t.getMessage() : "success").setService(name)
                .setSuccessful(successful).setTimestamp(System.currentTimeMillis())
                .setException(!successful ? t.getClass().getCanonicalName() : "")
                .setStack(!successful ? t.getStackTrace() : new StackTraceElement[0]);
        return report.toString();
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void setDescriptor(PluginDescriptor descriptor) {
        SpringmvcPlugin.descriptor = descriptor;
        try {
            key = ByteKey.getKey(descriptor.getKeyClass(), descriptor.getKeyValue());
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
                final ServiceWrapper wrapper = new ServiceWrapper();
                ClassNode cn = new ClassNode();
                ClassReader cr = new ClassReader(classfileBuffer);
                cr.accept(cn, 0);
                boolean transformed = wrapper.transform(cn);
                ClassWriter cw = new ClassWriter(0);
                cn.accept(cw);
                if (transformed) {
                    logger.info(className + " has been catched by springmvc plugin.");
                }
                return cw.toByteArray();
            }
        });
    }

}
