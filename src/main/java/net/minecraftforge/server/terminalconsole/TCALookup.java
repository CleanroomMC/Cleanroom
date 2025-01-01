package net.minecraftforge.server.terminalconsole;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link StrLookup} that returns properties specific to
 * {@link TerminalConsoleAppender}. The following properties are supported:
 *
 * <ul>
 *     <li>{@code ${tca:disableAnsi}}: Can be used together with
 *     {@code PatternLayout} to disable ANSI colors for patterns like
 *     {@code %highlight} or {@code %style} if ANSI colors are unsupported
 *     or are disabled for {@link TerminalConsoleAppender}.
 *
 *     <p><b>Example usage:</b>
 *     {@code <PatternLayout ... disableAnsi="${tca:disableAnsi}">}</p></li>
 * </ul>
 */
@Plugin(name = "tca", category = StrLookup.CATEGORY)
public final class TCALookup extends AbstractLookup {

    /**
     * Lookup key that returns if ANSI colors are unsupported/disabled.
     */
    public final static String KEY_DISABLE_ANSI = "disableAnsi";

    @Override
    @Nullable
    public String lookup(LogEvent event, String key) {
        if (KEY_DISABLE_ANSI.equals(key)) {
            return String.valueOf(!TerminalConsoleAppender.isAnsiSupported());
        }
        return null;
    }

}
