package pickitup;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@Mod(modid = "PickItUp",
     name = "PickItUp",
     version = "{conf:VERSION}")
@NetworkMod(serverSideRequired = false,
            clientSideRequired = true,
            channels = { "pickitup" },
            packetHandler = pickitup.PacketHandler.class)
            //connectionHandler = pickitup.ConnectionHandler.class)
public class PickItUp {
    public static final int DEFAULT_DW_INDEX = 27;
    public static int DW_INDEX = DEFAULT_DW_INDEX;

    public static final String HELD_TAG = "PickItUp_held";

    public static Configuration config = null;

    @SidedProxy(clientSide="pickitup.ClientProxy",
                serverSide="pickitup.ServerProxy")
    public static CommonProxy proxy;

    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {
        // Load config file.
        config = new Configuration(event.getSuggestedConfigurationFile());
    }

    @Mod.Init
    public void init(FMLInitializationEvent event) {
        try {
            config.load();
        } catch (RuntimeException e) {} // Just regenerate the config if it's
                                        // broken.


        // Fetch the DataWatcher ID for the held block.
        DW_INDEX = config.get("holdingBlockDataWatcherIndex",
                              config.CATEGORY_GENERAL,
                              DEFAULT_DW_INDEX,
                              "The index on EntityPlayer's DataWatcher used to store whether they are holding a block."
                             ).getInt(DEFAULT_DW_INDEX);

        // Register our listeners.
        MinecraftForge.EVENT_BUS.register(new EventListener());
        GameRegistry.registerPlayerTracker(new PlayerTracker());
    }

    public static boolean onWhitelist(int id, int meta) {
        if (id < 0 || id > Block.blocksList.length
                   || Block.blocksList[id] == null) {
            return false;
        }

        return true;
    }

    // --- The meat of block pick up and placement. ---

