package com.cleanroommc.cleanmix.service;

import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.IClassTransformer;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.ILegacyClassTransformer;
import org.spongepowered.asm.service.ITransformer;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.mojang.LegacyTransformerHandle;
import top.outlands.foundation.TransformerDelegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class FoundationTransformerProvider implements ITransformerProvider {

    /**
     * Forge's re-entrant transformers, which must never process meta class data when bytecode is fetched for a
     * mixin target, else resolving the target re-enters their pipeline. Other re-entrants are detected and
     * excluded automatically at runtime via the re-entrance lock.
     */
    private static final Set<String> REENTRANT_EXCLUSIONS = Sets.newHashSet(
            "net.minecraftforge.fml.common.asm.transformers.TerminalTransformer"
    );

    private final Set<String> excludeTransformers = new HashSet<>(REENTRANT_EXCLUSIONS);

    private List<ILegacyClassTransformer> delegatedTransformers;
    private int previousTransformerCount = -1;

    void refreshDelegatedTransformers() {
        this.delegatedTransformers = null;
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        List<IClassTransformer> transformers = TransformerDelegate.getTransformers();
        List<ITransformer> result = new ArrayList<>(transformers.size());
        for (IClassTransformer transformer : transformers) {
            if (transformer instanceof ITransformer) {
                result.add((ITransformer) transformer);
            } else {
                result.add(new LegacyTransformerHandle(transformer));
            }
        }
        return result;
    }

    @Override
    public List<ITransformer> getDelegatedTransformers() {
        return Collections.unmodifiableList(this.getDelegatedLegacyTransformers());
    }

    @Override
    public void addTransformerExclusion(String name) {
        this.excludeTransformers.add(name);
        this.delegatedTransformers = null;
    }

    List<ILegacyClassTransformer> getDelegatedLegacyTransformers() {
        int transformerCount = TransformerDelegate.getTransformers().size();
        if (this.delegatedTransformers == null || this.previousTransformerCount != transformerCount) {
            this.buildTransformerDelegationList();
            this.previousTransformerCount = transformerCount;
        }
        return this.delegatedTransformers;
    }

    private void buildTransformerDelegationList() {
        ILogger logger = MixinService.getService().getLogger("TransformerProvider");
        logger.debug("Rebuilding transformer delegation list:");
        this.delegatedTransformers = new ArrayList<>();
        for (ITransformer transformer : this.getTransformers()) {
            if (!(transformer instanceof ILegacyClassTransformer legacyTransformer)) {
                continue;
            }
            String transformerName = legacyTransformer.getName();
            boolean include = true;
            for (String excludeClass : this.excludeTransformers) {
                if (transformerName.contains(excludeClass)) {
                    include = false;
                    break;
                }
            }
            if (include && !legacyTransformer.isDelegationExcluded()) {
                logger.debug("  Adding:    {}", transformerName);
                this.delegatedTransformers.add(legacyTransformer);
            } else {
                logger.debug("  Excluding: {}", transformerName);
            }
        }
        logger.debug("Transformer delegation list created with {} entries", this.delegatedTransformers.size());
    }

}
