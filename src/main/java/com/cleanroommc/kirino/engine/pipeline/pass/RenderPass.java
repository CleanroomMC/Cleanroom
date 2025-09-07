package com.cleanroommc.kirino.engine.pipeline.pass;

import com.cleanroommc.kirino.gl.debug.KHRDebug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RenderPass {
    private final Map<String, Subpass> subpassMap = new HashMap<>();
    private final List<String> subpassOrder = new ArrayList<>();

    public final String passName;

    public RenderPass(String passName) {
        this.passName = passName;
    }

    public boolean hasSubpass(String subpassName) {
        return subpassMap.containsKey(subpassName);
    }

    public boolean addSubpass(String subpassName, Subpass subpass) {
        if (subpassMap.containsKey(subpassName)) {
            return false;
        }
        subpassMap.put(subpassName, subpass);
        subpassOrder.add(subpassName);
        return true;
    }

    public void removeSubpass(String subpassName) {
        subpassMap.remove(subpassName);
        subpassOrder.remove(subpassName);
    }

    public void render() {
        for (String subpassName : subpassOrder) {
            KHRDebug.pushGroup(passName + " - " + subpassName);
            subpassMap.get(subpassName).render();
            KHRDebug.popGroup();
        }
    }
}
