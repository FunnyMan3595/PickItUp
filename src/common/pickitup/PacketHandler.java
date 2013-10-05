package pickitup;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

public class PacketHandler implements IPacketHandler
{
    /**
     * Recieve a packet from one of the registered channels for this packet handler
     * @param manager The network manager this packet arrived from
     * @param packet The packet itself
     * @param player A dummy interface representing the player - it can be cast into a real player instance if needed
     */
    @SuppressWarnings("unchecked")
    public void onPacketData(INetworkManager manager,
                             Packet250CustomPayload packet, Player fmlMe) {
        if (!packet.channel.startsWith("piu.")) {
            return;
        }

        EntityPlayer me = (EntityPlayer) fmlMe;
        if (!me.worldObj.isRemote) {
            return;
        }

        if (packet.channel.equals("piu.heldblock")) {
            ItemStack syncStack = new ItemStack(1, 0, 0);
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(packet.data);
                DataInputStream d_in = new DataInputStream(in);
                int entity_id = d_in.readInt();
                NBTTagCompound tag = (NBTTagCompound) NBTBase.readNamedTag(d_in);

                syncStack = new ItemStack(1, 0, 0);
                syncStack.readFromNBT(tag);

                Entity entity = ((WorldClient) me.worldObj).getEntityByID(entity_id);
                if (! (entity instanceof EntityPlayer)) {
                    // Who?  Never heard of the guy.
                    return;
                }

                EntityPlayer player = (EntityPlayer) entity;

                try {
                    player.getDataWatcher().addObject(PickItUp.DW_INDEX, syncStack);
                } catch (IllegalArgumentException e) {
                    // Already added, update it instead.
                    player.getDataWatcher().updateObject(PickItUp.DW_INDEX, syncStack);
                }
            } catch (IOException e) { }
        } else if (packet.channel.equals("piu.freeze")) {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(packet.data);
                DataInputStream d_in = new DataInputStream(in);
                String username = d_in.readUTF();
                int x = d_in.readInt();
                int y = d_in.readInt();
                int z = d_in.readInt();

                FakeWorld.freeze(username, x, y, z);
            } catch (IOException e) { }
        }
    }
}
