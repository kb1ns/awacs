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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pixyonly on 16/11/7.
 */
class FilteredClassTransformer extends ClassTransformer {

    private Set<String> annotations = new HashSet<>();

    private Set<String> methodIds = new HashSet<>();

    FilteredClassTransformer() {
        //TODO
        if (StackTracePlugin.Config.F.enableAutoDetect) {
            annotations.add("Lorg/springframework/web/bind/annotation/RequestMapping;");
        }
        for (String a : StackTracePlugin.Config.F.annotations) {
            annotations.add("L" + a.replaceAll("\\.", "/") + ";");
        }
        for (String n : StackTracePlugin.Config.F.ids) {
            methodIds.add(n);
        }
    }

    @Override
    protected boolean filterClass(ClassNode cn) {
        if ((cn.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE ||
                (cn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT) {
            return false;
        }
        String[] filterClassPrefix = StackTracePlugin.Config.F.packges;
        String className = cn.name;
        if (className == null) {
            return false;
        } else if (className.startsWith("io/awacs/plugin/")) {
            return false;
        } else if (filterClassPrefix == null || filterClassPrefix.length == 0) {
            return !(className.startsWith("java") ||
                    className.startsWith("sun") ||
                    className.startsWith("jdk") ||
                    className.startsWith("com/sun/") ||
                    className.startsWith("com/intellij/") ||
                    className.startsWith("org/"));
        } else {
            boolean flag = false;
            for (String prefix : filterClassPrefix) {
                flag = flag || (className.startsWith(prefix.replaceAll("\\.", "/")));
            }
            return flag;
        }
    }

    /**
     * 方法过滤
     * (!接口方法)&&(!抽象方法)&&(!本地方法)&&(!初始化方法)&&(!类初始化方法)&&(!main方法)
     */
    @Override
    protected boolean filterMethod(MethodNode mn) {
        return ((mn.access & Opcodes.ACC_NATIVE) != Opcodes.ACC_NATIVE &&
                (mn.access & Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT &&
                !isConstructor(mn) && !isMainMethod(mn));
    }


    /**
     * 判断是否堆栈追踪切入点
     */
    @Override
    protected boolean isPointcut(MethodNode mn) {
        //TODO
        List<AnnotationNode> annotationNodes = mn.visibleAnnotations;
        if (annotationNodes != null) {
            for (AnnotationNode annotation : annotationNodes) {
                if (annotations.contains(annotation.desc)) {
                    return true;
                }
                if (methodIds.contains(super.currentClass + "#*") || methodIds.contains(super.currentClass + "#" + mn.name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
