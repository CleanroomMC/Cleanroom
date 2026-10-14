/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.compatibility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Stand-in for the removed {@code sun.reflect.Reflection}.
 * {@link CompatibilityTransformer} redirects calls here with their original names and descriptors.
 */
@SuppressWarnings("unused")
public final class SunReflection {

    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private SunReflection() { }

    public static Class<?> getCallerClass() {
        // Java 8 returned the caller of the method that called this. The delegation adds one frame.
        return getCallerClass(3);
    }

    // Frame 0 is this class, as it was for sun.reflect.Reflection.
    public static Class<?> getCallerClass(int depth) {
        return WALKER.walk(s -> s.skip(depth).findFirst()).map(StackWalker.StackFrame::getDeclaringClass).orElse(null);
    }

    public static int getClassAccessFlags(Class<?> clazz) {
        return clazz.getModifiers();
    }

    public static boolean quickCheckMemberAccess(Class<?> memberClass, int modifiers) {
        return Modifier.isPublic(getClassAccessFlags(memberClass) & modifiers);
    }

    public static void ensureMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target, int modifiers)
        throws IllegalAccessException {
        if (!verifyMemberAccess(currentClass, memberClass, target, modifiers)) {
            throw new IllegalAccessException("Class " + currentClass.getName() + " can not access a member of class " +
                    memberClass.getName() + " with modifiers \"" + Modifier.toString(modifiers) + "\""
            );
        }
    }

    // Java 8's rules, which predate modules and nestmates.
    public static boolean verifyMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target, int modifiers
    ) {
        if (currentClass == memberClass) {
            return true;
        }
        boolean samePackage = isSamePackage(currentClass, memberClass);
        if (!Modifier.isPublic(getClassAccessFlags(memberClass)) && !samePackage) {
            return false;
        }
        if (Modifier.isPublic(modifiers)) {
            return true;
        }
        boolean isProtected = Modifier.isProtected(modifiers);
        if (!(isProtected && isSubclassOf(currentClass, memberClass)) &&
            (Modifier.isPrivate(modifiers) || !samePackage)) {
            return false;
        }

        // A protected member reached through a subclass also needs the target to be of the caller's own type.
        return !isProtected ||
            target == null ||
            samePackage ||
            target.getClass() == currentClass ||
            isSubclassOf(target.getClass(), currentClass);
    }

    private static boolean isSamePackage(Class<?> a, Class<?> b) {
        return a.getClassLoader() == b.getClassLoader() && a.getPackageName().equals(b.getPackageName());
    }

    private static boolean isSubclassOf(Class<?> clazz, Class<?> superclass) {
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            if (current == superclass) {
                return true;
            }
        }
        return false;
    }

    public static synchronized void registerFieldsToFilter(Class<?> containingClass, String... fieldNames) { }

    public static synchronized void registerMethodsToFilter(Class<?> containingClass, String... methodNames) { }

    public static Field[] filterFields(Class<?> containingClass, Field[] fields) {
        return fields;
    }

    public static Method[] filterMethods(Class<?> containingClass, Method[] methods) {
        return methods;
    }

    public static boolean isCallerSensitive(Method method) {
        return false;
    }

}
