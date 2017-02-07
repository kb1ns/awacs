package io.awacs.agent.instrument;

import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * Created by pixyonly on 2/7/17.
 */
public class AMethod {

    public String getName() {
        return "";
    }

    public AClass getOwner() {
        return null;
    }

    public int getMaxStack() {
        return 0;
    }

    public List<AAnotation> getAnnotations() {
        return null;
    }

    public AExceptions getExceptions() {
        return null;
    }

    public int getInstructionSize() {
        return 0;
    }

    public String getDescriptor() {
        return "";
    }

    public int getParameterCount() {
        return 0;
    }

    public String getRetDescriptor() {
        return "";
    }

    public void intercept(String caller, String method) {

    }

    public void test() {
        Method m;
        MethodNode mn;
    }
}
