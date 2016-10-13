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

import io.awacs.core.transport.Client;
import io.awacs.core.transport.ResponseHandler;
import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;
import io.awacs.protocol.binary.BinaryMessage;
import io.awacs.protocol.binary.BinaryMessageDecoder;
import io.awacs.protocol.binary.BinaryMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by antong on 16/9/8.
 */
class NettyClient implements Client<BinaryMessage> {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(NettyClient.class);

    private List<InetSocketAddress> addresses;

    private Bootstrap bootstrap;

    private EventLoopGroup group;

    private NettyChannelPool pool;

    private NettyClient(List<InetSocketAddress> addresses) {
        this.addresses = addresses;
        start();
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    //TODO retrieve response from pipeline
    @Override
    public void request(BinaryMessage msg, ResponseHandler<BinaryMessage> handler) {
        ChannelFuture channelFuture = null;
        try {
            channelFuture = pool.borrowChannel();
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(msg);
            logger.debug("Message send successful, remote address: {}", channel.remoteAddress());
        } catch (Exception e) {
            handler.onFailure(e);
        } finally {
            pool.returnObject(channelFuture);
        }
    }

    @Override
    public void start() {
        try {
            this.bootstrap = new Bootstrap();
            this.group = new NioEventLoopGroup();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new BinaryMessageDecoder());
                            pipeline.addLast(new BinaryMessageEncoder());
                        }
                    });

            pool = new NettyChannelPool(bootstrap, addresses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.group.shutdownGracefully();
        this.pool.clear();
        logger.debug("Netty client stopped.");
    }

    public void finalize() {
        stop();
    }

    public static class Builder {

        private List<InetSocketAddress> addresses;

        public Builder setAddresses(List<InetSocketAddress> addresses) {
            this.addresses = addresses;
            return this;
        }

        public Builder addAddress(InetSocketAddress address) {
            if (this.addresses == null)
                this.addresses = new LinkedList<>();
            this.addresses.add(address);
            return this;
        }

        public NettyClient build() {
            return new NettyClient(addresses);
        }
    }

}
