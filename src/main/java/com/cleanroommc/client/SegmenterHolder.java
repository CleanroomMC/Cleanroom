package com.cleanroommc.client;

import com.ibm.icu.segmenter.LocalizedSegmenter;
import com.ibm.icu.segmenter.Segmenter;
import com.ibm.icu.util.ULocale;

public class SegmenterHolder {
    private SegmenterHolder() {}

    public static final Segmenter SEGMENTER = LocalizedSegmenter.builder()
            .setLocale(ULocale.getDefault())
            .setSegmentationType(LocalizedSegmenter.SegmentationType.LINE)
            .build();
}
