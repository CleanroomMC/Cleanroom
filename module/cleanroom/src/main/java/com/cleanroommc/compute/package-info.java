/**
 * Cleanroom OpenCL wrapper API
 *
 * <p>Provides functionality for the usage of OpenCL in Cleanroom and Cleanroom Mods. </p>
 * <p>The API, while designed with simplicity in mind, requires the knowledge of {@link java.nio.Buffer NIO Buffers},
 * memory management, parallel computation, and GPU based optimisations to be used effectively.
 * Misuse of the API can cause performance penalties.</p>
 * <p>
 *     The API relies on chained calls to the {@link com.cleanroommc.compute.cmd.CommandQueue CommandQueue} class.
 *     Other Memory objects such as {@link com.cleanroommc.compute.buffers.Buffer Buffers} and
 *     {@link com.cleanroommc.compute.images.Image Images} are instantiated outside the chains. <br/>
 *     Example: <pre>{@code
 *          Kernel kernel = Compute.instance().programs.get(new ResourceLocation("cleanroom:compute/example.cl")).kernel("example");
 *          CommandQueue queue = Compute.INSTANCE.queueDispatch.dispatch("example", true, false, true);
 *          Image2D img = new Image2D(queue, BufferFlags.READ_WRITE, 10, 10, 0, ChannelType.FLOAT, ChannelOrder.DEPTH, null);
 *          Buffer out = new Buffer(100 * 4, BufferFlags.WRITE_ONLY, BufferFlags.HOST_READ_ONLY);
 *          Pipe pipe = new Pipe(16, OpenCLPrimitive.FLOAT);
 *          float[] color = new float[] { .4f, 0.0f, 0.0f, 0.0f };
 *          float[] res = new float[100];
 *
 *          KernelParameterList paramList = new KernelParameterList(kernel);
 *          paramList.add(img);
 *          paramList.add(pipe);
 *          paramList.add(queue);
 *          paramList.add(out);
 *
 *          queue.imageFill(img, color, new Vector2L(0L,0L), new Vector2L(10L,10L))
 *              .next(kernel, paramList).read(out, res).execute();
 *     }</pre>
 * </p>
 */
@NullMarked
package com.cleanroommc.compute;

import org.jspecify.annotations.NullMarked;