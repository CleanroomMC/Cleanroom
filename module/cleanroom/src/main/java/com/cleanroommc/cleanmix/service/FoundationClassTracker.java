/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanmix.service;

import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.trie.PrefixTrie;
import top.outlands.foundation.trie.TrieNode;

import org.spongepowered.asm.service.IClassTracker;

import net.minecraft.launchwrapper.Launch;

final class FoundationClassTracker implements IClassTracker {

    private final ActualClassLoader classLoader;

    FoundationClassTracker() {
        this.classLoader = Launch.classLoader;
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
        return isExcluded(ActualClassLoader.classLoaderExceptions, name, transformedName) ||
            isExcluded(ActualClassLoader.transformerExceptions, name, transformedName);
    }

}
