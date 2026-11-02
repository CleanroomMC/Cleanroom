/**
 * <p>Command queue API</p>
 * <p>
 *     This is the heart of the API, it's where every OpenCL computation is scheduled to be executed.
 *     {@link com.cleanroommc.compute.cmd.CommandQueue} is at the centre, but it is not instantiated directly,
 *     instead, {@link com.cleanroommc.compute.cmd.CommandQueueDispatch} should be used instead, as it allows
 *     for the selection of device features one might need for their OpenCL goals.
 * </p>
 * <p>
 *     The code can be said to use a river-like approach. Chains of queue operations are joined together into one
 *     and then executed. This applies to both kernel dispatching and writing and reading to images and buffers.
 * </p>
 * <p>
 *     Example code: <pre>{@code
 *          CommandQueue.Event wv1 = queue.bufferWrite(v1, 0, bv1);
 *          CommandQueue.Event wv2 = queue.bufferWrite(v2, 0, bv2);
 *          queue.dispatchKernel(kernel, paramList, null, new long[]{vals1.length, vals2.length}, wv1.eventID, wv2.eventID)
 *              .read(output, out).execute();
 *     }</pre>
 * </p>
 */
@NullMarked
package com.cleanroommc.compute.cmd;

import org.jspecify.annotations.NullMarked;