package reflection.android.app;


import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.util.ArrayMap;

import java.util.List;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.app.ActivityThread} 的反射包装器。
 * <p>
 * ActivityThread 是 Android 应用进程的主线程入口，包含应用绑定数据、
 * Provider 映射、Instrumentation 等核心字段。
 * 该类还包含 ActivityThread 内部类 {@code ActivityClientRecord}、{@code AppBindData}、{@code H} 的反射包装。
 */
public class ActivityThread {
    /** 反射目标类 {@code android.app.ActivityThread} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.app.ActivityThread");

    /** 静态方法 {@code currentActivityThread()}，获取当前线程的 ActivityThread 实例 */
    public static MirrorReflection.StaticMethodWrapper<Object> currentActivityThread = REF.staticMethod("currentActivityThread");
    /** 字段 {@code mBoundApplication}，应用绑定数据（AppBindData） */
    public static MirrorReflection.FieldWrapper<Object> mBoundApplication = REF.field("mBoundApplication");
    /** 字段 {@code mH}，主线程 Handler */
    public static MirrorReflection.FieldWrapper<Handler> mH = REF.field("mH");
    /** 字段 {@code mInitialApplication}，初始 Application 实例 */
    public static MirrorReflection.FieldWrapper<Application> mInitialApplication = REF.field("mInitialApplication");
    /** 字段 {@code mProviderMap}，ContentProvider 映射表 */
    public static MirrorReflection.FieldWrapper<ArrayMap<Object, Object>> mProviderMap = REF.field("mProviderMap");
    /** 字段 {@code mInstrumentation}，Instrumentation 实例 */
    public static MirrorReflection.FieldWrapper<Instrumentation> mInstrumentation = REF.field("mInstrumentation");
    /** 静态字段 {@code sPackageManager}，全局 PackageManager 代理 */
    public static MirrorReflection.FieldWrapper<IInterface> sPackageManager = REF.field("sPackageManager");
    /** 实例方法 {@code getApplicationThread()}，获取 ApplicationThread 的 IBinder */
    public static MirrorReflection.MethodWrapper<IBinder> getApplicationThread = REF.method("getApplicationThread");
    /** 实例方法 {@code getSystemContext()}，获取系统 Context */
    public static MirrorReflection.MethodWrapper<Object> getSystemContext = REF.method("getSystemContext");


    /**
     * Android 隐藏内部类 {@code ActivityThread$ActivityClientRecord} 的反射包装器。
     * <p>
     * 记录 Activity 客户端信息，包括 Activity 实例、ActivityInfo 和 Intent。
     */
    public static class ActivityClientRecord {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.ActivityThread$ActivityClientRecord");
        public static MirrorReflection.FieldWrapper<Activity> activity = REF.field("activity");
        public static MirrorReflection.FieldWrapper<ActivityInfo> activityInfo = REF.field("activityInfo");
        public static MirrorReflection.FieldWrapper<Intent> intent = REF.field("intent");
    }

    /**
     * Android 隐藏内部类 {@code ActivityThread$AppBindData} 的反射包装器。
     * <p>
     * 存储应用绑定时的数据，包括 ApplicationInfo、进程名和 Provider 列表。
     */
    public static class AppBindData {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.ActivityThread$AppBindData");
        public static MirrorReflection.FieldWrapper<ApplicationInfo> appInfo = REF.field("appInfo");
        public static MirrorReflection.FieldWrapper<Object> info = REF.field("info");
        public static MirrorReflection.FieldWrapper<String> processName = REF.field("processName");
        public static MirrorReflection.FieldWrapper<ComponentName> instrumentationName = REF.field("instrumentationName");
        public static MirrorReflection.FieldWrapper<List<ProviderInfo>> providers = REF.field("providers");
    }

    /**
     * Android 隐藏内部类 {@code ActivityThread$H}（Handler 子类）的反射包装器。
     * <p>
     * 包含消息类型常量，用于在主线程中处理各种 Activity 生命周期事件。
     */
    public static class H {
        public static final MirrorReflection REF = MirrorReflection.on("android.app.ActivityThread$H");
        public static MirrorReflection.FieldWrapper<Integer> LAUNCH_ACTIVITY = REF.field("LAUNCH_ACTIVITY");
        public static MirrorReflection.FieldWrapper<Integer> EXECUTE_TRANSACTION = REF.field("EXECUTE_TRANSACTION");
    }
}
