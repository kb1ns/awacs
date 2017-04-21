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

import io.awacs.core.transport.Message;
import io.awacs.core.transport.ResponseHandler;
import io.awacs.protocol.binary.BinaryMessage;

/**
 *
 * Created by pixyonly on 16/9/12.
 */
public enum MessageHub {

    instance {
    };

    private NettyClient nettyClient;

    private MessageAccumulator messageAccumulator;

    void register(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        //TODO move to configuration
        this.messageAccumulator = new MessageAccumulator(nettyClient, 100, 1024*1024*10, 10000, 5000);
    }

    public void publish(Message message) {
        messageAccumulator.append(message, null);
    }

    void unregister() {
        messageAccumulator.close();
        nettyClient.stop();
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
