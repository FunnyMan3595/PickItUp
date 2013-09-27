package pickitup;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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
}
