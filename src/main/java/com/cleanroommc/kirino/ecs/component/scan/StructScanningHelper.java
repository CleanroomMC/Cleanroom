package com.cleanroommc.kirino.ecs.component.scan;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.ScanResult;

import java.util.*;

public final class StructScanningHelper {
    private static List<FieldInfo> getValidFields(Map<String, ClassInfo> allClassInfos, ClassInfo structClassInfo, FieldRegistry fieldRegistry) {
        List<FieldInfo> fields = new ArrayList<>();

        for (FieldInfo field : structClassInfo.getFieldInfo()) {
            String fieldClassName = field.getTypeDescriptor().toString();
            if (allClassInfos.containsKey(fieldClassName) || fieldRegistry.fieldTypeExists_ClassName(fieldClassName)) {
                fields.add(field);
            }
        }

        fields.sort(Comparator.comparing(FieldInfo::getName));

        return fields;
    }

    private static void removeSelfReference(Set<String> dirtyStructs, Map<String, List<FieldInfo>> structs, String structClass, List<String> prevStructClasses, List<FieldInfo> fields) {
        if (dirtyStructs.contains(structClass)) {
            return;
        }
        dirtyStructs.add(structClass);

        List<FieldInfo> fieldsToRemove = new ArrayList<>();

        for (FieldInfo field : fields) {
            String fieldClassName = field.getTypeDescriptor().toString();
            if (prevStructClasses.contains(fieldClassName)) {
                fieldsToRemove.add(field);
                continue;
            }
            if (structs.containsKey(fieldClassName)) {
                List<String> classes = new ArrayList<>(prevStructClasses);
                classes.add(fieldClassName);
                removeSelfReference(dirtyStructs, structs, fieldClassName, classes, structs.get(fieldClassName));
            }
        }

        fields.removeAll(fieldsToRemove);
    }

    private static String getClassSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot == -1 ? className : className.substring(lastDot + 1);
    }

    private static Class<?> loadClassWithoutInit(String className) {
        try {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static List<RegisterStructPlan> generatePlans(Map<String, List<FieldInfo>> structs, FieldRegistry fieldRegistry) {
        List<RegisterStructPlan> plans = new ArrayList<>();

        Map<String, Integer> structNameDuplicates = new HashMap<>();
        Map<String, String> structNames = new HashMap<>();

        for (Map.Entry<String, List<FieldInfo>> entry : structs.entrySet()) {
            String structName = getClassSimpleName(entry.getKey());
            int duplicate = 0;
            if (structNameDuplicates.containsKey(structName)) {
                duplicate = structNameDuplicates.get(structName);
                structNameDuplicates.put(structName, duplicate + 1);
            } else {
                structNameDuplicates.put(structName, 1);
            }
            if (duplicate > 0) {
                structName = structName + "$" + duplicate;
            }
            structNames.put(entry.getKey(), structName);
        }

        for (Map.Entry<String, List<FieldInfo>> entry : structs.entrySet()) {
            // load class
            Class<?> structClass = loadClassWithoutInit(entry.getKey());

            String structName = structNames.get(entry.getKey());
            MemberLayout memberLayout = new MemberLayout(entry.getValue().stream().map(FieldInfo::getName).toList());

            List<FieldDef> fieldDefs = new ArrayList<>();
            for (FieldInfo field : entry.getValue()) {
                String fieldClassName = field.getTypeDescriptor().toString();
                if (structNames.containsKey(fieldClassName)) {
                    fieldDefs.add(new FieldDef(structNames.get(fieldClassName)));
                } else {
                    fieldDefs.add(fieldRegistry.getFieldDef(fieldRegistry.getFieldTypeName_ClassName(fieldClassName)));
                }
            }
            StructDef structDef = new StructDef(fieldDefs);

            plans.add(new RegisterStructPlan(structName, structClass, memberLayout, structDef));
        }

        return plans;
    }

    public static List<RegisterStructPlan> scanAndLoadStructClasses(StructScanningEvent event, FieldRegistry fieldRegistry) {
        Map<String, ClassInfo> allClassInfos = new TreeMap<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(event.scanPackageNames.toArray(new String[0]))
                .scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation("com.cleanroommc.kirino.ecs.component.scan.CleanStruct")) {
                allClassInfos.put(classInfo.getName(), classInfo);
            }
        }

        Map<String, List<FieldInfo>> structs = new TreeMap<>();
        for (Map.Entry<String, ClassInfo> entry : allClassInfos.entrySet()) {
            structs.put(entry.getKey(), getValidFields(allClassInfos, entry.getValue(), fieldRegistry));
        }

        Set<String> dirtyStructs = new HashSet<>();
        for (Map.Entry<String, List<FieldInfo>> entry : structs.entrySet()) {
            removeSelfReference(dirtyStructs, structs, entry.getKey(), Collections.singletonList(entry.getKey()), entry.getValue());
        }

        return generatePlans(structs, fieldRegistry);
    }
}
