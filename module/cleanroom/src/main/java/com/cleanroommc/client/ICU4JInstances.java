/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client;

import com.ibm.icu.segmenter.LocalizedSegmenter;
import com.ibm.icu.segmenter.Segmenter;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.util.ULocale;

public final class ICU4JInstances {

    public static final BreakIterator BREAK_ITERATOR = BreakIterator.getLineInstance();
    public static final Segmenter SEGMENTER = LocalizedSegmenter.builder()
        .setLocale(ULocale.getDefault())
        .setSegmentationType(LocalizedSegmenter.SegmentationType.LINE)
        .build();

    private ICU4JInstances() { }

}
