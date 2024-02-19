package net.minecraftforge.fml.relauncher;

import com.cleanroommc.bouncepad.Bouncepad;

import java.lang.reflect.Method;
import java.util.HashMap;
/**
 * An Public API for modder to define a new class with byte[] at running time.
 * a example usage {@link net.minecraftforge.fml.common.eventhandler.ASMEventHandler#createWrapper(Method)} r}
 * To get a ASMClassLoader, you need to call {@link ASMClassLoader#getOrCreate(ClassLoader)} or {{@link ASMClassLoader#getOrCreate()}} use {@link Bouncepad#classLoader} for default.
 * **/
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
        return getOrCreate(Bouncepad.classLoader);
    }
    public static void clearCaches(){
        caches.clear();
    }
    public static void clearCaches(ClassLoader classLoader){
        caches.remove(classLoader);
    }
}
