package dev.nguyendevs.malevolentshrine.mechanic;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FakeBlockHandler {

    public void sendBlockChange(World world, int x, int y, int z, Material material) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
        packet.getBlockData().write(0, WrappedBlockData.createData(material));

        for (Player player : world.getPlayers()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            } catch (Exception ignored) {
            }
        }
    }

    public void restoreBlock(World world, int x, int y, int z, Material original) {
        sendBlockChange(world, x, y, z, original);
    }
}
