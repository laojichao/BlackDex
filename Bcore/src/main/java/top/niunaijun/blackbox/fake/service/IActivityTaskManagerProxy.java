package top.niunaijun.blackbox.fake.service;

import reflection.android.app.IActivityTaskManager;
import reflection.android.os.ServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.ScanClass;

/**
 * IActivityTaskManager 系统服务代理（Android 10.0+），拦截 Activity 任务管理相关调用。
 * <p>
 * Android 10.0 引入了 IActivityTaskManager 接口，将部分 Activity 管理功能从
 * IActivityManager 中分离出来。本类通过替换 ServiceManager 中 "activity_task"
 * 服务的 Binder 对象实现拦截。
 * </p>
 * <p>
 * 通过 {@link ScanClass} 引用 {@link ActivityManagerCommonProxy} 的公共方法钩子。
 * </p>
 *
 * @author Milk
 * @see ActivityManagerCommonProxy
 * @see IActivityManagerProxy
 */
@ScanClass(ActivityManagerCommonProxy.class)
public class IActivityTaskManagerProxy extends BinderInvocationStub {
    public static final String TAG = "ActivityTaskManager";

    public IActivityTaskManagerProxy() {
        super(ServiceManager.getService.call("activity_task"));
    }

    @Override
    protected Object getWho() {
        return IActivityTaskManager.Stub.asInterface.call(ServiceManager.getService.call("activity_task"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("activity_task");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
