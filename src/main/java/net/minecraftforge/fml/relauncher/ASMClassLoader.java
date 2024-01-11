package net.minecraftforge.fml.relauncher;

import java.util.HashMap;

public class ASMClassLoader extends ClassLoader{
    private static final HashMap<ClassLoader,ASMClassLoader> caches=new HashMap<>();
    private ASMClassLoader(ClassLoader classLoader){
        super(classLoader);
    }
    public Class<?> define(String name, byte[] data)
    {
        return defineClass(name, data, 0, data.length);
    }
    public static ASMClassLoader getOrCreate(ClassLoader classLoader){
        if (!caches.containsKey(classLoader)){
            caches.put(classLoader,new ASMClassLoader(classLoader));
        }
        return caches.get(classLoader);
    }
    public static ASMClassLoader getOrCreate(){
        return getOrCreate(ASMClassLoader.class.getClassLoader());
    }
    public static void clearCaches(){
        caches.clear();
    }
    public static void clearCaches(ClassLoader classLoader){
        caches.remove(classLoader);
    }
}
