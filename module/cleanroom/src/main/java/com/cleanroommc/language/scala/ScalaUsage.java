/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.language.scala;

import java.io.File;

/**
 * @param config   whether the jar references Typesafe Config without bundling it, as Forge used to ship it alongside
 *                 Scala
 * @param original the jar before it was rewritten for Scala, null if it was not rewritten
 */
public record ScalaUsage(Kind kind, boolean config, File original) {

    public static final ScalaUsage NONE = new ScalaUsage(Kind.NONE, false, null);

    public enum Kind {

        NONE,
        /**
         * Java that imported Scala or akka by accident, fully covered by Cleanroom's shims
         */
        SHIM,
        /**
         * Java that calls into Scala 2 mods
         */
        LINKED,
        /**
         * Written in Scala 2.11
         */
        LEGACY,
        /**
         * Written in Scala 2.13 or 3. TODO: marker in CG's integration with language providers
         */
        MODERN,
        /**
         * Ships its own Scala runtime
         */
        BUNDLED

    }

}
