package com.cleanroommc.cleanmix.service;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.service.IClassTracker;
import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.trie.PrefixTrie;
import top.outlands.foundation.trie.TrieNode;

final class FoundationClassTracker implements IClassTracker {

    private final ActualClassLoader classLoader;

    FoundationClassTracker() {
        this.classLoader = Launch.classLoader;
    }

    @Override
    public boolean isClassLoaded(String name) {
        return this.classLoader.isClassLoaded(name);
    }

    @Override
    public String getClassRestrictions(String className) {
        String restrictions = "";
        if (isExcluded(ActualClassLoader.classLoaderExceptions, className)) {
            restrictions = "PACKAGE_CLASSLOADER_EXCLUSION";
        }
        if (isExcluded(ActualClassLoader.transformerExceptions, className)) {
            restrictions = (!restrictions.isEmpty() ? restrictions + "," : "") + "PACKAGE_TRANSFORMER_EXCLUSION";
        }
        return restrictions;
    }

    @Override
    public void registerInvalidClass(String name) {
        this.classLoader.getInvalidClasses().add(name);
    }

    /** Whether the name or transformedName is excluded from the classloader or from the transformer chain. */
    boolean isClassExcluded(String name, String transformedName) {
        return isExcluded(ActualClassLoader.classLoaderExceptions, name, transformedName)
                || isExcluded(ActualClassLoader.transformerExceptions, name, transformedName);
    }

    private static boolean isExcluded(PrefixTrie<Boolean> trie, String name, String transformedName) {
        return isExcluded(trie, name) || (transformedName != null && isExcluded(trie, transformedName));
    }

    /**
     * An exclusion can be revoked by re-putting it as {@code false} (see
     * {@link ActualClassLoader#removeTransformerExclusion}), so the node's value decides, not its presence.
     */
    private static boolean isExcluded(PrefixTrie<Boolean> trie, String name) {
        TrieNode<Boolean> node = trie.getFirstKeyValueNode(name);
        return node != null && Boolean.TRUE.equals(node.getValue());
    }

}
