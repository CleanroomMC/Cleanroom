package net.minecraftforge.fml.common.eventhandler.differential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.List;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacyDispatchScenarios;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacyMetadataScenarios;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacySubscriberScenarios;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.inherited.PackageParent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyEventBusDifferentialTest {
    private static HistoricalEventBusFixture historical;

    @BeforeAll
    static void compileHistory(@TempDir Path output) throws Exception {
        HistoricalEventBusFixture.compile(output);
        historical = new HistoricalEventBusFixture(output);
    }

    @AfterAll
    static void closeHistory() throws Exception {
        if (historical != null) {
            historical.close();
        }
    }

    @Test
    void realHistoricalTransformersChangeLoadedClasses() throws Exception {
        var event = LegacyMetadataScenarios.ParameterizedEvent.class;
        assertThrows(NoSuchMethodException.class, event::getConstructor);
        var oldEvent = historical.loadClass(event.getName());
        assertSame(historical, oldEvent.getClassLoader());
        assertTrue(Modifier.isPublic(oldEvent.getConstructor().getModifiers()));

        var parameter = LegacySubscriberScenarios.CallbackEvent.class;
        assertFalse(Modifier.isPublic(PackageParent.class.getDeclaredMethod("on", parameter).getModifiers()));
        var oldParent = historical.loadClass(PackageParent.class.getName());
        var oldParameter = historical.loadClass(parameter.getName());
        assertTrue(Modifier.isPublic(oldParent.getDeclaredMethod("on", oldParameter).getModifiers()));
    }

    @Test
    void inheritedAnnotationsOnSyntheticAndBuiltinEvents() throws Throwable {
        exact(LegacyMetadataScenarios.class, "inheritedAnnotations", "cancelable=true", "result=true", "value=DENY", "post=true");
        exact(LegacyMetadataScenarios.class, "builtinAnnotations", "pre=true", "post cancelable=false",
            "config has result=true", "config result=ALLOW");
        exact(LegacyMetadataScenarios.class, "abstractChild", "cancelable=true", "has result=true", "post=true");
    }

    @Test
    void normalSuperCallsAndSameClassOverride() throws Throwable {
        exact(LegacyMetadataScenarios.class, "superChain", "second cancelable", "first cancelable", "cancelable=true",
            "second result", "first result", "has result=true", "same-class=false/false");
    }

    @Test
    void handwrittenListIdentityAndPerInstanceRouting() throws Throwable {
        exact(LegacyMetadataScenarios.class, "handwrittenListChain", "distinct=true", "shared=true",
            "registered=HIGH", "leaf", "first", "parent", "unregister", "leaf", "first", "parent");
        exact(LegacyMetadataScenarios.class, "instanceLists", "distinct=true", "registered", "other");
    }

    @Test
    void priorityThreadAffinityAndPublicListInvalidation() throws Throwable {
        exact(LegacyDispatchScenarios.class, "priorityAndInheritance", "high:HIGH", "child1:NORMAL",
            "child2:NORMAL", "parent:NORMAL", "same thread=true");
        exact(LegacyDispatchScenarios.class, "publicListInvalidation", "registered", "shared:HIGH", "shared:NORMAL",
            "other bus", "direct unregister", "shared:HIGH", "global unregister");
    }

    @Test
    void dynamicCancellationAndGenericIdentity() throws Throwable {
        exact(LegacyDispatchScenarios.class, "dynamicCancellation", "phase=HIGHEST", "cancelable=true", "canceled=false",
            "cancel", "cancelable=true", "phase=HIGH", "cancelable=true", "canceled=true", "phase=NORMAL",
            "cancelable=true", "canceled=true", "resume", "cancelable=true", "phase=LOW", "cancelable=false",
            "base callback=LOW", "cancelable=false", "post=false");
        exact(LegacyDispatchScenarios.class, "overriddenCanceledGetter", "get canceled", "get canceled", "receive",
            "get canceled", "post=true");
        exact(LegacyDispatchScenarios.class, "genericIdentityAndShortCircuit", "change=HIGH", "selector=java.lang.String",
            "typed=LOW", "matched=false", "change=HIGH", "selector=java.lang.Double", "mismatched=false",
            "change=HIGH", "canceled=true/LOW", "equal distinct selector=true", "selector=java.util.List<java.lang.String>",
            "equal selector post=false");
    }

    @Test
    void dispatchReturnAndExceptionBoundary() throws Throwable {
        exact(LegacyDispatchScenarios.class, "emptyAndShutdown", "cancelable=true", "canceled=true", "empty=true",
            "result without annotation=ALLOW", "shutdown=false");
        exact(LegacyDispatchScenarios.class, "getterExceptions", "getter thrown by identity=true",
            "null getter=NullPointerException");
        exact(LegacyDispatchScenarios.class, "dispatchExceptions", "index=0 cause=IllegalStateException",
            "identities=true/true/true", "phase thrown by identity=true", "index=1 cause=IOException",
            "identities=true/true/true", "wrapped checked=true");
    }

    @Test
    void snapshotAliasingAndReentrantRegistration() throws Throwable {
        exact(LegacyDispatchScenarios.class, "nestedRegistration", "outer", "nested", "new", "resume", "old");
        exact(LegacyDispatchScenarios.class, "mutableArray", "first:HIGH", "replacement:HIGH", "last:LOW", "first:HIGH",
            "handler index=2 cause=NullPointerException", "same bus=true same array=true", "same thrown=true");
        exact(LegacyDispatchScenarios.class, "equalRegistrationKeys", "distinct equal=true", "first", "unregistered equal");
    }

    @Test
    void subscriptionSelectionAndInvocation() throws Throwable {
        exact(LegacySubscriberScenarios.class, "staticAndInstance", "static hidden", "derived static", "static base",
            "base static", "instance", "base instance");
        exact(LegacySubscriberScenarios.class, "inheritedAnnotation", "child:HIGH");
        exact(LegacySubscriberScenarios.class, "publicizedDispatchTarget", "cross-package child:HIGH");
        exact(LegacySubscriberScenarios.class, "subscriberAccess", "package:HIGH", "protected:NORMAL");
        exact(LegacySubscriberScenarios.class, "defaultsAndDiamond", "default:HIGH", "interface annotation:NORMAL");
        exact(LegacySubscriberScenarios.class, "objectParameter", "object callback", "same event=true");
        exact(LegacySubscriberScenarios.class, "receiverBinding", "first", "second", "second");
    }

    @Test
    void contextSetterAndOwnerRestoration() throws Throwable {
        exact(LegacySubscriberScenarios.class, "ownerContext", "context set=true", "listener owner=true",
            "event owner=true", "restored=true");
    }

    @Test
    void definedChangesInRegistrationDoNotCountAsExact() throws Throwable {
        assertEquals(List.of("constructed=1"), old(LegacyMetadataScenarios.class, "constructorProbe"));
        assertEquals(List.of("constructed=0"), LegacyMetadataScenarios.constructorProbe());
        assertEquals(List.of(), old(LegacyMetadataScenarios.class, "abstractRegistration"));
        assertEquals(List.of("abstract=true"), LegacyMetadataScenarios.abstractRegistration());
        assertEquals(List.of("covariant child", "covariant child"), old(LegacySubscriberScenarios.class, "covariantBridge"));
        assertEquals(List.of("covariant child"), LegacySubscriberScenarios.covariantBridge());
        var privateMethod = assertThrows(RuntimeException.class,
            () -> old(LegacySubscriberScenarios.class, "privateSubscriber"));
        assertTrue(privateMethod.getMessage().contains("Cannot apply @SubscribeEvent to private method"));
        assertEquals(List.of("public"), LegacySubscriberScenarios.privateSubscriber());
    }

    @Test
    void observedHistoricalBehaviorOutsideExactContract() throws Throwable {
        assertEquals(List.of("cancelable=true", "has result=true"),
            old(LegacyMetadataScenarios.class, "inheritedHandwrittenPropertyConflict"));
        assertEquals(List.of("distinct gap=true", "distinct final pair=true"),
            old(LegacyMetadataScenarios.class, "handwrittenGap"));
        // An arbitrary helper's invokespecial is outside the supported property super chain.
        assertEquals(List.of("virtual=true", "helper=false"), old(LegacyMetadataScenarios.class, "helperSuperCall"));
    }

    @Test
    void ordinaryEventsWithoutPublicNoArgConstructorStillRegister() throws Throwable {
        exact(LegacyMetadataScenarios.class, "injectedConstructorRegistration", "value=explicit");
    }

    private static void exact(Class<?> scenarios, String scenario, String... expected) throws Throwable {
        Object baseline = old(scenarios, scenario);
        assertEquals(List.of(expected), baseline, () -> scenario + " historical trace");
        assertEquals(baseline, HistoricalEventBusFixture.run(scenarios.getClassLoader(), scenarios, scenario),
            () -> scenario + " candidate trace");
    }

    private static Object old(Class<?> scenarios, String scenario) throws Throwable {
        return HistoricalEventBusFixture.run(historical, scenarios, scenario);
    }
}
