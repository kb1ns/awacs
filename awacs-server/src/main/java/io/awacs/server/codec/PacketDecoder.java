package io.awacs.server.codec;

import io.awacs.common.net.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by pixyonly on 03/09/2017.
 */
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        int readable = in.readableBytes();
        //FIXME
//        if (readable > BinaryMessage.MAX_PACKET_SIZE) {
//            in.skipBytes(readable);
//            throw new TooLongFrameException();
//        }
        //判断获取的是否比头信息大
        if (readable < 16) {
            return;
        }
        byte[] headerBytes = new byte[16];
        in.readBytes(headerBytes, 0, 16);
        int namespaceLength = Packet.namespaceLength(headerBytes);
        int bodyLength = Packet.bodyLength(headerBytes);
        if (in.readableBytes() < bodyLength + namespaceLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] next = new byte[bodyLength + namespaceLength];
        in.readBytes(next, 0, bodyLength + namespaceLength);
        Packet packet = Packet.parse(headerBytes, next);
        in.markReaderIndex();
        out.add(packet);
    }
}
