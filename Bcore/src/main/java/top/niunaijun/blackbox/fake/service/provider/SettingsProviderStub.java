package top.niunaijun.blackbox.fake.service.provider;

import android.os.IInterface;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;

/**
 * Settings Provider 代理桩，拦截 Settings 相关的 ContentProvider 调用。
 * <p>
 * 继承 {@link ClassInvocationStub} 并实现 {@link VContentProvider}，专门处理
 * "settings" authority 的 ContentProvider 调用。与 {@link ContentProviderStub} 类似，
 * 将第一个 String 类型参数替换为宿主应用包名。
 * </p>
 * <p>
 * Settings Provider 的特殊性在于其内部有 NameValueCache 缓存机制，
 * 需要配合 {@link top.niunaijun.blackbox.fake.delegate.ContentProviderDelegate#clearSettingProvider()}
 * 进行缓存清理。
 * </p>
 *
 * @author Milk
 * @see ContentProviderStub
 * @see VContentProvider
 * @see ContentProviderDelegate
 */
public class SettingsProviderStub extends ClassInvocationStub implements VContentProvider {
    private IInterface mBase;

    /**
     * 包装原始 Settings Provider 代理，创建虚拟环境代理并注入 Hook。
     *
     * @param contentProviderProxy 原始 ContentProvider 的 IInterface 代理
     * @param appPkg               应用包名（当前未使用）
     * @return 包装后的 IInterface 代理
     */
    @Override
    public IInterface wrapper(IInterface contentProviderProxy, String appPkg) {
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

    @Override
    public boolean isBadEnv() {
        return false;
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
        return method.invoke(mBase, args);
    }
}
