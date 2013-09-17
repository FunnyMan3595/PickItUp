package pickitup;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PlayerTracker implements IPlayerTracker
{
    @SuppressWarnings("unchecked")
    void updateHeldState(EntityPlayer player) {
        // Fetch the player's state.
        NBTTagCompound player_persisted = PickItUp.getPersistedTag(player);
        byte block_held = 0;
        if (player_persisted.hasKey(PickItUp.HELD_TAG)) {
            block_held = 1;
        }

        // Add the DataWatcher entry that keeps the player up-to-date normally.
        try {
            player.getDataWatcher().addObject(PickItUp.DW_INDEX, (Byte)block_held);
        } catch (IllegalArgumentException e) {
            player.getDataWatcher().updateObject(PickItUp.DW_INDEX, (Byte)block_held);
        }

        // And send an initialization packet to set their initial state.
        PacketDispatcher.sendPacketToPlayer(
            new Packet250CustomPayload("pickitup", new byte[] {block_held}),
            (Player) player);
    }

    public void onPlayerLogin(EntityPlayer player) {
        updateHeldState(player);
    }

    public void onPlayerLogout(EntityPlayer player) {}

    public void onPlayerChangedDimension(EntityPlayer player) {
        updateHeldState(player);
    }

    public void onPlayerRespawn(EntityPlayer player) {
        updateHeldState(player);
    }
}
