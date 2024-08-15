package cf.pies.obfuscator.visitor;

import cf.pies.obfuscator.Obfuscator;
import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceFieldVisitor;

import java.lang.reflect.Method;

public class PreVisitor extends ClassVisitor {
    private final Obfuscator obfuscator;
    private final String className;
    private final ClassReader reader;

    public PreVisitor(Obfuscator obfuscator, ClassReader reader, ClassWriter writer, String className) {
        super(Opcodes.ASM6, writer);
        this.obfuscator = obfuscator;
        this.className = className;
        this.reader = reader;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        String newName = obfuscator.fieldRenameContext.create(className, name);
        return super.visitField(access, newName, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        // Skip constructors, static initializers, and the main method
        try {
            if (name.equals("<init>") || name.equals("<clinit>")
                    || (name.equals("main") && desc.equals("([Ljava/lang/String;)V"))
                    || name.contains("lambda$")
                    || isSuperMethod(name)) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String newName = obfuscator.methodRenameContext.create(className, name);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public boolean isSuperMethod(String name) throws ClassNotFoundException {
        Class<?> superClass = obfuscator.loadClass(reader.getSuperName());
        for (Method method : superClass.getMethods()) {
            if (method.getName().equals(name)) {
                return true;
            }
        }

        for (String interfacePath : reader.getInterfaces()) {
            Class<?> clazz = obfuscator.loadClass(interfacePath);
            if (clazz == null) continue;
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
