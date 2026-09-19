package com.cleanroommc.client.sdl.hid;

/**
 * One enumerated HID device. Vendor/product are unsigned 16-bits.
 */
public record HidInfo(String path, int vendor, int product, String serial, String manufacturer, String productName,
                      int usagePage, int usage, HidBus bus) { }
