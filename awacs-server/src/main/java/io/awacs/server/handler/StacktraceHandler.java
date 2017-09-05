package io.awacs.server.handler;

/**
 *
 * Created by pixyonly on 03/09/2017.
 */
public class StacktraceHandler {

//    private static final Logger log = LoggerFactory.getLogger(StacktraceHandler.class);
//
////    @Inject("email")
////    private EmailComponent emailComponent;
//
////    @Inject("influxdb")
//    private InfluxdbComponent influxdbComponent;
//
//    @Override
//    public Packet onReceive(Packet packet, InetSocketAddress address) {
//        String content = packet.getBody();
//        String namespace = packet.getNamespace();
//        JSONObject json = JSONObject.parseObject(content);
//
//        JSONObject stack = json.getJSONObject("stack");
//        //TODO config measurement
//        Point p = Point.measurement(namespace).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .tag("ip", address.getAddress().getHostAddress())
//                .tag("entry", stack.getString("caller").replaceAll("/", "."))
//                .addField("thread", json.getString("thread"))
//                .addField("stack", prettify(stack))
//                .addField("execution_time", stack.getIntValue("elapsedTime"))
//                .build();
//        influxdbComponent.write(p);
//        return null;
//    }
//
//    @Override
//    public byte key() {
//        return 0x01;
//    }
//
//    String prettify(JSONObject json) {
//        return tabLevel(json, 0).toString();
//    }
//
//    String prettify(JSONArray json) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < json.size(); i++) {
//            JSONObject span = json.getJSONObject(i);
//            if (span.getBooleanValue("nativeMethod"))
//                break;
//            sb.append('-').append(span.getString("className"))
//                    .append('#')
//                    .append(span.getString("methodName"))
//                    .append('@')
//                    .append(span.getIntValue("lineNumber"))
//                    .append('\n');
//        }
//        return sb.toString();
//    }
//
//    private StringBuilder tabLevel(JSONObject sub, int tab) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < tab * 2; i++)
//            sb.append("+");
//        sb.append(sub.getString("caller").replaceAll("/", "."))
//                .append(":")
//                .append(sub.getIntValue("elapsedTime"))
//                .append("|")
//                .append(sub.getIntValue("callCount"));
//        JSONArray subMethods = sub.getJSONArray("subMethods");
//        if (!subMethods.isEmpty() && sub.getIntValue("elapsedTime") > 0) {
//            for (int i = 0; i < subMethods.size(); i++) {
//                sb.append('\n').append(tabLevel(subMethods.getJSONObject(i), tab + 1));
//            }
//        }
//        return sb;
//    }
}
