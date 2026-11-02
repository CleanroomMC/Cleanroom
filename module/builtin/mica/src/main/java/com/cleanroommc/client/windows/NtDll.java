/// Copyright under MIT https://github.com/LemonCaramel/Mica
package com.cleanroommc.client.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;


public interface NtDll extends Library {

    /* NTDLL */
    NtDll INSTANCE = Native.load("ntdll", NtDll.class);

    void RtlGetNtVersionNumbers(
            IntByReference MajorVersion,
            IntByReference MinorVersion,
            IntByReference BuildNumber
    );

    static void initializeWindowsInformation(long handle) {
        // Get Windows Info
        final IntByReference majorVersion = new IntByReference();
        final IntByReference buildNumber = new IntByReference();
        INSTANCE.RtlGetNtVersionNumbers(majorVersion, new IntByReference(), buildNumber);

        // Write Info
        WindowsProperties.majorVersion = majorVersion.getValue();
        WindowsProperties.buildNumber = (buildNumber.getValue() & ~0xF0000000);
        WindowsProperties.handle = handle;
    }
}