package com.cleanroommc.cleanroom.client.windows;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.platform.win32.Guid.GUID;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.lwjgl.glfw.GLFWNativeWin32;

import java.util.List;

public final class TaskbarApi implements AutoCloseable {
    private TaskbarApi(Pointer raw, boolean needCoUninitialize) {
        this.raw = raw;
        this.needCoUninitialize = needCoUninitialize;
    }

    private final Pointer raw;
    private final boolean needCoUninitialize;
    private boolean closed;

    private static final GUID CLSID_TASKBAR_LIST =
            new GUID("{56FDF344-FD6D-11d0-958A-006097C9A090}");
    private static final GUID IID_ITASKBAR_LIST3 =
            new GUID("{EA1AFB91-9E28-4B86-90E9-9E9F8A5EEFAF}");

    private static final int CLSCTX_INPROC_SERVER = 0x1;
    private static final int COINIT_APARTMENTTHREADED = 0x2;
    private static final int RPC_E_CHANGED_MODE = 0x80010106;

    // IUnknown
    private static final int VTBL_QUERY_INTERFACE = 0;
    private static final int VTBL_ADD_REF = 1;
    private static final int VTBL_RELEASE = 2;

    // ITaskbarList
    private static final int VTBL_HR_INIT = 3;

    // ITaskbarList3
    private static final int VTBL_SET_PROGRESS_VALUE = 9;
    private static final int VTBL_SET_PROGRESS_STATE = 10;
    private static final int VTBL_SET_OVERLAY_ICON = 18;
    
    private static TaskbarApi INSTANCE;
    
    public static TaskbarApi getInstance() {
        return INSTANCE;
    }

    public enum TBPFLAG {
        TBPF_NOPROGRESS(0x00000000),
        TBPF_INDETERMINATE(0x00000001),
        TBPF_NORMAL(0x00000002),
        TBPF_ERROR(0x00000004),
        TBPF_PAUSED(0x00000008);

        public final int value;

        TBPFLAG(int value) {
            this.value = value;
        }
    }

    public static TaskbarApi create() {
        HRESULT hrInit = Ole32Ex.INSTANCE.CoInitializeEx(Pointer.NULL, COINIT_APARTMENTTHREADED);
        int initCode = hrInit.intValue();

        // S_OK = 0, S_FALSE = 1, RPC_E_CHANGED_MODE = 0x80010106
        if (initCode != 0 && initCode != 1 && initCode != RPC_E_CHANGED_MODE) {
            throw new IllegalStateException("CoInitializeEx failed: 0x" + hex(initCode));
        }

        boolean needCoUninitialize = (initCode == 0 || initCode == 1);

        PointerByReference ppv = new PointerByReference();
        HRESULT hr = Ole32Ex.INSTANCE.CoCreateInstance(
                CLSID_TASKBAR_LIST,
                Pointer.NULL,
                CLSCTX_INPROC_SERVER,
                IID_ITASKBAR_LIST3,
                ppv
        );
        checkHR(hr, "CoCreateInstance(CLSID_TaskbarList, IID_ITaskbarList3)");

        TaskbarApi api = new TaskbarApi(ppv.getValue(), needCoUninitialize);
        api.hrInit();
        INSTANCE = api;
        return api;
    }

    public static HWND hwndFromGlfw(long glfwWindow) {
        return new HWND(Pointer.createConstant(GLFWNativeWin32.glfwGetWin32Window(glfwWindow)));
    }

    public void hrInit() {
        checkHR(invokeHRESULT(VTBL_HR_INIT), "ITaskbarList::HrInit");
    }

    public void setProgressState(HWND hwnd, TBPFLAG state) {
        checkHR(invokeHRESULT(VTBL_SET_PROGRESS_STATE, hwnd, state.value),
                "ITaskbarList3::SetProgressState");
    }

    public void setProgressValue(HWND hwnd, long completed, long total) {
        checkHR(invokeHRESULT(VTBL_SET_PROGRESS_VALUE, hwnd, completed, total),
                "ITaskbarList3::SetProgressValue");
    }

    public void clearProgress(HWND hwnd) {
        setProgressState(hwnd, TBPFLAG.TBPF_NOPROGRESS);
    }

    public void setOverlayIcon(HWND hwnd, HICON hIcon, String description) {
        checkHR(invokeHRESULT(
                        VTBL_SET_OVERLAY_ICON,
                        hwnd,
                        hIcon,
                        description == null ? null : new WString(description)
                ),
                "ITaskbarList3::SetOverlayIcon");
    }

