package dev.nguyendevs.malevolentshrine.domain;

import org.bukkit.block.data.BlockData;

public class BlockPos {
    private final int x, y, z;
    private final BlockData data;

    public BlockPos(int x, int y, int z, BlockData data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.data = data;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public BlockData getData() { return data; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BlockPos p)) return false;
        return p.x == x && p.y == y && p.z == z;
    }

    @Override
    public int hashCode() {
        return (x * 31 + y) * 31 + z;
    }
}
