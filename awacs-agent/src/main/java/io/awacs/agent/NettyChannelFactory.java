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

import io.awacs.core.util.LoggerPlus;
import io.awacs.core.util.LoggerPlusFactory;
import io.netty.channel.ChannelFuture;
import org.apache.commons.pool.BasePoolableObjectFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * Created by antong on 16/9/19.
 */
class NettyChannelFactory extends BasePoolableObjectFactory<ChannelFuture> {

    private static final LoggerPlus logger = LoggerPlusFactory.getLogger(NettyClient.class);

    private io.netty.bootstrap.Bootstrap bootstrap;

    private List<InetSocketAddress> addresses;

    private Random random = new Random();

    public NettyChannelFactory(io.netty.bootstrap.Bootstrap bootstrap, List<InetSocketAddress> addresses) {
        super();
        this.addresses = addresses;
        this.bootstrap = bootstrap;
    }

    @Override
    public ChannelFuture makeObject() {
        ChannelFuture channelFuture = bootstrap.connect(pick()).syncUninterruptibly();
        logger.debug("Channel added into object pool: {}", channelFuture);
        return channelFuture;
    }

    @Override
    public void destroyObject(final ChannelFuture channelFuture) throws Exception {
        if (channelFuture != null) {
            channelFuture.channel().close().syncUninterruptibly();
            logger.debug("Connection terminated. Channel removed.");
        }
    }

    @Override
    public boolean validateObject(final ChannelFuture channelFuture) {
        return channelFuture != null && channelFuture.channel().isActive();
    }

    @Override
    public void activateObject(ChannelFuture channelFuture) throws Exception {

    }

    @Override
    public void passivateObject(ChannelFuture channelFuture) throws Exception {

    }

    private InetSocketAddress pick() {
        return addresses.get(random.nextInt(addresses.size()));
    }
}
