package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.command.DrawQueue;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.gl.debug.KHRDebug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RenderPass {
    private final Map<String, Subpass> subpassMap = new HashMap<>();
    private final Map<String, List<ISubpassDecorator>> subpassDecoratorMap = new HashMap<>();
    private final List<String> subpassOrder = new ArrayList<>();
    private final DrawQueue drawQueue;

    public final String passName;

    public RenderPass(GraphicResourceManager graphicResourceManager, String passName) {
        this.passName = passName;
        drawQueue = new DrawQueue(graphicResourceManager);
    }

    public boolean hasSubpass(String subpassName) {
        return subpassMap.containsKey(subpassName);
    }

    public void addSubpass(String subpassName, Subpass subpass) {
        if (subpassMap.containsKey(subpassName)) {
            return;
        }
        subpassMap.put(subpassName, subpass);
        subpassOrder.add(subpassName);
    }

    public void removeSubpass(String subpassName) {
        subpassMap.remove(subpassName);
        subpassOrder.remove(subpassName);
    }

    public void attachSubpassDecorator(String subpassName, ISubpassDecorator decorator) {
        List<ISubpassDecorator> list = subpassDecoratorMap.computeIfAbsent(subpassName, k -> new ArrayList<>());
        list.add(decorator);
    }

    public void render(ICamera camera) {
        for (String subpassName : subpassOrder) {
            KHRDebug.pushGroup(passName + " - " + subpassName);

            drawQueue.clear();
            Subpass subpass = subpassMap.get(subpassName);
            subpass.collectCommands(drawQueue);
            List<ISubpassDecorator> list = subpassDecoratorMap.get(subpassName);
            if (list != null) {
                for (ISubpassDecorator decorator : list) {
                    subpass.decorateCommands(drawQueue, decorator);
                }
            }
            subpass.render(drawQueue, camera);

            KHRDebug.popGroup();
        }
    }
}
