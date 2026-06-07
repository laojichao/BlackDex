package top.niunaijun.blackbox.utils.compat;

import android.text.TextUtils;

import top.niunaijun.blackbox.utils.Reflector;


/**
 * 系统属性（SystemProperties）兼容性工具类。
 * <p>
 * 通过反射访问Android隐藏的 {@code android.os.SystemProperties} 类，
 * 提供读取系统属性的能力。
 * <p>
 * SystemProperties是Android系统的核心配置机制，存储设备型号、ROM版本、
 * 功能开关等信息。此类主要用于ROM类型检测和系统特性判断。
 */
public class SystemPropertiesCompat {

    /**
     * 获取系统属性值（带默认值）。
     *
     * @param key 属性键
     * @param def 默认值
     * @return 属性值，获取失败返回默认值
     */
    public static String get(String key, String def) {
        try {
            return (String) Reflector.on("android.os.SystemProperties")
                    .method("get", String.class, String.class)
                    .call(key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * 获取系统属性值。
     *
     * @param key 属性键
     * @return 属性值，获取失败返回null
     */
    public static String get(String key) {
        try {
            return (String) Reflector.on("android.os.SystemProperties")
                    .method("get", String.class)
                    .call(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断系统属性是否存在（值不为空）。
     *
     * @param key 属性键
     * @return 属性存在且不为空返回true
     */
    public static boolean isExist(String key) {
        return !TextUtils.isEmpty(get(key));
    }

    /**
     * 获取系统属性的整数值。
     *
     * @param key 属性键
     * @param def 默认值
     * @return 属性的整数值，获取失败返回默认值
     */
    public static int getInt(String key, int def) {
        try {
            return (int) Reflector.on("android.os.SystemProperties")
                    .method("getInt", String.class, int.class)
                    .call(key, def);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

}
