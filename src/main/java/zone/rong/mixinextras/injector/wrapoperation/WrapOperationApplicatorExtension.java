package zone.rong.mixinextras.injector.wrapoperation;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This extension is responsible for actually injecting all `@WrapOperation`s which were queued up during the normal
 * injection phase. Applying them here means we are guaranteed to run after every other injector, which is crucial.
 */
public class WrapOperationApplicatorExtension implements IExtension {
  static Map<ITargetClassContext, List<WrapOperationInjectionInfo>> QUEUED_INJECTIONS = Collections.synchronizedMap(new WeakHashMap<>());

  @Override
  public boolean checkActive(MixinEnvironment environment) {
    return true;
  }

  @Override
  public void preApply(ITargetClassContext context) {
  }

  @Override
  public void postApply(ITargetClassContext context) {
    List<WrapOperationInjectionInfo> queuedInjections = QUEUED_INJECTIONS.get(context);
    if (queuedInjections != null) {
      for (WrapOperationInjectionInfo injection : queuedInjections) {
        injection.lateApply();
      }
    }
  }

  @Override
  public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
  }
}