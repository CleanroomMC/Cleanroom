__kernel void produce(write_only pipe int packets, int count, __global int *status) {
    for (int i = 0; i < count; i++) {
        int value = i * 17 - 3;
        status[i] = write_pipe(packets, &value);
    }
}

__kernel void consume(read_only pipe int packets, int count, __global int *output, __global int *status) {
    for (int i = 0; i < count; i++) {
        int value = -999;
        status[count + i] = read_pipe(packets, &value);
        output[i] = value;
    }
}

__kernel void empty(read_only pipe int packets, __global int *output) {
    int value = 0;
    output[0] = read_pipe(packets, &value);
}

__kernel void overflow(write_only pipe int packets, __global int *output) {
    for (int i = 0; i < 5; i++) {
        int value = i;
        output[i] = write_pipe(packets, &value);
    }
}
