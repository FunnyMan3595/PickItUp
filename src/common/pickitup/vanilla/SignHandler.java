package pickitup.vanilla;
import pickitup.api.*;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public class SignHandler implements ISimplePickup {
    public boolean handlesPickupOf(int id, int meta) {
        if (id == Block.signPost.blockID) {
            return true;
        } else if (id == Block.signWall.blockID) {
            return true;
        }

        return false;
    }

    public void afterPutdown(EntityPlayer player, int x, int y, int z, int face) {
        NBTTagCompound data = new NBTTagCompound();
        player.worldObj.getBlockTileEntity(x, y, z).writeToNBT(data);

        if (face == 1)
        {
            int rotation = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
            player.worldObj.setBlock(x, y, z, Block.signPost.blockID, rotation, 2);
        }
        else
        {
            player.worldObj.setBlock(x, y, z, Block.signWall.blockID, face, 2);
        }

        player.worldObj.getBlockTileEntity(x, y, z).readFromNBT(data);
    }
}
