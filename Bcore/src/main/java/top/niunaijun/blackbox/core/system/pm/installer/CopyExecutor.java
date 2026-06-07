package top.niunaijun.blackbox.core.system.pm.installer;


import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.IOException;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.core.system.pm.BPackageSettings;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.NativeUtils;

/**
 * 文件拷贝执行器。
 * <p>
 * 该执行器负责在包安装过程中拷贝应用相关的文件，包括：
 * <ul>
 *   <li>从APK中提取并拷贝原生库文件（.so）</li>
 *   <li>拷贝BlackDex的原生库文件</li>
 *   <li>对于外部安装（FLAG_STORAGE），将APK拷贝到应用目录</li>
 * </ul>
 * </p>
 *
 * @see Executor
 * @see BPackageInstallerService
 */
public class CopyExecutor implements Executor {

    /**
     * 执行文件拷贝操作。
     *
     * @param ps     包设置信息
     * @param option 安装选项
     * @param userId 目标用户ID
     * @return 执行结果，0表示成功，-1表示失败
     */
    @Override
    public int exec(BPackageSettings ps, InstallOption option, int userId) {
        try {
            NativeUtils.copyNativeLib(new File(ps.pkg.baseCodePath), BEnvironment.getAppLibDir(ps.pkg.packageName));
            ApplicationInfo applicationInfo = BlackBoxCore.getContext().getApplicationInfo();
            FileUtils.copyFile(new File(applicationInfo.nativeLibraryDir, "libblackdex.so"),
                    new File(BEnvironment.getAppLibDir(ps.pkg.packageName), "libblackdex.so"));

            FileUtils.copyFile(new File(applicationInfo.nativeLibraryDir, "libblackdex_d.so"),
                    new File(BEnvironment.getAppLibDir(ps.pkg.packageName), "libblackdex_d.so"));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        if (option.isFlag(InstallOption.FLAG_STORAGE)) {
            // 外部安装
            File origFile = new File(ps.pkg.baseCodePath);
            File newFile = BEnvironment.getBaseApkDir(ps.pkg.packageName);
            try {
                if (option.isFlag(InstallOption.FLAG_URI_FILE)) {
                    boolean b = FileUtils.renameTo(origFile, newFile);
                    if (!b) {
                        FileUtils.copyFile(origFile, newFile);
                    }
                } else {
                    FileUtils.copyFile(origFile, newFile);
                }
                // update baseCodePath
                ps.pkg.baseCodePath = newFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        } else if (option.isFlag(InstallOption.FLAG_SYSTEM)) {
            // 系统安装
        }
        return 0;
    }
}
