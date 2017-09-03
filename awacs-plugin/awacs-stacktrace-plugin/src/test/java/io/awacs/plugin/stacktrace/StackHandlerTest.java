package io.awacs.plugin.stacktrace;

import org.junit.Test;

/**
 * Created by pixyonly on 01/09/2017.
 */
public class StackHandlerTest {

    @Test
    public void test() {
//        String s = "{\"caller\":\"io/awacs/plugin/stacktrace/CallStackTest#test\",\"timestamp\":1504249842198,\"elapsedTime\":1134,\"callCount\":1,\"subMethods\":[{\"caller\":\"io/awacs/plugin/stacktrace/CallStackTest$Stub0#stubMethod0\",\"timestamp\":1504249842198,\"elapsedTime\":1134,\"callCount\":1,\"subMethods\":[{\"caller\":\"io/awacs/plugin/stacktrace/CallStackTest$Stub1#stubMethod1\",\"timestamp\":1504249842198,\"elapsedTime\":1031,\"callCount\":1,\"subMethods\":[{\"caller\":\"io/awacs/plugin/stacktrace/CallStackTest$Stub2#stubMethod2\",\"timestamp\":1504249842198,\"elapsedTime\":1029,\"callCount\":5,\"subMethods\":[{\"caller\":\"io/awacs/plugin/stacktrace/CallStackTest$Stub3#stubMethod3\",\"timestamp\":1504249842198,\"elapsedTime\":927,\"callCount\":5,\"subMethods\":[]}]}]}, {\"caller\":\"io/awacs/plugin/stacktrace/CallStackTest$Stub3#stubMethod3\",\"timestamp\":1504249843229,\"elapsedTime\":103,\"callCount\":1,\"subMethods\":[]}]}]}";
//        StackTraceHandler handler = new StackTraceHandler();
//        System.out.println(handler.prettify(JSONObject.parseObject(s)));
//
//        try {
//            sub1();
//        } catch (Exception e) {
//            JSONObject report = new JSONObject();
//            report.put("thread", Thread.currentThread().getName());
//            report.put("stack", e.getStackTrace());
//            report.put("exception", e.getClass().getCanonicalName());
//            report.put("message", e.getMessage());
//            System.out.println(String.format("Host: %s\nPID: %d\nThread: %s\nthrows %s:%s\n%s",
//                    "127.0.0.1",
//                    1203,
//                    report.getString("thread"),
//                    report.getString("exception"),
//                    report.getString("message"),
//                    handler.prettify(report.getJSONArray("stack"))));
//        }
    }

    public void sub1() {
        throw new RuntimeException();
    }
}
