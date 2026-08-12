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