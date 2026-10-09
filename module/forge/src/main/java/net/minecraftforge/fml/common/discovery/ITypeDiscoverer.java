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

package net.minecraftforge.fml.common.discovery;

import java.util.List;
import java.util.regex.Pattern;

import net.minecraftforge.fml.common.ModContainer;

public interface ITypeDiscoverer
{
    // main class part, followed by an optional $ and an "inner class" part. $ cannot be last, otherwise scala breaks (old regex)
    Pattern classFile = Pattern.compile("[^\\s$]+(\\$\\S+)?\\.class$");

    /** Faster new way to replace regex */
    static boolean shouldScan(String name)
    {
        final String SUFFIX = ".class";
        if (name == null || name.startsWith("__MACOSX"))
        {
            return false;
        }
        int end = name.length() - SUFFIX.length();          // exclusive end of the part before ".class"
        if (end < 1 || !name.endsWith(SUFFIX))
        {
            return false;
        }
        int i = 0;
        for (; i < end; i++)                               // [^\s$]+, up to the first '$' (if any)
        {
            char c = name.charAt(i);
            if (c == '$')
            {
                break;
            }
            if (isWhitespace(c))
            {
                return false;
            }
        }
        if (i == end)
        {
            return true;                                    // no '$': the whole main part was checked above
        }
        if (i == 0 || i + 1 >= end)
        {
            return false;                                   // '$' needs a character before and after it (\$\S+)
        }
        for (i++; i < end; i++)                             // \S+ after the '$'
        {
            if (isWhitespace(name.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    /** The six characters Java's \s matches without {@code UNICODE_CHARACTER_CLASS}. */
    private static boolean isWhitespace(char c)
    {
        return c == ' ' || c == '\t' || c == '\n' || c == 0x0B || c == '\f' || c == '\r';
    }

    List<ModContainer> discover(ModCandidate candidate, ASMDataTable table);
}
