package pickitup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
public class ConnectionHandler implements IConnectionHandler {
    /**
     * Called when a player logs into the server
     *  SERVER SIDE
     *
     * @param player
     * @param netHandler
     * @param manager
     */
    @SuppressWarnings("unchecked")
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) {
        EntityPlayer eplayer = (EntityPlayer) player;
        NBTTagCompound player_persisted = PickItUp.getPersistedTag(eplayer);
        byte block_held = 0;
        if (player_persisted.hasKey(PickItUp.HELD_TAG)) {
            block_held = 1;
        }
        eplayer.getDataWatcher().addObject(PickItUp.DW_INDEX, (Byte)block_held);
    }

    /**
     * If you don't want the connection to continue, return a non-empty string here
     * If you do, you can do other stuff here- note no FML negotiation has occured yet
     * though the client is verified as having FML installed
     *
     * SERVER SIDE
     *
     * @param netHandler
     * @param manager
     */
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) {
        return null;
    }

    /**
     * Fired when a remote connection is opened
     * CLIENT SIDE
     *
     * @param netClientHandler
     * @param server
     * @param port
     */
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) {
    }
    /**
     *
     * Fired when a local connection is opened
     *
     * CLIENT SIDE
     *
     * @param netClientHandler
     * @param server
     */
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) {
    }

    /**
     * Fired when a connection closes
     *
     * ALL SIDES
     *
     * @param manager
     */
    public void connectionClosed(INetworkManager manager) {
    }

    /**
     * Fired when the client established the connection to the server
     *
     * CLIENT SIDE
     * @param clientHandler
     * @param manager
     * @param login
     */
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) {
    }
}
