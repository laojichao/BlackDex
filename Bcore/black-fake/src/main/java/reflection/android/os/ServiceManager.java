package reflection.android.os;

import android.os.IBinder;
import android.os.IInterface;

import java.util.Map;

import reflection.MirrorReflection;

/**
 * Android 隐藏 API {@code android.os.ServiceManager} 的反射包装器。
 * <p>
 * ServiceManager 是 Android 系统服务管理器，负责管理系统服务的注册和获取。
 * 提供 {@code addService()}、{@code getService()}、{@code getIServiceManager()} 等
 * 静态方法以及 {@code sCache} 服务缓存的访问。
 */
public class ServiceManager {
    /** 反射目标类 {@code android.os.ServiceManager} 的包装器 */
    public static final MirrorReflection REF = MirrorReflection.on("android.os.ServiceManager");
    /** 静态方法 {@code addService(String, IBinder)}，注册系统服务 */
    public static MirrorReflection.StaticMethodWrapper<Void> addService = REF.staticMethod("addService", String.class, IBinder.class);
    /** 静态方法 {@code getIServiceManager()}，获取 IServiceManager 代理 */
    public static MirrorReflection.StaticMethodWrapper<IInterface> getIServiceManager = REF.staticMethod("getIServiceManager");
    /** 静态方法 {@code getService(String)}，获取系统服务的 IBinder */
    public static MirrorReflection.StaticMethodWrapper<IBinder> getService = REF.staticMethod("getService");
    /** 静态字段 {@code sCache}，系统服务缓存映射表 */
    public static MirrorReflection.FieldWrapper<Map<String, IBinder>> sCache = REF.field("sCache");
}
