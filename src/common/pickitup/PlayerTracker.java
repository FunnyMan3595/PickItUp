package pickitup;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PlayerTracker implements IPlayerTracker
{
    @SuppressWarnings("unchecked")
    public static void updateHeldState(EntityPlayer player) {
        // Fetch the player's held block.
        ItemStack block_held = PickItUp.buildHeldItemStack(player);

        // Add the DataWatcher entry that keeps the player up-to-date normally.
        try {
            player.getDataWatcher().addObject(PickItUp.DW_INDEX, block_held);
        } catch (IllegalArgumentException e) {
            player.getDataWatcher().updateObject(PickItUp.DW_INDEX, block_held);
        }

        if (player.worldObj.isRemote) {
            // Servers only, please.
            return;
        }

        // And send an initialization packet to set their initial state.
        try {
            // Set up output stuff for dumping to a byte array.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream d_out = new DataOutputStream(out);
            NBTTagCompound nbt_out = new NBTTagCompound();

            // Dump it.
            block_held.writeToNBT(nbt_out);
            d_out.writeInt(player.entityId);
            NBTBase.writeNamedTag(nbt_out, d_out);
            d_out.close();

            try {
                // Try to send the packet.
                PacketDispatcher.sendPacketToPlayer(
                    new Packet250CustomPayload("piu.heldblock", out.toByteArray()),
                                               (Player) player);
            } catch (IllegalArgumentException e) {
                // The tag was too big to send, so strip out the complex data.
                block_held.setTagCompound(null);

                // Reset the output stuff.
                out = new ByteArrayOutputStream();
                d_out = new DataOutputStream(out);
                nbt_out = new NBTTagCompound();

                // Rewrite the bytes.
                block_held.writeToNBT(nbt_out);
                d_out.writeInt(player.entityId);
                NBTBase.writeNamedTag(nbt_out, d_out);
                d_out.close();

                // Retry the packet.
                PacketDispatcher.sendPacketToPlayer(
                    new Packet250CustomPayload("piu.heldblock",
                                               out.toByteArray()),
                    (Player) player);
            }
        } catch (NullPointerException e) {
            // Connection not fully established yet.  Don't worry about it.
            // We'll do this again when they finish logging in.
        } catch (IOException e) {}
    }

    public static void sendFreezePacket(EntityPlayer player, int x, int y, int z) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream d_out = new DataOutputStream(out);

            d_out.writeUTF(player.username);
            d_out.writeInt(x);
            d_out.writeInt(y);
            d_out.writeInt(z);

            Packet packet = new Packet250CustomPayload("piu.freeze",
                                                       out.toByteArray());

            PacketDispatcher.sendPacketToAllAround(player.posX, player.posY,
                                                   player.posZ, 160D,
                                                   player.dimension, packet);
        } catch (Exception e) {}
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
