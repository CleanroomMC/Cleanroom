package com.cleanroommc.event;

import com.cleanroommc.bouncepad.Bouncepad;
import com.cleanroommc.utils.FileUtil;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * The Event Bus used for early event.
 * For example: Transform event, Config event, mod load event...
 * Because the design. You need to register instances to subscribe the events.
 * for example:
 * <pre>
 *     @ Subscribe
 *     public void listen(Event event){
 *         //...
 *     }
 * </pre>
**/
public class EarlyBus {
    public static final EventBus BUS=new EventBus("cleanroom_early_bus");
    @SuppressWarnings("all")
    private static void register(){
        Method m_register;
        try{
            Class<?> clazz=Bouncepad.classLoader.findClass("net.minecraftforge.fml.relauncher.IEarlyBusSubscribePlugin");
            m_register= clazz.getDeclaredMethod("addToBus");
        } catch (ClassNotFoundException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        List<File> file_canidates = LibraryManager.gatherLegacyCanidates((File)FMLInjectionData.data()[6]);
        List<String> listeners=new LinkedList<>();
        for(File modSource:file_canidates){
            byte[] fileByte =FileUtil.getFile(modSource,"META-INF/earlybus.cfg");
            for(String rawLine:new String(fileByte).split("\n")){
                String line=rawLine.trim();
                if (!line.isEmpty()){
                    listeners.add(line);
                }
            }
        }
        for(String clazzName:listeners){
            try {
                Class<?> clazz=Bouncepad.classLoader.findClass(clazzName);
                Object instance=clazz.getDeclaredConstructor(new Class[0]).newInstance();
                m_register.invoke(instance,BUS);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                FMLLog.log.error(e);
            }
        }
    }
    static {
        register();
    }
}
