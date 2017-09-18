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
import io.awacs.common.format.Influx;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 16/10/25.
 * 方法调用堆栈的追踪插件
 */
public class StackTracePlugin implements Plugin {

    private final static String FILTER_PACKAGE_PREFIXES = "filter_package_prefixes";

    private final static String EXCLUDE_EXCEPTION_PREFIXES = "exclude_exception_prefixes";

    private final static String INCLUDE_EXCEPTION_PREFIXES = "include_exception_prefixes";

    private final static String EXCEPTION_TRACE_LEVEL = "exception_trace_level";

    private static Logger log = Logger.getLogger("AWACS");

    private ClassFilter classFilter;

    //发送线程的堆栈信息
    public static void incrAccess() {
        log.fine("Request completed.");
        CallElement root = CallStack.reset();
        if (root != null) {
            String s = Influx.measurement(AWACS.M.namespace()).time(System.nanoTime(), TimeUnit.NANOSECONDS)
                    .addField("thread", Thread.currentThread().getName())
                    .addField("stack", root.toString())
                    .tag("entry", root.id())
                    .build()
                    .lineProtocol();
            log.fine(s);
            Sender.I.send((byte) 1, s);
        }
    }

    //发送异常信息
    public static void incrFailure(Throwable e) {
        log.fine("Exception catched.");
        CallStack.reset();
        if (Config.F.isValid(e.getClass())) {
            String s = buildErrReport(e);
            log.fine(s);
            Sender.I.send((byte) 1, s);
        }
    }

    private static String buildErrReport(Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        int level = Config.F.maxExceptionLevel;
        List<StackTraceElement> reducedStack = new ArrayList<>(level);
        for (StackTraceElement element : stack) {
            if (level-- < 1) {
                break;
            }
            reducedStack.add(element);
        }
        return Influx.measurement(AWACS.M.namespace()).time(System.nanoTime(), TimeUnit.NANOSECONDS)
                .addField("thread", Thread.currentThread().getName())
                .addField("stack", reducedStack.toString())
                .addField("message", e.getMessage())
                .tag("entry", e.getClass().getCanonicalName())
                .build()
                .lineProtocol();
    }

    @Override
    public void init(Configuration configuration) {
        classFilter = new ClassFilter(configuration.getArray(FILTER_PACKAGE_PREFIXES));
        Config.F.init(configuration);
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
                    log.log(Level.FINE, "{0} transformed.", className);
                    return cw.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                    return classfileBuffer;
                }
            }
        });
    }

    @Override
    public void over() {
        //DO NOTHING
    }

    private enum Config {

        F;

        String[] excludes;

        String[] includes;

        String[] packges;

        int maxExceptionLevel;

        void init(Configuration config) {
            excludes = config.getArray(EXCLUDE_EXCEPTION_PREFIXES);
            includes = config.getArray(INCLUDE_EXCEPTION_PREFIXES);
            packges = config.getArray(FILTER_PACKAGE_PREFIXES);
            maxExceptionLevel = config.getInteger(EXCEPTION_TRACE_LEVEL, 20);
        }

        boolean isValid(Class<?> clazz) {
            String name = clazz.getCanonicalName();
            boolean r = true;
            for (String n : excludes) {
                r = r && !n.startsWith(name);
            }
            for (String n : includes) {
                r = r || n.startsWith(name);
            }
            return r;
        }

        boolean isFocus(StackTraceElement element) {
            for (String n : packges) {
                if (element.getClassName().startsWith(n)) {
                    return true;
                }
            }
            return false;
        }
    }
}
