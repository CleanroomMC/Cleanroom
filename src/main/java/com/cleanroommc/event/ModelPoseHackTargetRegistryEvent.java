package com.cleanroommc.event;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.LinkedList;
import java.util.List;

/**
 * Subscribe this event in {@link IFMLLoadingPlugin#Object()}
 **/
public class ModelPoseHackTargetRegistryEvent {
    private List<String> targets=new LinkedList<>();
    public void add(String target){
        targets.add(target);
    }

    public List<String> getTargets() {
        return targets;
    }
}
