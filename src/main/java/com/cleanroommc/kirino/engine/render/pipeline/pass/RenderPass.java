package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferManager;
import com.cleanroommc.kirino.gl.debug.KHRDebug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RenderPass {
    private final Map<String, Subpass> subpassMap = new HashMap<>();
    private final Map<String, List<ISubpassDecorator>> subpassDecoratorMap = new HashMap<>();
    private final List<String> subpassOrder = new ArrayList<>();
    private final IndirectDrawBufferManager idbManager;

    public final String passName;

    public RenderPass(String passName, IndirectDrawBufferManager idbManager) {
        this.passName = passName;
        this.idbManager = idbManager;
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

            Subpass subpass = subpassMap.get(subpassName);
            subpass.drawQueue.clear();
            subpass.collectCommands(subpass.drawQueue);
            List<ISubpassDecorator> list = subpassDecoratorMap.get(subpassName);
            if (list != null) {
                for (ISubpassDecorator decorator : list) {
                    subpass.decorateCommands(subpass.drawQueue, decorator);
                }
            }
            subpass.render(subpass.drawQueue, camera, idbManager);

            KHRDebug.popGroup();
        }
    }
}
