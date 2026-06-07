package top.niunaijun.blackbox.app.configuration;

import android.app.Application;
import android.content.Context;

/**
 * 虚拟应用生命周期回调接口。
 * <p>
 * 提供虚拟环境中 Application 创建前后的回调钩子，允许宿主应用在关键生命周期节点
 * 执行自定义逻辑（如初始化 SDK、注入代码等）。
 *
 * @author Milk
 * @see BlackBoxCore#setAppLifecycleCallback
 */
public class AppLifecycleCallback {
    /** 空实现的默认回调实例 */
    public static AppLifecycleCallback EMPTY = new AppLifecycleCallback() {

    };

    /**
     * 在创建 Application 之前调用。
     *
     * @param packageName 目标应用包名
     * @param processName 目标进程名
     * @param context     包上下文
     */
    public void beforeCreateApplication(String packageName, String processName, Context context) {

    }

    /**
     * 在 Application.onCreate() 之前调用。
     *
     * @param packageName 目标应用包名
     * @param processName 目标进程名
     * @param application 目标应用的 Application 实例
     */
    public void beforeApplicationOnCreate(String packageName, String processName, Application application) {

    }

    /**
     * 在 Application.onCreate() 之后调用。
     *
     * @param packageName 目标应用包名
     * @param processName 目标进程名
     * @param application 目标应用的 Application 实例
     */
    public void afterApplicationOnCreate(String packageName, String processName, Application application) {

    }
}
