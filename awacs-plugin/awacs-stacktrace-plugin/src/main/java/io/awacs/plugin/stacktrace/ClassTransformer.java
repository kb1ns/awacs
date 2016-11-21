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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Java Bytecode transformer using asm.
 * <p>
 * <p>
 * Created by pixyonly on 16/10/24.
 */
abstract class ClassTransformer {

    //对类进行过滤
    public abstract boolean filterClass(String className);

    //对方法进行过滤
    protected abstract boolean filterMethod(ClassNode cn, MethodNode mn);

    //判断是否为起始代理方法
    protected abstract boolean isTerminatedMethod(MethodNode mn);

    //对类进行处理
    public void visit(ClassNode cn) {
        cn.check(Opcodes.ASM5);
        //定义需要添加的类的集合
        List<MethodNode> appended = new ArrayList<>(cn.methods.size());
        //对每个类进行处理
        for (Object mn : cn.methods) {
            MethodNode src = (MethodNode) mn;
            //对方法进行过滤
            if (!filterMethod(cn, src)) {
                continue;
            }
            boolean terminated = isTerminatedMethod(src);
            if (terminated) {
                interceptTerminatedMethod(src, cn);
            } else {
                interceptPlainMethod(src, cn);
            }
        }
    }

    private void interceptPlainMethod(MethodNode mn, ClassNode cn) {
        InsnList inst = new InsnList();
        inst.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        inst.add(new LdcInsnNode(mn.name));
        inst.add(new LdcInsnNode(0));
        inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        mn.instructions.insert(inst);
        AbstractInsnNode ret = mn.instructions.getLast();
        mn.instructions.remove(ret);
        mn.instructions.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        mn.instructions.add(new LdcInsnNode(mn.name));
        mn.instructions.add(new LdcInsnNode(1));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        mn.instructions.add(ret);
        mn.maxStack = mn.maxStack + 5;
    }

    private void interceptTerminatedMethod(MethodNode mn, ClassNode cn) {
        LabelNode l0 = new LabelNode();
        LabelNode l1 = new LabelNode();
        LabelNode l2 = new LabelNode();
        //添加try catch语句
        mn.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception"));
        InsnList inst = new InsnList();
        inst.add(l0);
        inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "init", "()V", false));
        inst.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        inst.add(new LdcInsnNode(mn.name));
        inst.add(new LdcInsnNode(0));
        inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        mn.instructions.insert(inst);
        AbstractInsnNode ret = mn.instructions.getLast();
        mn.instructions.remove(ret);
        mn.instructions.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        mn.instructions.add(new LdcInsnNode(mn.name));
        mn.instructions.add(new LdcInsnNode(1));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "dump",
                "()Ljava/util/List;", false));
        //发送线程信息
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackTracePlugin", "incrAccess",
                "(Ljava/util/List;)V", false));
        mn.instructions.add(l1);
        mn.instructions.add(ret);
        mn.instructions.add(l2);
        //进行异常捕获并抛出
        mn.instructions.add(new FrameNode(Opcodes.F_FULL, 1, new Object[]{cn.name}, 1, new Object[]{"java/lang/Exception"}));
//        int varIndex = getParameterSize(mn);
//        mn.instructions.add(new VarInsnNode(Opcodes.ASTORE, varIndex));
//        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackTracePlugin", "incrFailure",
                "(Ljava/lang/Throwable;)Ljava/lang/RuntimeException;", false));
        mn.instructions.add(new InsnNode(Opcodes.ATHROW));
        mn.maxStack = mn.maxStack + 5;
    }

    //判断是否为原始类型
    private static boolean isPrimitive(char c) {
        return c == 'J' || c == 'D' || c == 'F' || c == 'I' || c == 'S' || c == 'C' || c == 'B' || c == 'Z';
    }

    //根据方法描述符来获取参数数组
    private static List<String> resolveParameters(String descriptor) {
        String desc = descriptor.substring(1, descriptor.indexOf(')'));
        List<String> params = new LinkedList<>();
        for (int i = 0; i < desc.length(); i++) {
            int tag = 0;
            switch (desc.charAt(i)) {
                case 'L':
                    tag = expectType(desc, i);
                    params.add(tag + 1 == desc.length() ? desc.substring(i) : desc.substring(i, tag + 1));
                    i = tag;
                    break;
                case '[':
                    tag = expectAny(desc, i);
                    params.add(tag + 1 == desc.length() ? desc.substring(i) : desc.substring(i, tag + 1));
                    i = tag;
                    break;
                default:
                    if (!isPrimitive(desc.charAt(i)))
                        throw new IllegalDescriptorException(desc);
                    params.add(String.valueOf(desc.charAt(i)));
            }
        }
        return params;
    }

    //获取对象类型描述符的结束位置
    private static int expectType(String s, int offset) {
        while (s.charAt(offset) != ';') offset++;
        return offset;
    }

    //获取任意类型描述符的结束位置
    private static int expectAny(String s, int offset) {
        if (s.charAt(offset + 1) == '[')
            return expectAny(s, offset + 1);
        else if (s.charAt(offset + 1) == 'L')
            return expectType(s, offset + 1);
        else if (isPrimitive(s.charAt(offset + 1)))
            return offset + 1;
        else
            throw new IllegalDescriptorException(s);
    }
    protected boolean isMainMethod(MethodNode mn) {
        return mn.name.equals("main") && mn.desc.equals("([Ljava/lang/String;)V") &&
                (mn.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
    }

    protected boolean isConstructor(MethodNode mn) {
        return mn.name.equals("<init>") || mn.name.equals("<clinit>");
    }
}
