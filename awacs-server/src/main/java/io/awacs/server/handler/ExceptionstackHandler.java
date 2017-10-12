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
        influxdb.write(r);
        return null;
    }

    @Override
    public byte key() {
        return 0x01;
    }

    @Override
    public void release() {
    }
}
