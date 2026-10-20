package net.minecraftforge.fml.common.asm.transformers;

import java.io.InputStream;
import java.lang.reflect.Method;

import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.ForgeTestRunner;
import net.minecraftforge.fml.common.registry.MinecraftTestHarness;
import net.minecraftforge.registries.GameData;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cleanroommc.cleanroom.common.id.ChunkBiomes;

/**
 * {@link BiomeArrayTransformer} on real javac output from {@code testmods.biomearray.BiomeArrayFixture}:
 * transformed writes land in the chunk (full ids when the I2B is dropped, resolved bytes otherwise),
 * reads behave like BALOAD, and array references that escape the method are the documented limit.
 */
@ForgeTestRunner.Isolated
public class BiomeArrayTransformerTest
{
    private static final String FIXTURE = "testmods.biomearray.BiomeArrayFixture";

    @BeforeAll
    public static void setup()
    {
        MinecraftTestHarness.bootstrap();
    }

    private static Chunk chunk()
    {
        return new Chunk(null, 1, -1);
    }

    /** A biome past 255 whose low byte another biome also has; registers extras if this test runs first. */
    private static int wideBiomeSharingALowByte()
    {
        for (int i = 0; i < 256; i++)
        {
            ResourceLocation name = new ResourceLocation("wideids_test", "palette_" + i);
            if (!ForgeRegistries.BIOMES.containsKey(name))
            {
                Biome biome = new Biome(new Biome.BiomeProperties("palette_" + i)) { };
                biome.setRegistryName(name);
                ForgeRegistries.BIOMES.register(biome);
            }
        }
        for (Biome biome : ForgeRegistries.BIOMES)
        {
            int id = Biome.getIdForBiome(biome);
            if (id > 255 && GameData.getBiomeIdsWithLowByte(id & 0xFF).length > 1)
            {
                return id;
            }
        }
        throw new AssertionError("no wide biome shares a low byte with another");
    }

    private static byte[] classBytes(String binaryName) throws Exception
    {
        String path = binaryName.replace('.', '/') + ".class";
        try (InputStream in = BiomeArrayTransformerTest.class.getClassLoader().getResourceAsStream(path))
        {
            return in.readAllBytes();
        }
    }

    /** The fixture rewritten by the transformer, defined in a throwaway loader (verified on define). */
    private static Class<?> transformedFixture() throws Exception
    {
        byte[] transformed = new BiomeArrayTransformer().transform(FIXTURE, FIXTURE, classBytes(FIXTURE));
        ClassLoader loader = new ClassLoader(BiomeArrayTransformerTest.class.getClassLoader())
        {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
            {
                if (name.equals(FIXTURE))
                {
                    Class<?> cls = defineClass(name, transformed, 0, transformed.length);
                    if (resolve)
                    {
                        resolveClass(cls);
                    }
                    return cls;
                }
                return super.loadClass(name, resolve);
            }
        };
        return loader.loadClass(FIXTURE);
    }

    private static void invoke(Class<?> fixture, String method, Chunk chunk, int... args) throws Exception
    {
        Class<?>[] sig = new Class[args.length + 1];
        sig[0] = Chunk.class;
        for (int i = 0; i < args.length; i++)
        {
            sig[i + 1] = int.class;
        }
        Method m = fixture.getMethod(method, sig);
        Object[] boxed = new Object[args.length + 1];
        boxed[0] = chunk;
        for (int i = 0; i < args.length; i++)
        {
            boxed[i + 1] = args[i];
        }
        m.invoke(null, boxed);
    }

    @Test
    public void classesWithoutTheAccessorAreUntouched() throws Exception
    {
        byte[] helper = classBytes(FIXTURE + "$Helper");
        assertSame(helper, new BiomeArrayTransformer().transform("testmods.biomearray.BiomeArrayFixture$Helper", "testmods.biomearray.BiomeArrayFixture$Helper", helper));
    }

    @Test
    public void fillReachesTheChunkWithTheFullId() throws Exception
    {
        int wide = wideBiomeSharingALowByte();
        assertTrue(wide > 255);
        Class<?> fixture = transformedFixture();
        Chunk chunk = chunk();
        invoke(fixture, "fill", chunk, wide);
        for (int i = 0; i < 256; i++)
        {
            assertEquals(wide, chunk.getBiomeIds().get(i), "column " + i);
        }
    }

    @Test
    public void copyColumnResolvesThroughTheChunk() throws Exception
    {
        int wide = wideBiomeSharingALowByte();
        Class<?> fixture = transformedFixture();
        Chunk chunk = chunk();
        chunk.getBiomeIds().set(0, 0, wide);
        invoke(fixture, "copyColumn", chunk);
        for (int i = 0; i < 16; i++)
        {
            assertEquals(wide, chunk.getBiomeIds().get(i), "column " + i);
        }
    }

    @Test
    public void nonAdjacentI2bTakesTheBytePath() throws Exception
    {
        int wide = wideBiomeSharingALowByte();
        Class<?> fixture = transformedFixture();
        Chunk chunk = chunk(); // empty chunk, no world: resolve falls to the lowest candidate
        invoke(fixture, "viaLocal", chunk, wide);
        int lowest = GameData.getBiomeIdsWithLowByte(wide & 0xFF)[0];
        assertEquals(lowest, chunk.getBiomeIds().get(3), "a truncated byte resolves like a mod byte write");
        assertEquals(ChunkBiomes.UNSET, chunk.getBiomeIds().get(4));
    }

