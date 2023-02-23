package org.lwjglx.opengl;

public class NVPathRendering {

    public static final int GL_ACCUM_ADJACENT_PAIRS_NV = (int) 37037;
    public static final int GL_ADJACENT_PAIRS_NV = (int) 37038;
    public static final int GL_AFFINE_2D_NV = (int) 37010;
    public static final int GL_AFFINE_3D_NV = (int) 37012;
    public static final int GL_ARC_TO_NV = (int) 254;
    public static final int GL_BEVEL_NV = (int) 37030;
    public static final int GL_BOLD_BIT_NV = (int) 1;
    public static final int GL_BOUNDING_BOX_NV = (int) 37005;
    public static final int GL_BOUNDING_BOX_OF_BOUNDING_BOXES_NV = (int) 37020;
    public static final int GL_CIRCULAR_CCW_ARC_TO_NV = (int) 248;
    public static final int GL_CIRCULAR_CW_ARC_TO_NV = (int) 250;
    public static final int GL_CIRCULAR_TANGENT_ARC_TO_NV = (int) 252;
    public static final int GL_CLOSE_PATH_NV = (int) 0;
    public static final int GL_CONVEX_HULL_NV = (int) 37003;
    public static final int GL_COUNT_DOWN_NV = (int) 37001;
    public static final int GL_COUNT_UP_NV = (int) 37000;
    public static final int GL_CUBIC_CURVE_TO_NV = (int) 12;
    public static final int GL_FILE_NAME_NV = (int) 36980;
    public static final int GL_FIRST_TO_REST_NV = (int) 37039;
    public static final int GL_FONT_ASCENDER_NV = (int) 2097152;
    public static final int GL_FONT_DESCENDER_NV = (int) 4194304;
    public static final int GL_FONT_HAS_KERNING_NV = (int) 268435456;
    public static final int GL_FONT_HEIGHT_NV = (int) 8388608;
    public static final int GL_FONT_MAX_ADVANCE_HEIGHT_NV = (int) 33554432;
    public static final int GL_FONT_MAX_ADVANCE_WIDTH_NV = (int) 16777216;
    public static final int GL_FONT_UNDERLINE_POSITION_NV = (int) 67108864;
    public static final int GL_FONT_UNDERLINE_THICKNESS_NV = (int) 134217728;
    public static final int GL_FONT_UNITS_PER_EM_NV = (int) 1048576;
    public static final int GL_FONT_X_MAX_BOUNDS_NV = (int) 262144;
    public static final int GL_FONT_X_MIN_BOUNDS_NV = (int) 65536;
    public static final int GL_FONT_Y_MAX_BOUNDS_NV = (int) 524288;
    public static final int GL_FONT_Y_MIN_BOUNDS_NV = (int) 131072;
    public static final int GL_GLYPH_HAS_KERNING_NV = (int) 256;
    public static final int GL_GLYPH_HEIGHT_BIT_NV = (int) 2;
    public static final int GL_GLYPH_HORIZONTAL_BEARING_ADVANCE_BIT_NV = (int) 16;
    public static final int GL_GLYPH_HORIZONTAL_BEARING_X_BIT_NV = (int) 4;
    public static final int GL_GLYPH_HORIZONTAL_BEARING_Y_BIT_NV = (int) 8;
    public static final int GL_GLYPH_VERTICAL_BEARING_ADVANCE_BIT_NV = (int) 128;
    public static final int GL_GLYPH_VERTICAL_BEARING_X_BIT_NV = (int) 32;
    public static final int GL_GLYPH_VERTICAL_BEARING_Y_BIT_NV = (int) 64;
    public static final int GL_GLYPH_WIDTH_BIT_NV = (int) 1;
    public static final int GL_HORIZONTAL_LINE_TO_NV = (int) 6;
    public static final int GL_ITALIC_BIT_NV = (int) 2;
    public static final int GL_LARGE_CCW_ARC_TO_NV = (int) 22;
    public static final int GL_LARGE_CW_ARC_TO_NV = (int) 24;
    public static final int GL_LINE_TO_NV = (int) 4;
    public static final int GL_MITER_REVERT_NV = (int) 37031;
    public static final int GL_MITER_TRUNCATE_NV = (int) 37032;
    public static final int GL_MOVE_TO_CONTINUES_NV = (int) 37046;
    public static final int GL_MOVE_TO_NV = (int) 2;
    public static final int GL_MOVE_TO_RESETS_NV = (int) 37045;
    public static final int GL_PATH_CLIENT_LENGTH_NV = (int) 36991;
    public static final int GL_PATH_COMMAND_COUNT_NV = (int) 37021;
    public static final int GL_PATH_COMPUTED_LENGTH_NV = (int) 37024;
    public static final int GL_PATH_COORD_COUNT_NV = (int) 37022;
    public static final int GL_PATH_COVER_DEPTH_FUNC_NV = (int) 37055;
    public static final int GL_PATH_DASH_ARRAY_COUNT_NV = (int) 37023;
    public static final int GL_PATH_DASH_CAPS_NV = (int) 36987;
    public static final int GL_PATH_DASH_OFFSET_NV = (int) 36990;
    public static final int GL_PATH_DASH_OFFSET_RESET_NV = (int) 37044;
    public static final int GL_PATH_END_CAPS_NV = (int) 36982;
    public static final int GL_PATH_ERROR_POSITION_NV = (int) 37035;
    public static final int GL_PATH_FILL_BOUNDING_BOX_NV = (int) 37025;
    public static final int GL_PATH_FILL_COVER_MODE_NV = (int) 36994;
    public static final int GL_PATH_FILL_MASK_NV = (int) 36993;
    public static final int GL_PATH_FILL_MODE_NV = (int) 36992;
    public static final int GL_PATH_FOG_GEN_MODE_NV = (int) 37036;
    public static final int GL_PATH_FORMAT_PS_NV = (int) 36977;
    public static final int GL_PATH_FORMAT_SVG_NV = (int) 36976;
    public static final int GL_PATH_GEN_COEFF_NV = (int) 37041;
    public static final int GL_PATH_GEN_COLOR_FORMAT_NV = (int) 37042;
    public static final int GL_PATH_GEN_COMPONENTS_NV = (int) 37043;
    public static final int GL_PATH_GEN_MODE_NV = (int) 37040;
    public static final int GL_PATH_INITIAL_DASH_CAP_NV = (int) 36988;
    public static final int GL_PATH_INITIAL_END_CAP_NV = (int) 36983;
    public static final int GL_PATH_JOIN_STYLE_NV = (int) 36985;
    public static final int GL_PATH_MITER_LIMIT_NV = (int) 36986;
    public static final int GL_PATH_OBJECT_BOUNDING_BOX_NV = (int) 37002;
    public static final int GL_PATH_STENCIL_DEPTH_OFFSET_FACTOR_NV = (int) 37053;
    public static final int GL_PATH_STENCIL_DEPTH_OFFSET_UNITS_NV = (int) 37054;
    public static final int GL_PATH_STENCIL_FUNC_NV = (int) 37047;
    public static final int GL_PATH_STENCIL_REF_NV = (int) 37048;
    public static final int GL_PATH_STENCIL_VALUE_MASK_NV = (int) 37049;
    public static final int GL_PATH_STROKE_BOUNDING_BOX_NV = (int) 37026;
    public static final int GL_PATH_STROKE_COVER_MODE_NV = (int) 36995;
    public static final int GL_PATH_STROKE_MASK_NV = (int) 36996;
    public static final int GL_PATH_STROKE_WIDTH_NV = (int) 36981;
    public static final int GL_PATH_TERMINAL_DASH_CAP_NV = (int) 36989;
    public static final int GL_PATH_TERMINAL_END_CAP_NV = (int) 36984;
    public static final int GL_PRIMARY_COLOR = (int) 34167;
    public static final int GL_PRIMARY_COLOR_NV = (int) 34092;
    public static final int GL_QUADRATIC_CURVE_TO_NV = (int) 10;
    public static final int GL_RELATIVE_ARC_TO_NV = (int) 255;
    public static final int GL_RELATIVE_CUBIC_CURVE_TO_NV = (int) 13;
    public static final int GL_RELATIVE_HORIZONTAL_LINE_TO_NV = (int) 7;
    public static final int GL_RELATIVE_LARGE_CCW_ARC_TO_NV = (int) 23;
    public static final int GL_RELATIVE_LARGE_CW_ARC_TO_NV = (int) 25;
    public static final int GL_RELATIVE_LINE_TO_NV = (int) 5;
    public static final int GL_RELATIVE_MOVE_TO_NV = (int) 3;
    public static final int GL_RELATIVE_QUADRATIC_CURVE_TO_NV = (int) 11;
    public static final int GL_RELATIVE_SMALL_CCW_ARC_TO_NV = (int) 19;
    public static final int GL_RELATIVE_SMALL_CW_ARC_TO_NV = (int) 21;
    public static final int GL_RELATIVE_SMOOTH_CUBIC_CURVE_TO_NV = (int) 17;
    public static final int GL_RELATIVE_SMOOTH_QUADRATIC_CURVE_TO_NV = (int) 15;
    public static final int GL_RELATIVE_VERTICAL_LINE_TO_NV = (int) 9;
    public static final int GL_ROUND_NV = (int) 37028;
    public static final int GL_SECONDARY_COLOR_NV = (int) 34093;
    public static final int GL_SKIP_MISSING_GLYPH_NV = (int) 37033;
    public static final int GL_SMALL_CCW_ARC_TO_NV = (int) 18;
    public static final int GL_SMALL_CW_ARC_TO_NV = (int) 20;
    public static final int GL_SMOOTH_CUBIC_CURVE_TO_NV = (int) 16;
    public static final int GL_SMOOTH_QUADRATIC_CURVE_TO_NV = (int) 14;
    public static final int GL_SQUARE_NV = (int) 37027;
    public static final int GL_STANDARD_FONT_NAME_NV = (int) 36978;
    public static final int GL_SYSTEM_FONT_NAME_NV = (int) 36979;
    public static final int GL_TRANSLATE_2D_NV = (int) 37008;
    public static final int GL_TRANSLATE_3D_NV = (int) 37009;
    public static final int GL_TRANSLATE_X_NV = (int) 37006;
    public static final int GL_TRANSLATE_Y_NV = (int) 37007;
    public static final int GL_TRANSPOSE_AFFINE_2D_NV = (int) 37014;
    public static final int GL_TRANSPOSE_AFFINE_3D_NV = (int) 37016;
    public static final int GL_TRIANGULAR_NV = (int) 37029;
    public static final int GL_USE_MISSING_GLYPH_NV = (int) 37034;
    public static final int GL_UTF16_NV = (int) 37019;
    public static final int GL_UTF8_NV = (int) 37018;
    public static final int GL_VERTICAL_LINE_TO_NV = (int) 8;

