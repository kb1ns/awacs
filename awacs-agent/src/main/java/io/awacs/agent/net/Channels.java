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

package io.awacs.agent.net;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pixyonly on 14/09/2017.
 */
public final class Channels {

    private final Set<Connection> readyConnections;

    Channels(String[] serverAddrs, int timeout) {
        readyConnections = new HashSet<>();
        for (String a : serverAddrs) {
            readyConnections.add(new Connection(new Remote(a), timeout));
        }
    }

    void flush(final ByteBuffer buffer, final Callback cb) {
        Connection c = readyConnections.iterator().next();
        c.flush(buffer, cb);
    }
}
