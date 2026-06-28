package com.cleanroommc.cleanmix;

import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CleanMixHooks {

    public static String addMixinMetadataToCrashReport(Throwable throwable) {
        Map<String, ClassInfo> classes = new LinkedHashMap<>();
        while (throwable != null) {
            if (throwable instanceof NoClassDefFoundError) {
                ClassInfo classInfo = ClassInfo.fromCache(throwable.getMessage());
                if (classInfo != null) {
                    classes.put(throwable.getMessage(), classInfo);
                }
            }
            StackTraceElement[] stacktrace = throwable.getStackTrace();
            for (StackTraceElement stackTraceElement : stacktrace) {
                String className = stackTraceElement.getClassName().replace('.', '/');
                if (classes.containsKey(className)) {
                    ClassInfo classInfo = ClassInfo.fromCache(className);
                    while (classInfo != null) {
                        classes.put(className, classInfo);
                        className = classInfo.getSuperName();
                        if (className == null || className.isEmpty() || "java/lang/Object".equals(className)) {
                            break;
                        }
                        classInfo = classInfo.getSuperClass();
                    }
                }
            }
            throwable = throwable.getCause();
        }
        if (classes.isEmpty()) {
            return "\nNo Mixin Metadata is found in the Stacktrace.\n";
        } else {
            StringBuilder mixinMetadataBuilder = new StringBuilder("\nMixins in Stacktrace:");
            boolean addedMetadata = false;
            for (Map.Entry<String, ClassInfo> entry : classes.entrySet()) {
                addedMetadata |= findAndAddMixinMetadata(mixinMetadataBuilder, entry.getKey(), entry.getValue());
            }
            if (addedMetadata) {
                return mixinMetadataBuilder.toString();
            } else {
                return "\nNo Mixin Metadata is found in the Stacktrace.\n";
            }
        }
    }

    private static boolean findAndAddMixinMetadata(StringBuilder mixinMetadataBuilder, String className, ClassInfo classInfo) {
        Set<IMixinInfo> mixinInfos = classInfo.getMixins();
        if (!mixinInfos.isEmpty()) {
            mixinMetadataBuilder.append("\n\t");
            mixinMetadataBuilder.append(className);
            mixinMetadataBuilder.append(':');
            for (IMixinInfo mixinInfo : mixinInfos) {
                mixinMetadataBuilder.append("\n\t\t");
                mixinMetadataBuilder.append(mixinInfo.getClassName());
                mixinMetadataBuilder.append(" (");
                mixinMetadataBuilder.append(mixinInfo.getConfig());
                mixinMetadataBuilder.append(") [");
                mixinMetadataBuilder.append(mixinInfo.getConfig().getCleanSourceId());
                mixinMetadataBuilder.append("]");
            }
            return true;
        }
        return false;
    }

}
