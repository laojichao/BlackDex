package top.niunaijun.blackbox.core.system.pm.installer;

import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;

/**
 * 包安装执行器接口。
 * <p>
 * 定义了虚拟环境中包安装/卸载操作的执行步骤。
 * 通过实现该接口，可以定义不同的安装步骤（如创建目录、拷贝文件等），
 * 并由{@link top.niunaijun.blackbox.core.system.pm.BPackageInstallerService}按顺序执行。
 * </p>
 *
 * @see CopyExecutor
 * @see CreatePackageExecutor
 * @see CreateUserExecutor
 * @see RemoveAppExecutor
 * @see RemoveUserExecutor
 */
public interface Executor {
    public static final String TAG = "InstallExecutor";

    /**
     * 执行安装/卸载操作步骤。
     *
     * @param ps     包设置信息
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 执行结果，0表示成功，负值表示失败
     */
    int exec(BPackageSettings ps, InstallOption option, int userId);
}