    public static void glCopyPathNV(int resultPath, int srcPath) {
        org.lwjgl.opengl.NVPathRendering.glCopyPathNV(resultPath, srcPath);
    }

    public static void glCoverFillPathInstancedNV(int pathNameType, java.nio.ByteBuffer paths, int pathBase,
            int coverMode, int transformType, java.nio.FloatBuffer transformValues) {
        org.lwjgl.opengl.NVPathRendering
                .glCoverFillPathInstancedNV(pathNameType, paths, pathBase, coverMode, transformType, transformValues);
    }

    public static void glCoverFillPathNV(int path, int coverMode) {
        org.lwjgl.opengl.NVPathRendering.glCoverFillPathNV(path, coverMode);
    }

    public static void glCoverStrokePathInstancedNV(int pathNameType, java.nio.ByteBuffer paths, int pathBase,
            int coverMode, int transformType, java.nio.FloatBuffer transformValues) {
        org.lwjgl.opengl.NVPathRendering
                .glCoverStrokePathInstancedNV(pathNameType, paths, pathBase, coverMode, transformType, transformValues);
    }

    public static void glCoverStrokePathNV(int name, int coverMode) {
        org.lwjgl.opengl.NVPathRendering.glCoverStrokePathNV(name, coverMode);
    }

    public static void glDeletePathsNV(int path, int range) {
        org.lwjgl.opengl.NVPathRendering.glDeletePathsNV(path, range);
    }

