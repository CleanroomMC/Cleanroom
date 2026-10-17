package net.minecraftforge.fml.common.network.handshake;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 13: a refused login reports mod rejections and registry hash mismatches in one reason.
 */
@ForgeTestRunner.Isolated
public class FMLHandshakeRejectionTest
{
    @Test
    public void rejectionReasonCombinesModsThenRegistries()
    {
        List<ResourceLocation> mismatches = Arrays.asList(
                new ResourceLocation("minecraft", "blocks"),
                new ResourceLocation("somemod", "things"));
        String reason = FMLHandshakeClientState.rejectionReason("Mod rejections: [somemod]", mismatches);
        assertTrue(reason.startsWith("Mod rejections: [somemod]"), reason);
        assertTrue(reason.contains("minecraft:blocks"), reason);
        assertTrue(reason.contains("somemod:things"), reason);
        assertTrue(reason.indexOf("Mod rejections") < reason.indexOf("Registry hash mismatches"), reason);
    }

    @Test
    public void rejectionReasonWithoutMismatchesIsTheModReason()
    {
        assertEquals("Mod rejections: [x]", FMLHandshakeClientState.rejectionReason("Mod rejections: [x]", Collections.emptyList()));
    }

    private static Map<ResourceLocation, List<ResourceLocation>> missing(Object... registryAndNames)
    {
        Map<ResourceLocation, List<ResourceLocation>> missing = new LinkedHashMap<>();
        for (int i = 0; i < registryAndNames.length; i += 2)
        {
            missing.put(new ResourceLocation((String)registryAndNames[i]), Arrays.asList((ResourceLocation[])registryAndNames[i + 1]));
        }
        return missing;
    }

    private static ResourceLocation rl(String name)
    {
        return new ResourceLocation(name);
    }

    @Test
    public void missingEntriesNamesTheAbsentMod()
    {
        Map<String, String> remote = new LinkedHashMap<>();
        remote.put("wideids", "0.1.0");
        String reason = MismatchMessages.missingEntries(
                missing("minecraft:blocks", new ResourceLocation[] {rl("wideids:b59")},
                        "minecraft:items", new ResourceLocation[] {rl("wideids:b59"), rl("wideids:i59")}),
                remote, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(reason.contains("Missing mod: wideids (wideids) 0.1.0 — the server needs it (adds 1 blocks, 2 items)"), reason);
        assertTrue(reason.startsWith("This client is missing registry entries"), reason);
    }

    @Test
    public void missingEntriesReportsADifferentVersion()
    {
        Map<String, String> remote = Collections.singletonMap("wideids", "0.2.0");
        Map<String, String> local = Collections.singletonMap("wideids", "0.1.0");
        Map<String, String> names = Collections.singletonMap("wideids", "WideIds");
        String reason = MismatchMessages.missingEntries(
                missing("minecraft:blocks", new ResourceLocation[] {rl("wideids:b59")}), remote, local, names);
        assertTrue(reason.contains("WideIds: you have 0.1.0, the server has 0.2.0 (1 entries differ)"), reason);
    }

    @Test
    public void missingEntriesReportsSameVersionDifferentContent()
    {
        Map<String, String> remote = Collections.singletonMap("wideids", "0.1.0");
        Map<String, String> local = Collections.singletonMap("wideids", "0.1.0");
        Map<String, String> names = Collections.singletonMap("wideids", "WideIds");
        String reason = MismatchMessages.missingEntries(
                missing("minecraft:blocks", new ResourceLocation[] {rl("wideids:b59"), rl("wideids:b60"), rl("wideids:b61"), rl("wideids:b62")}),
                remote, local, names);
        assertTrue(reason.contains("WideIds 0.1.0: the server registers 4 entries your copy doesn't (wideids:b59 (block), wideids:b60 (block), wideids:b61 (block)…) — check its config"), reason);
    }

    @Test
    public void missingEntriesCountsDistinctNamesWithTheirRegistries()
    {
        Map<String, String> remote = Collections.singletonMap("wideids", "0.1.0");
        Map<String, String> local = Collections.singletonMap("wideids", "0.1.0");
        Map<String, String> names = Collections.singletonMap("wideids", "WideIds");
        String reason = MismatchMessages.missingEntries(
                missing("minecraft:blocks", new ResourceLocation[] {rl("wideids:b59")},
                        "minecraft:items", new ResourceLocation[] {rl("wideids:b59")}),
                remote, local, names);
        assertTrue(reason.contains("the server registers 1 entries your copy doesn't (wideids:b59 (block, item))"), reason);
    }

    @Test
    public void missingEntriesFallsBackForUnclaimedNamespaces()
    {
        String reason = MismatchMessages.missingEntries(
                missing("minecraft:blocks", new ResourceLocation[] {rl("somemod:block")}),
                Collections.singletonMap("othermod", "1.0"), Collections.emptyMap(), Collections.emptyMap());
        assertTrue(reason.contains("Registry minecraft:blocks: missing somemod:block"), reason);
    }

    @Test
    public void missingEntriesOrdersModsByEntryCount()
    {
        Map<String, String> remote = new LinkedHashMap<>();
        remote.put("small", "1.0");
        remote.put("big", "1.0");
        String reason = MismatchMessages.missingEntries(
                missing("minecraft:blocks", new ResourceLocation[] {rl("small:a"), rl("big:a"), rl("big:b")}),
                remote, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(reason.indexOf("Missing mod: big") < reason.indexOf("Missing mod: small"), reason);
    }

    @Test
    public void formatMismatchIsNullWhenFormatsAgree()
    {
        assertEquals(null, MismatchMessages.formatMismatch("4", "a", "4", "b", true));
        assertEquals(null, MismatchMessages.formatMismatch("4", "a", null, "b", true));
    }

    @Test
    public void formatMismatchNamesBothVersionsAndTheOlderSide()
    {
        assertEquals("This server runs Cleanroom 0.6.14 (network format 5); you are running Cleanroom 0.6.13 (format 4). Update the client.",
                MismatchMessages.formatMismatch("4", "0.6.13", "5", "0.6.14", true));
        assertEquals("You are running Cleanroom 0.6.13 (network format 4); this server runs Cleanroom 0.6.14 (format 5). Update the client.",
                MismatchMessages.formatMismatch("5", "0.6.14", "4", "0.6.13", false));
        assertEquals("This server runs Cleanroom 0.6.13 (network format 4); you are running Cleanroom 0.6.14 (format 5). Update the server.",
                MismatchMessages.formatMismatch("5", "0.6.14", "4", "0.6.13", true));
    }

    @Test
    public void legacyRemoteMessageNamesTheServerVersion()
    {
        assertEquals("This server runs Cleanroom 0.6.14 with wide IDs; your game is too old to join",
                MismatchMessages.legacyRemoteMessage("0.6.14"));
    }
}
