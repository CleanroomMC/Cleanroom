/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

/**
 * Typed, synchronous event channels and mod-owned subscription management.
 * <p>
 * Forge EventBus 7 events ({@link net.minecraftforge.eventbus.api.event.RecordEvent RecordEvent},
 * {@link net.minecraftforge.eventbus.api.event.MutableEvent MutableEvent}, and
 * {@link net.minecraftforge.eventbus.api.event.InheritableEvent InheritableEvent}) are exposed through
 * {@link com.cleanroommc.cleanroom.event.EventChannel} and
 * {@link com.cleanroommc.cleanroom.event.CancellableEventChannel}, defaulting to
 * {@link net.minecraftforge.eventbus.api.bus.BusGroup#DEFAULT}.
 * <p>
 * Legacy Forge {@link net.minecraftforge.fml.common.eventhandler.Event} subclasses are exposed through
 * {@link com.cleanroommc.cleanroom.event.LegacyEventChannel}, defaulting to
 * {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}, and support both legacy and Forge 7
 * {@link net.minecraftforge.eventbus.api.listener.SubscribeEvent} annotations and {@code Priority} constants.
 * <p>
 * {@link com.cleanroommc.cleanroom.event.SubscriptionGroup} and
 * {@link com.cleanroommc.cleanroom.event.Subscription} track mod ownership across both generations; channel
 * overloads without a {@code SubscriptionGroup} use the owning mod's framework-managed default group and retire when
 * the mod is disabled.
 */
@NullMarked package com.cleanroommc.cleanroom.event;

import org.jspecify.annotations.NullMarked;
