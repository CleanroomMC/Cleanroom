package com.cleanroommc.utils.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ListIterator;
import java.util.function.Predicate;

public class ASMUtil {
    public static void injectBefore(InsnList method, InsnList hook, Predicate<AbstractInsnNode> predicate){
        ListIterator<AbstractInsnNode> iterator=method.iterator();
        AbstractInsnNode node;
        while (iterator.hasNext()){
            node=iterator.next();
            if (predicate.test(node)){
                iterator.remove();
                ListIterator<AbstractInsnNode> hookIterator=hook.iterator();
                while (hookIterator.hasNext()){
                    iterator.add(hookIterator.next());
                }
                iterator.add(node);
            }
        }
    }
    public static void injectAfter(InsnList method, InsnList hook, Predicate<AbstractInsnNode> predicate){
        ListIterator<AbstractInsnNode> iterator=method.iterator();
        AbstractInsnNode node;
        while (iterator.hasNext()){
            node=iterator.next();
            if (predicate.test(node)){
                ListIterator<AbstractInsnNode> hookIterator=hook.iterator();
                while (hookIterator.hasNext()){
                    iterator.add(hookIterator.next());
                }
            }
        }
    }
    public static void redirect(InsnList method, InsnList hook, Predicate<AbstractInsnNode> predicate){
        ListIterator<AbstractInsnNode> iterator=method.iterator();
        AbstractInsnNode node;
        while (iterator.hasNext()){
            node=iterator.next();
            if (predicate.test(node)){
                iterator.remove();
                ListIterator<AbstractInsnNode> hookIterator=hook.iterator();
                while (hookIterator.hasNext()){
                    iterator.add(hookIterator.next());
                }
            }
        }
    }
}
