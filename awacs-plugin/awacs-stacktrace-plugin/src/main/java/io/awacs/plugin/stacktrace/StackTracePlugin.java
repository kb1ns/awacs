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
import io.awacs.common.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pixyonly on 16/10/25.
 * 方法调用堆栈的追踪插件
 */
public class StackTracePlugin implements Plugin {

    private final static String FILTER_PACKAGE_PREFIXES = "filter_package_prefixes";

    private final static String EXCLUDE_EXCEPTION_PREFIXES = "exclude_exception_prefixes";

    private final static String INCLUDE_EXCEPTION_PREFIXES = "include_exception_prefixes";

    private final static String EXCEPTION_TRACE_LEVEL = "exception_trace_level";

    private static Logger log = LoggerFactory.getLogger(StackTracePlugin.class);

    private ClassFilter classFilter;

    //发送线程的堆栈信息
    public static void incrAccess() {
        log.debug("Request complete, event fired.");

        String report = buildAccessReport(CallStack.reset());
//        Sender.I.send();
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
        if (Config.F.isValid(e.getClass())) {
            System.out.println(buildErrReport(e));
        }
    }

    private static String buildErrReport(Throwable e) {
        StackTraceElement[] stack = e.getStackTrace();
        int level = Config.F.maxExceptionLevel;
        int terminate = 0;
        List<StackTraceElement> reducedStack = new ArrayList<>(level);
        for (StackTraceElement element : stack) {
            if (level <= 1 || terminate >= 2) {
                break;
            }
            boolean r = Config.F.isFocus(element);
            if (r) {
                terminate = 1;
            } else if (terminate == 1) {
                terminate++;
            }
            reducedStack.add(element);
        }
        return String.format("%s|%s|%s|%s|%s",
                e.getClass().getCanonicalName(),
                Thread.currentThread().getName(),
                System.currentTimeMillis(),
                reducedStack.toString(),
                e.getMessage());
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
