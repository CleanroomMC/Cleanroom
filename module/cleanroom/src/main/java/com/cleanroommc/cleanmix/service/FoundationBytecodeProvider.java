package com.cleanroommc.cleanmix.service;

import com.google.common.io.ByteStreams;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.transformers.MixinClassReader;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;

final class FoundationBytecodeProvider implements IClassBytecodeProvider {

    private static final ClassLoader APP_CLASS_LOADER = Launch.class.getClassLoader();

    private final FoundationTransformerProvider transformerProvider;
    private final ReEntranceLock lock;
    private final FoundationClassTracker classTracker;

    FoundationBytecodeProvider(FoundationTransformerProvider transformerProvider, ReEntranceLock lock, FoundationClassTracker classTracker) {
        this.transformerProvider = transformerProvider;
        this.lock = lock;
        this.classTracker = classTracker;
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, true, ClassReader.EXPAND_FRAMES);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, runTransformers, ClassReader.EXPAND_FRAMES);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        String transformedName = name.replace('/', '.');
        String originalName = Launch.classLoader.untransformName(transformedName);
        byte[] bytes = getClassBytes(originalName, transformedName);
        if (runTransformers) {
            bytes = this.applyTransformers(originalName, transformedName, bytes);
        }
        if (bytes == null) {
            throw new ClassNotFoundException(transformedName);
        }
        ClassNode classNode = new ClassNode();
        new MixinClassReader(bytes, name).accept(classNode, readerFlags);
        return classNode;
    }

    private static byte[] getClassBytes(String name, String transformedName) throws IOException {
        byte[] classBytes = Launch.classLoader.getClassBytes(name);
        if (classBytes != null) {
            return classBytes;
        }
        try (InputStream classStream = APP_CLASS_LOADER.getResourceAsStream(transformedName.replace('.', '/').concat(".class"))) {
            return classStream != null ? ByteStreams.toByteArray(classStream) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] applyTransformers(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        if (this.classTracker.isClassExcluded(name, transformedName)) {
            return basicClass;
        }
        for (ILegacyClassTransformer legacyTransformer : this.transformerProvider.getDelegatedLegacyTransformers()) {
            this.lock.clear();
            basicClass = legacyTransformer.transformClassBytes(name, transformedName, basicClass);
            if (this.lock.isSet()) {
                this.transformerProvider.addTransformerExclusion(legacyTransformer.getName());
                this.lock.clear();
            }
        }
        return basicClass;
    }

}
