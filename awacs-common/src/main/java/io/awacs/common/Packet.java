package io.awacs.common;

/**
 *
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

    public byte[] serialize() {
        byte[] bb = body.getBytes();
        byte[] ab = namespace.getBytes();
        int length = 16 + bb.length + ab.length;
        byte[] payload = new byte[length];
        payload[0] = 0x5c;
        payload[1] = 0x4e;
        payload[2] = VERSION;
        payload[3] = key;

        payload[8] = (byte) (bb.length >> 24);
        payload[9] = (byte) ((bb.length & 0x00ff0000) >> 16);
        payload[10] = (byte) ((bb.length & 0x0000ff00) >> 8);
        payload[11] = (byte) (bb.length & 0x000000ff);

        payload[12] = (byte) (ab.length >> 8);
        payload[13] = (byte) (ab.length & 0x000000ff);
        System.arraycopy(ab, 0, payload, 16, ab.length);
        System.arraycopy(bb, 0, payload, 16 + ab.length, bb.length);
        return payload;
    }

    public static Packet parse(byte[] header, byte[] next) throws IllegalPacketException {
        if (header[0] != MAGIC_H || header[1] != MAGIC_L || header[2] != VERSION) {
            throw new IllegalPacketException();
        }
        byte k = header[3];
        int bodyLen = (Byte.toUnsignedInt(header[8]) << 24) |
                (Byte.toUnsignedInt(header[9]) << 16) |
                (Byte.toUnsignedInt(header[10]) << 8) |
                Byte.toUnsignedInt(header[11]);
        int namespaceLen = (Byte.toUnsignedInt(header[12]) << 8) |
                (Byte.toUnsignedInt(header[13]));
        String namespace = new String(next, 0, namespaceLen);
        String body = new String(next, namespaceLen, bodyLen);
        return new Packet(namespace, k, body);
    }

    public static int namespaceLength(byte[] payload) {
        return (Byte.toUnsignedInt(payload[12]) << 8) | (Byte.toUnsignedInt(payload[13]));
    }

    public static int bodyLength(byte[] payload) {
        return (Byte.toUnsignedInt(payload[8]) << 24) | (Byte.toUnsignedInt(payload[9]) << 16) |
                (Byte.toUnsignedInt(payload[12]) << 8) | Byte.toUnsignedInt(payload[11]);
    }

    @Override
    public String toString() {
        return "Packet{namespace=" + namespace +
                ", key=" + key +
                ", body=" + body +
                '}';
    }
}
