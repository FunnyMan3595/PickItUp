package pickitup;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

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
                             Packet250CustomPayload packet, Player player) {
        assert(packet.channel.equals("pickitup"));

        EntityPlayer eplayer = (EntityPlayer) player;
        assert(eplayer.worldObj.isRemote);

        ItemStack syncStack = new ItemStack(0, 0, 0);
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(packet.data);
            DataInputStream d_in = new DataInputStream(in);
            NBTTagCompound tag = (NBTTagCompound) NBTBase.readNamedTag(d_in);

            syncStack = new ItemStack(0,0,0);
            syncStack.readFromNBT(tag);
        } catch (IOException e) {}

        eplayer.getDataWatcher().addObject(PickItUp.DW_INDEX, syncStack);
    }
}
