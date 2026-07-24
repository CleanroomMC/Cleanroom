#include <forge/header.h>

__kernel void test(__global float* vals) {
    vals[get_local_id(0)] += one();
}