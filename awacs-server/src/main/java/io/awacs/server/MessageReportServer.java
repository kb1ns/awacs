/**
 * Copyright 2016-2017 AWACS Project.
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

package io.awacs.server;

import com.google.common.collect.ImmutableMap;
import io.awacs.core.*;
import io.awacs.core.transport.Message;
import io.awacs.core.transport.Server;
import io.awacs.protocol.binary.BinaryMessageDecoder;
import io.awacs.protocol.binary.BinaryMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;


/**
 *
 * Created by pixyonly on 8/24/16.
 */
public final class MessageReportServer implements Server, Configurable {

    private final static Logger logger = LoggerFactory.getLogger(MessageReportServer.class);

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private String host;

    private int port;

    private Plugins plugins;

    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //可更换
                        ch.pipeline().addLast(new BinaryMessageDecoder());
                        ch.pipeline().addLast(new BinaryMessageEncoder());
                        //
                        ch.pipeline().addLast(new MessageReportRouter(MessageReportServer.this));
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            bootstrap.bind(host, port).sync();
        } catch (InterruptedException e) {
            stop();
        }
    }

    @Override
    public void stop() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }


    @Override
    public void init(Configuration configuration) throws InitializationException {

        String serverName = configuration.getString(Configurations.SERVER_PREFIX);
        ImmutableMap<String, String> serverConfig = configuration.getSubProperties(Configurations.SERVER_PREFIX + "." + serverName + ".");
        host = serverConfig.getOrDefault(Configurations.TCP_BIND_HOST, Configurations.DEFAULT_TCP_BIND_HOST);
        port = Integer.parseInt(serverConfig.getOrDefault(Configurations.TCP_BIND_PORT, Configurations.DEFAULT_TCP_BIND_PORT));
        int bossCore = Integer.parseInt(serverConfig.getOrDefault(Configurations.TCP_BOSS_CORE, Configurations.DEFAULT_TCP_BOSS_CORE));
        int workerCore = Integer.parseInt(serverConfig.getOrDefault(Configurations.TCP_WORKER_CORE, Configurations.DEFAULT_TCP_WORKER_CORE));
        boss = new NioEventLoopGroup(bossCore);
        worker = new NioEventLoopGroup(workerCore);
    }

    /**
     * Created by pixyonly on 8/24/16.
     */
    public static class MessageReportRouter extends SimpleChannelInboundHandler<Message> {

        private MessageReportServer owner;

        MessageReportRouter(MessageReportServer owner) {
            this.owner = owner;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Message message)
                throws Exception {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            PluginHandler handler = owner.plugins.getPluginHandler(message.getKey());
            //TODO 交由线程组执行
            Message ret = handler.handle(message, address);
            if (ret == null) {
                logger.debug("plugin method returns void.");
            } else {
                ctx.writeAndFlush(ret);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
        }
    }
}
