package top.niunaijun.blackbox.core.system.pm;

import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.ISystemService;
import top.niunaijun.blackbox.core.system.pm.installer.CopyExecutor;
import top.niunaijun.blackbox.core.system.pm.installer.CreatePackageExecutor;
import top.niunaijun.blackbox.core.system.pm.installer.CreateUserExecutor;
import top.niunaijun.blackbox.core.system.pm.installer.Executor;
import top.niunaijun.blackbox.core.system.pm.installer.RemoveAppExecutor;
import top.niunaijun.blackbox.core.system.pm.installer.RemoveUserExecutor;
import top.niunaijun.blackbox.utils.Slog;

/**
 * 虚拟环境中的包安装服务。
 * <p>
 * 该服务负责管理虚拟环境中应用的安装、卸载和更新操作。
 * 采用执行器链模式，将安装/卸载操作拆分为多个步骤依次执行：
 * <ul>
 *   <li>安装：创建用户环境 → 创建应用环境 → 拷贝文件</li>
 *   <li>卸载：移除应用目录 → 移除用户目录</li>
 *   <li>更新：创建应用环境 → 拷贝文件</li>
 * </ul>
 * </p>
 *
 * @see Executor
 * @see BPackageSettings
 */
public class BPackageInstallerService extends IBPackageInstallerService.Stub implements ISystemService {
    private static final BPackageInstallerService sService = new BPackageInstallerService();

    /**
     * 获取BPackageInstallerService单例。
     *
     * @return BPackageInstallerService实例
     */
    public static BPackageInstallerService get() {
        return sService;
    }

    public static final String TAG = "BPackageInstallerService";

    /**
     * 为指定用户安装包，依次执行创建用户环境、创建应用环境、拷贝文件三个步骤。
     *
     * @param ps     包设置信息
     * @param userId 目标用户ID
     * @return 安装结果，0表示成功，负值表示失败
     * @throws RemoteException IPC通信异常
     */
    @Override
    public int installPackageAsUser(BPackageSettings ps, int userId) throws RemoteException {
        List<Executor> executors = new ArrayList<>();
        // 创建用户环境相关操作
        executors.add(new CreateUserExecutor());
        // 创建应用环境相关操作
        executors.add(new CreatePackageExecutor());
        // 拷贝应用相关文件
        executors.add(new CopyExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "installPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    /**
     * 为指定用户卸载包，可选择是否移除应用目录。
     *
     * @param ps       包设置信息
     * @param removeApp 是否移除应用目录
     * @param userId    目标用户ID
     * @return 卸载结果，0表示成功，负值表示失败
     */
    @Override
    public int uninstallPackageAsUser(BPackageSettings ps, boolean removeApp, int userId) {
        List<Executor> executors = new ArrayList<>();
        if (removeApp) {
            // 移除App
            executors.add(new RemoveAppExecutor());
        }
        // 移除用户相关目录
        executors.add(new RemoveUserExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, userId);
            Slog.d(TAG, "uninstallPackageAsUser: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    /**
     * 更新已安装的包，重新创建应用环境并拷贝文件。
     *
     * @param ps 包设置信息
     * @return 更新结果，0表示成功，负值表示失败
     */
    @Override
    public int updatePackage(BPackageSettings ps) {
        List<Executor> executors = new ArrayList<>();
        executors.add(new CreatePackageExecutor());
        executors.add(new CopyExecutor());
        InstallOption option = ps.installOption;
        for (Executor executor : executors) {
            int exec = executor.exec(ps, option, -1);
            Slog.d(TAG, "updatePackage: " + executor.getClass().getSimpleName() + " exec: " + exec);
            if (exec != 0) {
                return exec;
            }
        }
        return 0;
    }

    @Override
    public void systemReady() {

    }
}
