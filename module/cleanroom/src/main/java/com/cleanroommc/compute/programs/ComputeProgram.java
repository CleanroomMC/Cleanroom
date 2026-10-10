package com.cleanroommc.compute.programs;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.Device;
import com.cleanroommc.compute.errors.CompilationError;
import com.cleanroommc.compute.errors.HeaderParsingError;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.KernelMetadata;
import com.cleanroommc.compute.types.OpenCLType;
import com.cleanroommc.compute.types.OpenCLTypeDeserializer;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.NonNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/**
 * OpenCL program; holds all the kernels.
 * This is handled internally by the {@link Compute} class.
 * @author EΣrie
 */
public class ComputeProgram implements Closeable {
    private final transient ResourceLocation resourceLocation;
    private final transient ProgramMetadata metadata;
    private transient long programHandle;
    private transient Map<String, Kernel> kernels;

    /**
     * Create a new ComputeProgram.
     * @param resourceLocation Location of the program's JSON file.
     * @author EΣrie
     * @throws NullPointerException if the program doesn't exist.
     * @throws RuntimeException if the program couldn't be loaded for any reason other than the file not being present.
     */
    public ComputeProgram(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;

        try (Reader stream = getResourceUniversal(resourceLocation)) {
            Gson gson = new GsonBuilder().registerTypeAdapter(OpenCLType.class, new OpenCLTypeDeserializer()).create();
            metadata = gson.fromJson(stream, ProgramMetadata.class);
            for (var kernel : metadata.kernels.entrySet()) {
                kernel.getValue().kernelName = kernel.getKey();
                kernel.getValue().parent = metadata;
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem loading compute program %s. ", resourceLocation));
        }
    }

    /**
     * Compiles the program.
     * @param cache [<i>Unused</i>] Used to check if the program matches the program in cache.
     * @param stack MemoryStack for temporary variables.
     * @author EΣrie
     * @apiNote Write the fucking cache EΣrie.
     */
    public void compile(ProgramCacheIntegrityTable cache, MemoryStack stack) {
        IntBuffer err_code = stack.mallocInt(1);
        String src = getResourceAsString(new ResourceLocation(resourceLocation.getNamespace(), metadata.fname));
        long program = CL10.clCreateProgramWithSource(Compute.instance().context, src, err_code);
        switch(err_code.get(0)) {
            case CL10.CL_INVALID_VALUE -> throw new NullPointerException(String.format("Source code of %s is null. ", resourceLocation));
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL program.");
        }
        Set<ResourceLocation> dependencies = getHeadersFromFile(src, resourceLocation);
        PointerBuffer paths = null;
        PointerBuffer libraryHandles = null;
        if (!dependencies.isEmpty()) {
            paths = stack.mallocPointer(dependencies.size());
            libraryHandles = stack.mallocPointer(dependencies.size());
            for (ResourceLocation rl : dependencies) {
                String fname = String.format("%s/%s", rl.getNamespace(), rl.getPath());
                ByteBuffer buf = stack.UTF8(fname);
                paths.put(buf);
                for (long library : Compute.instance().getOrCreateLibraries(rl, stack))
                    libraryHandles.put(library);
            }
            libraryHandles.flip();
            paths.flip();
        }
        PointerBuffer devices = stack.mallocPointer(Compute.instance().devices.length);
        for (Device device : Compute.instance().devices) {
            devices.put(device.handle());
        }
        devices.flip();
        switch (CL12.clCompileProgram(program, devices, "", libraryHandles, paths, null, 0)) {
            case CL12.CL_COMPILER_NOT_AVAILABLE -> throw new CompilationError("Compiler unavailable.");
            case CL12.CL_COMPILE_PROGRAM_FAILURE -> throw new CompilationError(String.format("Failed to compile program %s. \nBuild Logs:\n%s", resourceLocation, combineStrings(getBuildLog(stack, program), "\n\t")));
            case CL10.CL_INVALID_OPERATION -> throw new Error("Invalid Operation. Program either has no source, already has kernel objects attached or has been compiled during program creation.");
            case CL10.CL_INVALID_PROGRAM -> throw new RuntimeException("Invalid program.");
            case CL10.CL_INVALID_DEVICE -> throw new RuntimeException("Invalid device.");
            case CL10.CL_INVALID_VALUE -> throw new RuntimeException("Invalid value.");
            case CL12.CL_INVALID_COMPILER_OPTIONS -> throw new CompilationError("Invalid compiler options");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to compile OpenCL program.");
        }
        for (String log : getBuildLog(stack, program)) {
            Compute.instance().LOGGER.info(log);
        }
        err_code.rewind();
        programHandle = CL12.clLinkProgram(Compute.instance().context, devices, "", program, null, 0, err_code);
        switch(err_code.get(0)) {
            case CL10.CL_INVALID_PROGRAM -> throw new CompilationError(String.format("Program %s is invalid and therefore can't be linked.", resourceLocation));
            case CL10.CL_INVALID_DEVICE -> throw new RuntimeException(String.format("A device unrelated to the context has been provided to the linker for program %s.", resourceLocation));
            case CL12.CL_INVALID_LINKER_OPTIONS -> throw new CompilationError(String.format(String.format("Linker options for program %s are invalid.", resourceLocation)));
            case CL10.CL_INVALID_OPERATION -> throw new CompilationError(String.format("Program %s hasn't finished building yet.", resourceLocation));
            case CL12.CL_LINKER_NOT_AVAILABLE -> throw new RuntimeException("Linker is unavailable");
            case CL12.CL_LINK_PROGRAM_FAILURE -> throw new CompilationError(String.format("Failed to link program %s. \nBuild Logs:\n\t%s", resourceLocation, combineStrings(getBuildLog(stack, programHandle), "\t\n")));
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to link OpenCL program.");
        }
        for (String log : getBuildLog(stack, program)) {
            Compute.instance().LOGGER.info(log);
        }
        CL10.clReleaseProgram(program);
        ImmutableMap.Builder<String, Kernel> mapBuilder = new ImmutableMap.Builder<>();
        for (Map.Entry<String, KernelMetadata> kernel : metadata.kernels.entrySet()) {
            mapBuilder.put(kernel.getKey(), new Kernel(programHandle, kernel.getValue()));
        }
        kernels = mapBuilder.build();
    }

    /**
     * Returns a kernel
     * @param name name of the kernel
     * @return kernel with a name
     */
    public Kernel kernel(@NonNull String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(kernels.containsKey(name),
                "Program has no kernel named \"%s\"", name);
        return kernels.get(name);
    }

    /**
     * <p><i>Internal Function</i></p>
     * <p>
     *     Returns build logs of the program
     * </p>
     * @param stack MemoryStack for temporary variables.
     * @param program the program
     * @return logs in a list of strings.
     * @apiNote Trim the fucking strings EΣrie
     * @author EΣrie
     */
    private List<String> getBuildLog(MemoryStack stack, long program) {
        List<String> logs = new ObjectArrayList<>();
        try (MemoryStack substack = stack.push()) {
            PointerBuffer len = substack.mallocPointer(1);
            for (Device device : Compute.instance().devices) {
                CL10.clGetProgramBuildInfo(program, device.handle(), CL10.CL_PROGRAM_BUILD_LOG, (ByteBuffer) null, len);
                ByteBuffer data = substack.malloc((int) len.get(0));
                len.rewind();
                CL10.clGetProgramBuildInfo(program, device.handle(), CL10.CL_PROGRAM_BUILD_LOG, data, len);
                data.rewind();
                StringBuilder builder = new StringBuilder();
                CharBuffer dataDecoded = StandardCharsets.US_ASCII.decode(data);
                while (dataDecoded.hasRemaining()) {
                    builder.append(dataDecoded.get());
                }
                String res = builder.toString().trim();
                if (!res.isEmpty())
                    logs.add(res);
            }
        }
        return logs;
    }

    /**
     * <p><i>Internal Function</i></p>
     * <p>Combines strings with a delimeter.</p>
     */
    private String combineStrings(List<String> strings, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
            builder.append(delimiter);
        }
        return builder.toString();
    }

