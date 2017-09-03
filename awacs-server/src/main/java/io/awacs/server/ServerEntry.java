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

package io.awacs.server;

import io.awacs.common.Configurable;
import io.awacs.common.Configuration;
import io.awacs.common.Packet;
import io.awacs.server.codec.PacketDecoder;
import io.awacs.server.codec.PacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by pixyonly on 8/24/16.
 */
public final class ServerEntry implements Server, Configurable {

    private final static Logger log = LoggerFactory.getLogger(ServerEntry.class);

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private String host;

    private int port;

    private EventExecutorGroup businessGroup;

    private Map<Integer, Handler> handlerHolder = new HashMap<>();

    @Override
    public void load(Components components) {
        Reflections ref = new Reflections("io.awacs.server.handler");
        Set<Class<? extends Handler>> classes = ref.getSubTypesOf(Handler.class);
        for (Class<? extends Handler> clazz : classes) {
            try {
                Handler handler = clazz.newInstance();
                List<Field> waitForInject = Stream.of(clazz.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Inject.class))
                        .collect(Collectors.toList());
                for (Field f : waitForInject) {
                    f.setAccessible(true);
                    Inject i = f.getDeclaredAnnotation(Inject.class);
                    String name = i.value();
                    Object component = components.lookup(name, f.getType());
                    f.set(handler, component);
                    log.info("Inject component {} to handler {}", name, handler);
                }
                handlerHolder.put(Byte.toUnsignedInt(handler.key()), handler);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new PacketDecoder());
                        ch.pipeline().addLast(new PacketEncoder());
                        ch.pipeline().addLast(businessGroup, new Dispatcher(ServerEntry.this));
                    }
                });
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
        businessGroup.shutdownGracefully();
    }

    @Override
    public void init(Configuration configuration) {
        String serverName = configuration.getString(Configurations.SERVER_PREFIX);
        Configuration selfConfig = configuration.getSubConfig(serverName + ".");
        host = selfConfig.getString(Configurations.TCP_BIND_HOST, Configurations.DEFAULT_TCP_BIND_HOST);
        port = selfConfig.getInteger(Configurations.TCP_BIND_PORT, Configurations.DEFAULT_TCP_BIND_PORT);
        int bossCore = selfConfig.getInteger(Configurations.TCP_BOSS_CORE, Configurations.DEFAULT_TCP_BOSS_CORE);
        int workerCore = selfConfig.getInteger(Configurations.TCP_WORKER_CORE, Configurations.DEFAULT_TCP_WORKER_CORE);
        boss = new NioEventLoopGroup(bossCore);
        worker = new NioEventLoopGroup(workerCore);
        businessGroup = new DefaultEventExecutorGroup(16);
    }


    /**
     * Created by pixyonly on 8/24/16.
     */
    public static class Dispatcher extends SimpleChannelInboundHandler<Packet> {

        private ServerEntry ref;

        Dispatcher(ServerEntry ref) {
            this.ref = ref;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Packet packet)
                throws Exception {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            Handler handler = ref.handlerHolder.get(Byte.toUnsignedInt(packet.key()));
            if (handler == null) {
                //TODO default handler
                System.out.println(packet.getNamespace());
                System.out.println(packet.getBody());
                return;
            }
            Packet response = handler.onReceive(packet, address);
            if (response != null) {
                ctx.writeAndFlush(response);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
        }
    }
}
