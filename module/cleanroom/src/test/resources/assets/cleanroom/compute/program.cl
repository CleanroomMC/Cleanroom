#include <forge/header.h>

__kernel void test(__global float* vals) {
    vals[get_global_id(0)] += one();
}

__kernel void byteTest(const ulong dimX, const ulong dimY,
                       __global const unsigned char* vals1,
                       __global const unsigned char* vals2,
                       __global unsigned char* outs) {
    outs[(get_global_id(0)*dimY)+get_global_id(1)] = vals1[get_global_id(0)] + vals2[get_global_id(1)];
}

__kernel void shortTest(const short2 p1, const short4 p2, __global short* out) {
    out[0] = p2.x;
    out[1] = p2.y;
    out[2] = p2.z;
    out[3] = p2.w;
    out[4] = p1.x;
    out[5] = p1.y;
}