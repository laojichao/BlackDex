package top.niunaijun.blackbox.core.system.pm.installer;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 应用目录移除执行器。
 * <p>
 * 该执行器负责在包卸载过程中删除应用的根目录，
 * 包括应用的所有文件和原生库。
 * </p>
 *
 * @see Executor
 * @see BPackageInstallerService
 */
public class RemoveAppExecutor implements Executor {
    /**
     * 执行应用目录移除操作。
     *
     * @param ps     包设置信息
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 始终返回0（成功）
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));
        return 0;
    }
}
