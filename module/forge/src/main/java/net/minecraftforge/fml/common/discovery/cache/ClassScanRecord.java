package net.minecraftforge.fml.common.discovery.cache;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;

public record ClassScanRecord(String internalName, int classVersion, String[] interfaces, Annotation[] annotations) {

    public record Annotation(
            String annotationName,
            @Nullable String member,
            @Nullable Map<String, Object> values) {}

    public String className() {
        return internalName.replace('/', '.');
    }

    public void sendToTable(ModCandidate candidate, ASMDataTable table) {
        String className = className();
        for (Annotation annotation : annotations) {
            table.addASMData(
                    candidate, annotation.annotationName(), className, annotation.member(), annotation.values());
        }
        for (String intf : interfaces) {
            table.addASMData(candidate, intf, internalName, null, null);
        }
    }
}
