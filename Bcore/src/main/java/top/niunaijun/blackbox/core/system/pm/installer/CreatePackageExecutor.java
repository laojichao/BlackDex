package top.niunaijun.blackbox.core.system.pm.installer;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 应用目录创建执行器。
 * <p>
 * 该执行器负责在包安装过程中创建应用所需的目录结构，
 * 包括应用根目录和原生库目录。在创建前会先清理已有的旧目录。
 * </p>
 *
 * @see Executor
 * @see BPackageInstallerService
 */
public class CreatePackageExecutor implements Executor {

    /**
     * 执行应用目录创建操作。
     *
     * @param ps     包设置信息
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 始终返回0（成功）
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        FileUtils.deleteDir(BEnvironment.getAppDir(ps.pkg.packageName));

        // create app dir
        FileUtils.mkdirs(BEnvironment.getAppDir(ps.pkg.packageName));
        FileUtils.mkdirs(BEnvironment.getAppLibDir(ps.pkg.packageName));
        return 0;
    }
}
