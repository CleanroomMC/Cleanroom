package com.cleanroommc.client.ime;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WindowsIMEHandler implements Consumer<Boolean> {

    private static native WinNT.HANDLE ImmGetContext(WinDef.HWND hwnd);
    private static native WinNT.HANDLE ImmAssociateContext(WinDef.HWND hwnd, WinNT.HANDLE himc);
    private static native boolean ImmReleaseContext(WinDef.HWND hwnd, WinNT.HANDLE himc);
    private static native WinNT.HANDLE ImmCreateContext();
    private static native boolean ImmDestroyContext(WinNT.HANDLE himc);

    static {
        Native.register("imm32");
    }

    User32 user32 = User32.INSTANCE;

    @Override
    public void accept(Boolean active) {
        WinDef.HWND hwnd = user32.GetForegroundWindow();
        WinNT.HANDLE himc;
        if (active) {
            himc = ImmGetContext(hwnd);
            if (himc == null) {
                himc = ImmCreateContext();
                ImmAssociateContext(hwnd, himc);
            }
        } else {
            himc = ImmAssociateContext(hwnd, null);
            if (himc != null) {
                ImmDestroyContext(himc);
            }
        }
        ImmReleaseContext(hwnd, himc);
    }

}
