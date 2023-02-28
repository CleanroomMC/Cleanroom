package zone.rong.mixinbooter.mixin;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import zone.rong.mixinbooter.MixinBooterPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Set;

@Mixin(CrashReport.class)
public class CrashReportMixin {

    @Inject(method = "getCauseStackTraceOrString", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void afterStackTracePopulation(CallbackInfoReturnable<String> cir, StringWriter stringwriter, PrintWriter printwriter, Throwable throwable) {
        StackTraceElement[] stacktrace = throwable.getStackTrace();
        if (stacktrace.length > 0) {
            try {
                StringBuilder mixinMetadataBuilder = null;
                Set<String> classes = new ObjectOpenHashSet<>();
                for (StackTraceElement stackTraceElement : stacktrace) {
                    classes.add(stackTraceElement.getClassName());
                }
                Method classInfo$getMixins;
                try {
                    classInfo$getMixins = ClassInfo.class.getDeclaredMethod("getMixins");
                    classInfo$getMixins.setAccessible(true);
                    for (String className : classes) {
                        ClassInfo classInfo = ClassInfo.fromCache(className);
                        if (classInfo != null) {
                            @SuppressWarnings("unchecked")
                            Set<IMixinInfo> mixinInfos = (Set<IMixinInfo>) classInfo$getMixins.invoke(classInfo);
                            if (!mixinInfos.isEmpty()) {
                                if (mixinMetadataBuilder == null) {
                                    mixinMetadataBuilder = new StringBuilder("\n(MixinBooter) Mixins in Stacktrace:");
                                }
                                mixinMetadataBuilder.append("\n\t");
                                mixinMetadataBuilder.append(className);
                                mixinMetadataBuilder.append(":");
                                for (IMixinInfo mixinInfo : mixinInfos) {
                                    mixinMetadataBuilder.append("\n\t\t");
                                    mixinMetadataBuilder.append(mixinInfo.getClassName());
                                    mixinMetadataBuilder.append(" (");
                                    mixinMetadataBuilder.append(mixinInfo.getConfig().getName());
                                    mixinMetadataBuilder.append(")");
                                }
                            }
                        }
                    }
                } catch (ReflectiveOperationException e) {
                    MixinBooterPlugin.LOGGER.warn("Not able to reflect ClassInfo#getMixins");
                }
                if (mixinMetadataBuilder == null) {
                    cir.setReturnValue(cir.getReturnValue() + "\nNo Mixin Metadata is found in the Stacktrace.\n");
                } else {
                    cir.setReturnValue(cir.getReturnValue() + mixinMetadataBuilder);
                }
            } catch (Throwable t) {
                cir.setReturnValue(cir.getReturnValue() + "\nFailed to find Mixin Metadata in Stacktrace:\n" + t);
            }
        }
    }

}
