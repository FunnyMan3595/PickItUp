package pickitup;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class CommonProxy {
    // Called from the coremod's Hook.DROP_HELD_BLOCK.
    public static void onPlayerDrop(EntityPlayer player) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack != null) {
            return;
        }

        if (player.worldObj.isRemote) {
            // Server-side only, please.
            return;
        }

        // The player has pressed their "throw item" key with an empty hand.
        // This is the "force-place" keystroke, so we invoke that if they are
        // holding a block.
        NBTTagCompound block = PickItUp.getBlockHeld(player);
        if (block != null) {
            PickItUp.forcePlace(block, player);
        }
    }

    // Called from the coremod's Hook.DROP_HELD_BLOCK.
    public static void initPlayer(EntityPlayer player) {
        PlayerTracker.updateHeldState(player);
    }

    public double getReach(EntityPlayer player) {
        return ((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance() + 1;
    }

    public MovingObjectPosition getPlayerTarget(EntityPlayer player, float partialTick) {
        double reach = getReach(player);
        Vec3 playerPos = getPlayerPos(player, partialTick);
        Vec3 lookTarget = getPlayerLook(player, playerPos, reach, partialTick);
        return player.worldObj.rayTraceBlocks(playerPos, lookTarget);
    }

    public Vec3 getPlayerPos(EntityPlayer player, float partialTick) {
        Vec3 playerPos = player.worldObj.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
        if (player.isSneaking()) {
            // Eye height while sneaking.
            playerPos.yCoord += 1.54;
        } else {
            // Normal eye height.
            playerPos.yCoord += 1.62;
        }

        return playerPos;
    }

    public Vec3 getPlayerLook(EntityPlayer player, Vec3 playerPos, double reach, float partialTick) {
        if (playerPos == null) {
            playerPos = getPlayerPos(player, partialTick);
        }
        Vec3 playerLook = player.getLook(partialTick);
        Vec3 playerLookTarget = playerPos.addVector(
                                    playerLook.xCoord * reach,
                                    playerLook.yCoord * reach,
                                    playerLook.zCoord * reach);
        return playerLookTarget;
    }
}
