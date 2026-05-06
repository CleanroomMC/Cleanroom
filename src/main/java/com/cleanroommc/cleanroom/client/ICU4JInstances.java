package com.cleanroommc.cleanroom.client;

import com.ibm.icu.segmenter.LocalizedSegmenter;
import com.ibm.icu.segmenter.Segmenter;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.util.ULocale;

public class ICU4JInstances {
    private ICU4JInstances() {}

    public static final BreakIterator BREAK_ITERATOR = BreakIterator.getLineInstance();
    public static final Segmenter SEGMENTER = LocalizedSegmenter.builder()
            .setLocale(ULocale.getDefault())
            .setSegmentationType(LocalizedSegmenter.SegmentationType.LINE)
            .build();
}
