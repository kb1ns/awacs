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
public class ReportHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(ReportHandler.class);

//    @Inject("email")
//    private EmailComponent emailComponent;

    @Inject("influxdb")
    private InfluxdbComponent influxdb;

    private ConcurrentHashMap<String, List<String>> ms = new ConcurrentHashMap<>();

    @Override
    public Packet onReceive(Packet packet, InetSocketAddress address) {
        String namespace = packet.getNamespace();
        String content = new String(packet.getBody());
        log.debug(content);
        if (!ms.containsKey(namespace)) {
            ms.putIfAbsent(namespace, new LinkedList<>());
            log.info("New batch allocated.");
        }
        List<String> records = ms.get(namespace);
        synchronized (records) {
            records.add(content);
            //TODO config
            if (records.size() > 1000) {
                ms.put(namespace, new LinkedList<>());
                influxdb.write(records);
                log.info("Records commited.");
            }
        }
        return null;
    }

    @Override
    public byte key() {
        return 0x01;
    }
}
