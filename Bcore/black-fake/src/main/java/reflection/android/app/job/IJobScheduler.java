package reflection.android.app.job;

import android.os.IBinder;
import android.os.IInterface;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.job.IJobScheduler} 的反射包装器。
 * <p>
 * IJobScheduler 是 JobScheduler 服务的 Binder 接口，用于调度后台任务。
 * 通过 {@code IJobScheduler$Stub.asInterface()} 获取服务代理。
 */
public class IJobScheduler {
    /**
     * IJobScheduler 的 Stub 内部类反射包装器，提供 {@code asInterface()} 方法。
     */
    public static class Stub {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.job.IJobScheduler$Stub");
        public static MirrorReflection.StaticMethodWrapper<IInterface> asInterface = REF.staticMethod("asInterface", IBinder.class);
    }
}
