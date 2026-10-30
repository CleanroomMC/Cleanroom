package net.minecraftforge.registries;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RegistrySync}: the login handshake sends a count and a hash of each registry's ordered
 * names instead of a name-to-id map. Both sides derive the same numbering, so matching hashes prove
 * the name lists (and hence every id) agree.
 */
@ForgeTestRunner.Isolated
@SuppressWarnings("deprecation")
public class RegistrySyncTest
{
    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
        GameData.freezeData();
    }

    private static ForgeRegistry<Block> blocks(RegistryManager mgr, ResourceLocation name, String... paths)
    {
        ForgeRegistry<Block> reg = mgr.createRegistry(name, Block.class, null, null, null, null, null, false, false, false, null, null);
        for (String path : paths)
        {
            reg.register(new Block(Material.ROCK).setRegistryName(new ResourceLocation("wideids_test", path)));
        }
        reg.freeze();
        return reg;
    }

    private static ForgeRegistry<Item> items(RegistryManager mgr, ResourceLocation name, String... paths)
    {
        ForgeRegistry<Item> reg = mgr.createRegistry(name, Item.class, null, null, null, null, null, false, false, false, null, null);
        for (String path : paths)
        {
            reg.register(new Item().setRegistryName(new ResourceLocation("wideids_test", path)));
        }
        reg.freeze();
        return reg;
    }

    private static long expectedHash(String... pathsInIdOrder)
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            for (String path : pathsInIdOrder)
            {
                sha.update(("wideids_test:" + path).getBytes(StandardCharsets.UTF_8));
                sha.update((byte)0);
            }
            byte[] digest = sha.digest();
            long ret = 0;
            for (int i = 0; i < 8; i++)
            {
                ret = (ret << 8) | (digest[i] & 0xFFL);
            }
            return ret;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void identicalRegistriesMatch()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "sync_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, name, "a", "b", "c");
        blocks(client, name, "c", "a", "b");

        List<RegistrySync.Entry> remote = RegistrySync.local(server, false);
        assertNull(RegistrySync.check(remote, client));
        assertEquals(remote.get(0).hash(), RegistrySync.hash(client.getRegistry(name), 3));
    }

    @Test
    public void hashIsStableAndOrderSensitive()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "hash_blocks");
        RegistryManager mgr = new RegistryManager("SYNC_HASH");
        ForgeRegistry<Block> reg = blocks(mgr, name, "a", "b", "c");
        assertEquals(expectedHash("a", "b", "c"), RegistrySync.hash(reg, 3));
        assertEquals(expectedHash("a"), RegistrySync.hash(reg, 1));

        // An extra name sorting in the middle changes both order and count
        RegistryManager other = new RegistryManager("SYNC_HASH_B");
        blocks(other, name, "a", "aab", "b", "c");
        String problem = RegistrySync.check(RegistrySync.local(mgr, false), other);
        assertNotNull(problem);
        assertTrue(problem.contains(name.toString()), problem);
        assertTrue(problem.contains("differ"), problem);
    }

    @Test
    public void clientExtrasPastTheServerCountAreFine()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "extra_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, name, "a", "b", "c");
        blocks(client, name, "a", "b", "c", "zzz_d");

        assertNull(RegistrySync.check(RegistrySync.local(server, false), client));
    }

    @Test
    public void clientExtraInTheMiddleIsRefused()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "middle_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, name, "a", "b", "c");
        blocks(client, name, "a", "aab", "b", "c");

        String problem = RegistrySync.check(RegistrySync.local(server, false), client);
        assertNotNull(problem);
        assertTrue(problem.contains(name.toString()), problem);
        assertTrue(problem.contains("differ"), problem);
    }

    @Test
    public void serverHasMoreIsRefused()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "short_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, name, "a", "b", "c");
        blocks(client, name, "a", "b");

        String problem = RegistrySync.check(RegistrySync.local(server, false), client);
        assertNotNull(problem);
        assertTrue(problem.contains(name.toString()), problem);
        assertTrue(problem.contains("3"), problem);
        assertTrue(problem.contains("2"), problem);
    }

    @Test
    public void missingRegistryIsRefused()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "absent_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, name, "a", "b", "c");

        String problem = RegistrySync.check(RegistrySync.local(server, false), client);
        assertNotNull(problem);
        assertTrue(problem.contains("absent"), problem);
    }

    @Test
    public void clientOnlyRegistryIsIgnored()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "shared_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, name, "a", "b");
        blocks(client, name, "a", "b");
        items(client, new ResourceLocation("wideids_test", "client_only_items"), "x");

        assertNull(RegistrySync.check(RegistrySync.local(server, false), client));
    }

    @Test
    public void messagesListEveryFailingRegistry()
    {
        ResourceLocation blocksName = new ResourceLocation("wideids_test", "fail_blocks");
        ResourceLocation itemsName = new ResourceLocation("wideids_test", "fail_items");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        blocks(server, blocksName, "a", "b", "c");
        items(server, itemsName, "i_a", "i_b");
        blocks(client, blocksName, "a", "b");
        items(client, itemsName, "i_a", "i_b", "i_aaa");

        String problem = RegistrySync.check(RegistrySync.local(server, false), client);
        assertNotNull(problem);
        assertTrue(problem.contains(blocksName.toString()), problem);
        assertTrue(problem.contains(itemsName.toString()), problem);
    }

    @Test
    public void modListCarriesRegistryHashesRoundTrip()
    {
        List<RegistrySync.Entry> entries = Arrays.asList(
                new RegistrySync.Entry(new ResourceLocation("minecraft", "blocks"), 408, 0x0102030405060708L),
                new RegistrySync.Entry(new ResourceLocation("wideids_test", "extras"), 3, -1L));

        FMLHandshakeMessage.ModList out = new FMLHandshakeMessage.ModList(java.util.Collections.emptyList(), entries);
        ByteBuf buf = Unpooled.buffer();
        out.toBytes(buf);

        FMLHandshakeMessage.ModList in = new FMLHandshakeMessage.ModList();
        in.fromBytes(buf);

        assertEquals(0, in.modListSize());
        assertEquals(entries, in.entries());
        buf.release();
    }

    @Test
    public void modListWithoutHashSectionDecodesEmpty()
    {
        FMLHandshakeMessage.ModList out = new FMLHandshakeMessage.ModList();
        ByteBuf buf = Unpooled.buffer();
        ByteBufUtils.writeVarInt(buf, 0, 2); // protocol 2 ModList: mods only, no trailing section

        out.fromBytes(buf);

        assertTrue(out.entries().isEmpty());
        buf.release();
    }

    @Test
    public void clientModListWritesEmptyHashSection()
    {
        FMLHandshakeMessage.ModList out = new FMLHandshakeMessage.ModList(java.util.Collections.emptyList());
        ByteBuf buf = Unpooled.buffer();
        out.toBytes(buf);

        FMLHandshakeMessage.ModList in = new FMLHandshakeMessage.ModList();
        in.fromBytes(buf);

        assertTrue(in.entries().isEmpty());
        buf.release();
    }

    @Test
    public void clientExtraInTheMiddleIsReconciledNotRefused()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "adopt_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        ForgeRegistry<Block> serverReg = blocks(server, name, "a", "b", "c");
        ForgeRegistry<Block> clientReg = blocks(client, name, "a", "aab", "b", "c");

        List<ResourceLocation> mismatches = RegistrySync.mismatches(RegistrySync.local(server, false), client);
        assertEquals(Arrays.asList(name), mismatches);

        RegistrySync.Reconciliation result = RegistrySync.reconcile(clientReg, RegistrySync.namesInOrder(serverReg));
        assertTrue(result.missingLocally().isEmpty());
        assertTrue(result.renumbered());

        // Server order first, then the client-only extra appended: a=0, b=1, c=2, aab=3
        assertEquals(0, clientReg.getID(new ResourceLocation("wideids_test", "a")));
        assertEquals(1, clientReg.getID(new ResourceLocation("wideids_test", "b")));
        assertEquals(2, clientReg.getID(new ResourceLocation("wideids_test", "c")));
        assertEquals(3, clientReg.getID(new ResourceLocation("wideids_test", "aab")));
        assertEquals(RegistrySync.hash(serverReg, 3), RegistrySync.hash(clientReg, 3));
    }

    @Test
    public void serverHasMoreIsRefusedWithNames()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "missing_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        ForgeRegistry<Block> serverReg = blocks(server, name, "a", "b", "c");
        ForgeRegistry<Block> clientReg = blocks(client, name, "a", "b");

        Object token = clientReg.numberingToken();
        RegistrySync.Reconciliation result = RegistrySync.reconcile(clientReg, RegistrySync.namesInOrder(serverReg));
        assertEquals(Arrays.asList(new ResourceLocation("wideids_test", "c")), result.missingLocally());
        assertFalse(result.renumbered());

        // Refused: the local numbering was not touched
        assertSame(token, clientReg.numberingToken());
        assertEquals(0, clientReg.getID(new ResourceLocation("wideids_test", "a")));
        assertEquals(1, clientReg.getID(new ResourceLocation("wideids_test", "b")));
    }

    @Test
    public void reconcileThenRevertRestoresOwnNumbering()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "revert_blocks");
        RegistryManager server = new RegistryManager("SYNC_SERVER");
        RegistryManager client = new RegistryManager("SYNC_CLIENT");
        ForgeRegistry<Block> serverReg = blocks(server, name, "a", "b", "c");
        ForgeRegistry<Block> clientReg = blocks(client, name, "a", "aab", "b", "c");

        RegistrySync.reconcile(clientReg, RegistrySync.namesInOrder(serverReg));
        assertEquals(3, clientReg.getID(new ResourceLocation("wideids_test", "aab")));

        // Disconnect reverts to the client's own derived numbering; loadRegistry ends in freeze()
        Object token = clientReg.numberingToken();
        clientReg.freeze();
        assertNotSame(token, clientReg.numberingToken());
        assertEquals(1, clientReg.getID(new ResourceLocation("wideids_test", "aab")));
        assertEquals(2, clientReg.getID(new ResourceLocation("wideids_test", "b")));
    }

    @Test
    public void namesInOrderMatchesHash()
    {
        ResourceLocation name = new ResourceLocation("wideids_test", "order_blocks");
        RegistryManager mgr = new RegistryManager("SYNC_ORDER");
        ForgeRegistry<Block> reg = blocks(mgr, name, "c", "a", "b");

        List<ResourceLocation> names = RegistrySync.namesInOrder(reg);
        assertEquals(3, names.size());
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            for (ResourceLocation entry : names)
            {
                sha.update(entry.toString().getBytes(StandardCharsets.UTF_8));
                sha.update((byte)0);
            }
            byte[] digest = sha.digest();
            long hashed = 0;
            for (int i = 0; i < 8; i++)
            {
                hashed = (hashed << 8) | (digest[i] & 0xFFL);
            }
            assertEquals(RegistrySync.hash(reg, names.size()), hashed);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void registryNamesMessagesWireRoundTrip()
    {
        List<ResourceLocation> registries = Arrays.asList(new ResourceLocation("minecraft", "blocks"), new ResourceLocation("wideids_test", "extras"));
        FMLHandshakeMessage.RegistryNamesRequest requestOut = new FMLHandshakeMessage.RegistryNamesRequest(registries);
        ByteBuf buf = Unpooled.buffer();
        requestOut.toBytes(buf);
        FMLHandshakeMessage.RegistryNamesRequest requestIn = new FMLHandshakeMessage.RegistryNamesRequest();
        requestIn.fromBytes(buf);
        assertEquals(registries, requestIn.registries());
        buf.release();

        // Enough names to force a split: each serializes to ~22 bytes against a 512 KiB budget
        ResourceLocation registry = new ResourceLocation("wideids_test", "many_names");
        List<ResourceLocation> names = new ArrayList<>();
        for (int i = 0; i < 40000; i++)
        {
            names.add(new ResourceLocation("wideids_test", "n" + i));
        }
        List<FMLHandshakeMessage.RegistryNames> parts = FMLHandshakeMessage.RegistryNames.split(registry, names);
        assertTrue(parts.size() > 1, "expected the name list to be split");
        for (int i = 0; i < parts.size(); i++)
        {
            assertEquals(i < parts.size() - 1, parts.get(i).hasMore());
            buf = Unpooled.buffer();
            parts.get(i).toBytes(buf);
            FMLHandshakeMessage.RegistryNames in = new FMLHandshakeMessage.RegistryNames();
            in.fromBytes(buf);
            assertEquals(registry, in.registry());
            assertEquals(parts.get(i).names(), in.names());
            assertEquals(parts.get(i).hasMore(), in.hasMore());
            buf.release();
        }
        List<ResourceLocation> reassembled = new ArrayList<>();
        parts.forEach(part -> reassembled.addAll(part.names()));
        assertEquals(names, reassembled);
    }

    @Test
    public void unfreezeIsNotPublic() throws Exception
    {
        assertFalse(java.lang.reflect.Modifier.isPublic(ForgeRegistry.class.getDeclaredMethod("unfreeze").getModifiers()));
    }
}
