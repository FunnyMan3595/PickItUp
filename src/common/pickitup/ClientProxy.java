package pickitup;

public class ClientProxy extends CommonProxy {
    public static boolean amIHoldingABlock() {
        return PickItUp.amIHoldingABlock();
    }

    public static void renderHeldBlock(float partialTick) {
        FakeWorld.renderHeldBlock(partialTick);
    }
}
