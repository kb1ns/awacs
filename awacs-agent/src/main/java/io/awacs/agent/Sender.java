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

package io.awacs.agent;

import io.awacs.agent.net.PacketAccumulator;
import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.net.Packet;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared by all plugins
 * Created by pixyonly on 02/09/2017.
 */
public enum Sender implements Configurable {

    I;

    private PacketAccumulator queue;

    private static final Logger log = Logger.getLogger("AWACS");

    @Override
    public void init(Configuration configuration) {
        queue = new PacketAccumulator();
        queue.init(configuration);
    }

    public void send(byte key, String body) {
        doSend(new Packet(AWACS.M.namespace(), key, body.getBytes()));
    }

    public void send(byte key, byte[] body) {
        doSend(new Packet(AWACS.M.namespace(), key, body));
    }

    void doSend(Packet packet) {
        queue.enqueue(packet);
    }

    void close() {
        queue.close();
        log.log(Level.INFO, "Sender closed.");
    }
}
