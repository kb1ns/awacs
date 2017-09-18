package io.awacs.server.handler;

import io.awacs.common.net.Packet;
import io.awacs.component.influxdb.InfluxdbComponent;
import io.awacs.server.Handler;
import io.awacs.server.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class StacktraceHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(StacktraceHandler.class);

//    @Inject("email")
//    private EmailComponent emailComponent;

    @Inject("influxdb")
    private InfluxdbComponent influxdb;

    private ConcurrentHashMap<String, List<String>> ms = new ConcurrentHashMap<>();

    @Override
    public Packet onReceive(Packet packet, InetSocketAddress address) {
        String namespace = packet.getNamespace();
        String content = packet.getBody();
        if (!ms.containsKey(namespace)) {
            ms.putIfAbsent(namespace, new LinkedList<>());
        }
        List<String> records = ms.get(namespace);
        synchronized (records) {
            records.add(content);
            //TODO config
            if (records.size() > 1000) {
                ms.put(namespace, new LinkedList<>());
                influxdb.write(records);
            }
        }
        return null;
    }

    @Override
    public byte key() {
        return 0x01;
    }
}
