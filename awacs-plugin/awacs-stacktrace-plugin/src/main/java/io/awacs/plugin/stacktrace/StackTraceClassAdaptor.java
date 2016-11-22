package io.awacs.plugin.stacktrace;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * Created by pixyonly on 16/11/22.
 */
public class StackTraceClassAdaptor extends ClassNode {

    public StackTraceClassAdaptor(ClassVisitor cv) {
        super(Opcodes.ASM5);
        this.cv = cv;
    }

    public void visitEnd() {
        ClassTransformer ct = new FilteredClassTransformer();
        ct.visit(this);
        accept(cv);
    }
}
