package top.niunaijun.blackbox.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * IO 重定向核心类。
 * <p>
 * 负责文件路径重定向规则的管理和执行，将虚拟应用的文件访问路径
 * （如 /data/data/包名/）重定向到实际的虚拟数据目录。
 * 主要解决目标应用中硬编码路径导致的文件访问问题。
 *
 * @author Milk
 * @see VMCore
 */
@SuppressLint("SdCardPath")
public class IOCore {
    private static final IOCore sIOCore = new IOCore();
    private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

    private static final Map<String, Map<String, String>> sCachePackageRedirect = new HashMap<>();

    /** @return IOCore 单例实例 */
    public static IOCore get() {
        return sIOCore;
    }

    /**
     * 添加文件路径重定向规则。
     * <p>将原始路径映射到重定向路径，同时在 Native 层注册对应的 IO 规则。</p>
     *
     * @param origPath     原始路径（如 /data/data/包名/）
     * @param redirectPath 重定向目标路径
     */
    public void addRedirect(String origPath, String redirectPath) {
        if (TextUtils.isEmpty(origPath) || TextUtils.isEmpty(redirectPath) || mRedirectMap.get(origPath) != null)
            return;
        mRedirectMap.put(origPath, redirectPath);
        File redirectFile = new File(redirectPath);
        if (!redirectFile.exists()) {
            FileUtils.mkdirs(redirectPath);
        }
        VMCore.addIORule(origPath, redirectPath);
    }

    /**
     * 根据已注册规则重定向文件路径。
     *
     * @param path 原始路径
     * @return 重定向后的路径，无匹配规则时返回原路径
     */
    public String redirectPath(String path) {
        if (TextUtils.isEmpty(path))
            return path;
        for (String orig : mRedirectMap.keySet()) {
            if (path.startsWith(orig)) {
                path = path.replace(orig, Objects.requireNonNull(mRedirectMap.get(orig)));
                break;
            }
        }
        return path;
    }

    public File redirectPath(File path) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr));
    }

    public String redirectPath(String path, Map<String, String> rule) {
        if (TextUtils.isEmpty(path))
            return path;
        for (String orig : rule.keySet()) {
            if (path.startsWith(orig)) {
                path = path.replace(orig, Objects.requireNonNull(rule.get(orig)));
                break;
            }
        }
        return path;
    }

    public File redirectPath(File path, Map<String, String> rule) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr, rule));
    }

    /**
     * 为虚拟应用启用全面的文件路径重定向。
     * <p>
     * 注册以下路径的重定向规则：
     * <ul>
     *     <li>/data/data/包名/ -> 虚拟数据目录</li>
     *     <li>/data/user/0/包名/ -> 虚拟数据目录</li>
     *     <li>/sdcard/Android/data/包名/ -> 虚拟外部存储</li>
     * </ul>
     */
    public void enableRedirect(Context context) {
        Map<String, String> rule = new LinkedHashMap<>();
        String packageName = context.getPackageName();

        try {
            ApplicationInfo packageInfo = BlackBoxCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, BActivityThread.getUserId());
            rule.put("/data/data/" + packageName + "/lib", packageInfo.nativeLibraryDir);
            rule.put("/data/user/0/" + packageName + "/lib", packageInfo.nativeLibraryDir);

            rule.put("/data/data/" + packageName, packageInfo.dataDir);
            rule.put("/data/user/0/" + packageName, packageInfo.dataDir);

            if (BlackBoxCore.getContext().getExternalCacheDir() != null && context.getExternalCacheDir() != null) {
                File external = context.getExternalCacheDir().getParentFile();

                // sdcard
                rule.put("/sdcard/Android/data/" + packageName,
                        external.getAbsolutePath());
                rule.put("/sdcard/android/data/" + packageName, external.getAbsolutePath());

                rule.put("/storage/emulated/0/android/data/" + packageName,
                        external.getAbsolutePath());
                rule.put("/storage/emulated/0/Android/data/" + packageName,
                        external.getAbsolutePath());

                rule.put("/storage/emulated/0/Android/data/" + packageName + "/files",
                        new File(external.getAbsolutePath(), "files").getAbsolutePath());
                rule.put("/storage/emulated/0/Android/data/" + packageName + "/cache",
                        new File(external.getAbsolutePath(), "cache").getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : rule.keySet()) {
            get().addRedirect(key, rule.get(key));
        }
        VMCore.enableIO();
    }
}
