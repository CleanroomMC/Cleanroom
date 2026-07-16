package com.cleanroommc.hackery;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public class Reflection {

    public Reflection() {
    }


    public static Class<?> getCallerClass(){
        return StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 1).walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass).skip(1).findFirst()).get();
    }


    public static Class<?> getCallerClass(int var0){
        return StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), var0 + 1).walk(s -> s.map(StackWalker.StackFrame::getDeclaringClass).skip(var0 + 1).findFirst()).get();
    }

    public static int getClassAccessFlags(Class<?> var0){return var0.getModifiers();}

    public static boolean quickCheckMemberAccess(Class<?> var0, int var1) {
        return false;
    }

    public static void ensureMemberAccess(Class<?> var0, Class<?> var1, Object var2, int var3) throws IllegalAccessException {}

    public static boolean verifyMemberAccess(Class<?> var0, Class<?> var1, Object var2, int var3) {
        return false;
    }


    static boolean isSubclassOf(Class<?> var0, Class<?> var1) {
        return false;
    }

    public static synchronized void registerFieldsToFilter(Class<?> var0, String... var1) {}

    public static synchronized void registerMethodsToFilter(Class<?> var0, String... var1) {}

    public static Field[] filterFields(Class<?> var0, Field[] var1) {
        return null;
    }

    public static Method[] filterMethods(Class<?> var0, Method[] var1) {
        return null;
    }


    public static boolean isCallerSensitive(Method var0) {
        return false;
    }

}