    @Test
    public void readsBehaveLikeBaloadTransformedOrNot() throws Exception
    {
        int desert = Biome.getIdForBiome(Biomes.DESERT);
        int plains = Biome.getIdForBiome(Biomes.PLAINS);
        Chunk chunk = chunk();
        chunk.getBiomeIds().set(4, desert);
        chunk.getBiomeIds().set(5, plains);

        Class<?> fixture = transformedFixture();
        assertTrue((Boolean)fixture.getMethod("isDesertAt", Chunk.class, int.class, int.class).invoke(null, chunk, 4, desert));
        assertFalse((Boolean)fixture.getMethod("isDesertAt", Chunk.class, int.class, int.class).invoke(null, chunk, 5, desert));

        // The untransformed class reads the snapshot's low bytes, so reads still work without ASM.
        Class<?> original = BiomeArrayTransformerTest.class.getClassLoader().loadClass(FIXTURE);
        assertTrue((Boolean)original.getMethod("isDesertAt", Chunk.class, int.class, int.class).invoke(null, chunk, 4, desert));
        assertFalse((Boolean)original.getMethod("isDesertAt", Chunk.class, int.class, int.class).invoke(null, chunk, 5, desert));
    }

    @Test
    public void arrayEscapesAreTheDocumentedLimit() throws Exception
    {
        int desert = Biome.getIdForBiome(Biomes.DESERT);
        Class<?> fixture = transformedFixture();
        Chunk chunk = chunk();
        invoke(fixture, "viaHelper", chunk, desert);
        assertEquals(ChunkBiomes.UNSET, chunk.getBiomeIds().get(5), "Helper.write ran on an untracked array: the write is lost");
    }

    @Test
    public void untrackedStoresInTheSameClassAreAlone() throws Exception
    {
        Class<?> fixture = transformedFixture();
        byte[] other = new byte[4];
        fixture.getMethod("unrelated", byte[].class, int.class).invoke(null, other, 0x41);
        assertEquals((byte)0x41, other[0]);
    }

    /** What a mod jar looks like: every getBiomeArray()[B call spelled with the SRG name. */
    private static byte[] renameToSrg(byte[] classBytes) throws Exception
    {
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(classBytes).accept(new ClassVisitor(Opcodes.ASM9, writer)
        {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions)
            {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions))
                {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface)
                    {
                        if ("net/minecraft/world/chunk/Chunk".equals(owner) && "getBiomeArray".equals(name) && "()[B".equals(descriptor))
                        {
                            name = "func_76605_m";
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static MethodNode method(ClassNode node, String name)
    {
        for (MethodNode method : node.methods)
        {
            if (method.name.equals(name))
            {
                return method;
            }
        }
        throw new AssertionError("no method " + name);
    }

    private static int countOpcode(MethodNode method, int opcode)
    {
        int count = 0;
        for (AbstractInsnNode insn : method.instructions)
        {
            if (insn.getOpcode() == opcode)
            {
                count++;
            }
        }
        return count;
    }

    @Test
    public void srgNamedCallsAreRedirectedToo() throws Exception
    {
        // The call cannot be invoked (Chunk has no func_76605_m in dev), so the assertions are structural.
        byte[] renamed = renameToSrg(classBytes(FIXTURE));
        ClassNode renamedNode = new ClassNode();
        new ClassReader(renamed).accept(renamedNode, 0);
        assertTrue(countOpcode(method(renamedNode, "fill"), Opcodes.BASTORE) > 0, "the renamed fixture still has plain BASTOREs");

        byte[] transformed = new BiomeArrayTransformer().transform(FIXTURE, FIXTURE, renamed);
        assertNotSame(renamed, transformed, "the SRG spelling went through the transform");
        ClassNode transformedNode = new ClassNode();
        new ClassReader(transformed).accept(transformedNode, 0);
        MethodNode fill = method(transformedNode, "fill");
        assertEquals(0, countOpcode(fill, Opcodes.BASTORE), "every BASTORE was redirected");
        assertEquals(0, countOpcode(fill, Opcodes.I2B), "the I2B before each store was dropped");
        boolean storeByte = false;
        for (AbstractInsnNode insn : fill.instructions)
        {
            if (insn.getOpcode() == Opcodes.INVOKESTATIC && insn instanceof MethodInsnNode
                    && "com/cleanroommc/cleanroom/common/id/ChunkBiomes".equals(((MethodInsnNode)insn).owner)
                    && "storeByte".equals(((MethodInsnNode)insn).name))
            {
                storeByte = true;
            }
        }
        assertTrue(storeByte, "fill stores through ChunkBiomes.storeByte");
    }

    @Test
    public void quickRejectSeesSrgName() throws Exception
    {
        // A class carrying only the SRG spelling is transformed, i.e. the cheap scan saw the name.
        byte[] renamed = renameToSrg(classBytes(FIXTURE));
        byte[] transformed = new BiomeArrayTransformer().transform(FIXTURE, FIXTURE, renamed);
        assertNotSame(renamed, transformed);
    }
}
