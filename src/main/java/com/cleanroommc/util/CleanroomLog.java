package com.cleanroommc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: temp
public final class CleanroomLog {

    private static final Logger LOGGER = LoggerFactory.getLogger("Cleanroom");

    public static Logger get() {
        return LOGGER;
    }

    private CleanroomLog() { }

}
