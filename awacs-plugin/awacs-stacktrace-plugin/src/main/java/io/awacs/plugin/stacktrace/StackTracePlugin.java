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


import io.awacs.agent.AWACS;
import io.awacs.agent.Plugin;
import io.awacs.agent.Sender;
import io.awacs.common.Configuration;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
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

    private final static String ENABLE_AUTO_DETECT_POINTCUT = "enable_auto_detect_pointcut";

    private final static String POINTCUT_BY_ANNOTATION = "pointcut_by_annotation";

    private final static String POINTCUT_BY_NAME = "pointcut_by_name";

    private final static int DEFAULT_EXCEPTION_TRACE_LEVEL = 20;

    private final static String RESPONSE_TIME_MS_THRESHOLD = "response_time_ms_threshold";

    private final static int DEFAULT_RESPONSE_TIME_MS_THRESHOLD = 0;

    private final static String CFG_ENABLE_OUTPUT_TRANSFORMED_CLASS = "enable_output_transformed_class";

    private final static String CFG_CLASSFILE_OUTPUT_PATH = "classfile_output_path";

    private final static String DEFAULT_CLASSFILE_OUTPUT_PATH = "/tmp/awacs";

    private static Logger log = Logger.getLogger("AWACS");

    @Override
    public void init(Configuration configuration) {
        Config.F.init(configuration);
    }

    @Override
    public void rock() {
        AWACS.M.getInstrumentation().addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    if (loader == null) {
                        return classfileBuffer;
                    }
                    ClassNode cn = new ClassNode(Opcodes.ASM5);
                    ClassReader cr = new ClassReader(classfileBuffer);
                    cr.accept(cn, 0);
                    boolean transformed = new FilteredClassTransformer().transform(cn);
                    ClassWriter cw = new ClassWriter(0);
                    cn.accept(cw);
                    byte[] bytecode = cw.toByteArray();
                    log.log(Level.FINE, "{0} transformed.", className);
                    if (transformed) {
                        Sender.I.send((byte) 0x02, classfileBuffer);
                        if (Config.F.enableDebug) {
                            outputClass(className, bytecode);
                        }
                    }
                    return bytecode;
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

    private void outputClass(String className, byte[] bytecode) {
        try (FileOutputStream os = new FileOutputStream(Config.F.outputPath + className.replaceAll("/", ".") + ".class")) {
            os.write(bytecode);
            os.flush();
        } catch (Exception e) {
            log.log(Level.WARNING, "Cannot output class file to {0}", Config.F.outputPath);
        }
    }

    enum Config {

        F;

        String[] excludes;

        String[] includes;

        String[] packges;

        String[] annotations;

        String[] ids;

        int maxExceptionLevel;

        int responseTimeThreshold;

        boolean enableDebug;

        boolean enableAutoDetect;

        String outputPath;

        void init(Configuration config) {
            excludes = config.getArray(EXCLUDE_EXCEPTION_PREFIXES);
            includes = config.getArray(INCLUDE_EXCEPTION_PREFIXES);
            packges = config.getArray(FILTER_PACKAGE_PREFIXES);
            annotations = config.getArray(POINTCUT_BY_ANNOTATION);
            ids = config.getArray(POINTCUT_BY_NAME);
            maxExceptionLevel = config.getInteger(EXCEPTION_TRACE_LEVEL, DEFAULT_EXCEPTION_TRACE_LEVEL);
            responseTimeThreshold = config.getInteger(RESPONSE_TIME_MS_THRESHOLD, DEFAULT_RESPONSE_TIME_MS_THRESHOLD);
            enableDebug = config.getBoolean(CFG_ENABLE_OUTPUT_TRANSFORMED_CLASS, false);
            outputPath = config.getString(CFG_CLASSFILE_OUTPUT_PATH, DEFAULT_CLASSFILE_OUTPUT_PATH);
            enableAutoDetect = config.getBoolean(ENABLE_AUTO_DETECT_POINTCUT, true);
            if (enableDebug) {
                if (!outputPath.endsWith("/")) {
                    outputPath = outputPath + "/";
                }
                File f = new File(outputPath);
                if (!f.exists()) {
                    if (!f.mkdir()) {
                        log.log(Level.WARNING, "cannot create directory at {0}", outputPath);
                    }
                } else if (f.isFile()) {
                    log.log(Level.WARNING, "{0} already exists.", outputPath);
                }
            }
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
    }
}
