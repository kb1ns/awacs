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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pixyonly on 8/2/16.
 */
class ServiceWrapper extends ClassWrapper {

    private Set<MethodNode> appendedMethods = new HashSet<>();

    @Override
    protected boolean doFilter(ClassNode cn) {
        List<AnnotationNode> annotations = cn.visibleAnnotations;
        boolean isService = false;
        if (annotations != null) {
            for (AnnotationNode annotation : annotations) {
                //org.springframework.stereotype.Controller
                if (annotation.desc.equals("Lorg/springframework/web/bind/annotation/RestController;") || annotation.desc.equals("Lorg/springframework/stereotype/Controller;"))
                    isService = true;
            }
        }
        return isService;
    }

    public void wrap(ClassNode cn) {
        cn.check(Opcodes.ASM5);
        List<MethodNode> methods = cn.methods;
        for (MethodNode method : methods) {
            List<AnnotationNode> annotationNodes = method.visibleAnnotations;
            if (annotationNodes != null) {
                for (AnnotationNode annotation : annotationNodes) {
                    if (annotation.desc.equals("Lorg/springframework/web/bind/annotation/RequestMapping;")) {
                        appendedMethods.add(method);
                        break;
                    }
                }
            }
        }
        for(MethodNode method : appendedMethods){
            cn.methods.add(doProxy(cn, method));
        }
//        cn.methods.addAll(appendedMethods.stream().map(method -> doProxy(cn, method))
//                .collect(Collectors.toList()));
        appendedMethods.clear();
    }

    private void hideAnnotations(MethodNode origin) {
        origin.visibleAnnotations = null;
        origin.visibleParameterAnnotations = null;
    }

    private MethodNode copyMethod(MethodNode src) {
        //copy exceptions
        String[] exceptions = new String[src.exceptions.size()];
        for (int i = 0; i < src.exceptions.size(); i++) {
            exceptions[i] = src.exceptions.get(i).toString();
        }
        //copy method annotations
        List<AnnotationNode> methodAnns = new ArrayList<>(src.visibleAnnotations.size());
        methodAnns.addAll(src.visibleAnnotations);
        //copy parameter annotations
        List[] parameterAnns = new List[src.visibleParameterAnnotations.length];
        System.arraycopy(src.visibleParameterAnnotations, 0, parameterAnns, 0,
                src.visibleParameterAnnotations.length);
        MethodNode proxy = new MethodNode(src.access, src.name + "_proxy",
                getDescAfterAppendParam(src.desc, "Ljavax/servlet/http/HttpServletRequest;"),
                src.signature, exceptions);
        proxy.visibleAnnotations = methodAnns;
        proxy.visibleParameterAnnotations = parameterAnns;
        return proxy;
    }

    private int copyParameters(MethodNode src, MethodNode dest, LabelNode start, LabelNode end) {
        int cursor = 0;
        int paramCount = getParamCount(src.desc, src.access);
        List<LocalVariableNode> variables = new ArrayList<>(paramCount + 1);
        for (int i = 0; i < paramCount; i++) {
            LocalVariableNode node = getNode(src.localVariables, i);
            variables.add(new LocalVariableNode(node.name, node.desc, node.signature, start, end,
                    node.index));
            String desc = node.desc;
            cursor += "D".equals(desc) || "J".equals(desc) ? 2 : 1;
        }
        variables.add(new LocalVariableNode("httpServletRequestFacade",
                "Ljavax/servlet/http/HttpServletRequest;", null, start, end, cursor++));
        dest.localVariables = variables;
        return cursor;
    }

    /**
     * origin method: (XXX)X
     * <p>
     * proxy method: (XXXLjavax/servlet/http/HttpServletRequest;)X
     * req -> localIndex - 1
     *
     * @param cn
     * @param origin
     * @return
     */
    private MethodNode doProxy(ClassNode cn, MethodNode origin) {

        MethodNode newNode = copyMethod(origin);
        //move annotations
        hideAnnotations(origin);

        LabelNode l0 = new LabelNode();
        LabelNode l1 = new LabelNode();
        LabelNode l2 = new LabelNode();

        String returnType = origin.desc.substring(origin.desc.indexOf(')') + 1);

        int localIndex = copyParameters(origin, newNode, l0, l2);

        newNode.tryCatchBlocks.add(new TryCatchBlockNode(l0, l1, l2, "java/lang/Exception"));

        //localIndex -> elapsedTime
        newNode.localVariables
                .add(new LocalVariableNode("elapsedTime", "J", null, l0, l1, localIndex));

        //进入方法体
        newNode.instructions.add(l0);

        //添加时间戳 long elapsedTime = System.currentTimeMillis();
        newNode.instructions.add(
                new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis",
                        "()J"));
        newNode.instructions.add(new VarInsnNode(Opcodes.LSTORE, localIndex));

