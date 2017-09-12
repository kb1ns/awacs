package io.awacs.common.net;

import io.awacs.common.IllegalPacketException;

/**
 *
 *
 * 	Byte /      0        |       1       |       2       |       3       |
 *       /               |               |               |               |
 *       +---------------+---------------+---------------+---------------+
 *      0|             Magic             |    Version    |      Key      |
 *       +---------------+---------------+---------------+---------------+
 *      4|                            SEQ-ID                             |
 *       +---------------+---------------+---------------+---------------+
 *      8|    compress   |               |        NAMESPACE_LEN          |
 *       +---------------+---------------+---------------+---------------+
 *     12|                            BODY_LEN                           |
 *       +---------------+---------------+---------------+---------------+
 *       Total 16 bytes
 * Created by pixyonly on 02/09/2017.
 */
public class Packet {

    public static final int MAX_PACKET_SIZE = 1 << 20;

    public static final byte MAGIC_H = 0x5c;

    public static final byte MAGIC_L = 0x4e;

    public static final byte VERSION = 0x02;

    private String namespace;

    private String body;

    private byte key;

    private long reqId;

    public Packet(String namespace, byte key, String body) {
        this.namespace = namespace;
        this.key = key;
        this.body = body;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getBody() {
        return body;
    }

    public int size() {
        return body.getBytes().length + namespace.getBytes().length + 16;
    }

    public byte key() {
        return this.key;
    }

    public byte[] serialize() {
        byte[] nb = namespace.getBytes();
        byte[] bb = body.getBytes();
        int length = 16 + bb.length + nb.length;
        byte[] payload = new byte[length];

        payload[0] = 0x5c;
        payload[1] = 0x4e;
        payload[2] = VERSION;
        payload[3] = key;

        //4,5,6,7,8,9 ignore

        //namespace len
        payload[10] = (byte) ((nb.length & 0x0000ff00) >> 8);
        payload[11] = (byte) (nb.length & 0x000000ff);

        //body len
        payload[12] = (byte) (bb.length >> 24);
        payload[13] = (byte) ((bb.length & 0x00ff0000) >> 16);
        payload[14] = (byte) (bb.length >> 8);
        payload[15] = (byte) (bb.length & 0x000000ff);

        //
        System.arraycopy(nb, 0, payload, 16, nb.length);
        System.arraycopy(bb, 0, payload, 16 + nb.length, bb.length);
        return payload;
    }

    public static Packet parse(byte[] header, byte[] next) throws IllegalPacketException {
        if (header[0] != MAGIC_H || header[1] != MAGIC_L || header[2] != VERSION) {
            throw new IllegalPacketException();
        }
        //key
        byte k = header[3];
        //compression ignore
        byte compression = header[8];
        int namespaceLen = (Byte.toUnsignedInt(header[10]) << 8) |
                (Byte.toUnsignedInt(header[11]));
        int bodyLen = (Byte.toUnsignedInt(header[12]) << 24) |
                (Byte.toUnsignedInt(header[13]) << 16) |
                (Byte.toUnsignedInt(header[14]) << 8) |
                Byte.toUnsignedInt(header[15]);
        String namespace = new String(next, 0, namespaceLen);
        String body = new String(next, namespaceLen, bodyLen);
        return new Packet(namespace, k, body);
    }

    public static int namespaceLength(byte[] payload) {
        return (Byte.toUnsignedInt(payload[10]) << 8) | (Byte.toUnsignedInt(payload[11]));
    }

    public static int bodyLength(byte[] payload) {
        return (Byte.toUnsignedInt(payload[12]) << 24) | (Byte.toUnsignedInt(payload[13]) << 16) |
                (Byte.toUnsignedInt(payload[14]) << 8) | Byte.toUnsignedInt(payload[15]);
    }

    @Override
    public String toString() {
        return "Packet{namespace=" + namespace +
                ", key=" + key +
                ", body=" + body +
                '}';
    }
}
