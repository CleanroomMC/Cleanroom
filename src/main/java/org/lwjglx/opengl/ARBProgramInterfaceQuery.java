package org.lwjglx.opengl;

public class ARBProgramInterfaceQuery {

    public static final int GL_ACTIVE_RESOURCES = (int) 37621;
    public static final int GL_ACTIVE_VARIABLES = (int) 37637;
    public static final int GL_ARRAY_SIZE = (int) 37627;
    public static final int GL_ARRAY_STRIDE = (int) 37630;
    public static final int GL_ATOMIC_COUNTER_BUFFER_INDEX = (int) 37633;
    public static final int GL_BLOCK_INDEX = (int) 37629;
    public static final int GL_BUFFER_BINDING = (int) 37634;
    public static final int GL_BUFFER_DATA_SIZE = (int) 37635;
    public static final int GL_BUFFER_VARIABLE = (int) 37605;
    public static final int GL_COMPUTE_SUBROUTINE = (int) 37613;
    public static final int GL_COMPUTE_SUBROUTINE_UNIFORM = (int) 37619;
    public static final int GL_FRAGMENT_SUBROUTINE = (int) 37612;
    public static final int GL_FRAGMENT_SUBROUTINE_UNIFORM = (int) 37618;
    public static final int GL_GEOMETRY_SUBROUTINE = (int) 37611;
    public static final int GL_GEOMETRY_SUBROUTINE_UNIFORM = (int) 37617;
    public static final int GL_IS_PER_PATCH = (int) 37607;
    public static final int GL_IS_ROW_MAJOR = (int) 37632;
    public static final int GL_LOCATION = (int) 37646;
    public static final int GL_LOCATION_INDEX = (int) 37647;
    public static final int GL_MATRIX_STRIDE = (int) 37631;
    public static final int GL_MAX_NAME_LENGTH = (int) 37622;
    public static final int GL_MAX_NUM_ACTIVE_VARIABLES = (int) 37623;
    public static final int GL_MAX_NUM_COMPATIBLE_SUBROUTINES = (int) 37624;
    public static final int GL_NAME_LENGTH = (int) 37625;
    public static final int GL_NUM_ACTIVE_VARIABLES = (int) 37636;
    public static final int GL_OFFSET = (int) 37628;
    public static final int GL_PROGRAM_INPUT = (int) 37603;
    public static final int GL_PROGRAM_OUTPUT = (int) 37604;
    public static final int GL_REFERENCED_BY_COMPUTE_SHADER = (int) 37643;
    public static final int GL_REFERENCED_BY_FRAGMENT_SHADER = (int) 37642;
    public static final int GL_REFERENCED_BY_GEOMETRY_SHADER = (int) 37641;
    public static final int GL_REFERENCED_BY_TESS_CONTROL_SHADER = (int) 37639;
    public static final int GL_REFERENCED_BY_TESS_EVALUATION_SHADER = (int) 37640;
    public static final int GL_REFERENCED_BY_VERTEX_SHADER = (int) 37638;
    public static final int GL_SHADER_STORAGE_BLOCK = (int) 37606;
    public static final int GL_TESS_CONTROL_SUBROUTINE = (int) 37609;
    public static final int GL_TESS_CONTROL_SUBROUTINE_UNIFORM = (int) 37615;
    public static final int GL_TESS_EVALUATION_SUBROUTINE = (int) 37610;
    public static final int GL_TESS_EVALUATION_SUBROUTINE_UNIFORM = (int) 37616;
    public static final int GL_TOP_LEVEL_ARRAY_SIZE = (int) 37644;
    public static final int GL_TOP_LEVEL_ARRAY_STRIDE = (int) 37645;
    public static final int GL_TRANSFORM_FEEDBACK_VARYING = (int) 37620;
    public static final int GL_TYPE = (int) 37626;
    public static final int GL_UNIFORM = (int) 37601;
    public static final int GL_UNIFORM_BLOCK = (int) 37602;
    public static final int GL_VERTEX_SUBROUTINE = (int) 37608;
    public static final int GL_VERTEX_SUBROUTINE_UNIFORM = (int) 37614;

    public static int glGetProgramInterfacei(int program, int programInterface, int pname) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery.glGetProgramInterfacei(program, programInterface, pname);
    }

    public static int glGetProgramResourceIndex(int program, int programInterface, java.lang.CharSequence name) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery.glGetProgramResourceIndex(program, programInterface, name);
    }

    public static int glGetProgramResourceIndex(int program, int programInterface, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery.glGetProgramResourceIndex(program, programInterface, name);
    }

    public static int glGetProgramResourceLocation(int program, int programInterface, java.lang.CharSequence name) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery.glGetProgramResourceLocation(program, programInterface, name);
    }

    public static int glGetProgramResourceLocation(int program, int programInterface, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery.glGetProgramResourceLocation(program, programInterface, name);
    }

    public static int glGetProgramResourceLocationIndex(int program, int programInterface,
            java.lang.CharSequence name) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery
                .glGetProgramResourceLocationIndex(program, programInterface, name);
    }

    public static int glGetProgramResourceLocationIndex(int program, int programInterface, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery
                .glGetProgramResourceLocationIndex(program, programInterface, name);
    }

    public static java.lang.String glGetProgramResourceName(int program, int programInterface, int index, int bufSize) {
        return org.lwjgl.opengl.ARBProgramInterfaceQuery
                .glGetProgramResourceName(program, programInterface, index, bufSize);
    }

    public static void glGetProgramResourceName(int program, int programInterface, int index, java.nio.IntBuffer length,
            java.nio.ByteBuffer name) {
        org.lwjgl.opengl.ARBProgramInterfaceQuery
                .glGetProgramResourceName(program, programInterface, index, length, name);
    }
}
