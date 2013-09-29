package pickitup;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

@SideOnly(Side.CLIENT)
public class FakeWorld implements IBlockAccess {
    public static final FakeWorld instance = new FakeWorld();
    public final Vec3Pool vecPool = new Vec3Pool(10, 100);
    public final RenderBlocks rb = new RenderBlocks(this);

    public NBTTagCompound old_block = null;
    public int id = 40;
    public int meta = 0;
    public Block block = Block.blocksList[40];
    public TileEntity te = null;
    public ChunkCoordinates where = null;

    public static void renderHeldBlock(double partialTick) {
        instance.doRender(partialTick);
    }

    public void doRender(double partialTick) {
        NBTTagCompound block_tag = PickItUp.getMyBlockHeld();
        // Do nothing if we're not holding a block.
        if (block_tag == null) {
            old_block = null;
            return;
        }

        // If the block we're holding changed, grab its details.
        if (block_tag != old_block) {
            id = block_tag.getInteger("packed_id");
            meta = block_tag.getInteger("packed_meta");
            block = Block.blocksList[id];

            NBTTagCompound data = block_tag.getCompoundTag("packed_data");
            te = null;
            if (data != null && !data.hasNoTags()) {
                te = TileEntity.createAndLoadEntity(data);
            }
        }

        // Render the block
        where = PickItUp.getHeldRenderCoords((float)partialTick);
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (where != null) {
            if (TileEntityRenderer.instance.hasSpecialRenderer(te)) {
                block = Block.blockNetherQuartz;
            }

            // Turn the lightmap back on, so that we match the standard pathway
            // exactly.
            Minecraft.getMinecraft().entityRenderer.enableLightmap(partialTick);

            // Make the block partially transparent.
            // Basics.
            boolean was_blending = GL11.glGetBoolean(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            boolean was_alpha_testing = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            boolean was_culling = GL11.glGetBoolean(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_CULL_FACE);
            // Set the alpha value to a constant.
            GL11.glBlendFunc(GL11.GL_CONSTANT_COLOR, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
            GL14.glBlendColor(0.75f, 0.75f, 0.75f, 0.75f);

            // Set up the Tessellator.
            Tessellator.instance.startDrawingQuads();
            Vec3 loc = player.getPosition((float)partialTick);
            Tessellator.instance.setTranslation(-loc.xCoord,
                                                -loc.yCoord,
                                                -loc.zCoord);
            rb.setRenderBoundsFromBlock(block);

            // Do the actual rendering.
            rb.renderBlockByRenderType(block, where.posX, where.posY, where.posZ);
            Tessellator.instance.draw();

            // Undo all the setup we did before.
            Tessellator.instance.setTranslation(0D,0D,0D);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (!was_culling) {
                GL11.glDisable(GL11.GL_CULL_FACE);
            }
            if (!was_alpha_testing) {
                GL11.glDisable(GL11.GL_ALPHA_TEST);
            }
            if (!was_blending) {
                GL11.glDisable(GL11.GL_BLEND);
            }
            Minecraft.getMinecraft().entityRenderer.disableLightmap(partialTick);
        }
    }

    /**
     * Returns the block ID at coords x,y,z
     */
    public int getBlockId(int x, int y, int z) {
        if (where == null  || (x == where.posX && y == where.posY &&
                               z == where.posZ)) {
            return id;
        } else {
            return 0;
        }
    }

    /**
     * Returns the TileEntity associated with a given block in X,Y,Z coordinates, or null if no TileEntity exists
     */
    public TileEntity getBlockTileEntity(int x, int y, int z) {
        if (where == null  || (x == where.posX && y == where.posY &&
                               z == where.posZ)) {
            return te;
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)

    /**
     * Any Light rendered on a 1.8 Block goes through here
     */
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int l) {
        return 15 << 20 | 15 << 4;
    }

    /**
     * Returns the block metadata at coords x,y,z
     */
    public int getBlockMetadata(int x, int y, int z) {
        if (TileEntityRenderer.instance.hasSpecialRenderer(te)) {
            return 0;
        }

        if (where == null  || (x == where.posX && y == where.posY &&
                               z == where.posZ)) {
            return meta;
        } else {
            return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public float getBrightness(int x, int y, int z, int l) {
        return 1.0F;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns how bright the block is shown as which is the block's light value looked up in a lookup table (light
     * values aren't linear for brightness). Args: x, y, z
     */
    public float getLightBrightness(int x, int y, int z) {
        return 1.0F;
    }

    /**
     * Returns the block's material.
     */
    public Material getBlockMaterial(int x, int y, int z) {
        return null;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the block at the specified coordinates is an opaque cube. Args: x, y, z
     */
    public boolean isBlockOpaqueCube(int x, int y, int z) {
        return false;
    }

    /**
     * Indicate if a material is a normal solid opaque cube.
     */
    public boolean isBlockNormalCube(int x, int y, int z) {
        return false;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the block at the specified coordinates is empty
     */
    public boolean isAirBlock(int x, int y, int z) {
        return true;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Gets the biome for a given set of x/z coordinates
     */
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return BiomeGenBase.plains;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns current world height.
     */
    public int getHeight() {
        return 256;
    }

    @SideOnly(Side.CLIENT)

    /**
     * set by !chunk.getAreLevelsEmpty
     */
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Returns true if the block at the given coordinate has a solid (buildable) top surface.
     */
    public boolean doesBlockHaveSolidTopSurface(int x, int y, int z) {
        return false;
    }

    /**
     * Return the Vec3Pool object for this world.
     */
    public Vec3Pool getWorldVec3Pool() {
        return vecPool;
    }

    /**
     * Is this block powering in the specified direction Args: x, y, z, direction
     */
    public int isBlockProvidingPowerTo(int x, int y, int z, int direction) {
        return 0;
    }

    /**
     * FORGE: isBlockSolidOnSide, pulled up from {@link World}
     *
     * @param x X coord
     * @param y Y coord
     * @param z Z coord
     * @param side Side
     * @param _default default return value
     * @return if the block is solid on the side
     */
    public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default) {
        return false;
    }
}
