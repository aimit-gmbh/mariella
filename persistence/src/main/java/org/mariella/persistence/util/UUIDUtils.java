package org.mariella.persistence.util;

import java.util.UUID;

public class UUIDUtils {

    public static UUID toUUID(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException();
        }
        long high = 0;
        for (int i = 0; i < 8; i++) {
            high = high * 256 + ((long) bytes[i] & 0xffL);
        }
        long low = 0;
        for (int i = 8; i < 16; i++) {
            low = low * 256 + ((long) bytes[i] & 0xffL);
        }
        return new UUID(high, low);
    }

    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) return null;
        byte[] result = new byte[16];
        long high = uuid.getMostSignificantBits();
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (high & 0xffL);
            high >>= 8;
        }
        long low = uuid.getLeastSignificantBits();
        for (int i = 15; i >= 8; i--) {
            result[i] = (byte) (low & 0xffL);
            low >>= 8;
        }
        return result;
    }

}
