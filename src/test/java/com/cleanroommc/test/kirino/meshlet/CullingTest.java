package com.cleanroommc.test.kirino.meshlet;

import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.component.MeshletComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;
import org.junit.Before;

import java.util.List;

public class CullingTest {

    private List<MeshletAABBPair> meshlets = new ObjectArrayList<>();

    @Before
    public void setup() {
        for (int i = 0; i < 36; i++) {
            float multiplier = i % 2 == 0 ? 0.25f : 1.75f;
            Vector3f baseAABBPosition = new Vector3f((float) Math.sin(multiplier*10*i), (float) Math.cos(multiplier*10*i), 0.f);
            Vector3f baseMeshletPoint = new Vector3f((float) Math.sin(10*i), (float) Math.cos(10*i), 0.f);
            MeshletComponent component = new MeshletComponent();
            component.transparent = false;
            component.aabb = new AABB(baseMeshletPoint.x, baseMeshletPoint.y, baseMeshletPoint.z, baseMeshletPoint.x+ 3.f, baseMeshletPoint.y + 3.f, baseMeshletPoint.z + 3.f);
            AABB entity = new AABB(baseAABBPosition.x, baseAABBPosition.y, baseAABBPosition.z, baseAABBPosition.x+ 1.f, baseAABBPosition.y + 1.f, baseAABBPosition.z + 1.f);
            meshlets.add(new MeshletAABBPair(component, entity, i % 2 == 0));
        }
    }

    private record MeshletAABBPair(MeshletComponent meshlet, AABB aabb, boolean expectedCullingResult) {}
}
