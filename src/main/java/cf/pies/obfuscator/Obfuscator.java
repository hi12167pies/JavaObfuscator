package cf.pies.obfuscator;

import cf.pies.obfuscator.context.MultiRenameContext;
import cf.pies.obfuscator.context.SingleRenameContext;
import cf.pies.obfuscator.visitor.PostVisitor;
import cf.pies.obfuscator.visitor.PreVisitor;
import com.sun.istack.internal.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Obfuscator {
    public final File input;
    public final File output;
    public final MultiRenameContext methodRenameContext = new MultiRenameContext("m");
    public final MultiRenameContext fieldRenameContext = new MultiRenameContext("f");
    public final SingleRenameContext classRenameContext = new SingleRenameContext("c");
    public URLClassLoader classLoader = null;
    public Set<String> classes = new HashSet<>();

    public Obfuscator(File input, File output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Starts obfuscator
     */
    public void obfuscate() throws IOException {
        JarFile jarFile = new JarFile(input);
        JarOutputStream out = new JarOutputStream(Files.newOutputStream(output.toPath()));

        preProcess(jarFile);
        postProcess(jarFile, out);

        out.close();
        jarFile.close();
        if (classLoader != null) {
            classLoader.close();
        }
    }

    /**
     * Loads a jar file for dependencies
     * @param paths Paths to jar file
     */
    public void setPaths(URL[] paths) {
        classLoader = new URLClassLoader(paths);
    }

    public @Nullable Class<?> loadClass(String className) throws ClassNotFoundException {
        String classNameDots = className.replaceAll("/", ".");
        Class<?> clazz = null;
        try {
            // Try to load using the system class loader first
            clazz = Class.forName(classNameDots);
        } catch (ClassNotFoundException e) {
            // Fallback to the custom class loader
            if (this.classLoader != null) {
                clazz = Class.forName(classNameDots, true, this.classLoader);
            }
        }
        return clazz;
    }

    private void preProcess(JarFile jarFile) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            InputStream entryInputStream = jarFile.getInputStream(entry);

            if (entry.getName().endsWith(".class")) {
                ClassReader reader = new ClassReader(entryInputStream);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

                String name = entry.getName();
                String nameWithoutExtension = name.substring(0, name.length() - 6);

//                classRenameContext.create(nameWithoutExtension);

                reader.accept(new PreVisitor(this, reader, writer, nameWithoutExtension), 0);
                classes.add(nameWithoutExtension);
            }
        }
    }

    private void postProcess(JarFile jarFile, JarOutputStream out) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            InputStream entryInputStream = jarFile.getInputStream(entry);

            if (entry.getName().endsWith("/")) continue;

            if (entry.getName().endsWith(".class")) {
                ClassReader reader = new ClassReader(entryInputStream);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

                String name = entry.getName();
                String nameWithoutExtension = name.substring(0, name.length() - 6);

                reader.accept(new PostVisitor(this, writer, nameWithoutExtension), 0);

                // Get obfuscated name
//                out.putNextEntry(new JarEntry(classRenameContext.get(nameWithoutExtension) + ".class"));
                out.putNextEntry(new JarEntry(nameWithoutExtension + ".class"));
                out.write(writer.toByteArray());
            } else {
                out.putNextEntry(new JarEntry(entry.getName()));
                copy(entryInputStream, out);
            }
        }
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
    }
}