    public static void pickUpBlock(EntityPlayer player, int x, int y, int z) {
        // Basic information about the block.
        World world = player.worldObj;
        int id = world.getBlockId(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        // Advanced information about the block, if any.
        TileEntity te = world.getBlockTileEntity(x, y, z);
        NBTTagCompound block_data = null;
        if (te != null) {
            block_data = new NBTTagCompound();
            te.writeToNBT(block_data);
        }

        // Pack the block into an NBT tag.
        NBTTagCompound item_tag = new NBTTagCompound();
        item_tag.setInteger("packed_id", id);
        item_tag.setInteger("packed_meta", meta);
        if (block_data != null) {
            item_tag.setCompoundTag("packed_data", block_data);
        }

        // Save the block in the player's NBT data.
        setBlockHeld(player, item_tag);

        // Delete the block from the world.
        world.removeBlockTileEntity(x, y, z);
        world.setBlock(x, y, z, 0);
    }

    // Try to place the block nicely.
    public static void tryToPlace(NBTTagCompound block, EntityPlayer player,
                           int x, int y, int z, int face,
                           boolean force) {
        int id = block.getInteger("packed_id");
        int meta = block.getInteger("packed_meta");
        ItemStack fakeStack = new ItemStack(id, 1, meta);

        // Check to see if the player has permission to place there.
        if (player.canPlayerEdit(x, y, z, face, fakeStack)) {
            if (face == 0) { --y; }
            if (face == 1) { ++y; }
            if (face == 2) { --z; }
            if (face == 3) { ++z; }
            if (face == 4) { --x; }
            if (face == 5) { ++x; }

            // Check to see if the target is a valid place to put the block.
            if (player.worldObj.canPlaceEntityOnSide(id, x, y, z, false, face,
                                                  player, fakeStack)) {
                if (placeAt(block, player, x, y, z)) {
                    clearBlockHeld(player);
                    return;
                }
            }
        }

        if (force) {
            forcePlace(block, player);
        }
    }

    // Try very hard to find a place to put the block.  This scans an 11x11x11
    // cube to find the best place to put it.  If no valid locations are found,
    // the block is deleted.
    public static void forcePlace(NBTTagCompound block, EntityPlayer player) {
        World world = player.worldObj;
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);

        int best_dx = 0;
        int best_dy = 0;
        int best_dz = 0;
        double best_score = Double.POSITIVE_INFINITY;

        for (int dy = -5; dy <= 5; dy++) {
            if (y + dy < 0 || y + dy > 255) {
                continue;
            }

            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    double score = Math.sqrt(dx*dx + dy*dy + dz*dz);

                    int id = world.getBlockId(x+dx, y+dy, z+dz);
                    if (id != 0) {
                        Block block_here = Block.blocksList[id];
                        if (block_here.blockMaterial.isReplaceable() ||
                            block_here.isBlockReplaceable(world, x, y, z)) {
                            // We prefer open air over replaceable blocks.
                            score += 10;
                        } else {
                            // Unreplaceable blocks... can't be replaced.  Duh.
                            continue;
                        }
                    }

                    if (dx == 0 && dz == 0) {
                        if (dy == 0) {
                            // We'd rather not place it at our feet.
                            score += 20;
                        } else if (dy == 1) {
                            // And we'd REALLY rather not place it in our
                            // head!
                            score += 40;
                        }
                    }

                    if (score < best_score) {
                        // This is our new best place.
                        best_dx = dx;
                        best_dy = dy;
                        best_dz = dz;
                        best_score = score;
                    }
                }
            }
        }

        // If we found a valid spot, use it.
        if (best_score != Double.POSITIVE_INFINITY) {
            placeAt(block, player, x+best_dx, y+best_dy, z+best_dz);
        }

        // If not, well, sucks to be you.  This was a force-place, so the block
        // is destroyed.
        // Also, how did you manage to get entombed in an 11x11x11 area filled
        // with non-replaceable blocks?
        // At a guess, the most likely time to hit this is if the player fell
        // out of the world, in which case y+5 is still below the world.
        clearBlockHeld(player);
    }

    // Mostly stolen from ItemBlock.placeBlockAt, this handles the nitty-gritty
    // of putting the block in the world, with all appropriate notifications.
    public static boolean placeAt(NBTTagCompound block, EntityPlayer player,
                           int x, int y, int z) {
        World world = player.worldObj;
        int id = block.getInteger("packed_id");
        int meta = block.getInteger("packed_meta");
        NBTTagCompound data = block.getCompoundTag("packed_data");
        ItemStack fakeStack = new ItemStack(id, 1, meta);

        if (!world.setBlock(x, y, z, id, meta, 3))
        {
            return false;
        }

        if (world.getBlockId(x, y, z) == id)
        {
            Block.blocksList[id].onBlockPlacedBy(world, x, y, z, player,
                                                 fakeStack);
            Block.blocksList[id].onPostBlockPlaced(world, x, y, z, meta);

            if (data != null) {
                TileEntity te = TileEntity.createAndLoadEntity(data);
                world.setBlockTileEntity(x, y, z, te);
            }
        }

        return true;
    }


    // --- Stuff stored in the player's NBT ---

    // Fetches the special tag that's guaranteed to survive player respawns,
    // including dimensional teleports.
    public static NBTTagCompound getPersistedTag(EntityPlayer player) {
        NBTTagCompound player_data = player.getEntityData();
        if (!player_data.hasKey(player.PERSISTED_NBT_TAG)) {
            player_data.setCompoundTag(player.PERSISTED_NBT_TAG, new NBTTagCompound());
        }
        return player_data.getCompoundTag(player.PERSISTED_NBT_TAG);
    }

    // Returns the tag for the block the player is currently holding, if any.
    @SuppressWarnings("unchecked")
    public static NBTTagCompound getBlockHeld(EntityPlayer player) {
        NBTTagCompound player_persisted = getPersistedTag(player);
        if (!player_persisted.hasKey(HELD_TAG)) {
            return null;
        } else {
            return player_persisted.getCompoundTag(HELD_TAG);
        }
    }

    // Is the player currently holding a block?
    public static boolean isHoldingBlock(EntityPlayer player) {
        try {
            return player.getDataWatcher().getWatchableObjectByte(DW_INDEX) != 0;
        } catch (NullPointerException e) {
            return false;
        }
    }

    // As isHoldingBlock, but for the local player.
    @SideOnly(Side.CLIENT)
    public static boolean amIHoldingABlock() {
        return isHoldingBlock(Minecraft.getMinecraft().thePlayer);
    }

    // Sets the block the player is currently holding.
    public static void setBlockHeld(EntityPlayer player, NBTTagCompound block) {
        NBTTagCompound player_persisted = getPersistedTag(player);
        player_persisted.setCompoundTag(HELD_TAG, block);
        player.getDataWatcher().updateObject(27, new Byte((byte)1));
    }

    // Remove the stored data after the player set down (or otherwise returned
    // to the world) the block they were holding.
    public static void clearBlockHeld(EntityPlayer player) {
        NBTTagCompound player_persisted = getPersistedTag(player);
        if (player_persisted.hasKey(HELD_TAG)) {
            player_persisted.removeTag(HELD_TAG);
        }
        player.getDataWatcher().updateObject(27, new Byte((byte)0));
    }

    public static class EventListener {
        // This is called when the player right clicks on a block.
        // (Or a couple other conditions, but we ignore them.)
        //
        // We use it to handle picking up and placing blocks under normal
        // conditions.
        //
        // Set to high priority just in case someone else is mucking with right
        // clicks.  We've got specific enough circumstances that we require that
        // it's safer to get ours out of the way before anyone else pokes at it.
        // Highest priority is left free, just in case someone *really* needs to
        // override us.
        @ForgeSubscribe(priority=EventPriority.HIGH)
        public void onInteract(PlayerInteractEvent event) {
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR
             || event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
             || event.isCanceled())
            {
                // We can ignore this.
                return;
            }

            if (event.entityPlayer.worldObj.isRemote) {
                return;
            }

            NBTTagCompound block_held = getBlockHeld(event.entityPlayer);
            if (block_held != null) {
                // Try to place the block.
                tryToPlace(block_held, event.entityPlayer, event.x, event.y, event.z,
                           event.face, !event.entityPlayer.isSneaking());

                // Prevent any further processing.
                event.setCanceled(true);
                return;
            }

            if (event.entityPlayer.getHeldItem() != null) {
                // They're holding an item, so let them place/use it.
                return;
            }

            if (!event.entityPlayer.isSneaking()) {
                // They're not sneaking, so let htem interact/place normally.
                return;
            }

            // The player has right-clicked on a block, their hand is empty, and
            // they are sneaking.  Check to see if the block can be picked up.
            int id = event.entityPlayer.worldObj.getBlockId(event.x, event.y, event.z);
            int meta = event.entityPlayer.worldObj.getBlockMetadata(event.x, event.y, event.z);
            if (onWhitelist(id, meta)) {
                // Valid block, PickItUp.
                pickUpBlock(event.entityPlayer, event.x, event.y, event.z);

                // Prevent any further processing.
                if (!event.entityPlayer.worldObj.isRemote) {
                    event.setCanceled(true);
                }
                return;
            }
        }

        // This is called whenever an entity (including the player) dies.
        //
        // We use it to force the held block to be placed in world before
        // the player's items drop.
        @ForgeSubscribe
        public void onDeath(LivingDeathEvent event) {
            if (!(event.entity instanceof EntityPlayer)) {
                return;
            }

            EntityPlayer player = (EntityPlayer) event.entity;

            // If keepInventory is true, you can hold a block through a respawn.
            if (player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
                return;
            }


            NBTTagCompound block_held = getBlockHeld(player);
            if (block_held != null) {
                forcePlace(block_held, player);
            }
        }


        // This is called whenever damage is inflicted on an entity.
        //
        // We use it to reduce attack damage while holding a block.
        //
        // Low priority is used here to come after any mods that set the damage
        // directly, as we're applying a 50% damage penalty.
        @ForgeSubscribe(priority=EventPriority.LOW)
        @SuppressWarnings("unchecked")
        public void onDamage(LivingHurtEvent event) {
            if (!event.source.damageType.equals("player")) {
                return;
            }

            EntityPlayer player = (EntityPlayer) ((EntityDamageSource) event.source).getEntity();
            // ammount [sic]
            if (isHoldingBlock(player) && event.ammount > 0) {
                event.ammount /= 2;
                if (event.ammount == 0) {
                    event.ammount = 1;
                }
            }
        }

        // This is called to determine how quickly a player can break a block.
        //
        // We use it to make block-breaking with a held block slow.
        //
        // Low priority is used here to come after any mods that set the speed
        // directly, as we're applying a 50% speed penalty.
        @ForgeSubscribe(priority=EventPriority.LOW)
        public void digSpeed(PlayerEvent.BreakSpeed event) {
            if (isHoldingBlock(event.entityPlayer)) {
                event.newSpeed /= 2.0f;
            }
        }
    }
}
