package io.awacs.server.handler;

import com.alibaba.fastjson.JSONObject;
import io.awacs.common.format.Influx;
import io.awacs.common.net.Packet;
import io.awacs.component.fernflow.FernflowerComponent;
import io.awacs.component.influxdb.InfluxdbComponent;
import io.awacs.server.Handler;
import io.awacs.server.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pixyonly on 12/10/2017.
 */
public class ExceptionstackHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionstackHandler.class);

//    @Inject("mail")
//    private MailComponent mail;

    @Inject("influxdb")
    private InfluxdbComponent influxdb;

    @Inject("fernflower")
    private FernflowerComponent fernflower;

    private ConcurrentHashMap<String, List<String>> ms = new ConcurrentHashMap<>();

    @Override
    public Packet onReceive(Packet packet, InetSocketAddress address) {
        String namespace = packet.getNamespace();
        String content = new String(packet.getBody());
        //TODO do decompile
        JSONObject json = JSONObject.parseObject(content);
        String r = Influx.measurement(namespace)
                .tag("entry", json.getString("entry"))
                .tag("namespace", namespace)
                .tag("hostname", json.getString("hostname"))
                .addField("thread", json.getString("thread"))
                .addField("message", json.getString("message"))
                .addField("stack", Arrays.asList(json.getJSONArray("stack").toArray()).toString())
                .build()
                .lineProtocol();
        log.debug(r);
        if (!ms.containsKey(namespace)) {
            log.info("Creating a new batch of {}", namespace);
            ms.putIfAbsent(namespace, new LinkedList<>());
        }
        List<String> records = ms.get(namespace);
        synchronized (records) {
            records.add(r);
            //TODO config
            if (records.size() > 100) {
                ms.put(namespace, new LinkedList<>());
                influxdb.write(records);
                log.info("Batch {} commited", namespace);
            }
        }
        return null;
    }

    @Override
    public byte key() {
        return 0x01;
    }

    @Override
    public void release() {
        ms.values().forEach(influxdb::write);
    }
}
