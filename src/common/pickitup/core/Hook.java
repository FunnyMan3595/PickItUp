package pickitup.core;

public enum Hook {
    // void MovementInputFromOptions.updatePlayerMoveState()
    FORCE_SNEAK("%MD:net/minecraft/util/MovementInputFromOptions/updatePlayerMoveState ()V%"),
    // int RenderGlobal.renderSortedRenderers(int, int, int, double)
    RENDER_HELD_BLOCK("%MD:net/minecraft/client/renderer/RenderGlobal/renderSortedRenderers (IIID)I%"),
    // EntityItem EntityPlayer.dropOneItem(boolean)
    DROP_HELD_BLOCK("%MD:net/minecraft/entity/player/EntityPlayer/dropOneItem (Z)Lnet/minecraft/entity/item/EntityItem;%"),
    // void Entity.entityInit()
    INIT_PLAYER("%MD:net/minecraft/entity/player/EntityPlayer/entityInit ()V%");

    public final String targetClass;
    public final String targetMethod;

    private Hook(String method) {
        // Example: net/minecraft/util/MovementInputFromOptions/updatePlayerMoveState ()V
        int split_index = method.lastIndexOf("/");
        targetClass     = method.substring(0, split_index);
        targetMethod    = method.substring(split_index + 1);
    }

    public static Hook[] all() {
        return Hook.class.getEnumConstants();
    }
}
