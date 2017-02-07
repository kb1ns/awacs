package io.awacs.agent.instrument;

import io.awacs.core.Plugin;

import java.lang.instrument.Instrumentation;

/**
 * <p>
 * Created by pixyonly on 2/7/17.
 */
public abstract class BytecodePlugin implements Plugin {

    protected Instrumentation inst;

    @Override
    public Instrumentation getInstrumentation() {
        return inst;
    }

    @Override
    public void setInstrumentation(Instrumentation inst) {
        this.inst = inst;
    }

    protected abstract boolean filterClass(AClass aClass);

    protected abstract boolean filterMethod(AMethod aMethod);

    protected abstract void onMethodEnter(AMethod aMethod);

    protected abstract void onMethodQuit(AMethod aMethod);

    protected abstract void onMethodExcept(AMethod aMethod);

    @Override
    public void boot() {

    }
}
