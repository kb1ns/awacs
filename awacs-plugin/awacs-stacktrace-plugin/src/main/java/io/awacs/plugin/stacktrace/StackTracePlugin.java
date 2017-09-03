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


import io.awacs.agent.AWACS;
import io.awacs.agent.Plugin;
import io.awacs.agent.Sender;
import io.awacs.common.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by pixyonly on 16/10/25.
 * 方法调用堆栈的追踪插件
 */
public class StackTracePlugin implements Plugin {

    private static Logger log = LoggerFactory.getLogger(StackTracePlugin.class);

    private ClassFilter classFilter;

    //发送线程的堆栈信息
    public static void incrAccess() {
        log.debug("Request complete, event fired.");
        String report = buildAccessReport(CallStack.reset());
        Sender.I.send((byte) 0x01, report, null);
    }

    private static String buildAccessReport(CallElement root) {
        String stack = root != null ? root.toString() : "{}";
        return "{\"thread\":\""
                + Thread.currentThread().getName()
                + "\",\"stack\":"
                + stack
                + "}";
    }

    //发送异常信息
    public static void incrFailure(Throwable e) {
        log.info("Exception occur, event fired.");
        CallStack.reset();
//        JSONObject report = new JSONObject();
//        report.put("thread", Thread.currentThread().getName());
//        report.put("stack", e.getStackTrace());
//        report.put("exception", e.getClass().getCanonicalName());
//        report.put("message", e.getMessage());
//        MessageHub.instance.publish(new BinaryMessage.BinaryMessageBuilder()
//                .setKey(key)
//                .setVersion(BinaryMessage.C_VERSION)
//                .setBody(report.toJSONString().getBytes())
//                .build());
    }

    private static String buildErrReport(Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();

        return "{\"thread\":\""
                + Thread.currentThread().getName()
                + "\",\"stack\":"
                + stack
                + "\",\"exception\":"
                + e.getCause()
                + "}";
    }

    @Override
    public void init(Configuration properties) {
        classFilter = new ClassFilter(properties.getArray("filter_package_prefix"));
    }

    @Override
    public void rock() {
        AWACS.M.getInstrumentation().addTransformer(new ClassFileTransformer() {
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

    @Override
    public void over() {
        //DO NOTHING
    }
}
