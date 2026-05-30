package dev.nguyendevs.malevolentshrine.util;

public final class BlockPosUtil {
    private static final int Y_OFFSET = 2048;

    public static long pack(int x, int y, int z) {
        return (((long) x & 0x3FFFFFF) << 38) | (((long) z & 0x3FFFFFF) << 12) | ((y + Y_OFFSET) & 0xFFF);
    }

    public static int unpackX(long packed) {
        return (int) (packed >> 38);
    }

    public static int unpackY(long packed) {
        return (int) ((packed & 0xFFF) - Y_OFFSET);
    }

    public static int unpackZ(long packed) {
        return (int) ((packed >> 12) & 0x3FFFFFFF);
    }

    private BlockPosUtil() {}
}
