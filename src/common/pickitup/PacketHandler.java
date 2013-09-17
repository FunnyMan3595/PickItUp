package pickitup;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

import net.minecraft.entity.player.EntityPlayer;
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
        eplayer.getDataWatcher().addObject(PickItUp.DW_INDEX,
                                           new Byte(packet.data[0]));
    }
}
