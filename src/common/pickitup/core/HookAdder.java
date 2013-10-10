package pickitup.core;

import pickitup.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

@SuppressWarnings("unchecked")
public class HookAdder extends AdviceAdapter {
    public Hook hook;

    public HookAdder(Hook which_hook, MethodVisitor delegate, int access, String name, String desc) {
        super(Opcodes.ASM4, delegate, access, name, desc);
        hook = which_hook;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        try {
            Class client_proxy = ClientProxy.class;

            /* We're changing this code:
            this.sneak = this.gameSettings.keyBindSneak.pressed;

            into this:
            this.sneak = this.gameSettings.keyBindSneak.pressed || ClientProxy.shouldForceSneak();
            */
            if (hook == Hook.FORCE_SNEAK && opcode == PUTFIELD &&
                owner.equals("%CL:net/minecraft/util/MovementInputFromOptions%") &&
                "%FD:net/minecraft/util/MovementInput/sneak%".endsWith("/" + name)) {
                invokeStatic(Type.getType(client_proxy),
                             new Method("shouldForceSneak",
                                        "()Z"));
                math(OR, Type.BOOLEAN_TYPE);
                System.out.println("Hook added!");
            }
        } catch (Exception e) {
            System.out.println("PickItUp: HOOK FAILED!");
            e.printStackTrace();
        }

        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        try {
            Class client_proxy = ClientProxy.class;
            Class common_proxy = CommonProxy.class;

            if (hook == Hook.DROP_HELD_BLOCK) {
                /* We're adding this code:
                CommonProxy.onPlayerDrop(this);
                */
                loadThis();
                invokeStatic(Type.getType(common_proxy),
                             new Method("onPlayerDrop",
                                        "(L%CL:net/minecraft/entity/player/EntityPlayer%;)V"));
                System.out.println("Hook added!");
            } else if (hook == Hook.RENDER_HELD_BLOCK_B) {
                /* We're adding this code:
                ClientProxy.renderHeldBlockFallback(par1, par2);
                */
                loadArgs(0, 2); // our own arguments #1-2
                invokeStatic(Type.getType(client_proxy),
                             new Method("renderHeldBlockFallback",
                                        "(ID)V"));
            }
        } catch (Exception e) {
            System.out.println("PickItUp: HOOK FAILED!");
            e.printStackTrace();
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        try {
            Class client_proxy = ClientProxy.class;
            Class common_proxy = CommonProxy.class;

            if (hook == Hook.RENDER_HELD_BLOCK_A) {
                /* We're adding this code:
                ClientProxy.renderHeldBlock(par3, par4);
                */
                loadArgs(2, 2); // our own arguments #3-4
                invokeStatic(Type.getType(client_proxy),
                             new Method("renderHeldBlock",
                                        "(ID)V"));
                // Increment the return value.
                push(1);
                math(ADD, Type.INT_TYPE);
                System.out.println("Hook added!");
            } else if (hook == Hook.INIT_PLAYER) {
                loadThis();
                invokeStatic(Type.getType(common_proxy),
                             new Method("initPlayer",
                                        "(L%CL:net/minecraft/entity/player/EntityPlayer%;)V"));
            }
        } catch (Exception e) {
            System.out.println("PickItUp: HOOK FAILED!");
            e.printStackTrace();
        }
    }
}