    private static Reader getResourceUniversal(ResourceLocation resourceLocation) throws IOException {
        FileSystem fs = null;
        try {
            ModContainer owner = Loader.instance().getIndexedModList().get(resourceLocation.getNamespace());
            fs = FileSystems.newFileSystem(owner.getResource().toPath(), Launch.classLoader);
            Path path = fs.getPath(String.format("/assets/%s/compute/%s.json", resourceLocation.getNamespace(), resourceLocation.getPath()));
            if (Files.exists(path)) {
                return Files.newBufferedReader(path);
            }
        } catch (Exception e) {
            return new InputStreamReader(MinecraftResourceUtils.getInputStream(new ResourceLocation(resourceLocation.getNamespace(), String.format("compute/%s.json", resourceLocation.getPath()))));
        } finally {
            IOUtils.closeQuietly(fs);
        }
        throw new MissingResourceException("There is no program. ",
            "com.cleanroommc.cleanroom.compute.programs.ComputeProgram",
            resourceLocation.toString());
    }

    public static String getResourceAsString(ResourceLocation resourceLocation) {
        FileSystem fs = null;
        try {
            ModContainer owner = Loader.instance().getIndexedModList().get(resourceLocation.getNamespace());
            fs = FileSystems.newFileSystem(owner.getResource().toPath(), Launch.classLoader);
            Path path = fs.getPath(String.format("/assets/%s/compute/%s", resourceLocation.getNamespace(), resourceLocation.getPath()));
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (Exception e) {
            return MinecraftResourceUtils.readText(
                new ResourceLocation(
                    resourceLocation.getNamespace(),
                    String.format("compute/%s", resourceLocation.getPath())
                ),
                MinecraftResourceUtils.NewLineType.BACK_SLASH_N
            );
        } finally {
            IOUtils.closeQuietly(fs);
        }
        throw new MissingResourceException("There is no program. ",
            "com.cleanroommc.cleanroom.compute.programs.ComputeProgram",
            resourceLocation.toString());
    }

    /**
     * <p><i>Internal function</i></p>
     * <p>
     *     Reads all the headers from the OpenCL source file and returns their
     *     {@link ResourceLocation ResourceLocations} as a Set.
     * </p>
     * @param source source code
     * @param program RL of the program.
     * @return all headers in a set.
     * @author EΣrie
     * @implSpec This has to be a finite state machine.
     */
    public static @NonNull Set<ResourceLocation> getHeadersFromFile(@NonNull String source, ResourceLocation program) {
        final char[] INCLUDE = {'i','n','c','l','u','d','e'};
        Set<ResourceLocation> headers = new ObjectArraySet<>();
        char[] buffer = new char[7]; // Only 7 chars are needed to fit "include", the # doesn't get stored.
        StringBuilder builder = new StringBuilder();
        String namespace = null; // Obsolete variable setting to shut up IntelliJ
        int bufferIdx = 0;
        int line = 0;
        // States:
        // 0 - searching for #
        // 1 - found #, checking for include
        // 2 - found line comment, skipping until new line
        // 3 - found block comment skipping until end
        // 4 - found include, skipping until <
        // 5 - found <, reading header name until first /
        // 6 - found first /, namespace detected, now detect path
        // 7 - comment between #include and <mod/library.h>
        int state = 0;
        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == '\n') {
                line++;
            }
            switch (state) {
                case 0:
                    if (source.charAt(i) == '#') {
                        state = 1;
                    } else if (source.charAt(i) == '/') {
                        if (source.charAt(i+1) == '/') {
                            state = 2;
                            i++;
                        } else if (source.charAt(i+1) == '*') {
                            state = 3;
                            i++;
                        }
                    }
                    continue;
                case 1:
                    if (bufferIdx < 7) {
                        buffer[bufferIdx] = source.charAt(i);
                        bufferIdx++;
                    } else {
                        if (Arrays.compare(buffer, INCLUDE) == 0) {
                            state = 4;
                        } else {
                            state = 0;
                        }
                        bufferIdx = 0;
                        i--; // prevent the fsm from going over an <
                    }
                    continue;
                case 2:
                    if (source.charAt(i) == '\n') {
                        state = 0;
                    }
                    continue;
                case 3:
                    if (source.charAt(i) == '*'
                    && source.charAt(i+1) == '/') {
                        state = 0;
                        i++;
                    }
                    continue;
                case 4:
                    if (source.charAt(i) == '<') {
                        state = 5;
                    } else if (source.charAt(i) == '/'
                            && source.charAt(i+1) == '*') {
                        state = 7;
                    } else if (!Character.isWhitespace(source.charAt(i))) {
                        throw new HeaderParsingError(String.format("Malformed OpenCL code at line %d in program %s", line, program));
                    }
                    continue;
                case 5:
                    if (Character.isWhitespace(source.charAt(i))) {
                        throw new HeaderParsingError(String.format("Malformed OpenCL code at line %d in program %s", line, program));
                    } else if (source.charAt(i) == '/') {
                        namespace = builder.toString();
                        builder.delete(0,Integer.MAX_VALUE);
                        state = 6;
                    } else if (source.charAt(i) == '>') {
                        throw new HeaderParsingError(String.format("Error at line %d in program %s. OpenCL libraries need to be in \"mod/library.h\" format.", line, program));
                    } else {
                        builder.append(source.charAt(i));
                    }
                    continue;
                case 6:
                    if (Character.isWhitespace(source.charAt(i))) {
                        throw new HeaderParsingError(String.format("Malformed OpenCL code at line %d in program %s", line, program));
                    } else if (source.charAt(i) == '>') {
                        headers.add(new ResourceLocation(namespace, builder.toString()));
                        builder.delete(0,Integer.MAX_VALUE);
                        state = 0;
                    } else {
                        builder.append(source.charAt(i));
                    }
                    continue;
                case 7:
                    if (source.charAt(i) == '*'
                            && source.charAt(i+1) == '/') {
                        state = 4;
                        i++;
                    }
            }
        }
        return headers;
    }

    /**
     * Releases the program.
     * @author EΣrie
     */
    @Override
    public void close() {
        CL10.clReleaseProgram(programHandle);
    }

    /**
     * Metadata of the program. This is what is read from the JSON file.
     */
    public static class ProgramMetadata {
        /**
         * Name of the code file.
         */
        @SerializedName("file_name")
        public String fname;
        /**
         * Requirements of the program in terms of OpenCL features.
         */
        @SerializedName("requirements")
        public Requirements requirements = new Requirements();
        /**
         * The kernels.
         */
        @SerializedName("kernels")
        public Map<String, KernelMetadata> kernels = new Object2ObjectAVLTreeMap<>();

        /**
         * Requirements of the program in terms of OpenCL features.
         */
        public static class Requirements {
            /**
             * Does the program use images.
             */
            public boolean images = false;
            /**
             * Does the program use mipmaps.
             */
            public boolean mipmaps = false;
            /**
             * Does the program use pipes.
             */
            public boolean pipes = false;
        }
    }
}
