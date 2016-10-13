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

package io.awacs.protocol.binary;

import io.awacs.core.transport.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

/**
 * Created by pixyonly on 6/28/16.
 */
public class BinaryMessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        int readable = in.readableBytes();
        if (readable > BinaryMessage.MAX_PACKET_SIZE) {
            in.skipBytes(readable);
            throw new TooLongFrameException();
        }
        //判断获取的是否比头信息大
        if (readable < 16) {
            return;
        }
        byte[] headerBytes = new byte[16];
        in.readBytes(headerBytes, 0, 16);
        int bodyLength = BinaryMessage.bodyLength(headerBytes);
        //判断body获取的是否比body大
        if (in.readableBytes() < bodyLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] bodyBytes = new byte[bodyLength];
        in.readBytes(bodyBytes, 0, bodyLength);
//        byte[] copy = new byte[16 + bodyLength];
//        System.arraycopy(headerBytes, 0, copy, 0, 16);
//        System.arraycopy(bodyBytes, 0, copy, 16, bodyLength);
        Message m = BinaryMessage.parse(headerBytes, bodyBytes);
        in.markReaderIndex();

        out.add(m);
    }
}
