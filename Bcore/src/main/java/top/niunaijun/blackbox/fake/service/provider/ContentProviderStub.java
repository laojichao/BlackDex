package top.niunaijun.blackbox.fake.service.provider;

import android.os.IInterface;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;

/**
 * 通用 ContentProvider 代理桩，拦截 ContentProvider 调用并替换包名参数。
 * <p>
 * 实现 {@link VContentProvider} 接口，通过 {@link #wrapper(IInterface, String)} 方法
 * 包装原始的 ContentProvider 代理对象。在方法调用时自动将第一个 String 类型参数
 * （通常是包名）替换为宿主应用包名，使虚拟环境中的 ContentProvider 调用
 * 看起来来自宿主应用。
 * </p>
 * <p>
 * 对 asBinder 方法不做替换，直接透传给原始对象以保持 Binder 一致性。
 * </p>
 *
 * @author Milk
 * @see ClassInvocationStub
 * @see VContentProvider
 * @see SettingsProviderStub
 */
public class ContentProviderStub extends ClassInvocationStub implements VContentProvider {
    public static final String TAG = "ContentProviderStub";
    private IInterface mBase;

    /**
     * 包装原始 ContentProvider 代理，创建虚拟环境代理并注入 Hook。
     *
     * @param contentProviderProxy 原始 ContentProvider 的 IInterface 代理
     * @param appPkg               应用包名（当前未使用，由宿主包名替代）
     * @return 包装后的 IInterface 代理
     */
    public IInterface wrapper(final IInterface contentProviderProxy, final String appPkg) {
        mBase = contentProviderProxy;
        injectHook();
        return (IInterface) getProxyInvocation();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {

    }

    @Override
    protected void onBindMethod() {

    }

    /**
     * 方法调用拦截器：asBinder 直接透传，其他方法的第一个 String 参数替换为宿主包名。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("asBinder".equals(method.getName())) {
            return method.invoke(mBase, args);
        }
        if (args != null && args.length > 0 && args[0] instanceof String) {
            String pkg = (String) args[0];
            args[0] = BlackBoxCore.getHostPkg();
        }
        try {
            return method.invoke(mBase, args);
        } catch (Throwable e) {
            throw e.getCause();
        }
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
