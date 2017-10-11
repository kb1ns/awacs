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
import org.objectweb.asm.tree.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Java Bytecode transformer using asm.
 * <p>
 * <p>
 * Created by pixyonly on 16/10/24.
 */
abstract class ClassTransformer {

    protected String currentClass;

    protected abstract boolean filterClass(ClassNode cn);

    protected abstract boolean filterMethod(MethodNode mn);

    protected abstract boolean isPointcut(MethodNode mn);

    private void addTryCatchBlock(MethodNode mn, ClassNode cn) {
        //清空instructions
        InsnList body = new InsnList();
        body.add(mn.instructions);

        LabelNode excHandler = new LabelNode();
        LabelNode exc0 = new LabelNode();

        InsnList enter = new InsnList();
        enter.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "initStack", "()V", false));
        enter.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        enter.add(new LdcInsnNode(mn.name));
        enter.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "methodEnter", "(Ljava/lang/String;Ljava/lang/String;)V", false));
        enter.add(exc0);
        mn.instructions.insert(enter);

        AbstractInsnNode node = body.getFirst();
        while (node != null) {
            int opcode = node.getOpcode();
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                InsnList quit = new InsnList();
                quit.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "methodQuit", "()V", false));
                quit.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "incrAccess", "()V", false));
                LabelNode exc1 = new LabelNode();
                quit.add(exc1);
                mn.tryCatchBlocks.add(new TryCatchBlockNode(exc0, exc1, excHandler, "java/lang/Exception"));
                body.insertBefore(node, quit);
                exc0 = new LabelNode();
                body.insert(node, exc0);
            }
            node = node.getNext();
        }
        mn.instructions.add(body);
        //进行异常捕获并抛出
        int varSlotIndex = 0;
        List<Object> parameters = resolveParameters(mn, cn);
        for (Object param : parameters) {
            if (param.equals(Opcodes.LONG) || param.equals(Opcodes.DOUBLE))
                varSlotIndex += 2;
            else
                varSlotIndex++;
        }

        mn.instructions.add(excHandler);
        mn.instructions.add(new FrameNode(Opcodes.F_FULL, parameters.size(), parameters.toArray(), 1, new Object[]{"java/lang/Exception"}));
        mn.instructions.add(new VarInsnNode(Opcodes.ASTORE, varSlotIndex));
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, varSlotIndex));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "incrFailure", "(Ljava/lang/Throwable;)V", false));
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, varSlotIndex));
        mn.instructions.add(new InsnNode(Opcodes.ATHROW));
        mn.maxStack += 2;
        mn.maxLocals += 1;
    }

    private void addInterceptor(MethodNode mn, ClassNode cn) {
        InsnList enter = new InsnList();
        enter.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        enter.add(new LdcInsnNode(mn.name));
        enter.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "methodEnter", "(Ljava/lang/String;Ljava/lang/String;)V", false));
        mn.instructions.insert(enter);

        List<AbstractInsnNode> returnLocations = new LinkedList<>();
        for (int i = 0; i < mn.instructions.size(); i++) {
            int opcode = mn.instructions.get(i).getOpcode();
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                returnLocations.add(mn.instructions.get(i));
            }
        }
        for (AbstractInsnNode ret : returnLocations) {
            InsnList quit = new InsnList();
            quit.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/CallStack", "methodQuit", "()V", false));
            mn.instructions.insertBefore(ret, quit);
        }
        mn.maxStack += 2;
    }

    public boolean transform(ClassNode cn) {
        cn.check(Opcodes.ASM5);
        currentClass = cn.name == null ? "" : cn.name.replaceAll("/", ".");
        if (!filterClass(cn))
            return false;
        for (Object mn : cn.methods) {
            MethodNode src = (MethodNode) mn;
            if (filterMethod(src)) {
                if (isPointcut(src)) {
                    addTryCatchBlock(src, cn);
                } else if (!isTinyMethod(src)) {
                    addInterceptor(src, cn);
                }
            }
        }
        return true;
    }

    private static boolean isTinyMethod(MethodNode method) {
        return method.instructions.size() <= 5;
    }

    //判断是否为原始类型
    private static boolean isPrimitive(char c) {
        return c == 'J' || c == 'D' || c == 'F' || c == 'I' || c == 'S' || c == 'C' || c == 'B' || c == 'Z';
    }

    //根据方法描述符来获取参数数组
    private static List<Object> resolveParameters(MethodNode mn, ClassNode cn) {
        List<Object> params = new LinkedList<>();
        if ((mn.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
            params.add(cn.name);
        }

        String desc = mn.desc.substring(1, mn.desc.indexOf(')'));
        for (int i = 0; i < desc.length(); i++) {
            int tag;
            switch (desc.charAt(i)) {
                case 'L':
                    tag = expectType(desc, i);
                    //Ljava/lang/String; in bytecode
                    String exp = tag + 1 == desc.length() ? desc.substring(i) : desc.substring(i, tag + 1);
                    //java/lang/String we need
                    params.add(exp.substring(1, exp.length() - 1));
                    i = tag;
                    break;
                case '[':
                    //[Ljava/lang/String;
                    tag = expectAny(desc, i);
                    params.add(tag + 1 == desc.length() ? desc.substring(i) : desc.substring(i, tag + 1));
                    i = tag;
                    break;
                case 'J':
                    params.add(Opcodes.LONG);
                    break;
                case 'D':
                    params.add(Opcodes.DOUBLE);
                    break;
                case 'F':
                    params.add(Opcodes.FLOAT);
                    break;
                default:
                    if (!isPrimitive(desc.charAt(i)))
                        throw new IllegalDescriptorException(desc);
//                    params.add(String.valueOf(desc.charAt(i)));
                    params.add(Opcodes.INTEGER);
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
