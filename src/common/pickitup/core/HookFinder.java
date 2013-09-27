package pickitup.core;

import java.io.*;
import java.util.*;

import cpw.mods.fml.relauncher.*;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class HookFinder extends ClassVisitor implements IClassTransformer {
    // The master table of classes and their hooks.
    public Map<String, Map<String, Hook>> class_table;

    // The current class's hooks.
    public Map<String, Hook> hook_table = null;

    public ClassWriter writer = null;

    // net.minecraft.util.MovementInput.sneak
    public static final String sneak = "%conf:OBF_SNEAK%";
    // net.minecraft.util.MovementInputFromOptions
    public static final String mifo_class = "%conf:OBF_MIFO%";
    // void MovementInputFromOptions.updatePlayerMoveState()
    public static final String update_move = "%conf:OBF_UPDATE_MOVE%()V";

    // net.minecraft.client.renderer.EntityRenderer
    public static final String entity_renderer_class = "%conf:OBF_ENTITYRENDERER%";
    //void EntityRenderer.renderHand
    public static final String render_hand = "%conf:OBF_RENDER_HAND%(FI)V";

    public HookFinder() {
        super(Opcodes.ASM4);

        class_table = new HashMap<String, Map<String, Hook>>();


        Map<String, Hook> mifo = new HashMap<String, Hook>();
        mifo.put(update_move, Hook.SNEAK);
        class_table.put(mifo_class, mifo);

        Map<String, Hook> er = new HashMap<String, Hook>();
        er.put(render_hand, Hook.RENDER);
        class_table.put(entity_renderer_class, er);
    }

    public byte[] transform(String name, String transformedName, byte[] bytes) {
        hook_table = class_table.get(name);

        if (hook_table == null) {
            return bytes;
        }

        try {
            ClassReader reader = new ClassReader(bytes);
            writer = new ClassWriter(reader, 0);

            reader.accept(this, ClassReader.EXPAND_FRAMES);

            byte[] output = writer.toByteArray();

            /*
            FileOutputStream fos = new FileOutputStream("test-" + name + ".class");
            fos.write(output);
            fos.close();
            */

            return output;
        } catch (Exception e) {
            System.out.println("PickItUp: Class transform FAILED!");
            e.printStackTrace();

            return bytes;
        }
    }

    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        System.out.println("PickItUp: Recognized class " + name + ".");

        writer.visit(version, access, name, signature, superName, interfaces);
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return writer.visitAnnotation(desc, visible);
    }

    public void visitAttribute(Attribute attr) {
        writer.visitAttribute(attr);
    }

    public void visitEnd() {
        hook_table = null;
        writer.visitEnd();
    }

    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        return writer.visitField(access, name, desc, signature, value);
    }

    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
        writer.visitInnerClass(name, outerName, innerName, access);
    }

    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor write_it = writer.visitMethod(access, name, desc,
                                                    signature, exceptions);

        if (hook_table != null) {
            Hook hook = hook_table.get(name + desc);
            if (hook != null) {
                System.out.println("PickItUp: Adding hook " + hook + ".");
                try {
                    return new HookAdder(hook, write_it, access, name, desc);
                } catch (Exception e) {
                    System.out.println("PickItUp: HOOK FAILED!");
                    e.printStackTrace();
                }
            }
        }

        return write_it;
    }

    public void visitOuterClass(String owner, String name, String desc) {
        writer.visitOuterClass(owner, name, desc);
    }

    public void visitSource(String source, String debug) {
        writer.visitSource(source, debug);
    }
}
