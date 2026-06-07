package top.niunaijun.blackbox.utils.compat;

import android.os.IBinder;
import android.os.IInterface;

import reflection.android.app.ApplicationThreadNative;
import reflection.android.app.IApplicationThreadOreo;

/**
 * ApplicationThread兼容性工具类。
 * <p>
 * 处理Android 8.0（Oreo）前后 {@code IApplicationThread} 接口的变化。
 * <ul>
 *   <li>Android 8.0+：使用 {@code IApplicationThreadOreo.Stub.asInterface}</li>
 *   <li>Android 8.0以下：使用 {@code ApplicationThreadNative.asInterface}</li>
 * </ul>
 * <p>
 * IApplicationThread是AMS与应用进程通信的核心接口，用于Activity生命周期管理。
 */
public class ApplicationThreadCompat {

    /**
     * 将IBinder转换为IApplicationThread接口实例。
     * <p>
     * 根据系统版本选择不同的反射入口。
     *
     * @param binder ApplicationThread的IBinder引用
     * @return IApplicationThread接口实例
     */
    public static IInterface asInterface(IBinder binder) {
        if (BuildCompat.isOreo()) {
            return IApplicationThreadOreo.Stub.asInterface.call(binder);
        }
        return ApplicationThreadNative.asInterface.call(binder);
    }
}
