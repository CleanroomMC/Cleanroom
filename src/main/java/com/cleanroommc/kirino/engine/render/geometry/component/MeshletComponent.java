package com.cleanroommc.kirino.engine.render.geometry.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;
import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.google.common.base.Preconditions;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

@CleanComponent
public class MeshletComponent implements ICleanComponent {
    public AABB aabb;
    public Vector3f normal;
    public boolean transparent;
    public Block block1;
    public Block block2;
    public Block block3;
    public Block block4;
    public Block block5;
    public Block block6;
    public Block block7;
    public Block block8;
    public Block block9;
    public Block block10;
    public Block block11;
    public Block block12;
    public Block block13;
    public Block block14;
    public Block block15;
    public Block block16;
    public Block block17;
    public Block block18;
    public Block block19;
    public Block block20;
    public Block block21;
    public Block block22;
    public Block block23;
    public Block block24;
    public Block block25;
    public Block block26;
    public Block block27;
    public Block block28;
    public Block block29;
    public Block block30;
    public Block block31;
    public Block block32;

    public static boolean cull(@NonNull MeshletComponent meshlet,
                               @NonNull AABB aabb,
                               @NonNull ICamera camera) {
        Preconditions.checkNotNull(meshlet);
        Preconditions.checkNotNull(aabb);
        Preconditions.checkNotNull(camera);

        if (meshlet.transparent) {
            return false;
        }

        final Matrix4f transformation = camera.getViewRotationMatrix().mul(camera.getProjectionMatrix());

        Vector3f direction = new Vector3f();
        Vector4f tmp = new Vector4f();

        // Bounding box points
        Vector3f[] points = {
                // Meshlet
                new Vector3f(meshlet.aabb.xMin, meshlet.aabb.yMin, meshlet.aabb.zMin),
                new Vector3f(meshlet.aabb.xMax, meshlet.aabb.yMin, meshlet.aabb.zMin),
                new Vector3f(meshlet.aabb.xMin, meshlet.aabb.yMin, meshlet.aabb.zMax),
                new Vector3f(meshlet.aabb.xMax, meshlet.aabb.yMin, meshlet.aabb.zMax),
                new Vector3f(meshlet.aabb.xMin, meshlet.aabb.yMax, meshlet.aabb.zMin),
                new Vector3f(meshlet.aabb.xMax, meshlet.aabb.yMax, meshlet.aabb.zMin),
                new Vector3f(meshlet.aabb.xMin, meshlet.aabb.yMax, meshlet.aabb.zMax),
                new Vector3f(meshlet.aabb.xMax, meshlet.aabb.yMax, meshlet.aabb.zMax),
                // AABB
                new Vector3f(aabb.xMin, aabb.yMin, aabb.zMin),
                new Vector3f(aabb.xMax, aabb.yMin, aabb.zMin),
                new Vector3f(aabb.xMin, aabb.yMin, aabb.zMax),
                new Vector3f(aabb.xMax, aabb.yMin, aabb.zMax),
                new Vector3f(aabb.xMin, aabb.yMax, aabb.zMin),
                new Vector3f(aabb.xMax, aabb.yMax, aabb.zMin),
                new Vector3f(aabb.xMin, aabb.yMax, aabb.zMax),
                new Vector3f(aabb.xMax, aabb.yMax, aabb.zMax)
        };

        // Convert points to use camera origin as (0;0)
        for (int i = 0; i < points.length; i++) {
            points[i].sub(camera.getWorldOffset(), points[i]);
        }

        camera.getViewRotationMatrix().getColumn(2, tmp);
        tmp.xyz(direction);

        return true;
    }
}
