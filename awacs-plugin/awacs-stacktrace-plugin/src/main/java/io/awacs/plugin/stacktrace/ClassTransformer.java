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
                //copy exceptions
                String[] exceptions = null;
                if (src.exceptions != null) {
                    exceptions = new String[src.exceptions.size()];
                    for (int i = 0; i < src.exceptions.size(); i++) {
                        exceptions[i] = src.exceptions.get(i).toString();
                    }
                }
                //declare method
                MethodNode proxy = new MethodNode(src.access, src.name, src.desc, src.signature, exceptions);
                appended.add(proxy);
                //copy method annotations
                List<AnnotationNode> methodAnns = null;
                if (src.visibleAnnotations != null) {
                    methodAnns = new ArrayList<>(src.visibleAnnotations.size());
                    methodAnns.addAll(src.visibleAnnotations);
                }
                proxy.visibleAnnotations = methodAnns;
                //copy parameter annotations
                List[] parameterAnns = null;
                if (src.visibleParameterAnnotations != null) {
                    parameterAnns = new List[src.visibleParameterAnnotations.length];
                    System.arraycopy(src.visibleParameterAnnotations, 0, parameterAnns, 0, src.visibleParameterAnnotations.length);
                }
                proxy.visibleParameterAnnotations = parameterAnns;
                //clear origin method's annotation and change name
                int _slash = cn.name.lastIndexOf('/');
                //修改原始方法名，删除原始方法的注解
                src.name = src.name + "_origin_" + cn.name.substring(_slash + 1);
                src.access = src.access & ~Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE;
                src.visibleAnnotations = null;
                src.visibleLocalVariableAnnotations = null;
                transformTerminatedMethod(src, proxy, cn);
            } else if (!isTinyMethod(src)) {
                transformPlainMethod(src, cn);
            }
        }
        cn.methods.addAll(appended);
    }

    private static boolean isTinyMethod(MethodNode method) {
        return method.instructions.size() < 4 || method.maxStack < 2;
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

    /**
     * 修改起始代理方法，步骤：
     * 1、添加try catch语句                 try{
     * 2、初始化当前线程的线程栈信息列表        io.awacs.plugin.stacktrace.StackFrames.init();
     * 3、保存当前线程的开始信息                io.awacs.plugin.stacktrace.StackFrames.push(className,methodName,0);
     * 4、调用原始方法                          Object val = methodName_origin_className(args);
     * 5、保存当前线程的结束信息                io.awacs.plugin.stacktrace.StackFrames.push(className,methodName,1);
     * 5、清除当前线程的线程栈信息列表          List list = io.awacs.plugin.stacktrace.StackFrames.dump();
     * 7、发送当前线程的线程栈信息列表          io.awacs.plugin.stacktrace.StackTracePlugin.incrAccess(list);
     * 8、调用返回方法                          return val;
     * }catch(java.lang.Exception e){
     * 9、异常部分执行：处理异常                io.awacs.plugin.stacktrace.StackTracePlugin.incrFailure(e);
     * 10、异常部分执行：抛出异常               throw e;
     * }
     */
    private void transformTerminatedMethod(MethodNode origin, MethodNode proxy, ClassNode owner) {
        LabelNode l0 = new LabelNode();
        LabelNode l1 = new LabelNode();
        LabelNode l2 = new LabelNode();
        //添加try catch语句
        proxy.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception"));
        proxy.instructions.add(l0);
        //其实方法初始化方法调用
        proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "init", "()V", false));
        proxy.instructions.add(new LdcInsnNode(owner.name.replaceAll("/", ".")));
        proxy.instructions.add(new LdcInsnNode(proxy.name));
        proxy.instructions.add(new LdcInsnNode(0));
        //方法开始调用
        proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        int varIndex = 0;//本地变量区的游标，用于计算最终大小
        //判断是否为静态方法,如果不是静态方法还需要加载this到操作数区
        if ((proxy.access & Opcodes.ACC_STATIC) != Opcodes.ACC_STATIC) {
            proxy.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            varIndex = 1;
        }
        List<String> parameters = resolveParameters(proxy.desc);
        //从本地变量去加载到栈的操作数中
        for (String param : parameters) {
            VarInsnNode insnNode;
            switch (param) {
                case "J":
                    insnNode = new VarInsnNode(Opcodes.LLOAD, varIndex);
                    varIndex += 2;
                    break;
                case "D":
                    insnNode = new VarInsnNode(Opcodes.DLOAD, varIndex);
                    varIndex += 2;
                    break;
                case "F":
                    insnNode = new VarInsnNode(Opcodes.FLOAD, varIndex++);
                    break;
                case "I":
                    insnNode = new VarInsnNode(Opcodes.ILOAD, varIndex++);
                    break;
                case "S":
                    insnNode = new VarInsnNode(Opcodes.ILOAD, varIndex++);
                    break;
                case "Z":
                    insnNode = new VarInsnNode(Opcodes.ILOAD, varIndex++);
                    break;
                case "B":
                    insnNode = new VarInsnNode(Opcodes.ILOAD, varIndex++);
                    break;
                case "C":
                    insnNode = new VarInsnNode(Opcodes.ILOAD, varIndex++);
                    break;
                default:
                    insnNode = new VarInsnNode(Opcodes.ALOAD, varIndex++);
                    break;
            }
            proxy.instructions.add(insnNode);
        }
        //调用原始方法
        if ((origin.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC)
            proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner.name, origin.name, origin.desc, false));
        else
            proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner.name, origin.name, origin.desc, false));
        proxy.instructions.add(new LdcInsnNode(owner.name.replaceAll("/", ".")));
        proxy.instructions.add(new LdcInsnNode(proxy.name));
        proxy.instructions.add(new LdcInsnNode(1));
        //方法结束调用
        proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        //方法线程信息获取并清除
        proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "dump",
                "()Ljava/util/Collection;", false));
        //发送线程信息
        proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackTracePlugin", "incrAccess", "(Ljava/util/Collection;)V", false));
        proxy.instructions.add(l1);
        //判断返回值类型
        String returnType = origin.desc.substring(origin.desc.indexOf(')') + 1);
        switch (returnType) {
            case "J":
                proxy.instructions.add(new InsnNode(Opcodes.LRETURN));
                break;
            case "D":
                proxy.instructions.add(new InsnNode(Opcodes.DRETURN));
                break;
            case "F":
                proxy.instructions.add(new InsnNode(Opcodes.FRETURN));
                break;
            case "I":
                proxy.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "S":
                proxy.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "C":
                proxy.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "B":
                proxy.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "Z":
                proxy.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "V":
                proxy.instructions.add(new InsnNode(Opcodes.RETURN));
                break;
            default:
                proxy.instructions.add(new InsnNode(Opcodes.ARETURN));
                break;
        }
        proxy.instructions.add(l2);
        //进行异常捕获并抛出
        proxy.instructions.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Exception"}));
        proxy.instructions.add(new VarInsnNode(Opcodes.ASTORE, varIndex));
        proxy.instructions.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
        proxy.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackTracePlugin", "incrFailure", "(Ljava/lang/Throwable;)V", false));
        proxy.instructions.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
        proxy.instructions.add(new InsnNode(Opcodes.ATHROW));
        proxy.maxLocals = varIndex + 1;
        proxy.maxStack = Math.max(varIndex, 5);
    }

    /**
     * 修改非起始代理方法，步骤：
     * 1、调用StackFrames.push方法(标示为0)：定义常量到操作数，然后调用方法
     * 2、调用原始方法：从本地变量去装载this对象和参数到操作数去，然后调用原始方法
     * 3、调用StackFrames.push方法(标示为1)：定义常量到操作数，然后调用方法
     * 4、调用返回方法：根据返回值添加返回指令
     */
    private void transformPlainMethod(MethodNode mn, ClassNode cn) {
        InsnList before = new InsnList();
        before.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        before.add(new LdcInsnNode(mn.name));
        before.add(new LdcInsnNode(0));
        before.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));
        InsnList end = new InsnList();
        end.add(new LdcInsnNode(cn.name.replaceAll("/", ".")));
        end.add(new LdcInsnNode(mn.name));
        end.add(new LdcInsnNode(1));
        end.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/stacktrace/StackFrames", "push",
                "(Ljava/lang/String;Ljava/lang/String;I)V", false));

        List<AbstractInsnNode> insts = new LinkedList<>();
        for (int i = 0; i < mn.instructions.size(); i++) {
            int opcode = mn.instructions.get(i).getOpcode();
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                insts.add(mn.instructions.get(i));
            }
        }
        if (!insts.isEmpty()) {
            mn.instructions.insert(before);
            for (AbstractInsnNode node : insts) {
                mn.instructions.insertBefore(node, end);
            }
        }
        mn.maxStack = mn.maxStack + 5;
    }

}
