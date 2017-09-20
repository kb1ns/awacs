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

import java.util.List;

/**
 *
 * Created by pixyonly on 16/11/7.
 */
class FilteredClassTransformer extends ClassTransformer {

    /**
     * 方法过滤
     * (!接口方法)&&(!抽象方法)&&(!本地方法)&&(!初始化方法)&&(!类初始化方法)&&(!main方法)
     */
    @Override
    protected boolean filterMethod(ClassNode cn, MethodNode mn) {
        return ((cn.access & Opcodes.ACC_INTERFACE) != Opcodes.ACC_INTERFACE &&
                (cn.access & Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT &&
                (mn.access & Opcodes.ACC_NATIVE) != Opcodes.ACC_NATIVE &&
                (mn.access & Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT &&
                !isConstructor(mn) && !isMainMethod(mn));
    }


    /**
     * 判断是否堆栈追踪的开始方法
     */
    @Override
    protected boolean isTerminatedMethod(MethodNode mn) {
        boolean terminated = false;
        List<AnnotationNode> annotationNodes = mn.visibleAnnotations;
        if (annotationNodes != null) {
            for (AnnotationNode annotation : annotationNodes) {
                if (annotation.desc.equals("Lorg/springframework/web/bind/annotation/RequestMapping;")) {
                    terminated = true;
                    break;
                }
            }
        }
        return terminated;
    }
}
