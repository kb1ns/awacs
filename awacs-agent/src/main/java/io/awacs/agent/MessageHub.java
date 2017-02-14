/**
 * Copyright 2016 AWACS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.awacs.agent;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.Subscribe;
import io.awacs.core.transport.ResponseHandler;
import io.awacs.protocol.binary.BinaryMessage;

import java.util.concurrent.Executors;

/**
 *
 * Created by pixyonly on 16/9/12.
 */
public enum MessageHub {

    instance {
    };

    private NettyClient nettyClient;

    private AsyncEventBus eventBus = new AsyncEventBus(Executors.newSingleThreadExecutor());

    void register(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        eventBus.register(this);
    }

    public void publish(Object event) {
        eventBus.post(event);
    }

    void unregister() {
        eventBus.unregister(this);
        nettyClient = null;
    }

    @Subscribe
    public void handleBinaryMessage(BinaryMessage message) {
        nettyClient.request(message, new RequestFailureHandler());
//        nettyClient.fwrite(message);
    }

    public static class RequestFailureHandler implements ResponseHandler<BinaryMessage> {

        @Override
        public void onSuccess(BinaryMessage message) {

        }

        @Override
        public void onFailure(Throwable t) {

        }
    }
}
