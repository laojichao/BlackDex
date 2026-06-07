package top.niunaijun.blackbox.core.system.pm;

/**
 * 包事件监听器接口。
 * <p>
 * 用于监听虚拟环境中应用包的安装和卸载事件。
 * 通过{@link BPackageManagerService#addPackageMonitor}注册监听器。
 * </p>
 *
 * @see BPackageManagerService
 */
public interface PackageMonitor {
    /**
     * 包卸载时的回调。
     *
     * @param packageName 被卸载的包名
     * @param userId      目标用户ID
     */
    void onPackageUninstalled(String packageName, int userId);

    /**
     * 包安装时的回调。
     *
     * @param packageName 被安装的包名
     * @param userId      目标用户ID
     */
    void onPackageInstalled(String packageName, int userId);
}
