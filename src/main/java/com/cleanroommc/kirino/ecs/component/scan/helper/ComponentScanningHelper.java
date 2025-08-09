package com.cleanroommc.kirino.ecs.component.scan.helper;

import com.cleanroommc.kirino.ecs.component.scan.ComponentRegisterPlan;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ComponentScanningHelper {
    private ComponentScanningHelper() {
    }

    private static List<FieldInfo> getValidFields() {
        List<FieldInfo> fields = new ArrayList<>();

        return fields;
    }

    private static List<ComponentRegisterPlan> generatePlans() {
        List<ComponentRegisterPlan> plans = new ArrayList<>();

        return plans;
    }

    public static List<ComponentRegisterPlan> scanComponentClasses(ComponentScanningEvent event) {
        Map<String, ClassInfo> allClassInfos = ClassScanUtils.scan(event.scanPackageNames, "com.cleanroommc.kirino.ecs.component.scan.CleanComponent");

        return generatePlans();
    }
}
