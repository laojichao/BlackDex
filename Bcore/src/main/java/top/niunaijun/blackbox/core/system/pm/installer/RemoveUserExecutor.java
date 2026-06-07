package top.niunaijun.blackbox.core.system.pm.installer;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;
import top.niunaijun.blackbox.core.system.pm.BPackageUserState;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 用户数据移除执行器。
 * <p>
 * 该执行器负责在包卸载过程中删除指定用户的数据目录，
 * 包括内部数据目录、设备加密数据目录和外部数据目录。
 * </p>
 *
 * @see Executor
 * @see BPackageInstallerService
 */
public class RemoveUserExecutor implements Executor {

    /**
     * 执行用户数据目录移除操作。
     *
     * @param ps     包设置信息
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 始终返回0（成功）
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        // delete user dir
        FileUtils.deleteDir(BEnvironment.getDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getDeDataDir(packageName, userId));
        FileUtils.deleteDir(BEnvironment.getExternalDataDir(packageName, userId));
        return 0;
    }
}
