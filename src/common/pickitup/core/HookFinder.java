package pickitup.core;

import java.io.*;
import java.util.*;

import cpw.mods.fml.relauncher.*;
import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class HookFinder implements IClassTransformer {
    // The master table of classes and their hooks.
    public Map<String, Map<String, Hook>> class_table;

    public HookFinder() {
        class_table = new HashMap<String, Map<String, Hook>>();

        for (Hook hook : Hook.all()) {
            if (!class_table.containsKey(hook.targetClass)) {
                class_table.put(hook.targetClass, new HashMap<String, Hook>());
            }

            class_table.get(hook.targetClass).put(hook.targetMethod, hook);
        }
    }

    public byte[] transform(String name, String transformedName, byte[] bytes) {
        Map<String, Hook> hook_table = class_table.get(name);

        if (hook_table == null) {
            return bytes;
        }

        try {
            ClassReader reader = new ClassReader(bytes);
            ClassWriter writer = new ClassWriter(reader, 0);

            reader.accept(new Visitor(hook_table, writer),
                          ClassReader.EXPAND_FRAMES);

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

    public class Visitor extends ClassVisitor {
        // The current class's hooks.
        public final Map<String, Hook> hook_table;

        public final ClassWriter writer;

        public Visitor(Map<String, Hook> hook_table, ClassWriter writer) {
            super(Opcodes.ASM4);

            this.hook_table = hook_table;
            this.writer = writer;
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
                Hook hook = hook_table.get(name + " " + desc);
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
}
