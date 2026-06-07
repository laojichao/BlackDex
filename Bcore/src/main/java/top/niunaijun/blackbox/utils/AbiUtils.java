package top.niunaijun.blackbox.utils;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import top.niunaijun.blackbox.BlackBoxCore;

/**
 * APK ABI（Application Binary Interface）兼容性检测工具。
 * <p>
 * 通过解析APK文件中lib目录下的so库路径，判断APK支持的CPU架构：
 * <ul>
 *   <li>arm64-v8a (64位ARM)</li>
 *   <li>armeabi-v7a (32位ARM v7)</li>
 *   <li>armeabi (32位ARM v5/v6)</li>
 * </ul>
 * <p>
 * 检测结果会缓存在静态Map中，避免重复解析同一APK文件。
 *
 * @author Milk
 */
public class AbiUtils {
    /** APK中检测到的CPU架构集合 */
    private final Set<String> mLibs = new HashSet<>();
    /** 按APK文件路径缓存的ABI检测结果 */
    private static final Map<File, AbiUtils> sAbiUtilsMap = new HashMap<>();

    /**
     * 判断指定APK文件是否与当前设备的CPU架构兼容。
     * <p>
     * 如果APK中没有native库，则视为兼容（纯Java应用）。
     * 如果设备是64位，需要APK包含arm64-v8a库；
     * 如果设备是32位，需要APK包含armeabi或armeabi-v7a库。
     *
     * @param apkFile APK文件
     * @return 如果兼容返回true
     */
    public static boolean isSupport(File apkFile) {
        AbiUtils abiUtils = sAbiUtilsMap.get(apkFile);
        if (abiUtils == null) {
            abiUtils = new AbiUtils(apkFile);
            sAbiUtilsMap.put(apkFile, abiUtils);
        }
        if (abiUtils.isEmptyAib()) {
            return true;
        }

        if (BlackBoxCore.is64Bit()) {
            return abiUtils.is64Bit();
        } else {
            return abiUtils.is32Bit();
        }
    }

    /**
     * 构造函数，解析APK文件中的CPU架构信息。
     * <p>
     * 遍历APK（ZIP格式）中所有条目，识别lib/目录下以arm64-v8a、
     * armeabi、armeabi-v7a开头的路径，记录到架构集合中。
     *
     * @param apkFile APK文件
     */
    public AbiUtils(File apkFile) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith("lib/arm64-v8a")) {
                    mLibs.add("arm64-v8a");
                } else if (name.startsWith("lib/armeabi")) {
                    mLibs.add("armeabi");
                } else if (name.startsWith("lib/armeabi-v7a")) {
                    mLibs.add("armeabi-v7a");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(zipFile);
        }
    }

    /**
     * 判断APK是否包含64位ARM native库。
     *
     * @return 包含arm64-v8a库返回true
     */
    public boolean is64Bit() {
        return mLibs.contains("arm64-v8a");
    }

    /**
     * 判断APK是否包含32位ARM native库。
     *
     * @return 包含armeabi或armeabi-v7a库返回true
     */
    public boolean is32Bit() {
        return mLibs.contains("armeabi") || mLibs.contains("armeabi-v7a");
    }

    /**
     * 判断APK是否没有任何native库（纯Java应用）。
     *
     * @return 没有native库返回true
     */
    public boolean isEmptyAib() {
        return mLibs.isEmpty();
    }
}
