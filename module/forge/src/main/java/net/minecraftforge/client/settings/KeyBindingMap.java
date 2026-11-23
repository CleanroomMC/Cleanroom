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

package net.minecraftforge.client.settings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class KeyBindingMap
{
    private static final IntHashMap<Collection<KeyBinding>>[] map = new IntHashMap[8];
    static
    {
        for (int i = 0; i < 8; i++) {
            map[i] = new IntHashMap<>();
        }
    }

    @Nullable
    @Deprecated // Cleanroom: Use lookupActives
    public KeyBinding lookupActive(int keyCode)
    {
        return lookupActives(keyCode).stream().findFirst().orElse(null);
    }

    public Set<KeyBinding> lookupActives(int keyCode)
    {
        int activeModifiersMask = KeyModifier.getActiveModifierMask();
        Set<KeyBinding> bindings = getBindings(keyCode, KeyModifier.filterMask(activeModifiersMask, keyCode));
        if (!bindings.isEmpty())
        {
            return bindings;
        }
        return getBindings(keyCode, KeyModifier.MASK_NONE);
    }

    private Set<KeyBinding> getBindings(int keyCode, int mask)
    {
        Collection<KeyBinding> bindings = map[mask].lookup(keyCode);
        if (bindings == null)
        {
            return Collections.emptySet();
        }
        return bindings.stream().filter($ -> $.isActiveAndMatches(keyCode)).collect(Collectors.toSet());
    }

    public List<KeyBinding> lookupAll(int keyCode)
    {
        List<KeyBinding> matchingBindings = new ArrayList<>();
        for (IntHashMap<Collection<KeyBinding>> bindingsMap : map)
        {
            Collection<KeyBinding> bindings = bindingsMap.lookup(keyCode);
            if (bindings != null)
            {
                matchingBindings.addAll(bindings);
            }
        }
        return matchingBindings;
    }

    public void addKey(int keyCode, KeyBinding keyBinding)
    {
        IntHashMap<Collection<KeyBinding>> bindingsMap = map[keyBinding.getKeyModifiersMask()];
        Collection<KeyBinding> bindingsForKey = bindingsMap.lookup(keyCode);
        if (bindingsForKey == null)
        {
            bindingsForKey = new ArrayList<>();
            bindingsMap.addKey(keyCode, bindingsForKey);
        }
        bindingsForKey.add(keyBinding);
    }

    public void removeKey(KeyBinding keyBinding)
    {
        int keyCode = keyBinding.getKeyCode();
        IntHashMap<Collection<KeyBinding>> bindingsMap = map[keyBinding.getKeyModifiersMask()];
        Collection<KeyBinding> bindingsForKey = bindingsMap.lookup(keyCode);
        if (bindingsForKey != null)
        {
            bindingsForKey.remove(keyBinding);
            if (bindingsForKey.isEmpty())
            {
                bindingsMap.removeObject(keyCode);
            }
        }
    }

    public void clearMap()
    {
        for (IntHashMap<Collection<KeyBinding>> bindings : map)
        {
            bindings.clearMap();
        }
    }
}
