package io.awacs.plugin.stacktrace;

import java.util.LinkedHashMap;

/**
 * element of CallStack
 * Created by pixyonly on 2/7/17.
 */
public class CallElement {

    private LinkedHashMap<String, CallElement> subElements;

    private String clazz;

    private String method;

    private int callCounter = 1;

    private transient boolean matchExpected = true;

    private long timestamp;

    private long elapsedTime = 0l;

    CallElement(String clazz, String method) {
        this.clazz = clazz;
        this.method = method;
        this.timestamp = System.currentTimeMillis();
        subElements = new LinkedHashMap<>();
    }

    public String id() {
        return clazz + "#" + method;
    }

    public CallElement end() {
        this.elapsedTime = System.currentTimeMillis() - this.timestamp;
        this.matchExpected = false;
        return this;
    }

    public void callSub(CallElement callee) {
        CallElement yetValue = subElements.putIfAbsent(callee.id(), callee);
        if (yetValue != null) {
            yetValue.callCounter++;
            yetValue.matchExpected = true;
        }
    }

    @Override
    public String toString() {
        return '{' +
                "\"caller\":\"" + id() + "\"," +
                "\"timestamp\":" + timestamp + "," +
                "\"elapsedTime\":" + elapsedTime + "," +
                "\"callCount\":" + callCounter + "," +
                "\"subMethods\":" + subElements.values().toString() +
                '}';
    }
}
