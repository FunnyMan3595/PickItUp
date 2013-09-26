package pickitup;

import net.minecraft.entity.player.EntityPlayer;

// This interface allows you to extend or override the behaviour of PickItUp to
// handle a specific block correctly.  Note that only one ICanBePickedUp or
// ISimplePickup will be called for any given pickup, so registering duplicates
// or wildcard handlers is highly inadviasable.
//
// This is the simple version, intended for blocks like signs and torches that
// just need a little tweaking after they're placed in the world, so they're
// actually mounted on the correct surface.  For more control over the entire
// pickup/putdown operation, use ICanBePickedUp.
public interface ISimplePickup {
    // If this class is added as a handler via IMC, it MUST define a
    // constructor with no parameters.
    //public ThisClass();

    // This should return true for the block type(s) that this class covers.
    public boolean handlesPickupOf(int id, int meta);

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
    public void afterPutdown(EntityPlayer player, int x, int y, int z, int face);
}
