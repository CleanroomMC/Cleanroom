package com.cleanroommc.kirino.ecs.component.scan.helper;

import com.cleanroommc.kirino.ecs.component.scan.ComponentRegisterPlan;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;

import java.util.*;

/**
 * Scan component classes and generate a list of register plans.
 * There is no class loading involved at all.
 *
 * @see com.cleanroommc.kirino.ecs.CleanECSRuntime
 */
public final class ComponentScanningHelper {
    private ComponentScanningHelper() {
    }

    private static List<FieldInfo> getValidFields(ClassInfo componentClassInfo, FieldRegistry fieldRegistry) {
        List<FieldInfo> fields = new ArrayList<>();

        for (FieldInfo field : componentClassInfo.getFieldInfo()) {
            String fieldClassName = field.getTypeDescriptor().toString();
            if (fieldRegistry.fieldTypeExists_ClassName(fieldClassName)) {
                fields.add(field);
            }
        }

        fields.sort(Comparator.comparing(FieldInfo::getName));

        return fields;
    }

    private static List<ComponentRegisterPlan> generatePlans(Map<String, List<FieldInfo>> components, FieldRegistry fieldRegistry) {
        List<ComponentRegisterPlan> plans = new ArrayList<>();

        Map<String, Integer> componentNameDuplicates = new HashMap<>();
        Map<String, String> componentNames = new HashMap<>();

        for (Map.Entry<String, List<FieldInfo>> entry : components.entrySet()) {
            String componentName = ClassScanUtils.getClassSimpleName(entry.getKey());
            int duplicate = 0;
            if (componentNameDuplicates.containsKey(componentName)) {
                duplicate = componentNameDuplicates.get(componentName);
                componentNameDuplicates.put(componentName, duplicate + 1);
            } else {
                componentNameDuplicates.put(componentName, 1);
            }
            if (duplicate > 0) {
                componentName = componentName + "$" + duplicate;
            }
            componentNames.put(entry.getKey(), componentName);
        }

        for (Map.Entry<String, List<FieldInfo>> entry : components.entrySet()) {
            String componentClass = entry.getKey();
            String componentName = componentNames.get(entry.getKey());
            MemberLayout memberLayout = new MemberLayout(entry.getValue().stream().map(FieldInfo::getName).toList());
            String[] fieldTypeNames = new String[entry.getValue().size()];
            for (int i = 0; i < fieldTypeNames.length; i++) {
                String fieldClassName = entry.getValue().get(i).getTypeDescriptor().toString();
                fieldTypeNames[i] = fieldRegistry.getFieldTypeName_ClassName(fieldClassName);
            }

            plans.add(new ComponentRegisterPlan(componentName, componentClass, memberLayout, fieldTypeNames));
        }

        return plans;
    }

    /**
     * Retrieve a list of component register plans.
     *
     * @param event The component scanning event
     * @param fieldRegistry The field registry
     * @return A list of component register plans
     */
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public static List<ComponentRegisterPlan> scanComponentClasses(ComponentScanningEvent event, FieldRegistry fieldRegistry) {
        Map<String, ClassInfo> allClassInfos = ClassScanUtils.scan(
                (List<String>) ReflectionUtils.getFieldValue(ReflectionUtils.findDeclaredField(ComponentScanningEvent.class, "scanPackageNames"), event),
                "com.cleanroommc.kirino.ecs.component.scan.CleanComponent");

        Map<String, List<FieldInfo>> components = new TreeMap<>();
        for (Map.Entry<String, ClassInfo> entry : allClassInfos.entrySet()) {
            if (entry.getValue().implementsInterface("com.cleanroommc.kirino.ecs.component.ICleanComponent")) {
                components.put(entry.getKey(), getValidFields(entry.getValue(), fieldRegistry));
            }
        }

        return generatePlans(components, fieldRegistry);
    }
}