    public static int glGenPathsNV(int range) {
        return org.lwjgl.opengl.NVPathRendering.glGenPathsNV(range);
    }

    public static void glGetPathColorGenNV(int color, int pname, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glGetPathColorGenfvNV(color, pname, value);
    }

    public static void glGetPathColorGenNV(int color, int pname, java.nio.IntBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glGetPathColorGenivNV(color, pname, value);
    }

    public static float glGetPathColorGenfNV(int color, int pname) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathColorGenfNV(color, pname);
    }

    public static int glGetPathColorGeniNV(int color, int pname) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathColorGeniNV(color, pname);
    }

    public static void glGetPathCommandsNV(int name, java.nio.ByteBuffer commands) {
        org.lwjgl.opengl.NVPathRendering.glGetPathCommandsNV(name, commands);
    }

    public static void glGetPathCoordsNV(int name, java.nio.FloatBuffer coords) {
        org.lwjgl.opengl.NVPathRendering.glGetPathCoordsNV(name, coords);
    }

    public static void glGetPathDashArrayNV(int name, java.nio.FloatBuffer dashArray) {
        org.lwjgl.opengl.NVPathRendering.glGetPathDashArrayNV(name, dashArray);
    }

    public static float glGetPathLengthNV(int path, int startSegment, int numSegments) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathLengthNV(path, startSegment, numSegments);
    }

    public static void glGetPathMetricRangeNV(int metricQueryMask, int fistPathName, int numPaths, int stride,
            java.nio.FloatBuffer metrics) {
        org.lwjgl.opengl.NVPathRendering
                .glGetPathMetricRangeNV(metricQueryMask, fistPathName, numPaths, stride, metrics);
    }

    public static void glGetPathMetricsNV(int metricQueryMask, int pathNameType, java.nio.ByteBuffer paths,
            int pathBase, int stride, java.nio.FloatBuffer metrics) {
        org.lwjgl.opengl.NVPathRendering
                .glGetPathMetricsNV(metricQueryMask, pathNameType, paths, pathBase, stride, metrics);
    }

    public static void glGetPathParameterNV(int name, int param, java.nio.IntBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glGetPathParameterivNV(name, param, value);
    }

    public static float glGetPathParameterfNV(int name, int param) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathParameterfNV(name, param);
    }

    public static void glGetPathParameterfvNV(int name, int param, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glGetPathParameterfvNV(name, param, value);
    }

    public static int glGetPathParameteriNV(int name, int param) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathParameteriNV(name, param);
    }

    public static void glGetPathSpacingNV(int pathListMode, int pathNameType, java.nio.ByteBuffer paths, int pathBase,
            float advanceScale, float kerningScale, int transformType, java.nio.FloatBuffer returnedSpacing) {
        org.lwjgl.opengl.NVPathRendering.glGetPathSpacingNV(
                pathListMode,
                pathNameType,
                paths,
                pathBase,
                advanceScale,
                kerningScale,
                transformType,
                returnedSpacing);
    }

    public static void glGetPathTexGenNV(int texCoordSet, int pname, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glGetPathTexGenfvNV(texCoordSet, pname, value);
    }

    public static void glGetPathTexGenNV(int texCoordSet, int pname, java.nio.IntBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glGetPathTexGenivNV(texCoordSet, pname, value);
    }

    public static float glGetPathTexGenfNV(int texCoordSet, int pname) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathTexGenfNV(texCoordSet, pname);
    }

    public static int glGetPathTexGeniNV(int texCoordSet, int pname) {
        return org.lwjgl.opengl.NVPathRendering.glGetPathTexGeniNV(texCoordSet, pname);
    }

    public static void glInterpolatePathsNV(int resultPath, int pathA, int pathB, float weight) {
        org.lwjgl.opengl.NVPathRendering.glInterpolatePathsNV(resultPath, pathA, pathB, weight);
    }

    public static boolean glIsPathNV(int path) {
        return org.lwjgl.opengl.NVPathRendering.glIsPathNV(path);
    }

    public static boolean glIsPointInFillPathNV(int path, int mask, float x, float y) {
        return org.lwjgl.opengl.NVPathRendering.glIsPointInFillPathNV(path, mask, x, y);
    }

    public static boolean glIsPointInStrokePathNV(int path, float x, float y) {
        return org.lwjgl.opengl.NVPathRendering.glIsPointInStrokePathNV(path, x, y);
    }

    public static void glPathColorGenNV(int color, int genMode, int colorFormat, java.nio.FloatBuffer coeffs) {
        org.lwjgl.opengl.NVPathRendering.glPathColorGenNV(color, genMode, colorFormat, coeffs);
    }

    public static void glPathCommandsNV(int path, java.nio.ByteBuffer commands, int coordType,
            java.nio.ByteBuffer coords) {
        org.lwjgl.opengl.NVPathRendering.glPathCommandsNV(path, commands, coordType, coords);
    }

    public static void glPathCoordsNV(int path, int coordType, java.nio.ByteBuffer coords) {
        org.lwjgl.opengl.NVPathRendering.glPathCoordsNV(path, coordType, coords);
    }

    public static void glPathCoverDepthFuncNV(int zfunc) {
        org.lwjgl.opengl.NVPathRendering.glPathCoverDepthFuncNV(zfunc);
    }

    public static void glPathDashArrayNV(int path, java.nio.FloatBuffer dashArray) {
        org.lwjgl.opengl.NVPathRendering.glPathDashArrayNV(path, dashArray);
    }

    public static void glPathFogGenNV(int genMode) {
        org.lwjgl.opengl.NVPathRendering.glPathFogGenNV(genMode);
    }

    public static void glPathGlyphRangeNV(int firstPathName, int fontTarget, java.nio.ByteBuffer fontName,
            int fontStyle, int firstGlyph, int numGlyphs, int handleMissingGlyphs, int pathParameterTemplate,
            float emScale) {
        org.lwjgl.opengl.NVPathRendering.glPathGlyphRangeNV(
                firstPathName,
                fontTarget,
                fontName,
                fontStyle,
                firstGlyph,
                numGlyphs,
                handleMissingGlyphs,
                pathParameterTemplate,
                emScale);
    }

    public static void glPathGlyphsNV(int firstPathName, int fontTarget, java.nio.ByteBuffer fontName, int fontStyle,
            int type, java.nio.ByteBuffer charcodes, int handleMissingGlyphs, int pathParameterTemplate,
            float emScale) {
        org.lwjgl.opengl.NVPathRendering.glPathGlyphsNV(
                firstPathName,
                fontTarget,
                fontName,
                fontStyle,
                type,
                charcodes,
                handleMissingGlyphs,
                pathParameterTemplate,
                emScale);
    }

    public static void glPathParameterNV(int path, int pname, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glPathParameterfvNV(path, pname, value);
    }

    public static void glPathParameterNV(int path, int pname, java.nio.IntBuffer value) {
        org.lwjgl.opengl.NVPathRendering.glPathParameterivNV(path, pname, value);
    }

    public static void glPathParameterfNV(int path, int pname, float value) {
        org.lwjgl.opengl.NVPathRendering.glPathParameterfNV(path, pname, value);
    }

    public static void glPathParameteriNV(int path, int pname, int value) {
        org.lwjgl.opengl.NVPathRendering.glPathParameteriNV(path, pname, value);
    }

    public static void glPathStencilFuncNV(int func, int ref, int mask) {
        org.lwjgl.opengl.NVPathRendering.glPathStencilFuncNV(func, ref, mask);
    }

    public static void glPathStringNV(int path, int format, java.nio.ByteBuffer pathString) {
        org.lwjgl.opengl.NVPathRendering.glPathStringNV(path, format, pathString);
    }

    public static void glPathSubCommandsNV(int path, int commandStart, int commandsToDelete,
            java.nio.ByteBuffer commands, int coordType, java.nio.ByteBuffer coords) {
        org.lwjgl.opengl.NVPathRendering
                .glPathSubCommandsNV(path, commandStart, commandsToDelete, commands, coordType, coords);
    }

    public static void glPathSubCoordsNV(int path, int coordStart, int coordType, java.nio.ByteBuffer coords) {
        org.lwjgl.opengl.NVPathRendering.glPathSubCoordsNV(path, coordStart, coordType, coords);
    }

    public static boolean glPointAlongPathNV(int path, int startSegment, int numSegments, float distance,
            java.nio.FloatBuffer x, java.nio.FloatBuffer y, java.nio.FloatBuffer tangentX,
            java.nio.FloatBuffer tangentY) {
        return org.lwjgl.opengl.NVPathRendering
                .glPointAlongPathNV(path, startSegment, numSegments, distance, x, y, tangentX, tangentY);
    }

    public static void glStencilFillPathInstancedNV(int pathNameType, java.nio.ByteBuffer paths, int pathBase,
            int fillMode, int mask, int transformType, java.nio.FloatBuffer transformValues) {
        org.lwjgl.opengl.NVPathRendering.glStencilFillPathInstancedNV(
                pathNameType,
                paths,
                pathBase,
                fillMode,
                mask,
                transformType,
                transformValues);
    }

    public static void glStencilFillPathNV(int path, int fillMode, int mask) {
        org.lwjgl.opengl.NVPathRendering.glStencilFillPathNV(path, fillMode, mask);
    }

    public static void glStencilStrokePathInstancedNV(int pathNameType, java.nio.ByteBuffer paths, int pathBase,
            int reference, int mask, int transformType, java.nio.FloatBuffer transformValues) {
        org.lwjgl.opengl.NVPathRendering.glStencilStrokePathInstancedNV(
                pathNameType,
                paths,
                pathBase,
                reference,
                mask,
                transformType,
                transformValues);
    }

    public static void glStencilStrokePathNV(int path, int reference, int mask) {
        org.lwjgl.opengl.NVPathRendering.glStencilStrokePathNV(path, reference, mask);
    }

    public static void glTransformPathNV(int resultPath, int srcPath, int transformType,
            java.nio.FloatBuffer transformValues) {
        org.lwjgl.opengl.NVPathRendering.glTransformPathNV(resultPath, srcPath, transformType, transformValues);
    }

    public static void glWeightPathsNV(int resultPath, java.nio.IntBuffer paths, java.nio.FloatBuffer weights) {
        org.lwjgl.opengl.NVPathRendering.glWeightPathsNV(resultPath, paths, weights);
    }
}
