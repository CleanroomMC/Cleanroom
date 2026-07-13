package com.cleanroommc.cleanroom.compute.programs;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.errors.CompilationError;
import com.cleanroommc.cleanroom.compute.errors.HeaderParsingError;
import com.cleanroommc.cleanroom.compute.kernels.KernelMetadata;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class ComputeProgram {
    private final transient ResourceLocation resourceLocation;
    private final transient ProgramMetadata metadata;
    private byte @Nullable [] @NonNull [] compiledProgramBinary;
    private transient long programHandle;

    public ComputeProgram(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;

        try (InputStreamReader stream = new InputStreamReader(
                this.getClass().getClassLoader().getResourceAsStream(String.format("assets/%s/compute/%s.json", 
                        resourceLocation.getNamespace(), 
                        resourceLocation.getPath())))) {
            Gson gson = new GsonBuilder().create();
            metadata = gson.fromJson(stream, ProgramMetadata.class);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem loading compute program %s. ", resourceLocation));
        } catch (NullPointerException e) {
            throw new MissingResourceException("There is no program. ",
                    "com.cleanroommc.cleanroom.compute.programs.ComputeProgram",
                    resourceLocation.toString());
        }
    }

    public void compile(ProgramCacheIntegrityTable cache, MemoryStack stack) {
        IntBuffer err_code = stack.mallocInt(1);
        String src = MinecraftResourceUtils.readText(new ResourceLocation(resourceLocation.getNamespace(),
                "compute/" + resourceLocation.getPath() + ".cl"), MinecraftResourceUtils.NewLineType.BACK_SLASH_N);
        if (cache.contains(resourceLocation)) {
            if (cache.compare(resourceLocation, src)) {
                // TODO: read binary from cache
                return;
            }
        }
        long program = CL10.clCreateProgramWithSource(Compute.instance().context, src, err_code);
        switch(err_code.get(0)) {
            case CL10.CL_INVALID_VALUE -> throw new NullPointerException(String.format("Source code of %s is null. ", resourceLocation));
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL program.");
        }
        programHandle = program;
        Set<ResourceLocation> dependencies = getHeadersFromFile(src, resourceLocation);
        List<PointerBuffer> pathList = new ObjectArrayList<>();
        PointerBuffer paths = stack.mallocPointer(dependencies.size());
        PointerBuffer libraryHandles = stack.mallocPointer(dependencies.size());
        for (ResourceLocation rl : dependencies) {
            String fname = String.format("%s/%s", rl.getNamespace(), rl.getPath());
            PointerBuffer buf = stack.mallocPointer(fname.length());
            buf.getByteBuffer(0, fname.length()).put(fname.getBytes());
            pathList.add(buf);
            paths.putAddressOf(buf);
            libraryHandles.put(Compute.instance().getOrCreateLibrary(rl, stack));
        }
        PointerBuffer devices = stack.mallocPointer(Compute.instance().devices.length);
        devices.put(Compute.instance().devices);
        devices.rewind();
        paths.rewind();
        libraryHandles.rewind();
        switch (CL12.clCompileProgram(program, devices, "", libraryHandles, paths, null, 0)) {
            case CL12.CL_COMPILER_NOT_AVAILABLE -> throw new CompilationError("Compiler unavailable.");
            case CL12.CL_COMPILE_PROGRAM_FAILURE -> throw new CompilationError(String.format("Failed to compile program %s.", resourceLocation));
            case CL10.CL_INVALID_OPERATION -> throw new Error("Invalid Operation. Program either has no source, already has kernel objects attached or has been compiled during program creation.");
            case CL10.CL_INVALID_PROGRAM -> throw new RuntimeException("Invalid program.");
            case CL10.CL_INVALID_DEVICE -> throw new RuntimeException("Invalid device.");
            case CL10.CL_INVALID_VALUE -> throw new RuntimeException("Invalid value.");
            case CL12.CL_INVALID_COMPILER_OPTIONS -> throw new CompilationError("Invalid compiler options");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL program.");
        }
        PointerBuffer binaryCount = stack.mallocPointer(1);
        CL10.clGetProgramInfo(program, CL10.CL_PROGRAM_BINARY_SIZES, (ByteBuffer) null, binaryCount);
        IntBuffer binarySizes = stack.mallocInt((int) binaryCount.get(0)); // Hopefully no one creates a 2 GB OpenCL program
        CL10.clGetProgramInfo(program, CL10.CL_PROGRAM_BINARY_SIZES, binarySizes, binaryCount);
        compiledProgramBinary = new byte[binarySizes.get(0)][];
        List<PointerBuffer> binaryList = new ObjectArrayList<>();
        PointerBuffer binaries = stack.mallocPointer((int) binaryCount.get(0));
        for (int i = 0; i < binaryCount.get(0); i++) {
            PointerBuffer buf = stack.mallocPointer(binarySizes.get(i));
            binaryList.add(buf);
            binaries.putAddressOf(buf);
        }
        CL10.clGetProgramInfo(program, CL10.CL_PROGRAM_BINARIES, binaries, binaryCount);
        for (int i = 0; i < compiledProgramBinary.length; i++) {
            compiledProgramBinary[i] = binaryList.get(i).getByteBuffer(0, binarySizes.get(i)).array();
        }
        // TODO: Save to cache
    }

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

    static class ProgramMetadata {
        @SerializedName("kernels")
        public Map<String, KernelMetadata> kernels = new Object2ObjectAVLTreeMap<>();
    }
}
