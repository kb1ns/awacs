package io.awacs.server.codec;

import io.awacs.common.net.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out)
            throws Exception {
        out.writeBytes(packet.serialize());
    }
}