        int paramCount = getParamCount(origin.desc, origin.access);

        //load原方法参数
        for (int i = 0; i < paramCount; i++) {
            LocalVariableNode node = (LocalVariableNode) newNode.localVariables.get(i);
            switch (node.desc) {
                case "J":
                    newNode.instructions.add(new VarInsnNode(Opcodes.LLOAD, node.index));
                    break;
                case "D":
                    newNode.instructions.add(new VarInsnNode(Opcodes.DLOAD, node.index));
                    break;
                case "F":
                    newNode.instructions.add(new VarInsnNode(Opcodes.FLOAD, node.index));
                    break;
                case "I":
                    newNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, node.index));
                    break;
                case "S":
                    newNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, node.index));
                    break;
                case "Z":
                    newNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, node.index));
                    break;
                case "B":
                    newNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, node.index));
                    break;
                case "C":
                    newNode.instructions.add(new VarInsnNode(Opcodes.ILOAD, node.index));
                    break;
                default:
                    newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, node.index));
                    break;
            }
        }
        //调用原方法
        newNode.instructions
                .add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name, origin.name, origin.desc));

        //计算耗时
        newNode.instructions.add(
                new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis",
                        "()J"));
        newNode.instructions.add(new VarInsnNode(Opcodes.LLOAD, localIndex));
        newNode.instructions.add(new InsnNode(Opcodes.LSUB));
        newNode.instructions.add(new VarInsnNode(Opcodes.LSTORE, localIndex));

        newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, localIndex - 1));
        newNode.instructions.add(
                new MethodInsnNode(Opcodes.INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest",
                        "getRequestURI", "()Ljava/lang/String;"));

        //获取耗时
        newNode.instructions.add(new VarInsnNode(Opcodes.LLOAD, localIndex));
        //SpringmvcPlugin.incrAccess(uri, elapsedTime);
        newNode.instructions.add(
                new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/springmvc/SpringmvcPlugin",
                        "incrAccess", "(Ljava/lang/String;J)V"));
        //退出
        newNode.instructions.add(l1);
        switch (returnType) {
            case "J":
                newNode.instructions.add(new InsnNode(Opcodes.LRETURN));
                break;
            case "D":
                newNode.instructions.add(new InsnNode(Opcodes.DRETURN));
                break;
            case "F":
                newNode.instructions.add(new InsnNode(Opcodes.FRETURN));
                break;
            case "I":
                newNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "S":
                newNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "C":
                newNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "B":
                newNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            case "Z":
                newNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                break;
            default:
                newNode.instructions.add(new InsnNode(Opcodes.ARETURN));
                break;
        }

        newNode.instructions.add(l2);
        newNode.instructions
                .add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Exception"}));
        newNode.instructions.add(new VarInsnNode(Opcodes.ASTORE, localIndex));

        newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, localIndex - 1));
        newNode.instructions.add(
                new MethodInsnNode(Opcodes.INVOKEINTERFACE, "javax/servlet/http/HttpServletRequest",
                        "getRequestURI", "()Ljava/lang/String;"));

        newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, localIndex));
        newNode.instructions.add(
                new MethodInsnNode(Opcodes.INVOKESTATIC, "io/awacs/plugin/springmvc/SpringmvcPlugin",
                        "incrFailure", "(Ljava/lang/String;Ljava/lang/Throwable;)V"));

        newNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, localIndex));
        newNode.instructions.add(new InsnNode(Opcodes.ATHROW));
        newNode.maxLocals = origin.maxLocals + 3;
        newNode.maxStack = Math.max(localIndex + 2, 6);
        return newNode;
    }

    private int getParamCount(String desc, int access) {
        String params = desc.substring(1, desc.indexOf(')'));
        int count = 0;
        for (int i = 0; i < params.length(); i++) {
            if (params.charAt(i) == 'L') {
                count++;
                while (params.charAt(i) != ';')
                    i++;
            } else if (params.charAt(i) != '[') {
                count++;
            }
        }
        return count + ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC ? 0 : 1);
    }

    private LocalVariableNode getNode(List locals, int index) {
        for (Object local : locals) {
            LocalVariableNode node = (LocalVariableNode) local;
            if (index == node.index)
                return node;
        }
        return null;
    }

    private String getDescAfterAppendParam(String methodDesc, String paramDesc) {
        int g = methodDesc.indexOf(')');
        return methodDesc.substring(0, g) + paramDesc + methodDesc.substring(g);
    }
}
