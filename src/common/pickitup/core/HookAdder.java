package pickitup.core;

import pickitup.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

@SuppressWarnings("unchecked")
public class HookAdder extends AdviceAdapter {
    public int hook; 
    public static final int HOOK_SNEAK = 0;

    public HookAdder(int which_hook, MethodVisitor delegate, int access, String name, String desc) {
        super(Opcodes.ASM4, delegate, access, name, desc);
        hook = which_hook;
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        try {
            Class client_proxy = ClientProxy.class;

            /* We're changing this code:
            this.sneak = this.gameSettings.keyBindSneak.pressed;

            into this:
            this.sneak = this.gameSettings.keyBindSneak.pressed || PickItUp.amIHoldingABlock();
            */
            if (hook == HOOK_SNEAK && opcode == PUTFIELD
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

    protected void onMethodEnter() {
        try {
            // No hooks here yet.  :)
        } catch (Exception e) {
            System.out.println("PickItUp: HOOK FAILED!");
            e.printStackTrace();
        }
    }
}
