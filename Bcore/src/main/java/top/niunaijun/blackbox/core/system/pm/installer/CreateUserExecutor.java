package top.niunaijun.blackbox.core.system.pm.installer;

import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 用户环境创建执行器。
 * <p>
 * 该执行器负责在包安装过程中创建指定用户的目录结构，
 * 包括数据目录、缓存目录、文件目录、数据库目录和设备加密数据目录。
 * 在创建前会先清理已有的旧数据库目录。
 * </p>
 *
 * @see Executor
 * @see BPackageInstallerService
 */
public class CreateUserExecutor implements Executor {

    /**
     * 执行用户环境目录创建操作。
     *
     * @param ps     包设置信息
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 始终返回0（成功）
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        String packageName = ps.pkg.packageName;
        FileUtils.deleteDir(BEnvironment.getDataLibDir(packageName, userId));

        // create user dir
        FileUtils.mkdirs(BEnvironment.getDataDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataCacheDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataFilesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDataDatabasesDir(packageName, userId));
        FileUtils.mkdirs(BEnvironment.getDeDataDir(packageName, userId));

//        try {
//            // /data/data/xx/lib -> /data/app/xx/lib
//            FileUtils.createSymlink(BEnvironment.getAppLibDir(ps.pkg.packageName).getAbsolutePath(), BEnvironment.getDataLibDir(packageName, userId).getAbsolutePath());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return -1;
//        }
        return 0;
    }
}
