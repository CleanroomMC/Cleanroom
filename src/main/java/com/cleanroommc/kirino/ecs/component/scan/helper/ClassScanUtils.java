package com.cleanroommc.kirino.ecs.component.scan.helper;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A util class that only works for {@link ComponentScanningHelper} and {@link StructScanningHelper}
 *
 * @see ComponentScanningHelper
 * @see StructScanningHelper
 */
public final class ClassScanUtils {
    private ClassScanUtils() {
    }

    static String getClassSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot == -1 ? className : className.substring(lastDot + 1);
    }

    static Map<String, ClassInfo> scan(List<String> packages, String annotation) {
        Map<String, ClassInfo> allClassInfos = new TreeMap<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(packages.toArray(new String[0]))
                .scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(annotation)) {
                allClassInfos.put(classInfo.getName(), classInfo);
            }
        }
        return allClassInfos;
    }
}
