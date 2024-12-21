package com.cleanroommc.client.ime;

import ca.weblite.objc.Runtime;
import ca.weblite.objc.RuntimeUtils;
import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.util.function.Consumer;

/**
 * Copied from <a href="https://github.com/reserveword/IMBlocker/pull/38">IMBlocker</a>
 */
public class CocoaIMEHandler implements Consumer<Boolean> {
    private static boolean active = false;
    static private final Pointer viewClass = Runtime.INSTANCE.objc_getClass("GLFWContentView");
    static private Pointer view = null;
    static private final InterpretKeyEventsCallback Imp;
    static private final InterpretKeyEventsCallback NewImp;


    static {
        // see https://github.com/glfw/glfw/blob/b4c3ef9d0fdf46845f3e81e5d989dab06e71e6c1/src/cocoa_window.m#L571
        // Replacing the method dynamically to determine whether to send text based on state
        // see reference for objc_runtime's dynamic manipulation at https://developer.apple.com/documentation/objectivec/objective-c_runtime
        var selector = RuntimeUtils.sel("interpretKeyEvents:");
        var method = Runtime.INSTANCE.class_getInstanceMethod(viewClass, selector);
        Imp = ObjC.INSTANCE.method_getImplementation(method);
        NewImp = (self, sel, eventArray) -> {
            if (view == null) view = self;
            if (!active) {
                var textInputContext = RuntimeUtils.cls("NSTextInputContext");
                var current = RuntimeUtils.msgPointer(textInputContext, "currentInputContext");
                RuntimeUtils.msg(current, "discardMarkedText");
                return;
            }
            Imp.invoke(self, sel, eventArray);
        };
        ObjC.INSTANCE.class_replaceMethod(viewClass, selector, NewImp, "v@:@");
    }
    /**
     * The underlying native type is IMP, which should be a function pointer to the implementation of interpretKeyEvents:
     * @see <a href="https://developer.apple.com/documentation/objectivec/objective-c_runtime/imp">Documentation for IMP</a>
     * @see <a href="https://developer.apple.com/documentation/appkit/nsresponder/1531599-interpretkeyevents?language=objc">Documentation for interpretKeyEvents:</a>
     */
    private interface InterpretKeyEventsCallback extends Callback {
        /**
         * @param self       "this" pointer for NSObject
         * @param selector   selector for interpretKeyEvents:
         * @param eventArray an array of NSEvent objects
         */
        void invoke(Pointer self, Pointer selector, Pointer eventArray);
    }

    /**
     * @see <a href="https://developer.apple.com/documentation/objectivec/objective-c_runtime">Apple Developer Documentation for objc_runtime:</a>
     */
    private interface ObjC extends Library {
        ObjC INSTANCE = Native.load("objc.A", ObjC.class);

        void class_replaceMethod(Pointer cls, Pointer selector, InterpretKeyEventsCallback imp, String types);

        InterpretKeyEventsCallback method_getImplementation(Pointer selector);
    }
    @Override
    public void accept(Boolean aBoolean) {
        if (active != aBoolean) {
            active = aBoolean;
        }
    }
}
