package pickitup;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class ClientProxy extends CommonProxy {
    // Called from the coremod's Hook.FORCE_SNEAK.
    public static boolean amIHoldingABlock() {
        return PickItUp.amIHoldingABlock();
    }

    // Called from the coremod's Hook.RENDER_HELD_BLOCK_A.
    public static void renderHeldBlock(int render_pass, double partialTick) {
        if (render_pass == 1) {
            FakeWorld.renderHeldBlock(partialTick);
        }
    }

    // Called from the coremod's Hook.RENDER_HELD_BLOCK_B.
    // This is a fallback hook that we only use for the one path we can't hit
    // any other way: Fancy graphics without OptiFine.
    public static void renderHeldBlockFallback(int render_pass, double partialTick) {
        if (Minecraft.getMinecraft().gameSettings.fancyGraphics) {
            renderHeldBlock(render_pass, partialTick);
        }
    }

    public Vec3 getPlayerPos(EntityPlayer player, float partialTick) {
        Minecraft mc = Minecraft.getMinecraft();
        if (player == mc.thePlayer) {
            return player.getPosition(partialTick);
        }

        return super.getPlayerPos(player, partialTick);
    }

    public double getReach(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        return (double)mc.playerController.getBlockReachDistance();
    }
}