    public void clearOverlayIcon(HWND hwnd) {
        setOverlayIcon(hwnd, null, null);
    }

    public int addRef() {
        return invokeInt(VTBL_ADD_REF);
    }

    public int release() {
        return invokeInt(VTBL_RELEASE);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        try {
            release();
        } finally {
            if (needCoUninitialize) {
                Ole32Ex.INSTANCE.CoUninitialize();
            }
        }
    }

    public static boolean flashTaskbar(HWND hwnd, int count) {
        User32Ex.FLASHWINFO info = new User32Ex.FLASHWINFO();
        info.hwnd = hwnd;
        info.dwFlags = User32Ex.FLASHW_TRAY;
        info.uCount = count;
        info.dwTimeout = 0;
        return User32Ex.INSTANCE.FlashWindowEx(info);
    }

    public static boolean flashTaskbarUntilForeground(long window) {
        final WinDef.HWND hwnd = TaskbarApi.hwndFromGlfw(window);
        User32Ex.FLASHWINFO info = new User32Ex.FLASHWINFO();
        info.hwnd = hwnd;
        info.dwFlags = User32Ex.FLASHW_TRAY | User32Ex.FLASHW_TIMERNOFG;
        info.uCount = 0;
        info.dwTimeout = 0;
        return User32Ex.INSTANCE.FlashWindowEx(info);
    }

    public static boolean stopFlash(HWND hwnd) {
        User32Ex.FLASHWINFO info = new User32Ex.FLASHWINFO();
        info.hwnd = hwnd;
        info.dwFlags = User32Ex.FLASHW_STOP;
        info.uCount = 0;
        info.dwTimeout = 0;
        return User32Ex.INSTANCE.FlashWindowEx(info);
    }

    public static void clearInstance() {
        TaskbarApi old = INSTANCE;
        INSTANCE = null;
        if (old != null) {
            old.close();
        }
    }

    private HRESULT invokeHRESULT(int vtblIndex, Object... args) {
        Pointer vtbl = raw.getPointer(0);
        Pointer fnPtr = vtbl.getPointer((long) vtblIndex * Native.POINTER_SIZE);
        Function fn = Function.getFunction(fnPtr, Function.ALT_CONVENTION);

        Object[] fullArgs = new Object[args.length + 1];
        fullArgs[0] = raw;
        System.arraycopy(args, 0, fullArgs, 1, args.length);

        return (HRESULT) fn.invoke(HRESULT.class, fullArgs);
    }

    private int invokeInt(int vtblIndex, Object... args) {
        Pointer vtbl = raw.getPointer(0);
        Pointer fnPtr = vtbl.getPointer((long) vtblIndex * Native.POINTER_SIZE);
        Function fn = Function.getFunction(fnPtr, Function.ALT_CONVENTION);

        Object[] fullArgs = new Object[args.length + 1];
        fullArgs[0] = raw;
        System.arraycopy(args, 0, fullArgs, 1, args.length);

        return fn.invokeInt(fullArgs);
    }

    private static void checkHR(HRESULT hr, String action) {
        int code = hr.intValue();
        if (code < 0) {
            throw new IllegalStateException(action + " failed: 0x" + hex(code));
        }
    }

    private static String hex(int value) {
        return String.format("%08X", value);
    }

    public interface Ole32Ex extends StdCallLibrary {
        Ole32Ex INSTANCE = Native.load("ole32", Ole32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        HRESULT CoInitializeEx(Pointer pvReserved, int dwCoInit);

        void CoUninitialize();

        HRESULT CoCreateInstance(
                GUID rclsid,
                Pointer pUnkOuter,
                int dwClsContext,
                GUID riid,
                PointerByReference ppv
        );
    }

    public interface User32Ex extends StdCallLibrary {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        int FLASHW_STOP = 0x00000000;
        int FLASHW_CAPTION = 0x00000001;
        int FLASHW_TRAY = 0x00000002;
        int FLASHW_ALL = FLASHW_CAPTION | FLASHW_TRAY;
        int FLASHW_TIMER = 0x00000004;
        int FLASHW_TIMERNOFG = 0x0000000C;

        boolean FlashWindowEx(FLASHWINFO info);

        class FLASHWINFO extends Structure {
            public int cbSize = size();
            public HWND hwnd;
            public int dwFlags;
            public int uCount;
            public int dwTimeout;

            @Override
            protected List<String> getFieldOrder() {
                return List.of("cbSize", "hwnd", "dwFlags", "uCount", "dwTimeout");
            }
        }
    }
}