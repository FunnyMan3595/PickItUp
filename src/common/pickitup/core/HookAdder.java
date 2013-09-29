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
            this.sneak = this.gameSettings.keyBindSneak.pressed || ClientProxy.amIHoldingABlock();
            */
            if (hook == Hook.FORCE_SNEAK && opcode == PUTFIELD
                                         && owner.equals(HookFinder.mifo_class)
                                         && name.equals(HookFinder.sneak)) {
                invokeStatic(Type.getType(client_proxy),
                             new Method("amIHoldingABlock",
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
                                        "(L" + HookFinder.entity_player_class
                                             + ";)V"));
                System.out.println("Hook added!");
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

            if (hook == Hook.RENDER_HELD_BLOCK) {
                /* We're adding this code:
                ClientProxy.renderHeldBlock(par1);
                */
                loadArgs(2, 2); // our own arguments #3-4
                invokeStatic(Type.getType(client_proxy),
                             new Method("renderHeldBlock",
                                        "(ID)V"));
                System.out.println("Hook added!");
            }
        } catch (Exception e) {
            System.out.println("PickItUp: HOOK FAILED!");
            e.printStackTrace();
        }
    }
}
