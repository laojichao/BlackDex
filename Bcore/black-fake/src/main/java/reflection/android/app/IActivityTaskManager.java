package reflection.android.app;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.IActivityTaskManager} 的反射包装器。
 * <p>
 * IActivityTaskManager 是 Android 10 (Q) 引入的 Activity 任务管理服务接口，
 * 从 IActivityManager 中分离出来，专门负责 Activity 任务栈的管理。
 */
public class IActivityTaskManager {

    /**
     * IActivityTaskManager 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.IActivityTaskManager$Stub");

        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
