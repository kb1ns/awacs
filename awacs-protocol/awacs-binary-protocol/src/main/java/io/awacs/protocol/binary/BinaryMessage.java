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

import io.awacs.core.transport.Key;
import io.awacs.core.transport.Message;

/**
 * Created by pixyonly on 8/23/16.
 */
public class BinaryMessage implements Message {

    public static final int MAX_PACKET_SIZE = 1 << 16;

    private ByteKey key;

    private byte[] body;

    private int pid;

    private int timestamp;

    private byte version;

    private BinaryMessage(Key<?> key, byte[] body, int pid, int timestamp, byte version) {
        this.key = (ByteKey) key;
        this.body = body;
        this.pid = pid;
        this.timestamp = timestamp;
        this.version = version;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public int getPid() {
        return pid;
    }

    @Override
    public Key<?> getKey() {
        return key;
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public byte[] serialize() {
        int length = 16 + body.length;
        byte[] content = new byte[length];
        content[0] = 0x5c;
        content[1] = 0x4e;
        content[2] = C_VERSION;
        content[3] = key.value();

        content[4] = (byte) (body.length >> 24);
        content[5] = (byte) ((body.length & 0x00ff0000) >> 16);
        content[6] = (byte) ((body.length & 0x0000ff00) >> 8);
        content[7] = (byte) (body.length & 0x000000ff);

        content[8] = (byte) (pid >> 24);
        content[9] = (byte) ((pid & 0x00ff0000) >> 16);
        content[10] = (byte) ((pid & 0x0000ff00) >> 8);
        content[11] = (byte) (pid & 0x000000ff);

        content[12] = (byte) (timestamp >> 24);
        content[13] = (byte) ((timestamp & 0x00ff0000) >> 16);
        content[14] = (byte) ((timestamp & 0x0000ff00) >> 8);
        content[15] = (byte) (timestamp & 0x000000ff);
        System.arraycopy(body, 0, content, 16, body.length);
        return content;
    }

    public static BinaryMessage parse(byte[] headers, byte[] body) {
        //TODO check magic number
        byte v = headers[2];
        Key<?> k = ByteKey.getKey(headers[3]);

        int length = (Byte.toUnsignedInt(headers[4]) << 24) | (Byte.toUnsignedInt(headers[5]) << 16) | (
                Byte.toUnsignedInt(headers[6]) << 8) | Byte.toUnsignedInt(headers[7]);
        int pid = (Byte.toUnsignedInt(headers[8]) << 24) | (Byte.toUnsignedInt(headers[9]) << 16) | (
                Byte.toUnsignedInt(headers[10]) << 8) | Byte.toUnsignedInt(headers[11]);
        int timestamp =
                (Byte.toUnsignedInt(headers[12]) << 24) | (Byte.toUnsignedInt(headers[13]) << 16) | (
                        Byte.toUnsignedInt(headers[14]) << 8) | Byte.toUnsignedInt(headers[15]);

        return new BinaryMessageBuilder().setKey(k).setVersion(v).setPid(pid)
                .setTimestamp(timestamp).setBody(body).build();
    }

    public static int bodyLength(byte[] bytes) {
        return (Byte.toUnsignedInt(bytes[4]) << 24) | (Byte.toUnsignedInt(bytes[5]) << 16) | (
                Byte.toUnsignedInt(bytes[6]) << 8) | Byte.toUnsignedInt(bytes[7]);
    }

    public static class BinaryMessageBuilder extends Builder<BinaryMessage> {

        @Override
        public BinaryMessage build() {
            return new BinaryMessage(super.key, super.body, super.pid, super.timestamp, super.version);
        }
    }

    @Override
    public String toString() {
        return "BinaryMessage{" +
                "key=" + key +
                ", body=" + new String(body) +
                ", pid=" + pid +
                ", timestamp=" + timestamp +
                ", version=" + version +
                '}';
    }
}
