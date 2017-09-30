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

import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pixyonly on 04/09/2017.
 */
class Connection {

    private final static Logger log = Logger.getLogger("AWACS");

    Remote remote;

    AsynchronousSocketChannel channel;

    int timeout;

    Connection(Remote remote, int timeout) {
        this.remote = remote;
        this.timeout = timeout;
        ready();
    }

    void ready() {
        try {
            channel = AsynchronousSocketChannel.open();
            Future<Void> future = channel.connect(remote.getAddress());
            future.get(timeout, TimeUnit.MILLISECONDS);
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            log.log(Level.INFO, "Connected to server {0}", remote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void flush(ByteBuffer batch, final Callback cb) {
        batch.flip();
        channel.write(batch, batch, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (!attachment.hasRemaining()) {
                    log.finest("Batch flushed.");
                    if (cb != null) {
                        cb.onComplete();
                    }
                } else {
                    channel.write(attachment, attachment, this);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
                if (cb != null) {
                    cb.onException(exc);
                }
                ready();
            }
        });
    }
}
