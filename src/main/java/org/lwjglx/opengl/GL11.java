package org.lwjglx.opengl;

import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

public class GL11 {

    public static final int GL_2D = (int) 1536;
    public static final int GL_2_BYTES = (int) 5127;
    public static final int GL_3D = (int) 1537;
    public static final int GL_3D_COLOR = (int) 1538;
    public static final int GL_3D_COLOR_TEXTURE = (int) 1539;
    public static final int GL_3_BYTES = (int) 5128;
    public static final int GL_4D_COLOR_TEXTURE = (int) 1540;
    public static final int GL_4_BYTES = (int) 5129;
    public static final int GL_ACCUM = (int) 256;
    public static final int GL_ACCUM_ALPHA_BITS = (int) 3419;
    public static final int GL_ACCUM_BLUE_BITS = (int) 3418;
    public static final int GL_ACCUM_BUFFER_BIT = (int) 512;
    public static final int GL_ACCUM_CLEAR_VALUE = (int) 2944;
    public static final int GL_ACCUM_GREEN_BITS = (int) 3417;
    public static final int GL_ACCUM_RED_BITS = (int) 3416;
    public static final int GL_ADD = (int) 260;
    public static final int GL_ALL_ATTRIB_BITS = (int) 1048575;
    public static final int GL_ALL_CLIENT_ATTRIB_BITS = (int) -1;
    public static final int GL_ALPHA12 = (int) 32829;
    public static final int GL_ALPHA16 = (int) 32830;
    public static final int GL_ALPHA4 = (int) 32827;
    public static final int GL_ALPHA8 = (int) 32828;
    public static final int GL_ALPHA = (int) 6406;
    public static final int GL_ALPHA_BIAS = (int) 3357;
    public static final int GL_ALPHA_BITS = (int) 3413;
    public static final int GL_ALPHA_SCALE = (int) 3356;
    public static final int GL_ALPHA_TEST = (int) 3008;
    public static final int GL_ALPHA_TEST_FUNC = (int) 3009;
    public static final int GL_ALPHA_TEST_REF = (int) 3010;
    public static final int GL_ALWAYS = (int) 519;
    public static final int GL_AMBIENT = (int) 4608;
    public static final int GL_AMBIENT_AND_DIFFUSE = (int) 5634;
    public static final int GL_AND = (int) 5377;
    public static final int GL_AND_INVERTED = (int) 5380;
    public static final int GL_AND_REVERSE = (int) 5378;
    public static final int GL_ATTRIB_STACK_DEPTH = (int) 2992;
    public static final int GL_AUTO_NORMAL = (int) 3456;
    public static final int GL_AUX0 = (int) 1033;
    public static final int GL_AUX1 = (int) 1034;
    public static final int GL_AUX2 = (int) 1035;
    public static final int GL_AUX3 = (int) 1036;
    public static final int GL_AUX_BUFFERS = (int) 3072;
    public static final int GL_BACK = (int) 1029;
    public static final int GL_BACK_LEFT = (int) 1026;
    public static final int GL_BACK_RIGHT = (int) 1027;
    public static final int GL_BITMAP = (int) 6656;
    public static final int GL_BITMAP_TOKEN = (int) 1796;
    public static final int GL_BLEND = (int) 3042;
    public static final int GL_BLEND_DST = (int) 3040;
    public static final int GL_BLEND_SRC = (int) 3041;
    public static final int GL_BLUE = (int) 6405;
    public static final int GL_BLUE_BIAS = (int) 3355;
    public static final int GL_BLUE_BITS = (int) 3412;
    public static final int GL_BLUE_SCALE = (int) 3354;
    public static final int GL_BYTE = (int) 5120;
    public static final int GL_C3F_V3F = (int) 10788;
    public static final int GL_C4F_N3F_V3F = (int) 10790;
    public static final int GL_C4UB_V2F = (int) 10786;
    public static final int GL_C4UB_V3F = (int) 10787;
    public static final int GL_CCW = (int) 2305;
    public static final int GL_CLAMP = (int) 10496;
    public static final int GL_CLEAR = (int) 5376;
    public static final int GL_CLIENT_ATTRIB_STACK_DEPTH = (int) 2993;
    public static final int GL_CLIENT_PIXEL_STORE_BIT = (int) 1;
    public static final int GL_CLIENT_VERTEX_ARRAY_BIT = (int) 2;
    public static final int GL_CLIP_PLANE0 = (int) 12288;
    public static final int GL_CLIP_PLANE1 = (int) 12289;
    public static final int GL_CLIP_PLANE2 = (int) 12290;
    public static final int GL_CLIP_PLANE3 = (int) 12291;
    public static final int GL_CLIP_PLANE4 = (int) 12292;
    public static final int GL_CLIP_PLANE5 = (int) 12293;
    public static final int GL_COEFF = (int) 2560;
    public static final int GL_COLOR = (int) 6144;
    public static final int GL_COLOR_ARRAY = (int) 32886;
    public static final int GL_COLOR_ARRAY_POINTER = (int) 32912;
    public static final int GL_COLOR_ARRAY_SIZE = (int) 32897;
    public static final int GL_COLOR_ARRAY_STRIDE = (int) 32899;
    public static final int GL_COLOR_ARRAY_TYPE = (int) 32898;
    public static final int GL_COLOR_BUFFER_BIT = (int) 16384;
    public static final int GL_COLOR_CLEAR_VALUE = (int) 3106;
    public static final int GL_COLOR_INDEX = (int) 6400;
    public static final int GL_COLOR_INDEXES = (int) 5635;
    public static final int GL_COLOR_LOGIC_OP = (int) 3058;
    public static final int GL_COLOR_MATERIAL = (int) 2903;
    public static final int GL_COLOR_MATERIAL_FACE = (int) 2901;
    public static final int GL_COLOR_MATERIAL_PARAMETER = (int) 2902;
    public static final int GL_COLOR_WRITEMASK = (int) 3107;
    public static final int GL_COMPILE = (int) 4864;
    public static final int GL_COMPILE_AND_EXECUTE = (int) 4865;
    public static final int GL_CONSTANT_ALPHA = (int) 32771;
    public static final int GL_CONSTANT_ATTENUATION = (int) 4615;
    public static final int GL_CONSTANT_COLOR = (int) 32769;
    public static final int GL_COPY = (int) 5379;
    public static final int GL_COPY_INVERTED = (int) 5388;
    public static final int GL_COPY_PIXEL_TOKEN = (int) 1798;
    public static final int GL_CULL_FACE = (int) 2884;
    public static final int GL_CULL_FACE_MODE = (int) 2885;
    public static final int GL_CURRENT_BIT = (int) 1;
    public static final int GL_CURRENT_COLOR = (int) 2816;
    public static final int GL_CURRENT_INDEX = (int) 2817;
    public static final int GL_CURRENT_NORMAL = (int) 2818;
    public static final int GL_CURRENT_RASTER_COLOR = (int) 2820;
    public static final int GL_CURRENT_RASTER_DISTANCE = (int) 2825;
    public static final int GL_CURRENT_RASTER_INDEX = (int) 2821;
    public static final int GL_CURRENT_RASTER_POSITION = (int) 2823;
    public static final int GL_CURRENT_RASTER_POSITION_VALID = (int) 2824;
    public static final int GL_CURRENT_RASTER_TEXTURE_COORDS = (int) 2822;
    public static final int GL_CURRENT_TEXTURE_COORDS = (int) 2819;
    public static final int GL_CW = (int) 2304;
    public static final int GL_DECAL = (int) 8449;
    public static final int GL_DECR = (int) 7683;
    public static final int GL_DEPTH = (int) 6145;
    public static final int GL_DEPTH_BIAS = (int) 3359;
    public static final int GL_DEPTH_BITS = (int) 3414;
    public static final int GL_DEPTH_BUFFER_BIT = (int) 256;
    public static final int GL_DEPTH_CLEAR_VALUE = (int) 2931;
    public static final int GL_DEPTH_COMPONENT = (int) 6402;
    public static final int GL_DEPTH_FUNC = (int) 2932;
    public static final int GL_DEPTH_RANGE = (int) 2928;
    public static final int GL_DEPTH_SCALE = (int) 3358;
    public static final int GL_DEPTH_TEST = (int) 2929;
    public static final int GL_DEPTH_WRITEMASK = (int) 2930;
    public static final int GL_DIFFUSE = (int) 4609;
    public static final int GL_DITHER = (int) 3024;
    public static final int GL_DOMAIN = (int) 2562;
    public static final int GL_DONT_CARE = (int) 4352;
    public static final int GL_DOUBLE = (int) 5130;
    public static final int GL_DOUBLEBUFFER = (int) 3122;
    public static final int GL_DRAW_BUFFER = (int) 3073;
    public static final int GL_DRAW_PIXEL_TOKEN = (int) 1797;
    public static final int GL_DST_ALPHA = (int) 772;
    public static final int GL_DST_COLOR = (int) 774;
    public static final int GL_EDGE_FLAG = (int) 2883;
    public static final int GL_EDGE_FLAG_ARRAY = (int) 32889;
    public static final int GL_EDGE_FLAG_ARRAY_POINTER = (int) 32915;
    public static final int GL_EDGE_FLAG_ARRAY_STRIDE = (int) 32908;
    public static final int GL_EMISSION = (int) 5632;
    public static final int GL_ENABLE_BIT = (int) 8192;
    public static final int GL_EQUAL = (int) 514;
    public static final int GL_EQUIV = (int) 5385;
    public static final int GL_EVAL_BIT = (int) 65536;
    public static final int GL_EXP2 = (int) 2049;
    public static final int GL_EXP = (int) 2048;
    public static final int GL_EXTENSIONS = (int) 7939;
    public static final int GL_EYE_LINEAR = (int) 9216;
    public static final int GL_EYE_PLANE = (int) 9474;
    public static final int GL_FALSE = (int) 0;
    public static final int GL_FASTEST = (int) 4353;
    public static final int GL_FEEDBACK = (int) 7169;
    public static final int GL_FEEDBACK_BUFFER_POINTER = (int) 3568;
    public static final int GL_FEEDBACK_BUFFER_SIZE = (int) 3569;
    public static final int GL_FEEDBACK_BUFFER_TYPE = (int) 3570;
    public static final int GL_FILL = (int) 6914;
    public static final int GL_FLAT = (int) 7424;
    public static final int GL_FLOAT = (int) 5126;
    public static final int GL_FOG = (int) 2912;
    public static final int GL_FOG_BIT = (int) 128;
    public static final int GL_FOG_COLOR = (int) 2918;
    public static final int GL_FOG_DENSITY = (int) 2914;
    public static final int GL_FOG_END = (int) 2916;
    public static final int GL_FOG_HINT = (int) 3156;
    public static final int GL_FOG_INDEX = (int) 2913;
    public static final int GL_FOG_MODE = (int) 2917;
    public static final int GL_FOG_START = (int) 2915;
    public static final int GL_FRONT = (int) 1028;
    public static final int GL_FRONT_AND_BACK = (int) 1032;
    public static final int GL_FRONT_FACE = (int) 2886;
    public static final int GL_FRONT_LEFT = (int) 1024;
    public static final int GL_FRONT_RIGHT = (int) 1025;
    public static final int GL_GEQUAL = (int) 518;
    public static final int GL_GREATER = (int) 516;
    public static final int GL_GREEN = (int) 6404;
    public static final int GL_GREEN_BIAS = (int) 3353;
    public static final int GL_GREEN_BITS = (int) 3411;
    public static final int GL_GREEN_SCALE = (int) 3352;
    public static final int GL_HINT_BIT = (int) 32768;
    public static final int GL_INCR = (int) 7682;
    public static final int GL_INDEX_ARRAY = (int) 32887;
    public static final int GL_INDEX_ARRAY_POINTER = (int) 32913;
    public static final int GL_INDEX_ARRAY_STRIDE = (int) 32902;
    public static final int GL_INDEX_ARRAY_TYPE = (int) 32901;
    public static final int GL_INDEX_BITS = (int) 3409;
    public static final int GL_INDEX_CLEAR_VALUE = (int) 3104;
    public static final int GL_INDEX_LOGIC_OP = (int) 3057;
    public static final int GL_INDEX_MODE = (int) 3120;
    public static final int GL_INDEX_OFFSET = (int) 3347;
    public static final int GL_INDEX_SHIFT = (int) 3346;
    public static final int GL_INDEX_WRITEMASK = (int) 3105;
    public static final int GL_INT = (int) 5124;
    public static final int GL_INTENSITY12 = (int) 32844;
    public static final int GL_INTENSITY16 = (int) 32845;
    public static final int GL_INTENSITY4 = (int) 32842;
    public static final int GL_INTENSITY8 = (int) 32843;
    public static final int GL_INTENSITY = (int) 32841;
    public static final int GL_INVALID_ENUM = (int) 1280;
    public static final int GL_INVALID_OPERATION = (int) 1282;
    public static final int GL_INVALID_VALUE = (int) 1281;
    public static final int GL_INVERT = (int) 5386;
    public static final int GL_KEEP = (int) 7680;
    public static final int GL_LEFT = (int) 1030;
    public static final int GL_LEQUAL = (int) 515;
    public static final int GL_LESS = (int) 513;
    public static final int GL_LIGHT0 = (int) 16384;
    public static final int GL_LIGHT1 = (int) 16385;
    public static final int GL_LIGHT2 = (int) 16386;
    public static final int GL_LIGHT3 = (int) 16387;
    public static final int GL_LIGHT4 = (int) 16388;
    public static final int GL_LIGHT5 = (int) 16389;
    public static final int GL_LIGHT6 = (int) 16390;
    public static final int GL_LIGHT7 = (int) 16391;
    public static final int GL_LIGHTING = (int) 2896;
    public static final int GL_LIGHTING_BIT = (int) 64;
    public static final int GL_LIGHT_MODEL_AMBIENT = (int) 2899;
    public static final int GL_LIGHT_MODEL_LOCAL_VIEWER = (int) 2897;
    public static final int GL_LIGHT_MODEL_TWO_SIDE = (int) 2898;
    public static final int GL_LINE = (int) 6913;
    public static final int GL_LINEAR = (int) 9729;
    public static final int GL_LINEAR_ATTENUATION = (int) 4616;
    public static final int GL_LINEAR_MIPMAP_LINEAR = (int) 9987;
    public static final int GL_LINEAR_MIPMAP_NEAREST = (int) 9985;
    public static final int GL_LINES = (int) 1;
    public static final int GL_LINE_BIT = (int) 4;
    public static final int GL_LINE_LOOP = (int) 2;
    public static final int GL_LINE_RESET_TOKEN = (int) 1799;
    public static final int GL_LINE_SMOOTH = (int) 2848;
    public static final int GL_LINE_SMOOTH_HINT = (int) 3154;
    public static final int GL_LINE_STIPPLE = (int) 2852;
    public static final int GL_LINE_STIPPLE_PATTERN = (int) 2853;
    public static final int GL_LINE_STIPPLE_REPEAT = (int) 2854;
    public static final int GL_LINE_STRIP = (int) 3;
    public static final int GL_LINE_TOKEN = (int) 1794;
    public static final int GL_LINE_WIDTH = (int) 2849;
    public static final int GL_LINE_WIDTH_GRANULARITY = (int) 2851;
    public static final int GL_LINE_WIDTH_RANGE = (int) 2850;
    public static final int GL_LIST_BASE = (int) 2866;
    public static final int GL_LIST_BIT = (int) 131072;
    public static final int GL_LIST_INDEX = (int) 2867;
    public static final int GL_LIST_MODE = (int) 2864;
    public static final int GL_LOAD = (int) 257;
    public static final int GL_LOGIC_OP = (int) 3057;
    public static final int GL_LOGIC_OP_MODE = (int) 3056;
    public static final int GL_LUMINANCE12 = (int) 32833;
    public static final int GL_LUMINANCE12_ALPHA12 = (int) 32839;
    public static final int GL_LUMINANCE12_ALPHA4 = (int) 32838;
    public static final int GL_LUMINANCE16 = (int) 32834;
    public static final int GL_LUMINANCE16_ALPHA16 = (int) 32840;
    public static final int GL_LUMINANCE4 = (int) 32831;
    public static final int GL_LUMINANCE4_ALPHA4 = (int) 32835;
    public static final int GL_LUMINANCE6_ALPHA2 = (int) 32836;
    public static final int GL_LUMINANCE8 = (int) 32832;
    public static final int GL_LUMINANCE8_ALPHA8 = (int) 32837;
    public static final int GL_LUMINANCE = (int) 6409;
    public static final int GL_LUMINANCE_ALPHA = (int) 6410;
    public static final int GL_MAP1_COLOR_4 = (int) 3472;
    public static final int GL_MAP1_GRID_DOMAIN = (int) 3536;
    public static final int GL_MAP1_GRID_SEGMENTS = (int) 3537;
    public static final int GL_MAP1_INDEX = (int) 3473;
    public static final int GL_MAP1_NORMAL = (int) 3474;
    public static final int GL_MAP1_TEXTURE_COORD_1 = (int) 3475;
    public static final int GL_MAP1_TEXTURE_COORD_2 = (int) 3476;
    public static final int GL_MAP1_TEXTURE_COORD_3 = (int) 3477;
    public static final int GL_MAP1_TEXTURE_COORD_4 = (int) 3478;
    public static final int GL_MAP1_VERTEX_3 = (int) 3479;
    public static final int GL_MAP1_VERTEX_4 = (int) 3480;
    public static final int GL_MAP2_COLOR_4 = (int) 3504;
    public static final int GL_MAP2_GRID_DOMAIN = (int) 3538;
    public static final int GL_MAP2_GRID_SEGMENTS = (int) 3539;
    public static final int GL_MAP2_INDEX = (int) 3505;
    public static final int GL_MAP2_NORMAL = (int) 3506;
    public static final int GL_MAP2_TEXTURE_COORD_1 = (int) 3507;
    public static final int GL_MAP2_TEXTURE_COORD_2 = (int) 3508;
    public static final int GL_MAP2_TEXTURE_COORD_3 = (int) 3509;
    public static final int GL_MAP2_TEXTURE_COORD_4 = (int) 3510;
    public static final int GL_MAP2_VERTEX_3 = (int) 3511;
    public static final int GL_MAP2_VERTEX_4 = (int) 3512;
    public static final int GL_MAP_COLOR = (int) 3344;
    public static final int GL_MAP_STENCIL = (int) 3345;
    public static final int GL_MATRIX_MODE = (int) 2976;
    public static final int GL_MAX_ATTRIB_STACK_DEPTH = (int) 3381;
    public static final int GL_MAX_CLIENT_ATTRIB_STACK_DEPTH = (int) 3387;
    public static final int GL_MAX_CLIP_PLANES = (int) 3378;
    public static final int GL_MAX_EVAL_ORDER = (int) 3376;
    public static final int GL_MAX_LIGHTS = (int) 3377;
    public static final int GL_MAX_LIST_NESTING = (int) 2865;
    public static final int GL_MAX_MODELVIEW_STACK_DEPTH = (int) 3382;
    public static final int GL_MAX_NAME_STACK_DEPTH = (int) 3383;
    public static final int GL_MAX_PIXEL_MAP_TABLE = (int) 3380;
    public static final int GL_MAX_PROJECTION_STACK_DEPTH = (int) 3384;
    public static final int GL_MAX_TEXTURE_SIZE = (int) 3379;
    public static final int GL_MAX_TEXTURE_STACK_DEPTH = (int) 3385;
    public static final int GL_MAX_VIEWPORT_DIMS = (int) 3386;
    public static final int GL_MODELVIEW = (int) 5888;
    public static final int GL_MODELVIEW_MATRIX = (int) 2982;
    public static final int GL_MODELVIEW_STACK_DEPTH = (int) 2979;
    public static final int GL_MODULATE = (int) 8448;
    public static final int GL_MULT = (int) 259;
    public static final int GL_N3F_V3F = (int) 10789;
    public static final int GL_NAME_STACK_DEPTH = (int) 3440;
    public static final int GL_NAND = (int) 5390;
    public static final int GL_NEAREST = (int) 9728;
    public static final int GL_NEAREST_MIPMAP_LINEAR = (int) 9986;
    public static final int GL_NEAREST_MIPMAP_NEAREST = (int) 9984;
    public static final int GL_NEVER = (int) 512;
    public static final int GL_NICEST = (int) 4354;
    public static final int GL_NONE = (int) 0;
    public static final int GL_NOOP = (int) 5381;
    public static final int GL_NOR = (int) 5384;
    public static final int GL_NORMALIZE = (int) 2977;
    public static final int GL_NORMAL_ARRAY = (int) 32885;
    public static final int GL_NORMAL_ARRAY_POINTER = (int) 32911;
    public static final int GL_NORMAL_ARRAY_STRIDE = (int) 32895;
    public static final int GL_NORMAL_ARRAY_TYPE = (int) 32894;
    public static final int GL_NOTEQUAL = (int) 517;
    public static final int GL_NO_ERROR = (int) 0;
    public static final int GL_OBJECT_LINEAR = (int) 9217;
    public static final int GL_OBJECT_PLANE = (int) 9473;
    public static final int GL_ONE = (int) 1;
    public static final int GL_ONE_MINUS_CONSTANT_ALPHA = (int) 32772;
    public static final int GL_ONE_MINUS_CONSTANT_COLOR = (int) 32770;
    public static final int GL_ONE_MINUS_DST_ALPHA = (int) 773;
    public static final int GL_ONE_MINUS_DST_COLOR = (int) 775;
    public static final int GL_ONE_MINUS_SRC_ALPHA = (int) 771;
    public static final int GL_ONE_MINUS_SRC_COLOR = (int) 769;
    public static final int GL_OR = (int) 5383;
    public static final int GL_ORDER = (int) 2561;
    public static final int GL_OR_INVERTED = (int) 5389;
    public static final int GL_OR_REVERSE = (int) 5387;
    public static final int GL_OUT_OF_MEMORY = (int) 1285;
    public static final int GL_PACK_ALIGNMENT = (int) 3333;
    public static final int GL_PACK_LSB_FIRST = (int) 3329;
    public static final int GL_PACK_ROW_LENGTH = (int) 3330;
    public static final int GL_PACK_SKIP_PIXELS = (int) 3332;
    public static final int GL_PACK_SKIP_ROWS = (int) 3331;
    public static final int GL_PACK_SWAP_BYTES = (int) 3328;
    public static final int GL_PASS_THROUGH_TOKEN = (int) 1792;
    public static final int GL_PERSPECTIVE_CORRECTION_HINT = (int) 3152;
    public static final int GL_PIXEL_MAP_A_TO_A = (int) 3193;
    public static final int GL_PIXEL_MAP_A_TO_A_SIZE = (int) 3257;
    public static final int GL_PIXEL_MAP_B_TO_B = (int) 3192;
    public static final int GL_PIXEL_MAP_B_TO_B_SIZE = (int) 3256;
    public static final int GL_PIXEL_MAP_G_TO_G = (int) 3191;
    public static final int GL_PIXEL_MAP_G_TO_G_SIZE = (int) 3255;
    public static final int GL_PIXEL_MAP_I_TO_A = (int) 3189;
    public static final int GL_PIXEL_MAP_I_TO_A_SIZE = (int) 3253;
    public static final int GL_PIXEL_MAP_I_TO_B = (int) 3188;
    public static final int GL_PIXEL_MAP_I_TO_B_SIZE = (int) 3252;
    public static final int GL_PIXEL_MAP_I_TO_G = (int) 3187;
    public static final int GL_PIXEL_MAP_I_TO_G_SIZE = (int) 3251;
    public static final int GL_PIXEL_MAP_I_TO_I = (int) 3184;
    public static final int GL_PIXEL_MAP_I_TO_I_SIZE = (int) 3248;
    public static final int GL_PIXEL_MAP_I_TO_R = (int) 3186;
    public static final int GL_PIXEL_MAP_I_TO_R_SIZE = (int) 3250;
    public static final int GL_PIXEL_MAP_R_TO_R = (int) 3190;
    public static final int GL_PIXEL_MAP_R_TO_R_SIZE = (int) 3254;
    public static final int GL_PIXEL_MAP_S_TO_S = (int) 3185;
    public static final int GL_PIXEL_MAP_S_TO_S_SIZE = (int) 3249;
    public static final int GL_PIXEL_MODE_BIT = (int) 32;
    public static final int GL_POINT = (int) 6912;
    public static final int GL_POINTS = (int) 0;
    public static final int GL_POINT_BIT = (int) 2;
    public static final int GL_POINT_SIZE = (int) 2833;
    public static final int GL_POINT_SIZE_GRANULARITY = (int) 2835;
    public static final int GL_POINT_SIZE_RANGE = (int) 2834;
    public static final int GL_POINT_SMOOTH = (int) 2832;
    public static final int GL_POINT_SMOOTH_HINT = (int) 3153;
    public static final int GL_POINT_TOKEN = (int) 1793;
    public static final int GL_POLYGON = (int) 9;
    public static final int GL_POLYGON_BIT = (int) 8;
    public static final int GL_POLYGON_MODE = (int) 2880;
    public static final int GL_POLYGON_OFFSET_FACTOR = (int) 32824;
    public static final int GL_POLYGON_OFFSET_FILL = (int) 32823;
    public static final int GL_POLYGON_OFFSET_LINE = (int) 10754;
    public static final int GL_POLYGON_OFFSET_POINT = (int) 10753;
    public static final int GL_POLYGON_OFFSET_UNITS = (int) 10752;
    public static final int GL_POLYGON_SMOOTH = (int) 2881;
    public static final int GL_POLYGON_SMOOTH_HINT = (int) 3155;
    public static final int GL_POLYGON_STIPPLE = (int) 2882;
    public static final int GL_POLYGON_STIPPLE_BIT = (int) 16;
    public static final int GL_POLYGON_TOKEN = (int) 1795;
    public static final int GL_POSITION = (int) 4611;
    public static final int GL_PROJECTION = (int) 5889;
    public static final int GL_PROJECTION_MATRIX = (int) 2983;
    public static final int GL_PROJECTION_STACK_DEPTH = (int) 2980;
    public static final int GL_PROXY_TEXTURE_1D = (int) 32867;
    public static final int GL_PROXY_TEXTURE_2D = (int) 32868;
    public static final int GL_Q = (int) 8195;
    public static final int GL_QUADRATIC_ATTENUATION = (int) 4617;
    public static final int GL_QUADS = (int) 7;
    public static final int GL_QUAD_STRIP = (int) 8;
    public static final int GL_R3_G3_B2 = (int) 10768;
    public static final int GL_R = (int) 8194;
    public static final int GL_READ_BUFFER = (int) 3074;
    public static final int GL_RED = (int) 6403;
    public static final int GL_RED_BIAS = (int) 3349;
    public static final int GL_RED_BITS = (int) 3410;
    public static final int GL_RED_SCALE = (int) 3348;
    public static final int GL_RENDER = (int) 7168;
    public static final int GL_RENDERER = (int) 7937;
    public static final int GL_RENDER_MODE = (int) 3136;
    public static final int GL_REPEAT = (int) 10497;
    public static final int GL_REPLACE = (int) 7681;
    public static final int GL_RETURN = (int) 258;
    public static final int GL_RGB10 = (int) 32850;
    public static final int GL_RGB10_A2 = (int) 32857;
    public static final int GL_RGB12 = (int) 32851;
    public static final int GL_RGB16 = (int) 32852;
    public static final int GL_RGB4 = (int) 32847;
    public static final int GL_RGB5 = (int) 32848;
    public static final int GL_RGB5_A1 = (int) 32855;
    public static final int GL_RGB8 = (int) 32849;
    public static final int GL_RGB = (int) 6407;
    public static final int GL_RGBA12 = (int) 32858;
    public static final int GL_RGBA16 = (int) 32859;
    public static final int GL_RGBA2 = (int) 32853;
    public static final int GL_RGBA4 = (int) 32854;
    public static final int GL_RGBA8 = (int) 32856;
    public static final int GL_RGBA = (int) 6408;
    public static final int GL_RGBA_MODE = (int) 3121;
    public static final int GL_RIGHT = (int) 1031;
    public static final int GL_S = (int) 8192;
    public static final int GL_SCISSOR_BIT = (int) 524288;
    public static final int GL_SCISSOR_BOX = (int) 3088;
    public static final int GL_SCISSOR_TEST = (int) 3089;
    public static final int GL_SELECT = (int) 7170;
    public static final int GL_SELECTION_BUFFER_POINTER = (int) 3571;
    public static final int GL_SELECTION_BUFFER_SIZE = (int) 3572;
    public static final int GL_SET = (int) 5391;
    public static final int GL_SHADE_MODEL = (int) 2900;
    public static final int GL_SHININESS = (int) 5633;
    public static final int GL_SHORT = (int) 5122;
    public static final int GL_SMOOTH = (int) 7425;
    public static final int GL_SPECULAR = (int) 4610;
    public static final int GL_SPHERE_MAP = (int) 9218;
    public static final int GL_SPOT_CUTOFF = (int) 4614;
    public static final int GL_SPOT_DIRECTION = (int) 4612;
    public static final int GL_SPOT_EXPONENT = (int) 4613;
    public static final int GL_SRC_ALPHA = (int) 770;
    public static final int GL_SRC_ALPHA_SATURATE = (int) 776;
    public static final int GL_SRC_COLOR = (int) 768;
    public static final int GL_STACK_OVERFLOW = (int) 1283;
    public static final int GL_STACK_UNDERFLOW = (int) 1284;
    public static final int GL_STENCIL = (int) 6146;
    public static final int GL_STENCIL_BITS = (int) 3415;
    public static final int GL_STENCIL_BUFFER_BIT = (int) 1024;
    public static final int GL_STENCIL_CLEAR_VALUE = (int) 2961;
    public static final int GL_STENCIL_FAIL = (int) 2964;
    public static final int GL_STENCIL_FUNC = (int) 2962;
    public static final int GL_STENCIL_INDEX = (int) 6401;
    public static final int GL_STENCIL_PASS_DEPTH_FAIL = (int) 2965;
    public static final int GL_STENCIL_PASS_DEPTH_PASS = (int) 2966;
    public static final int GL_STENCIL_REF = (int) 2967;
    public static final int GL_STENCIL_TEST = (int) 2960;
    public static final int GL_STENCIL_VALUE_MASK = (int) 2963;
    public static final int GL_STENCIL_WRITEMASK = (int) 2968;
    public static final int GL_STEREO = (int) 3123;
    public static final int GL_SUBPIXEL_BITS = (int) 3408;
    public static final int GL_T2F_C3F_V3F = (int) 10794;
    public static final int GL_T2F_C4F_N3F_V3F = (int) 10796;
    public static final int GL_T2F_C4UB_V3F = (int) 10793;
    public static final int GL_T2F_N3F_V3F = (int) 10795;
    public static final int GL_T2F_V3F = (int) 10791;
    public static final int GL_T4F_C4F_N3F_V4F = (int) 10797;
    public static final int GL_T4F_V4F = (int) 10792;
    public static final int GL_T = (int) 8193;
    public static final int GL_TEXTURE = (int) 5890;
    public static final int GL_TEXTURE_1D = (int) 3552;
    public static final int GL_TEXTURE_2D = (int) 3553;
    public static final int GL_TEXTURE_ALPHA_SIZE = (int) 32863;
    public static final int GL_TEXTURE_BINDING_1D = (int) 32872;
    public static final int GL_TEXTURE_BINDING_2D = (int) 32873;
    public static final int GL_TEXTURE_BIT = (int) 262144;
    public static final int GL_TEXTURE_BLUE_SIZE = (int) 32862;
    public static final int GL_TEXTURE_BORDER = (int) 4101;
    public static final int GL_TEXTURE_BORDER_COLOR = (int) 4100;
    public static final int GL_TEXTURE_COMPONENTS = (int) 4099;
    public static final int GL_TEXTURE_COORD_ARRAY = (int) 32888;
    public static final int GL_TEXTURE_COORD_ARRAY_POINTER = (int) 32914;
    public static final int GL_TEXTURE_COORD_ARRAY_SIZE = (int) 32904;
    public static final int GL_TEXTURE_COORD_ARRAY_STRIDE = (int) 32906;
    public static final int GL_TEXTURE_COORD_ARRAY_TYPE = (int) 32905;
    public static final int GL_TEXTURE_ENV = (int) 8960;
    public static final int GL_TEXTURE_ENV_COLOR = (int) 8705;
    public static final int GL_TEXTURE_ENV_MODE = (int) 8704;
    public static final int GL_TEXTURE_GEN_MODE = (int) 9472;
    public static final int GL_TEXTURE_GEN_Q = (int) 3171;
    public static final int GL_TEXTURE_GEN_R = (int) 3170;
    public static final int GL_TEXTURE_GEN_S = (int) 3168;
    public static final int GL_TEXTURE_GEN_T = (int) 3169;
    public static final int GL_TEXTURE_GREEN_SIZE = (int) 32861;
    public static final int GL_TEXTURE_HEIGHT = (int) 4097;
    public static final int GL_TEXTURE_INTENSITY_SIZE = (int) 32865;
    public static final int GL_TEXTURE_INTERNAL_FORMAT = (int) 4099;
    public static final int GL_TEXTURE_LUMINANCE_SIZE = (int) 32864;
    public static final int GL_TEXTURE_MAG_FILTER = (int) 10240;
    public static final int GL_TEXTURE_MATRIX = (int) 2984;
    public static final int GL_TEXTURE_MIN_FILTER = (int) 10241;
    public static final int GL_TEXTURE_PRIORITY = (int) 32870;
    public static final int GL_TEXTURE_RED_SIZE = (int) 32860;
    public static final int GL_TEXTURE_RESIDENT = (int) 32871;
    public static final int GL_TEXTURE_STACK_DEPTH = (int) 2981;
    public static final int GL_TEXTURE_WIDTH = (int) 4096;
    public static final int GL_TEXTURE_WRAP_S = (int) 10242;
    public static final int GL_TEXTURE_WRAP_T = (int) 10243;
    public static final int GL_TRANSFORM_BIT = (int) 4096;
    public static final int GL_TRIANGLES = (int) 4;
    public static final int GL_TRIANGLE_FAN = (int) 6;
    public static final int GL_TRIANGLE_STRIP = (int) 5;
    public static final int GL_TRUE = (int) 1;
    public static final int GL_UNPACK_ALIGNMENT = (int) 3317;
    public static final int GL_UNPACK_LSB_FIRST = (int) 3313;
    public static final int GL_UNPACK_ROW_LENGTH = (int) 3314;
    public static final int GL_UNPACK_SKIP_PIXELS = (int) 3316;
    public static final int GL_UNPACK_SKIP_ROWS = (int) 3315;
    public static final int GL_UNPACK_SWAP_BYTES = (int) 3312;
    public static final int GL_UNSIGNED_BYTE = (int) 5121;
    public static final int GL_UNSIGNED_INT = (int) 5125;
    public static final int GL_UNSIGNED_SHORT = (int) 5123;
    public static final int GL_V2F = (int) 10784;
    public static final int GL_V3F = (int) 10785;
    public static final int GL_VENDOR = (int) 7936;
    public static final int GL_VERSION = (int) 7938;
    public static final int GL_VERTEX_ARRAY = (int) 32884;
    public static final int GL_VERTEX_ARRAY_POINTER = (int) 32910;
    public static final int GL_VERTEX_ARRAY_SIZE = (int) 32890;
    public static final int GL_VERTEX_ARRAY_STRIDE = (int) 32892;
    public static final int GL_VERTEX_ARRAY_TYPE = (int) 32891;
    public static final int GL_VIEWPORT = (int) 2978;
    public static final int GL_VIEWPORT_BIT = (int) 2048;
    public static final int GL_XOR = (int) 5382;
    public static final int GL_ZERO = (int) 0;
    public static final int GL_ZOOM_X = (int) 3350;
    public static final int GL_ZOOM_Y = (int) 3351;

