package android.content.pm;

import android.util.ArraySet;

/**
 * Android 隐藏 API {@code android.content.pm.PackageUserState} 的桩类。
 * <p>
 * 表示某个应用在特定用户下的安装和启用状态信息。
 * 包含是否已停止、是否已安装、是否被隐藏、启用状态、禁用组件列表等字段。
 */
public class PackageUserState {

    /** 应用是否已停止运行 */
    public boolean stopped;
    /** 应用是否从未被启动过 */
    public boolean notLaunched;
    /** 应用是否已安装 */
    public boolean installed;
    /** 应用是否被所有者/管理员限制（隐藏） */
    public boolean hidden; // Is the app restricted by owner / admin
    /** 应用的启用状态 */
    public int enabled;
    /** 是否阻止卸载 */
    public boolean blockUninstall;

    /** 最后修改启用状态的调用者包名 */
    public String lastDisableAppCaller;

    /** 已禁用的组件名称集合 */
    public ArraySet<String> disabledComponents;
    /** 已启用的组件名称集合 */
    public ArraySet<String> enabledComponents;

    /** 域名验证状态 */
    public int domainVerificationStatus;
    /** App Link 生成版本号 */
    public int appLinkGeneration;

    /**
     * 默认构造函数，创建默认状态的 PackageUserState。
     */
    public PackageUserState() {
        throw new RuntimeException("Stub!");
    }

    /**
     * 复制构造函数，从另一个 PackageUserState 对象复制状态。
     *
     * @param o 要复制的 PackageUserState 对象
     */
    public PackageUserState(final PackageUserState o) {
        throw new RuntimeException("Stub!");
    }

}
