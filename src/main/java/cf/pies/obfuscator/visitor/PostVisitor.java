package cf.pies.obfuscator.visitor;

import cf.pies.obfuscator.Obfuscator;
import org.objectweb.asm.*;

import java.util.Arrays;

public class PostVisitor extends ClassVisitor {
    private final Obfuscator obfuscator;
    private final String className;

    public PostVisitor(Obfuscator obfuscator, ClassWriter writer, String className) {
        super(Opcodes.ASM6, writer);
        this.obfuscator = obfuscator;
        this.className = className;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, obfuscator.fieldRenameContext.get(className, name), desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String methodDescription, String methodSignature, String[] methodExceptions) {
        String obfuscateName = obfuscator.methodRenameContext.get(className, methodName);
        MethodVisitor visitor = super.visitMethod(access, obfuscateName, methodDescription, methodSignature, methodExceptions);

        return new MethodVisitor(this.api, visitor) {
            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (!obfuscator.classes.contains(owner)) {
                    super.visitFieldInsn(opcode, owner, name, desc);
                    return;
                }
                String newName = obfuscator.fieldRenameContext.get(owner, name);
                super.visitFieldInsn(opcode, owner, newName != null ? newName : name, desc);
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                System.out.println(name + " " + desc);
                Handle handle = (Handle) bsmArgs[1];
                Handle newHandle = new Handle(
                        handle.getTag(),
                        handle.getOwner(),
                        obfuscator.methodRenameContext.get(handle.getOwner(), handle.getName()),
                        handle.getDesc()
                );
                bsmArgs[1] = newHandle;

                super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                if (!obfuscator.classes.contains(owner)) {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    return;
                }
                String newName = obfuscator.methodRenameContext.get(owner, name);
                super.visitMethodInsn(opcode, owner, newName, desc, itf);
            }
        };
    }
}
