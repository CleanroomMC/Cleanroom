package com.cleanroommc.kirino.utils;

/**
 * Immutable Pair Type
 * @param first first value
 * @param second second value
 * @param <T1> type of first value
 * @param <T2> type fo second value
 */
public record Pair<T1,T2>(T1 first,T2 second) {
}
