package pickitup;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;

// This interface allows you to extend or override the behaviour of PickItUp to
// handle a specific block correctly.  Note that only one ICanBePickedUp or
// ISimplePickup will be called for any given pickup, so registering duplicates
// or wildcard handlers is highly inadviasable.
//
// A picked up block is represented by an NBTTagCompund with this format:
// {
//     packed_id: int,
//     packed_meta: int,
//     packed_data: NBTTagCompound
// }
//
// By default, packed_data will be the tile entity's NBT representation,
// identical to what would be stored on world save.  The format is completely
// arbitrary, however, and a class that implements custom pickup and putdown
// methods is free to put any data it likes into this compound tag.  You can
// also safely add extra data in afterPickup and remove it in putdown (and
// return false).
//
// Note that any other fields on the root tag will probably not be preserved
// properly.  Keep your data in packed_data if you need any at all.
public interface ICanBePickedUp extends ISimplePickup {
    // Inherited from ISimplePickup
    // If this class is added as a handler via IMC, it MUST define a
    // constructor with no parameters.
    //public ThisClass();

    // Inherited from ISimplePickup
    // This should return true for the block type(s) that this class covers.
    //public boolean handlesPickupOf(int id, int meta);

    // Called when the block is about to be picked up.  If this returns false,
    // the pickup is cancelled.
    //
    // Note that this method is only called if the standard checks have already
    // passed.  You don't need to worry about the whitelist, what the player
    // is holding, and so on.
    public boolean allowPickup(EntityPlayer player, int x, int y, int z);

    // This method allows you to specify custom pickup behaviour.  Very few
    // blocks will actually need this, because all the proper hooks are called
    // on block removal, but it's here just in case.
    //
    // If this method returns null, the standard pickup method will be called.
    // This can be used to implement "just before pickup" hooks.
    //
    // Note that the tag is expected to be in standard format.  See the top of
    // this file for details.
    public NBTTagCompound pickup(EntityPlayer player, int x, int y, int z);

    // Called after the block has been picked up.  The pickedUp tag is in
    // standard format.  See the top of this file for details.
    public void afterPickup(EntityPlayer player, int x, int y, int z, NBTTagCompound pickedUp);

    // This is called to determine whether the block can be put down at the
    // given location, by a player clicking on the given face.  Note that the
    // face is relative to the block clicked on:
    //
    // 0: -Y side of block (placing on the ceiling)
    // 1: +Y side of block (placing on the floor)
    // 2: -Z side of block
    // 3: +Z side of block
    // 4: -X side of block
    // 5: +X side of block
    //
    // Face can also be -1, this corresponds to a forced placement attempt, as
    // when the player dies.  This method should return true if there is any
    // way to place this block at the specified coordinates.
    //
    // Return values of true or false will override the default detection.
    // You may also return null, allowing the default detection to decide.
    //
    // Note that this DOES NOT override the check to see if the player can edit
    // blocks.  This is a Good Thing, because it means PickItUp will always
    // obey world protection.
    public Boolean allowPutdown(EntityPlayer player, int x, int y, int z, int face, NBTTagCompound pickedUp);

    // This method allows you to specify custom putdown behaviour.  Very few
    // blocks will actually need this, because all the proper hooks are called
    // on block placement, but it's here just in case.
    //
    // face is as described for allowPutdown.
    //
    // The given tag will be in standard format.  See the top of this file
    // for details.
    //
    // If this method returns false, the standard pickup method will be called.
    // This can be used to implement "just before putdown" hooks.
    public boolean putdown(EntityPlayer player, int x, int y, int z, int face, NBTTagCompound pickedUp);

    // Inherited from ISimplePickup
    // This is called after the block has been fully situated in the world.
    // It can be used to rotate blocks into the correct orientation, for
    // example.
    //
    // Note that the face is relative to the block clicked on:
    // 0: -Y side of block (placing on the ceiling)
    // 1: +Y side of block (placing on the floor)
    // 2: -Z side of block
    // 3: +Z side of block
    // 4: -X side of block
    // 5: +X side of block
    //
    // Face can also be -1, this corresponds to a forced placement, as
    // when the player dies.  If this isn't a valid location, turning the block
    // back into an item is acceptable.  Valuable blocks should implement
    // ICanBePickedUp's allowPutdown method instead, to ensure they aren't
    // destroyed.
    //public void afterPutdown(EntityPlayer player, int x, int y, int z, int face);
}

/*
    // For comvenience, here is a complete no-op implementation of all the
    // ICanBePickedUp methods:

    // You probably shouldn't use this one.  It makes the class rather
    // pointless.
    public boolean handlesPickupOf(int id, int meta) { return false; }

    // You need a zero-parameter constructor if you want to register with IMC.
    //public ThisClass() {}
    // No additional constraints on picking up this block.
    public boolean allowPickup(EntityPlayer player, int x, int y, int z) { return true; }

    // Use the standard pickup method.
    public NBTTagCompound pickup(EntityPlayer player, int x, int y, int z) { return null; }

    // Nothing to do after pickup.
    public void afterPickup(EntityPlayer player, int x, int y, int z, NBTTagCompound pickedUp) {}

    // Use the standard methods to determine if this block can be placed here.
    public Boolean allowPutdown(EntityPlayer player, int x, int y, int z, int face, NBTTagCompound pickedUp) { return null; }

    // Use the standard putdown method.
    public boolean putdown(EntityPlayer player, int x, int y, int z, int face, NBTTagCompound pickedUp) { return false; }

    // Nothing to do after putdown.
    public void afterPutdown(EntityPlayer player, int x, int y, int z, int face) { }
*/
