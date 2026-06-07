package top.niunaijun.blackbox.fake.service.provider;

import android.os.IInterface;

/**
 * 虚拟 ContentProvider 包装接口，定义 ContentProvider 代理的创建契约。
 * <p>
 * 由 {@link ContentProviderStub} 和 {@link SettingsProviderStub} 实现，
 * 通过 {@link #wrapper(IInterface, String)} 方法将原始 ContentProvider 代理
 * 包装为虚拟环境代理。
 * </p>
 *
 * @author Milk
 * @see ContentProviderStub
 * @see SettingsProviderStub
 */
public interface VContentProvider {
    /**
     * 包装原始 ContentProvider 代理为虚拟环境代理。
     *
     * @param contentProviderProxy 原始 ContentProvider 的 IInterface 代理
     * @param appPkg               应用包名
     * @return 包装后的新 IInterface 代理
     */
    IInterface wrapper(final IInterface contentProviderProxy, final String appPkg);
}
