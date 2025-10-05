/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.common;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.util.TextTable;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.FMLThrowingEventBus;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.relauncher.MixinBooterPlugin;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.FormattedMessage;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.ModUtil;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.Proxy;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;
import org.spongepowered.asm.util.Constants;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.ILateMixinLoader;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadController
{
    private Loader loader;
    private EventBus masterChannel;
    private ImmutableMap<String, EventBus> eventChannels;
    private LoaderState state;
    private Multimap<String, ModState> modStates = MultimapBuilder.hashKeys().enumSetValues(ModState.class).build();
    private List<ModContainer> activeModList = Lists.newArrayList();
    private ModContainer activeContainer;
    private BiMap<ModContainer, Object> modObjectList;
    private ListMultimap<String, ModContainer> packageOwners;
    private boolean closeRequested = false;

    public LoadController(Loader loader)
    {
        this.loader = loader;
        this.masterChannel = new FMLThrowingEventBus((exception, context) -> {
            Throwables.throwIfUnchecked(exception);
            // should not happen, but add some extra context for checked exceptions
            Method method = context.getSubscriberMethod();
            String parameterNames = Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", "));
            String message = "Exception thrown during LoadController." + method.getName() + '(' + parameterNames + ')';
            throw new LoaderExceptionModCrash(message, exception);
        });
        this.masterChannel.register(this);

        state = LoaderState.NOINIT;
        packageOwners = ArrayListMultimap.create();

    }

    void disableMod(ModContainer mod)
    {
        HashMap<String, EventBus> temporary = Maps.newHashMap(eventChannels);
        String modId = mod.getModId();
        EventBus bus = temporary.remove(modId);
        bus.post(new FMLModDisabledEvent());
        eventChannels = ImmutableMap.copyOf(temporary);
        modStates.put(modId, ModState.DISABLED);
        modObjectList.remove(mod);
        activeModList.remove(mod);
    }

    @Subscribe
    public void buildModList(FMLLoadEvent event)
    {
        Builder<String, EventBus> eventBus = ImmutableMap.builder();

        for (final ModContainer mod : loader.getModList())
        {
            EventBus bus = new FMLThrowingEventBus((exception, context) -> this.errorOccurred(mod, exception));

            boolean isActive = mod.registerBus(bus, this);
            if (isActive)
            {
                activeModList.add(mod);
                modStates.put(mod.getModId(), ModState.LOADED);
                eventBus.put(mod.getModId(), bus);
                FMLCommonHandler.instance().addModToResourcePack(mod);
            }
            else
            {
                FMLLog.log.warn("Mod {} has been disabled through configuration", mod.getModId());
                modStates.put(mod.getModId(), ModState.UNLOADED);
                modStates.put(mod.getModId(), ModState.DISABLED);
            }
        }

        eventChannels = eventBus.build();
    }

    public void distributeStateMessage(LoaderState state, Object... eventData)
    {
        if (state.hasEvent())
        {
            if (state == LoaderState.CONSTRUCTING) { // This state is where Forge adds mod files to ModClassLoader

                ModClassLoader modClassLoader = (ModClassLoader) eventData[0];
                ASMDataTable asmDataTable = (ASMDataTable) eventData[1];

                try {

                    // Add mods into the delegated ModClassLoader
                    for (ModContainer container : this.loader.getActiveModList()) {
                        modClassLoader.addFile(container.getSource());
                    }
                    // Handle Mixin Configs and non-mod libraries
                    File mods_ver = new File(new File(Launch.minecraftHome, "mods"), ForgeVersion.mcVersion);
                    for (ModContainer container : this.loader.getActiveModList()) {
                        try (JarFile jarFile = new JarFile(container.getSource())) {
                            Attributes mfAttributes = jarFile.getManifest() == null ? null : jarFile.getManifest().getMainAttributes();
                            if (mfAttributes != null) {
                                String configs = mfAttributes.getValue(Constants.ManifestAttributes.MIXINCONFIGS);
                                if (!Strings.isNullOrEmpty(configs)) {
                                    Mixins.addConfigurations(configs.split(","));
                                }
                                boolean containNonMods = Boolean.parseBoolean(mfAttributes.getValue("NonModDeps"));
                                if (containNonMods) {
                                    for (String file: mfAttributes.getValue(LibraryManager.MODCONTAINSDEPS).split(" ")) {
                                        modClassLoader.addFile(new File(mods_ver, file));
                                    }
                                }
                            }
                        } catch (IOException ignored) {}
                    }

                    FMLContextQuery.init(); // Initialize FMLContextQuery and add it to the global list


                    // Load late mixins
                    FMLLog.log.info("Instantiating all ILateMixinLoader implemented classes...");
                    for (ASMDataTable.ASMData asmData : asmDataTable.getAll(ILateMixinLoader.class)) {
                        try {
                            modClassLoader.addFile(asmData.getCandidate().getModContainer()); // Add to path before `newInstance`
                            Class<?> clazz = Class.forName(asmData.getClassName().replace('/', '.'));
                            FMLLog.log.info("Instantiating {} for its mixins.", clazz);
                            @SuppressWarnings("deprecation")
                            ILateMixinLoader loader = (ILateMixinLoader) clazz.getConstructor().newInstance();
                            for (String mixinConfig : loader.getMixinConfigs()) {
                                @SuppressWarnings("deprecation")
                                Context context = new Context(mixinConfig);
                                if (loader.shouldMixinConfigQueue(context)) {
                                    try {
                                        FMLLog.log.info("Adding {} mixin configuration.", mixinConfig);
                                        Mixins.addConfiguration(mixinConfig);
                                        loader.onMixinConfigQueued(context);
                                    } catch (Throwable t) {
                                        FMLLog.log.error("Error adding mixin configuration for {}", mixinConfig, t);
                                    }
                                }
                            }
                        } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
                            FMLLog.log.error("Unable to load the ILateMixinLoader", e);
                        }
                    }

                    // mark config owners : for earlys, lates, and mfAttributes.
                    for (Config config : Mixins.getConfigs()) {
                        if (!config.getConfig().hasDecoration(ModUtil.OWNER_DECORATOR)) {
                            String pkg = config.getConfig().getMixinPackage();
                            pkg = pkg.charAt(pkg.length() - 1) == '.' ? pkg.substring(0, pkg.length() - 1) : pkg;
                            List<ModContainer> owners = getPackageOwners(pkg);
                            if (owners.isEmpty()) {
                                config.getConfig().decorate(ModUtil.OWNER_DECORATOR, (Supplier) () -> ModUtil.UNKNOWN_OWNER);
                            } else {
                                final String owner = owners.get(0).getModId(); // better assign ?
                                config.getConfig().decorate(ModUtil.OWNER_DECORATOR, (Supplier) () -> owner);
                            }
                        }
                    }

                    for (ModContainer container : this.loader.getActiveModList()) {
                        modClassLoader.addFile(container.getSource());
                    }
                } catch (Throwable t) {
                    FMLLog.log.error("Error loading Mods", t);
                }
                if (MixinService.getService() instanceof MixinServiceLaunchWrapper) {
                    ((MixinServiceLaunchWrapper) MixinService.getService()).setDelegatedTransformers(null);
                }
                MixinEnvironment current = MixinEnvironment.getCurrentEnvironment();

                Proxy.transformer.processor.selectConfigs(current);
                Proxy.transformer.processor.prepareConfigs(current, Proxy.transformer.processor.extensions);

            }
            MixinEnvironment.gotoPhase(MixinEnvironment.Phase.MOD);
            masterChannel.post(state.getEvent(eventData));
        }
    }

    public void transition(LoaderState desiredState, boolean forceState)
    {
        if (!closeRequested && FMLCommonHandler.instance().isDisplayCloseRequested())
        {
            closeRequested = true;
            FMLLog.log.info("The game window is being closed by the player, exiting.");
            if (state.ordinal() <= LoaderState.AVAILABLE.ordinal()) {
                FMLCommonHandler.instance().exitJava(0, false);
            }
        }

        LoaderState oldState = state;
        state = state.transition(false);
        if (state != desiredState)
        {
            if (!forceState)
            {
                FormattedMessage message = new FormattedMessage("A fatal error occurred during the state transition from {} to {}. State became {} instead. Loading cannot continue.", oldState, desiredState, state);
                throw new LoaderException(message.getFormattedMessage());
            }
            else
            {
                FMLLog.log.info("The state engine was in incorrect state {} and forced into state {}. Errors may have been discarded.", state, desiredState);
                forceState(desiredState);
            }
        }
    }

    @Deprecated // TODO remove in 1.13
    public void checkErrorsAfterAvailable()
    {
    }

    @Deprecated // TODO remove in 1.13
    public void checkErrors()
    {
    }

    @Nullable
    public ModContainer activeContainer()
    {
        return activeContainer != null ? activeContainer : findActiveContainerFromStack();
    }

    void forceActiveContainer(@Nullable ModContainer container)
    {
        activeContainer = container;
    }

    @Subscribe
    public void propogateStateMessage(FMLEvent stateEvent)
    {
        if (stateEvent instanceof FMLPreInitializationEvent)
        {
            modObjectList = buildModObjectList();
        }
        ProgressBar bar = ProgressManager.push(stateEvent.description(), activeModList.size(), true);
        for (ModContainer mc : activeModList)
        {
            bar.step(mc.getName());
            sendEventToModContainer(stateEvent, mc);
        }
        ProgressManager.pop(bar);
    }

    private void sendEventToModContainer(FMLEvent stateEvent, ModContainer mc)
    {
        String modId = mc.getModId();
        Collection<String> requirements = mc.getRequirements().stream().map(ArtifactVersion::getLabel).collect(Collectors.toCollection(HashSet::new));
        for (ArtifactVersion av : mc.getDependencies())
        {
            if (av.getLabel() != null && requirements.contains(av.getLabel()) && modStates.containsEntry(av.getLabel(), ModState.ERRORED))
            {
                FMLLog.log.error("Skipping event {} and marking errored mod {} since required dependency {} has errored", stateEvent.getEventType(), modId, av.getLabel());
                modStates.put(modId, ModState.ERRORED);
                return;
            }
        }
        activeContainer = mc;
        stateEvent.applyModContainer(mc);
        ThreadContext.put("mod", modId);
        FMLLog.log.trace("Sending event {} to mod {}", stateEvent.getEventType(), modId);
        eventChannels.get(modId).post(stateEvent);
        FMLLog.log.trace("Sent event {} to mod {}", stateEvent.getEventType(), modId);
        ThreadContext.remove("mod");
        activeContainer = null;
        if (stateEvent instanceof FMLStateEvent)
        {
            modStates.put(modId, ((FMLStateEvent) stateEvent).getModState());
        }
    }

    public ImmutableBiMap<ModContainer, Object> buildModObjectList()
    {
        ImmutableBiMap.Builder<ModContainer, Object> builder = ImmutableBiMap.builder();
        for (ModContainer mc : activeModList)
        {
            if (!mc.isImmutable() && mc.getMod() != null)
            {
                builder.put(mc, mc.getMod());
                List<String> packages = mc.getOwnedPackages();
                for (String pkg : packages)
                {
                    packageOwners.put(pkg, mc);
                }
            }
            if (mc.getMod() == null && !mc.isImmutable() && state != LoaderState.CONSTRUCTING)
            {
                FormattedMessage message = new FormattedMessage("There is a severe problem with {} ({}) - it appears not to have constructed correctly", mc.getName(), mc.getModId());
                this.errorOccurred(mc, new RuntimeException(message.getFormattedMessage()));
            }
        }
        return builder.build();
    }

    public void errorOccurred(ModContainer modContainer, Throwable exception)
    {
        String modId = modContainer.getModId();
        String modName = modContainer.getName();
        modStates.put(modId, ModState.ERRORED);
        if (exception instanceof InvocationTargetException)
        {
            exception = exception.getCause();
        }
        if (exception instanceof LoaderException) // avoid wrapping loader exceptions multiple times
        {
            throw (LoaderException) exception;
        }
        FormattedMessage message = new FormattedMessage("Caught exception from {} ({})", modName, modId);
        throw new LoaderExceptionModCrash(message.getFormattedMessage(), exception);
    }

    public void printModStates(StringBuilder ret)
    {
        ret.append("\n\tStates:");
        for (ModState state : ModState.values())
            ret.append(" '").append(state.getMarker()).append("' = ").append(state.toString());

        TextTable table = new TextTable(Lists.newArrayList(
            TextTable.column("State"),
            TextTable.column("ID"),
            TextTable.column("Version"),
            TextTable.column("Source"),
            TextTable.column("Signature"))
        );
        for (ModContainer mc : loader.getModList())
        {
            table.add(
                modStates.get(mc.getModId()).stream().map(ModState::getMarker).reduce("", (a, b) -> a + b),
                mc.getModId(),
                mc.getVersion(),
                mc.getSource().getName(),
                mc.getSigningCertificate() != null ? CertificateHelper.getFingerprint(mc.getSigningCertificate()) : "None"
            );
        }

        ret.append("\n");
        ret.append("\n\t");
        table.append(ret, "\n\t");
        ret.append("\n");
    }

    public List<ModContainer> getActiveModList()
    {
        return activeModList;
    }

    public ModState getModState(ModContainer selectedMod)
    {
        return Iterables.getLast(modStates.get(selectedMod.getModId()), ModState.AVAILABLE);
    }

    public void distributeStateMessage(Class<?> customEvent)
    {
        Object eventInstance;
        try
        {
            eventInstance = customEvent.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new LoaderException("Failed to create new event instance for " + customEvent.getName(), e);
        }
        masterChannel.post(eventInstance);
    }

    public BiMap<ModContainer, Object> getModObjectList()
    {
        if (modObjectList == null)
        {
            FMLLog.log.fatal("Detected an attempt by a mod {} to perform game activity during mod construction. This is a serious programming error.", activeContainer);
            return buildModObjectList();
        }
        return ImmutableBiMap.copyOf(modObjectList);
    }

    public boolean isInState(LoaderState state)
    {
        return this.state == state;
    }

    boolean hasReachedState(LoaderState state)
    {
        return this.state.ordinal() >= state.ordinal() && this.state != LoaderState.ERRORED;
    }

    void forceState(LoaderState newState)
    {
        this.state = newState;
    }

    @Nullable
    private ModContainer findActiveContainerFromStack()
    {
        return StackWalker.getInstance()
                .walk(frames -> frames.map(StackWalker.StackFrame::getClassName)
                        .filter(name -> name.lastIndexOf('.') != -1)
                        .map(name -> packageOwners.get(name.substring(0, name.lastIndexOf('.'))))
                        .filter(l -> !l.isEmpty())
                        .findFirst()
                        .map(List::getFirst)
                        .orElse(null)
                );
    }

    @Nullable
    public List<ModContainer> getPackageOwners(String pkg)
    {
        return packageOwners.get(pkg);
    }

    LoaderState getState()
    {
        return state;
    }
}
