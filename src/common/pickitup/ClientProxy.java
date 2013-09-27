package pickitup;

public class ClientProxy extends CommonProxy {
    // Called from the coremod's Hook.FORCE_SNEAK.
    public static boolean amIHoldingABlock() {
        return PickItUp.amIHoldingABlock();
    }

    // Called from the coremod's Hook.RENDER_HELD_BLOCK.
    public static void renderHeldBlock(float partialTick) {
        FakeWorld.renderHeldBlock(partialTick);
    }
}