    public static void glAccum(int op, float value) {
        org.lwjgl.opengl.GL11.glAccum(op, value);
    }

    public static void glAlphaFunc(int func, float ref) {
        org.lwjgl.opengl.GL11.glAlphaFunc(func, ref);
    }

    public static boolean glAreTexturesResident(java.nio.IntBuffer textures, java.nio.ByteBuffer residences) {
        return org.lwjgl.opengl.GL11.glAreTexturesResident(textures, residences);
    }

    public static void glArrayElement(int i) {
        org.lwjgl.opengl.GL11.glArrayElement(i);
    }

    public static void glBegin(int mode) {
        org.lwjgl.opengl.GL11.glBegin(mode);
    }

    public static void glBindTexture(int target, int texture) {
        org.lwjgl.opengl.GL11.glBindTexture(target, texture);
    }

    public static void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove,
            long bitmap_buffer_offset) {
        org.lwjgl.opengl.GL11.glBitmap(width, height, xorig, yorig, xmove, ymove, bitmap_buffer_offset);
    }

    public static void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove,
            java.nio.ByteBuffer bitmap) {
        org.lwjgl.opengl.GL11.glBitmap(width, height, xorig, yorig, xmove, ymove, bitmap);
    }

    public static void glBlendFunc(int sfactor, int dfactor) {
        org.lwjgl.opengl.GL11.glBlendFunc(sfactor, dfactor);
    }

    public static void glCallList(int list) {
        org.lwjgl.opengl.GL11.glCallList(list);
    }

    public static void glCallLists(java.nio.ByteBuffer lists) {
        org.lwjgl.opengl.GL11.glCallLists(lists);
    }

    public static void glCallLists(java.nio.IntBuffer lists) {
        org.lwjgl.opengl.GL11.glCallLists(lists);
    }

    public static void glCallLists(java.nio.ShortBuffer lists) {
        org.lwjgl.opengl.GL11.glCallLists(lists);
    }

    public static void glClear(int mask) {
        org.lwjgl.opengl.GL11.glClear(mask);
    }

    public static void glClearAccum(float red, float green, float blue, float alpha) {
        org.lwjgl.opengl.GL11.glClearAccum(red, green, blue, alpha);
    }

    public static void glClearColor(float red, float green, float blue, float alpha) {
        org.lwjgl.opengl.GL11.glClearColor(red, green, blue, alpha);
    }

    public static void glClearDepth(double depth) {
        org.lwjgl.opengl.GL11.glClearDepth(depth);
    }

    public static void glClearStencil(int s) {
        org.lwjgl.opengl.GL11.glClearStencil(s);
    }

    public static void glClipPlane(int plane, java.nio.DoubleBuffer equation) {
        org.lwjgl.opengl.GL11.glClipPlane(plane, equation);
    }

    public static void glColor3b(byte red, byte green, byte blue) {
        org.lwjgl.opengl.GL11.glColor3b(red, green, blue);
    }

    public static void glColor3d(double red, double green, double blue) {
        org.lwjgl.opengl.GL11.glColor3d(red, green, blue);
    }

    public static void glColor3f(float red, float green, float blue) {
        org.lwjgl.opengl.GL11.glColor3f(red, green, blue);
    }

    private static final int UNSIGNED_BYTE_TO_INT_RATIO = Integer.MAX_VALUE / 255;

    public static void glColor3ub(byte red, byte green, byte blue) {
        org.lwjgl.opengl.GL11.glColor3i(
                (red & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO,
                (green & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO,
                (blue & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO);
    }

    public static void glColor4b(byte red, byte green, byte blue, byte alpha) {
        org.lwjgl.opengl.GL11.glColor4b(red, green, blue, alpha);
    }

    public static void glColor4d(double red, double green, double blue, double alpha) {
        org.lwjgl.opengl.GL11.glColor4d(red, green, blue, alpha);
    }

    public static void glColor4f(float red, float green, float blue, float alpha) {
        org.lwjgl.opengl.GL11.glColor4f(red, green, blue, alpha);
    }

    public static void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        org.lwjgl.opengl.GL11.glColor4i(
                (red & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO,
                (green & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO,
                (blue & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO,
                (alpha & 0xff) * UNSIGNED_BYTE_TO_INT_RATIO);
    }

    public static void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        org.lwjgl.opengl.GL11.glColorMask(red, green, blue, alpha);
    }

    public static void glColorMaterial(int face, int mode) {
        org.lwjgl.opengl.GL11.glColorMaterial(face, mode);
    }

    public static void glColorPointer(int size, int type, int stride, long pointer_buffer_offset) {
        org.lwjgl.opengl.GL11.glColorPointer(size, type, stride, pointer_buffer_offset);
    }

    public static void glColorPointer(int size, int type, int stride, java.nio.ByteBuffer pointer) {
        org.lwjgl.opengl.GL11.glColorPointer(size, type, stride, pointer);
    }

    public static void glColorPointer(int size, int stride, java.nio.DoubleBuffer pointer) {

        org.lwjgl.opengl.GL11.glColorPointer(
                size,
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(pointer));
    }

    public static void glColorPointer(int size, int stride, java.nio.FloatBuffer pointer) {

        org.lwjgl.opengl.GL11.glColorPointer(size, org.lwjgl.opengl.GL11.GL_FLOAT, stride, pointer);
    }

    public static void glColorPointer(int size, boolean unsigned, int stride, java.nio.ByteBuffer pointer) {

        org.lwjgl.opengl.GL11.glColorPointer(
                size,
                (unsigned ? org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE : org.lwjgl.opengl.GL11.GL_BYTE),
                stride,
                MemoryUtil.getAddress(pointer));
    }

    public static void glCopyPixels(int x, int y, int width, int height, int type) {
        org.lwjgl.opengl.GL11.glCopyPixels(x, y, width, height, type);
    }

    public static void glCopyTexImage1D(int target, int level, int internalFormat, int x, int y, int width,
            int border) {
        org.lwjgl.opengl.GL11.glCopyTexImage1D(target, level, internalFormat, x, y, width, border);
    }

    public static void glCopyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height,
            int border) {
        org.lwjgl.opengl.GL11.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
    }

    public static void glCopyTexSubImage1D(int target, int level, int xoffset, int x, int y, int width) {
        org.lwjgl.opengl.GL11.glCopyTexSubImage1D(target, level, xoffset, x, y, width);
    }

    public static void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
            int height) {
        org.lwjgl.opengl.GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    public static void glCullFace(int mode) {
        org.lwjgl.opengl.GL11.glCullFace(mode);
    }

    public static void glDeleteLists(int list, int range) {
        org.lwjgl.opengl.GL11.glDeleteLists(list, range);
    }

    public static void glDeleteTextures(int texture) {
        org.lwjgl.opengl.GL11.glDeleteTextures(texture);
    }

    public static void glDeleteTextures(java.nio.IntBuffer textures) {
        org.lwjgl.opengl.GL11.glDeleteTextures(textures);
    }

    public static void glDepthFunc(int func) {
        org.lwjgl.opengl.GL11.glDepthFunc(func);
    }

    public static void glDepthMask(boolean flag) {
        org.lwjgl.opengl.GL11.glDepthMask(flag);
    }

    public static void glDepthRange(double zNear, double zFar) {
        org.lwjgl.opengl.GL11.glDepthRange(zNear, zFar);
    }

    public static void glDisable(int cap) {
        org.lwjgl.opengl.GL11.glDisable(cap);
    }

    public static void glDisableClientState(int cap) {
        org.lwjgl.opengl.GL11.glDisableClientState(cap);
    }

    public static void glDrawArrays(int mode, int first, int count) {
        org.lwjgl.opengl.GL11.glDrawArrays(mode, first, count);
    }

    public static void glDrawBuffer(int mode) {
        org.lwjgl.opengl.GL11.glDrawBuffer(mode);
    }

    public static void glDrawElements(int mode, int indices_count, int type, long indices_buffer_offset) {
        org.lwjgl.opengl.GL11.glDrawElements(mode, indices_count, type, indices_buffer_offset);
    }

    public static void glDrawElements(int mode, int count, int type, java.nio.ByteBuffer indices) {

        org.lwjgl.opengl.GL11.glDrawElements(mode, count, type, MemoryUtil.getAddress(indices));
    }

    public static void glDrawElements(int mode, java.nio.ByteBuffer indices) {
        org.lwjgl.opengl.GL11.glDrawElements(mode, indices);
    }

    public static void glDrawElements(int mode, java.nio.IntBuffer indices) {
        org.lwjgl.opengl.GL11.glDrawElements(mode, indices);
    }

    public static void glDrawElements(int mode, java.nio.ShortBuffer indices) {
        org.lwjgl.opengl.GL11.glDrawElements(mode, indices);
    }

    public static void glDrawPixels(int width, int height, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11.glDrawPixels(width, height, format, type, pixels_buffer_offset);
    }

    public static void glDrawPixels(int width, int height, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glDrawPixels(width, height, format, type, pixels);
    }

    public static void glDrawPixels(int width, int height, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glDrawPixels(width, height, format, type, pixels);
    }

    public static void glDrawPixels(int width, int height, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glDrawPixels(width, height, format, type, pixels);
    }

    public static void glEdgeFlag(boolean flag) {
        org.lwjgl.opengl.GL11.glEdgeFlag(flag);
    }

    public static void glEdgeFlagPointer(int stride, long pointer_buffer_offset) {
        org.lwjgl.opengl.GL11.glEdgeFlagPointer(stride, pointer_buffer_offset);
    }

    public static void glEdgeFlagPointer(int stride, java.nio.ByteBuffer pointer) {
        org.lwjgl.opengl.GL11.glEdgeFlagPointer(stride, pointer);
    }

    public static void glEnable(int cap) {
        org.lwjgl.opengl.GL11.glEnable(cap);
    }

    public static void glEnableClientState(int cap) {
        org.lwjgl.opengl.GL11.glEnableClientState(cap);
    }

    public static void glEnd() {
        org.lwjgl.opengl.GL11.glEnd();
    }

    public static void glEndList() {
        org.lwjgl.opengl.GL11.glEndList();
    }

    public static void glEvalCoord1d(double u) {
        org.lwjgl.opengl.GL11.glEvalCoord1d(u);
    }

    public static void glEvalCoord1f(float u) {
        org.lwjgl.opengl.GL11.glEvalCoord1f(u);
    }

    public static void glEvalCoord2d(double u, double v) {
        org.lwjgl.opengl.GL11.glEvalCoord2d(u, v);
    }

    public static void glEvalCoord2f(float u, float v) {
        org.lwjgl.opengl.GL11.glEvalCoord2f(u, v);
    }

    public static void glEvalMesh1(int mode, int i1, int i2) {
        org.lwjgl.opengl.GL11.glEvalMesh1(mode, i1, i2);
    }

    public static void glEvalMesh2(int mode, int i1, int i2, int j1, int j2) {
        org.lwjgl.opengl.GL11.glEvalMesh2(mode, i1, i2, j1, j2);
    }

    public static void glEvalPoint1(int i) {
        org.lwjgl.opengl.GL11.glEvalPoint1(i);
    }

    public static void glEvalPoint2(int i, int j) {
        org.lwjgl.opengl.GL11.glEvalPoint2(i, j);
    }

    public static void glFeedbackBuffer(int type, java.nio.FloatBuffer buffer) {
        org.lwjgl.opengl.GL11.glFeedbackBuffer(type, buffer);
    }

    public static void glFinish() {
        org.lwjgl.opengl.GL11.glFinish();
    }

    public static void glFlush() {
        org.lwjgl.opengl.GL11.glFlush();
    }

    public static void glFog(int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glFogfv(pname, params);
    }

    public static void glFog(int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glFogiv(pname, params);
    }

    public static void glFogf(int pname, float param) {
        org.lwjgl.opengl.GL11.glFogf(pname, param);
    }

    public static void glFogi(int pname, int param) {
        org.lwjgl.opengl.GL11.glFogi(pname, param);
    }

    public static void glFrontFace(int mode) {
        org.lwjgl.opengl.GL11.glFrontFace(mode);
    }

    public static void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar) {
        org.lwjgl.opengl.GL11.glFrustum(left, right, bottom, top, zNear, zFar);
    }

    public static int glGenLists(int range) {
        return org.lwjgl.opengl.GL11.glGenLists(range);
    }

    public static int glGenTextures() {
        return org.lwjgl.opengl.GL11.glGenTextures();
    }

    public static void glGenTextures(java.nio.IntBuffer textures) {
        org.lwjgl.opengl.GL11.glGenTextures(textures);
    }

    public static boolean glGetBoolean(int pname) {
        return org.lwjgl.opengl.GL11.glGetBoolean(pname);
    }

    public static void glGetBoolean(int pname, java.nio.ByteBuffer params) {
        org.lwjgl.opengl.GL11.glGetBooleanv(pname, params);
    }

    public static void glGetClipPlane(int plane, java.nio.DoubleBuffer equation) {
        org.lwjgl.opengl.GL11.glGetClipPlane(plane, equation);
    }

    public static double glGetDouble(int pname) {
        return org.lwjgl.opengl.GL11.glGetDouble(pname);
    }

    public static void glGetDouble(int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.GL11.glGetDoublev(pname, params);
    }

    public static int glGetError() {
        return org.lwjgl.opengl.GL11.glGetError();
    }

    public static float glGetFloat(int pname) {
        return org.lwjgl.opengl.GL11.glGetFloat(pname);
    }

    public static void glGetFloat(int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetFloatv(pname, params);
    }

    public static int glGetInteger(int pname) {
        return org.lwjgl.opengl.GL11.glGetInteger(pname);
    }

    public static void glGetInteger(int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetIntegerv(pname, params);
    }

    public static void glGetLight(int light, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetLightfv(light, pname, params);
    }

    public static void glGetLight(int light, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetLightiv(light, pname, params);
    }

    public static void glGetMap(int target, int query, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.GL11.glGetMapdv(target, query, v);
    }

    public static void glGetMap(int target, int query, java.nio.FloatBuffer v) {
        org.lwjgl.opengl.GL11.glGetMapfv(target, query, v);
    }

    public static void glGetMap(int target, int query, java.nio.IntBuffer v) {
        org.lwjgl.opengl.GL11.glGetMapiv(target, query, v);
    }

    public static void glGetMaterial(int face, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetMaterialfv(face, pname, params);
    }

    public static void glGetMaterial(int face, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetMaterialiv(face, pname, params);
    }

    public static void glGetPixelMap(int map, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.GL11.glGetPixelMapfv(map, values);
    }

    public static void glGetPixelMapfv(int map, long values_buffer_offset) {
        org.lwjgl.opengl.GL11.glGetPixelMapfv(map, values_buffer_offset);
    }

    public static void glGetPixelMapu(int map, java.nio.IntBuffer values) {
        org.lwjgl.opengl.GL11.glGetPixelMapuiv(map, values);
    }

    public static void glGetPixelMapu(int map, java.nio.ShortBuffer values) {
        org.lwjgl.opengl.GL11.glGetPixelMapusv(map, values);
    }

    public static void glGetPixelMapuiv(int map, long values_buffer_offset) {
        org.lwjgl.opengl.GL11.glGetPixelMapuiv(map, values_buffer_offset);
    }

    public static void glGetPixelMapusv(int map, long values_buffer_offset) {
        org.lwjgl.opengl.GL11.glGetPixelMapusv(map, values_buffer_offset);
    }

    public static void glGetPolygonStipple(long mask_buffer_offset) {
        org.lwjgl.opengl.GL11.glGetPolygonStipple(mask_buffer_offset);
    }

    public static void glGetPolygonStipple(java.nio.ByteBuffer mask) {
        org.lwjgl.opengl.GL11.glGetPolygonStipple(mask);
    }

    public static java.lang.String glGetString(int name) {
        return org.lwjgl.opengl.GL11.glGetString(name);
    }

    public static void glGetTexEnv(int coord, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexEnvfv(coord, pname, params);
    }

    public static void glGetTexEnv(int coord, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexEnviv(coord, pname, params);
    }

    public static float glGetTexEnvf(int coord, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexEnvf(coord, pname);
    }

    public static int glGetTexEnvi(int coord, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexEnvi(coord, pname);
    }

    public static void glGetTexGen(int coord, int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexGendv(coord, pname, params);
    }

    public static void glGetTexGen(int coord, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexGenfv(coord, pname, params);
    }

    public static void glGetTexGen(int coord, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexGeniv(coord, pname, params);
    }

    public static double glGetTexGend(int coord, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexGend(coord, pname);
    }

    public static float glGetTexGenf(int coord, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexGenf(coord, pname);
    }

    public static int glGetTexGeni(int coord, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexGeni(coord, pname);
    }

    public static void glGetTexImage(int target, int level, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11.glGetTexImage(target, level, format, type, pixels_buffer_offset);
    }

    public static void glGetTexImage(int target, int level, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void glGetTexImage(int target, int level, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void glGetTexImage(int target, int level, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void glGetTexImage(int target, int level, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void glGetTexImage(int target, int level, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glGetTexImage(target, level, format, type, pixels);
    }

    public static void glGetTexLevelParameter(int target, int level, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexLevelParameterfv(target, level, pname, params);
    }

    public static void glGetTexLevelParameter(int target, int level, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexLevelParameteriv(target, level, pname, params);
    }

    public static float glGetTexLevelParameterf(int target, int level, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexLevelParameterf(target, level, pname);
    }

    public static int glGetTexLevelParameteri(int target, int level, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexLevelParameteri(target, level, pname);
    }

    public static void glGetTexParameter(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexParameterfv(target, pname, params);
    }

    public static void glGetTexParameter(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glGetTexParameteriv(target, pname, params);
    }

    public static float glGetTexParameterf(int target, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexParameterf(target, pname);
    }

    public static int glGetTexParameteri(int target, int pname) {
        return org.lwjgl.opengl.GL11.glGetTexParameteri(target, pname);
    }

    public static void glHint(int target, int mode) {
        org.lwjgl.opengl.GL11.glHint(target, mode);
    }

    public static void glInitNames() {
        org.lwjgl.opengl.GL11.glInitNames();
    }

    public static void glInterleavedArrays(int format, int stride, long pointer_buffer_offset) {
        org.lwjgl.opengl.GL11.glInterleavedArrays(format, stride, pointer_buffer_offset);
    }

    public static void glInterleavedArrays(int format, int stride, java.nio.ByteBuffer pointer) {
        org.lwjgl.opengl.GL11.glInterleavedArrays(format, stride, pointer);
    }

    public static void glInterleavedArrays(int format, int stride, java.nio.DoubleBuffer pointer) {
        org.lwjgl.opengl.GL11.glInterleavedArrays(format, stride, pointer);
    }

    public static void glInterleavedArrays(int format, int stride, java.nio.FloatBuffer pointer) {
        org.lwjgl.opengl.GL11.glInterleavedArrays(format, stride, pointer);
    }

    public static void glInterleavedArrays(int format, int stride, java.nio.IntBuffer pointer) {
        org.lwjgl.opengl.GL11.glInterleavedArrays(format, stride, pointer);
    }

    public static void glInterleavedArrays(int format, int stride, java.nio.ShortBuffer pointer) {
        org.lwjgl.opengl.GL11.glInterleavedArrays(format, stride, pointer);
    }

    public static boolean glIsEnabled(int cap) {
        return org.lwjgl.opengl.GL11.glIsEnabled(cap);
    }

    public static boolean glIsList(int list) {
        return org.lwjgl.opengl.GL11.glIsList(list);
    }

    public static boolean glIsTexture(int texture) {
        return org.lwjgl.opengl.GL11.glIsTexture(texture);
    }

    public static void glLight(int light, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glLightfv(light, pname, params);
    }

    public static void glLight(int light, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glLightiv(light, pname, params);
    }

    public static void glLightModel(int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glLightModelfv(pname, params);
    }

    public static void glLightModel(int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glLightModeliv(pname, params);
    }

    public static void glLightModelf(int pname, float param) {
        org.lwjgl.opengl.GL11.glLightModelf(pname, param);
    }

    public static void glLightModeli(int pname, int param) {
        org.lwjgl.opengl.GL11.glLightModeli(pname, param);
    }

    public static void glLightf(int light, int pname, float param) {
        org.lwjgl.opengl.GL11.glLightf(light, pname, param);
    }

    public static void glLighti(int light, int pname, int param) {
        org.lwjgl.opengl.GL11.glLighti(light, pname, param);
    }

    public static void glLineStipple(int factor, short pattern) {
        org.lwjgl.opengl.GL11.glLineStipple(factor, pattern);
    }

    public static void glLineWidth(float width) {
        org.lwjgl.opengl.GL11.glLineWidth(width);
    }

    public static void glListBase(int base) {
        org.lwjgl.opengl.GL11.glListBase(base);
    }

    public static void glLoadIdentity() {
        org.lwjgl.opengl.GL11.glLoadIdentity();
    }

    public static void glLoadMatrix(java.nio.DoubleBuffer m) {
        org.lwjgl.opengl.GL11.glLoadMatrixd(m);
    }

    public static void glLoadMatrix(java.nio.FloatBuffer m) {
        org.lwjgl.opengl.GL11.glLoadMatrixf(m);
    }

    public static void glLoadName(int name) {
        org.lwjgl.opengl.GL11.glLoadName(name);
    }

    public static void glLogicOp(int opcode) {
        org.lwjgl.opengl.GL11.glLogicOp(opcode);
    }

    public static void glMap1d(int target, double u1, double u2, int stride, int order, java.nio.DoubleBuffer points) {
        org.lwjgl.opengl.GL11.glMap1d(target, u1, u2, stride, order, points);
    }

    public static void glMap1f(int target, float u1, float u2, int stride, int order, java.nio.FloatBuffer points) {
        org.lwjgl.opengl.GL11.glMap1f(target, u1, u2, stride, order, points);
    }

    public static void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2,
            int vstride, int vorder, java.nio.DoubleBuffer points) {
        org.lwjgl.opengl.GL11.glMap2d(target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points);
    }

    public static void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride,
            int vorder, java.nio.FloatBuffer points) {
        org.lwjgl.opengl.GL11.glMap2f(target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points);
    }

    public static void glMapGrid1d(int un, double u1, double u2) {
        org.lwjgl.opengl.GL11.glMapGrid1d(un, u1, u2);
    }

    public static void glMapGrid1f(int un, float u1, float u2) {
        org.lwjgl.opengl.GL11.glMapGrid1f(un, u1, u2);
    }

    public static void glMapGrid2d(int un, double u1, double u2, int vn, double v1, double v2) {
        org.lwjgl.opengl.GL11.glMapGrid2d(un, u1, u2, vn, v1, v2);
    }

    public static void glMapGrid2f(int un, float u1, float u2, int vn, float v1, float v2) {
        org.lwjgl.opengl.GL11.glMapGrid2f(un, u1, u2, vn, v1, v2);
    }

    public static void glMaterial(int face, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glMaterialfv(face, pname, params);
    }

    public static void glMaterial(int face, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glMaterialiv(face, pname, params);
    }

    public static void glMaterialf(int face, int pname, float param) {
        org.lwjgl.opengl.GL11.glMaterialf(face, pname, param);
    }

    public static void glMateriali(int face, int pname, int param) {
        org.lwjgl.opengl.GL11.glMateriali(face, pname, param);
    }

    public static void glMatrixMode(int mode) {
        org.lwjgl.opengl.GL11.glMatrixMode(mode);
    }

    public static void glMultMatrix(java.nio.DoubleBuffer m) {
        org.lwjgl.opengl.GL11.glMultMatrixd(m);
    }

    public static void glMultMatrix(java.nio.FloatBuffer m) {
        org.lwjgl.opengl.GL11.glMultMatrixf(m);
    }

    public static void glNewList(int list, int mode) {
        org.lwjgl.opengl.GL11.glNewList(list, mode);
    }

    public static void glNormal3b(byte nx, byte ny, byte nz) {
        org.lwjgl.opengl.GL11.glNormal3b(nx, ny, nz);
    }

    public static void glNormal3d(double nx, double ny, double nz) {
        org.lwjgl.opengl.GL11.glNormal3d(nx, ny, nz);
    }

    public static void glNormal3f(float nx, float ny, float nz) {
        org.lwjgl.opengl.GL11.glNormal3f(nx, ny, nz);
    }

    public static void glNormal3i(int nx, int ny, int nz) {
        org.lwjgl.opengl.GL11.glNormal3i(nx, ny, nz);
    }

    public static void glNormalPointer(int type, int stride, long pointer_buffer_offset) {
        org.lwjgl.opengl.GL11.glNormalPointer(type, stride, pointer_buffer_offset);
    }

    public static void glNormalPointer(int type, int stride, java.nio.ByteBuffer pointer) {
        org.lwjgl.opengl.GL11.glNormalPointer(type, stride, pointer);
    }

    public static void glNormalPointer(int stride, java.nio.ByteBuffer pointer) {

        org.lwjgl.opengl.GL11.glNormalPointer(org.lwjgl.opengl.GL11.GL_BYTE, stride, pointer);
    }

    public static void glNormalPointer(int stride, java.nio.DoubleBuffer pointer) {

        org.lwjgl.opengl.GL11.glNormalPointer(
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(pointer));
    }

    public static void glNormalPointer(int stride, java.nio.FloatBuffer pointer) {

        org.lwjgl.opengl.GL11.glNormalPointer(org.lwjgl.opengl.GL11.GL_FLOAT, stride, pointer);
    }

    public static void glNormalPointer(int stride, java.nio.IntBuffer pointer) {

        org.lwjgl.opengl.GL11.glNormalPointer(org.lwjgl.opengl.GL11.GL_INT, stride, pointer);
    }

    public static void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
        org.lwjgl.opengl.GL11.glOrtho(left, right, bottom, top, zNear, zFar);
    }

    public static void glPassThrough(float token) {
        org.lwjgl.opengl.GL11.glPassThrough(token);
    }

    public static void glPixelMap(int map, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.GL11.glPixelMapfv(map, values);
    }

    public static void glPixelMapfv(int map, int values_mapsize, long values_buffer_offset) {
        org.lwjgl.opengl.GL11.glPixelMapfv(map, values_mapsize, values_buffer_offset);
    }

    public static void glPixelMapu(int map, java.nio.IntBuffer values) {
        org.lwjgl.opengl.GL11.glPixelMapuiv(map, values);
    }

    public static void glPixelMapu(int map, java.nio.ShortBuffer values) {
        org.lwjgl.opengl.GL11.glPixelMapusv(map, values);
    }

    public static void glPixelMapuiv(int map, int values_mapsize, long values_buffer_offset) {
        org.lwjgl.opengl.GL11.glPixelMapuiv(map, values_mapsize, values_buffer_offset);
    }

    public static void glPixelMapusv(int map, int values_mapsize, long values_buffer_offset) {
        org.lwjgl.opengl.GL11.glPixelMapusv(map, values_mapsize, values_buffer_offset);
    }

    public static void glPixelStoref(int pname, float param) {
        org.lwjgl.opengl.GL11.glPixelStoref(pname, param);
    }

    public static void glPixelStorei(int pname, int param) {
        org.lwjgl.opengl.GL11.glPixelStorei(pname, param);
    }

    public static void glPixelTransferf(int pname, float param) {
        org.lwjgl.opengl.GL11.glPixelTransferf(pname, param);
    }

    public static void glPixelTransferi(int pname, int param) {
        org.lwjgl.opengl.GL11.glPixelTransferi(pname, param);
    }

    public static void glPixelZoom(float xfactor, float yfactor) {
        org.lwjgl.opengl.GL11.glPixelZoom(xfactor, yfactor);
    }

    public static void glPointSize(float size) {
        org.lwjgl.opengl.GL11.glPointSize(size);
    }

    public static void glPolygonMode(int face, int mode) {
        org.lwjgl.opengl.GL11.glPolygonMode(face, mode);
    }

    public static void glPolygonOffset(float factor, float units) {
        org.lwjgl.opengl.GL11.glPolygonOffset(factor, units);
    }

    public static void glPolygonStipple(long mask_buffer_offset) {
        org.lwjgl.opengl.GL11.glPolygonStipple(mask_buffer_offset);
    }

    public static void glPolygonStipple(java.nio.ByteBuffer mask) {
        org.lwjgl.opengl.GL11.glPolygonStipple(mask);
    }

    public static void glPopAttrib() {
        org.lwjgl.opengl.GL11.glPopAttrib();
    }

    public static void glPopClientAttrib() {
        org.lwjgl.opengl.GL11.glPopClientAttrib();
    }

    public static void glPopMatrix() {
        org.lwjgl.opengl.GL11.glPopMatrix();
    }

    public static void glPopName() {
        org.lwjgl.opengl.GL11.glPopName();
    }

    public static void glPrioritizeTextures(java.nio.IntBuffer textures, java.nio.FloatBuffer priorities) {
        org.lwjgl.opengl.GL11.glPrioritizeTextures(textures, priorities);
    }

    public static void glPushAttrib(int mask) {
        org.lwjgl.opengl.GL11.glPushAttrib(mask);
    }

    public static void glPushClientAttrib(int mask) {
        org.lwjgl.opengl.GL11.glPushClientAttrib(mask);
    }

    public static void glPushMatrix() {
        org.lwjgl.opengl.GL11.glPushMatrix();
    }

    public static void glPushName(int name) {
        org.lwjgl.opengl.GL11.glPushName(name);
    }

    public static void glRasterPos2d(double x, double y) {
        org.lwjgl.opengl.GL11.glRasterPos2d(x, y);
    }

    public static void glRasterPos2f(float x, float y) {
        org.lwjgl.opengl.GL11.glRasterPos2f(x, y);
    }

    public static void glRasterPos2i(int x, int y) {
        org.lwjgl.opengl.GL11.glRasterPos2i(x, y);
    }

    public static void glRasterPos3d(double x, double y, double z) {
        org.lwjgl.opengl.GL11.glRasterPos3d(x, y, z);
    }

    public static void glRasterPos3f(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glRasterPos3f(x, y, z);
    }

    public static void glRasterPos3i(int x, int y, int z) {
        org.lwjgl.opengl.GL11.glRasterPos3i(x, y, z);
    }

    public static void glRasterPos4d(double x, double y, double z, double w) {
        org.lwjgl.opengl.GL11.glRasterPos4d(x, y, z, w);
    }

    public static void glRasterPos4f(float x, float y, float z, float w) {
        org.lwjgl.opengl.GL11.glRasterPos4f(x, y, z, w);
    }

    public static void glRasterPos4i(int x, int y, int z, int w) {
        org.lwjgl.opengl.GL11.glRasterPos4i(x, y, z, w);
    }

    public static void glReadBuffer(int mode) {
        org.lwjgl.opengl.GL11.glReadBuffer(mode);
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11.glReadPixels(x, y, width, height, format, type, pixels_buffer_offset);
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type,
            java.nio.DoubleBuffer pixels) {

        org.lwjgl.opengl.GL11.glReadPixels(x, y, width, height, format, type, MemoryUtil.getAddress(pixels));
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static void glReadPixels(int x, int y, int width, int height, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    public static void glRectd(double x1, double y1, double x2, double y2) {
        org.lwjgl.opengl.GL11.glRectd(x1, y1, x2, y2);
    }

    public static void glRectf(float x1, float y1, float x2, float y2) {
        org.lwjgl.opengl.GL11.glRectf(x1, y1, x2, y2);
    }

    public static void glRecti(int x1, int y1, int x2, int y2) {
        org.lwjgl.opengl.GL11.glRecti(x1, y1, x2, y2);
    }

    public static int glRenderMode(int mode) {
        return org.lwjgl.opengl.GL11.glRenderMode(mode);
    }

    public static void glRotated(double angle, double x, double y, double z) {
        org.lwjgl.opengl.GL11.glRotated(angle, x, y, z);
    }

    public static void glRotatef(float angle, float x, float y, float z) {
        org.lwjgl.opengl.GL11.glRotatef(angle, x, y, z);
    }

    public static void glScaled(double x, double y, double z) {
        org.lwjgl.opengl.GL11.glScaled(x, y, z);
    }

    public static void glScalef(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glScalef(x, y, z);
    }

    public static void glScissor(int x, int y, int width, int height) {
        org.lwjgl.opengl.GL11.glScissor(x, y, width, height);
    }

    public static void glSelectBuffer(java.nio.IntBuffer buffer) {
        org.lwjgl.opengl.GL11.glSelectBuffer(buffer);
    }

    public static void glShadeModel(int mode) {
        org.lwjgl.opengl.GL11.glShadeModel(mode);
    }

    public static void glStencilFunc(int func, int ref, int mask) {
        org.lwjgl.opengl.GL11.glStencilFunc(func, ref, mask);
    }

    public static void glStencilMask(int mask) {
        org.lwjgl.opengl.GL11.glStencilMask(mask);
    }

    public static void glStencilOp(int fail, int zfail, int zpass) {
        org.lwjgl.opengl.GL11.glStencilOp(fail, zfail, zpass);
    }

    public static void glTexCoord1d(double s) {
        org.lwjgl.opengl.GL11.glTexCoord1d(s);
    }

    public static void glTexCoord1f(float s) {
        org.lwjgl.opengl.GL11.glTexCoord1f(s);
    }

    public static void glTexCoord2d(double s, double t) {
        org.lwjgl.opengl.GL11.glTexCoord2d(s, t);
    }

    public static void glTexCoord2f(float s, float t) {
        org.lwjgl.opengl.GL11.glTexCoord2f(s, t);
    }

    public static void glTexCoord3d(double s, double t, double r) {
        org.lwjgl.opengl.GL11.glTexCoord3d(s, t, r);
    }

    public static void glTexCoord3f(float s, float t, float r) {
        org.lwjgl.opengl.GL11.glTexCoord3f(s, t, r);
    }

    public static void glTexCoord4d(double s, double t, double r, double q) {
        org.lwjgl.opengl.GL11.glTexCoord4d(s, t, r, q);
    }

    public static void glTexCoord4f(float s, float t, float r, float q) {
        org.lwjgl.opengl.GL11.glTexCoord4f(s, t, r, q);
    }

    public static void glTexCoordPointer(int size, int type, int stride, long pointer_buffer_offset) {
        org.lwjgl.opengl.GL11.glTexCoordPointer(size, type, stride, pointer_buffer_offset);
    }

    public static void glTexCoordPointer(int size, int type, int stride, java.nio.ByteBuffer pointer) {
        org.lwjgl.opengl.GL11.glTexCoordPointer(size, type, stride, pointer);
    }

    public static void glTexCoordPointer(int size, int stride, java.nio.DoubleBuffer pointer) {

        org.lwjgl.opengl.GL11.glTexCoordPointer(
                size,
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(pointer));
    }

    public static void glTexCoordPointer(int size, int stride, java.nio.FloatBuffer pointer) {

        org.lwjgl.opengl.GL11.glTexCoordPointer(size, org.lwjgl.opengl.GL11.GL_FLOAT, stride, pointer);
    }

    public static void glTexCoordPointer(int size, int stride, java.nio.IntBuffer pointer) {

        org.lwjgl.opengl.GL11.glTexCoordPointer(size, org.lwjgl.opengl.GL11.GL_INT, stride, pointer);
    }

    public static void glTexCoordPointer(int size, int stride, java.nio.ShortBuffer pointer) {

        org.lwjgl.opengl.GL11.glTexCoordPointer(size, org.lwjgl.opengl.GL11.GL_SHORT, stride, pointer);
    }

    public static void glTexEnv(int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glTexEnvfv(target, pname, params);
    }

    public static void glTexEnv(int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glTexEnviv(target, pname, params);
    }

    public static void glTexEnvf(int target, int pname, float param) {
        org.lwjgl.opengl.GL11.glTexEnvf(target, pname, param);
    }

    public static void glTexEnvi(int target, int pname, int param) {
        org.lwjgl.opengl.GL11.glTexEnvi(target, pname, param);
    }

    public static void glTexGen(int coord, int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.GL11.glTexGendv(coord, pname, params);
    }

    public static void glTexGen(int coord, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL11.glTexGenfv(coord, pname, params);
    }

    public static void glTexGen(int coord, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL11.glTexGeniv(coord, pname, params);
    }

    public static void glTexGend(int coord, int pname, double param) {
        org.lwjgl.opengl.GL11.glTexGend(coord, pname, param);
    }

    public static void glTexGenf(int coord, int pname, float param) {
        org.lwjgl.opengl.GL11.glTexGenf(coord, pname, param);
    }

    public static void glTexGeni(int coord, int pname, int param) {
        org.lwjgl.opengl.GL11.glTexGeni(coord, pname, param);
    }

    public static void glTexImage1D(int target, int level, int internalformat, int width, int border, int format,
            int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11
                .glTexImage1D(target, level, internalformat, width, border, format, type, pixels_buffer_offset);
    }

    public static void glTexImage1D(int target, int level, int internalformat, int width, int border, int format,
            int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTexImage1D(int target, int level, int internalformat, int width, int border, int format,
            int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTexImage1D(int target, int level, int internalformat, int width, int border, int format,
            int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTexImage1D(int target, int level, int internalformat, int width, int border, int format,
            int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTexImage1D(int target, int level, int internalformat, int width, int border, int format,
            int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11
                .glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels_buffer_offset);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexImage2D(int target, int level, int internalformat, int width, int height, int border,
            int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    public static void glTexParameter(int target, int pname, java.nio.FloatBuffer param) {
        org.lwjgl.opengl.GL11.glTexParameterfv(target, pname, param);
    }

    public static void glTexParameter(int target, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.GL11.glTexParameteriv(target, pname, param);
    }

    public static void glTexParameterf(int target, int pname, float param) {
        org.lwjgl.opengl.GL11.glTexParameterf(target, pname, param);
    }

    public static void glTexParameteri(int target, int pname, int param) {
        org.lwjgl.opengl.GL11.glTexParameteri(target, pname, param);
    }

    public static void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels_buffer_offset);
    }

    public static void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    public static void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type,
            java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    public static void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    public static void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    public static void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL11
                .glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels_buffer_offset);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
            int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTranslated(double x, double y, double z) {
        org.lwjgl.opengl.GL11.glTranslated(x, y, z);
    }

    public static void glTranslatef(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glTranslatef(x, y, z);
    }

    public static void glVertex2d(double x, double y) {
        org.lwjgl.opengl.GL11.glVertex2d(x, y);
    }

    public static void glVertex2f(float x, float y) {
        org.lwjgl.opengl.GL11.glVertex2f(x, y);
    }

    public static void glVertex2i(int x, int y) {
        org.lwjgl.opengl.GL11.glVertex2i(x, y);
    }

    public static void glVertex3d(double x, double y, double z) {
        org.lwjgl.opengl.GL11.glVertex3d(x, y, z);
    }

    public static void glVertex3f(float x, float y, float z) {
        org.lwjgl.opengl.GL11.glVertex3f(x, y, z);
    }

    public static void glVertex3i(int x, int y, int z) {
        org.lwjgl.opengl.GL11.glVertex3i(x, y, z);
    }

    public static void glVertex4d(double x, double y, double z, double w) {
        org.lwjgl.opengl.GL11.glVertex4d(x, y, z, w);
    }

    public static void glVertex4f(float x, float y, float z, float w) {
        org.lwjgl.opengl.GL11.glVertex4f(x, y, z, w);
    }

    public static void glVertex4i(int x, int y, int z, int w) {
        org.lwjgl.opengl.GL11.glVertex4i(x, y, z, w);
    }

    public static void glVertexPointer(int size, int type, int stride, long pointer_buffer_offset) {
        org.lwjgl.opengl.GL11.glVertexPointer(size, type, stride, pointer_buffer_offset);
    }

    public static void glVertexPointer(int size, int type, int stride, java.nio.ByteBuffer pointer) {
        org.lwjgl.opengl.GL11.glVertexPointer(size, type, stride, pointer);
    }

    public static void glVertexPointer(int size, int stride, java.nio.DoubleBuffer pointer) {

        org.lwjgl.opengl.GL11.glVertexPointer(
                size,
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(pointer));
    }

    public static void glVertexPointer(int size, int stride, java.nio.FloatBuffer pointer) {

        org.lwjgl.opengl.GL11.glVertexPointer(size, org.lwjgl.opengl.GL11.GL_FLOAT, stride, pointer);
    }

    public static void glVertexPointer(int size, int stride, java.nio.IntBuffer pointer) {

        org.lwjgl.opengl.GL11.glVertexPointer(size, org.lwjgl.opengl.GL11.GL_INT, stride, pointer);
    }

    public static void glVertexPointer(int size, int stride, java.nio.ShortBuffer pointer) {

        org.lwjgl.opengl.GL11.glVertexPointer(size, org.lwjgl.opengl.GL11.GL_SHORT, stride, pointer);
    }

    public static void glViewport(int x, int y, int width, int height) {
        org.lwjgl.opengl.GL11.glViewport(x, y, width, height);
    }
}
