package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

import reflection.android.app.job.IJobScheduler;
import reflection.android.os.ServiceManager;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;

/**
 * IJobScheduler 系统服务代理，拦截后台任务调度相关调用。
 * <p>
 * 通过替换 ServiceManager 中 {@link Context#JOB_SCHEDULER_SERVICE} 的 Binder 对象
 * 实现拦截。拦截 schedule/cancel/cancelAll 方法并返回默认值，
 * 阻止虚拟环境中的应用直接操作系统级 JobScheduler。
 * </p>
 *
 * @author Milk
 * @see BinderInvocationStub
 */
public class IJobServiceProxy extends BinderInvocationStub {
    public static final String TAG = "JobServiceStub";

    public IJobServiceProxy() {
        super(ServiceManager.getService.call(Context.JOB_SCHEDULER_SERVICE));
    }

    @Override
    protected Object getWho() {
        IBinder jobScheduler = ServiceManager.getService.call("jobscheduler");
        return IJobScheduler.Stub.asInterface.call(jobScheduler);
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    /** 拦截 schedule 方法，返回 0 阻止任务调度 */
    @ProxyMethod(name = "schedule")
    public static class Schedule extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 cancel 方法，返回 0 阻止任务取消 */
    @ProxyMethod(name = "cancel")
    public static class Cancel extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    /** 拦截 cancelAll 方法，返回 0 阻止取消所有任务 */
    @ProxyMethod(name = "cancelAll")
    public static class CancelAll extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            return 0;
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
