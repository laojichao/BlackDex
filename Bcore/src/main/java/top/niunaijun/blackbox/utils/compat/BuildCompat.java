package top.niunaijun.blackbox.utils.compat;

import android.os.Build;

/**
 * Android系统版本和ROM类型检测兼容性工具类。
 * <p>
 * 提供精确的Android版本检测方法，正确处理Developer Preview版本，
 * 并识别主流国产ROM系统（EMUI、MIUI、Flyme、ColorOS等）。
 * <p>
 * 版本检测方法考虑了 {@code PREVIEW_SDK_INT}，确保在开发预览版上也能正确判断。
 */
public class BuildCompat {

    /**
     * 获取预览版SDK版本号。
     *
     * @return 预览SDK版本号，非预览版或Android 6.0以下返回0
     */
    public static int getPreviewSDKInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Build.VERSION.PREVIEW_SDK_INT;
            } catch (Throwable e) {
                // ignore
            }
        }
        return 0;
    }

    /** 判断是否为Android 12 (S/API 31)及以上 */
    // 12
    public static boolean isS() {
        return Build.VERSION.SDK_INT >= 31 || (Build.VERSION.SDK_INT >= 30 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 11 (R/API 30)及以上 */
    // 11
    public static boolean isR() {
        return Build.VERSION.SDK_INT >= 30 || (Build.VERSION.SDK_INT >= 29 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 10 (Q/API 29)及以上 */
    // 10
    public static boolean isQ() {
        return Build.VERSION.SDK_INT >= 29 || (Build.VERSION.SDK_INT >= 28 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 9 (Pie/API 28)及以上 */
    // 9
    public static boolean isPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || (Build.VERSION.SDK_INT >= 27 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 8.0 (Oreo/API 26)及以上 */
    // 8
    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || (Build.VERSION.SDK_INT >= 25 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 7.0 (N/API 24)及以上 */
    // 7
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || (Build.VERSION.SDK_INT >= 23 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 7.1 (N_MR1/API 25)及以上 */
    // 7.1
    public static boolean isN_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 || (Build.VERSION.SDK_INT >= 24 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** 判断是否为Android 6.0 (M/API 23)及以上 */
    // 6
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /** 判断是否为Android 5.0 (Lollipop/API 21)及以上 */
    // 5
    public static boolean isL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /** 判断是否为Android 5.1 (Lollipop_MR1/API 22)及以上 */
    // 5
    public static boolean isL_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * 判断是否为三星设备。
     *
     * @return 三星设备返回true
     */
    public static boolean isSamsung() {
        return "samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    /**
     * 判断是否为华为EMUI系统。
     *
     * @return EMUI系统返回true
     */
    public static boolean isEMUI() {
        if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            return true;
        }
        String property = SystemPropertiesCompat.get("ro.build.version.emui");
        return property != null && property.contains("EmotionUI");
    }

    /**
     * 判断是否为小米MIUI系统。
     *
     * @return MIUI系统返回true
     */
    public static boolean isMIUI() {
        return SystemPropertiesCompat.getInt("ro.miui.ui.version.code", 0) > 0;
    }

    /**
     * 判断是否为魅族Flyme系统。
     *
     * @return Flyme系统返回true
     */
    public static boolean isFlyme() {
        return Build.DISPLAY.toLowerCase().contains("flyme");
    }

    /**
     * 判断是否为OPPO ColorOS系统。
     *
     * @return ColorOS系统返回true
     */
    public static boolean isColorOS() {
        return SystemPropertiesCompat.isExist("ro.build.version.opporom")
                || SystemPropertiesCompat.isExist("ro.rom.different.version");
    }

    /**
     * 判断是否为360 OS系统。
     *
     * @return 360 OS系统返回true
     */
    public static boolean is360UI() {
        String property = SystemPropertiesCompat.get("ro.build.uiversion");
        return property != null && property.toUpperCase().contains("360UI");
    }

    /**
     * 判断是否为乐视设备。
     *
     * @return 乐视设备返回true
     */
    public static boolean isLetv() {
        return Build.MANUFACTURER.equalsIgnoreCase("Letv");
    }

    /**
     * 判断是否为vivo设备。
     *
     * @return vivo设备返回true
     */
    public static boolean isVivo() {
        return SystemPropertiesCompat.isExist("ro.vivo.os.build.display.id");
    }


    /** ROM类型缓存 */
    private static ROMType sRomType;

    /**
     * 获取当前设备的ROM类型。
     * <p>
     * 结果会被缓存，首次调用时按以下优先级判断：
     * EMUI > MIUI > FLYME > ColorOS > 360 > Letv > Vivo > Samsung > OTHER
     *
     * @return ROM类型枚举
     */
    public static ROMType getROMType() {
        if (sRomType == null) {
            if (isEMUI()) {
                sRomType = ROMType.EMUI;
            } else if (isMIUI()) {
                sRomType = ROMType.MIUI;
            } else if (isFlyme()) {
                sRomType = ROMType.FLYME;
            } else if (isColorOS()) {
                sRomType = ROMType.COLOR_OS;
            } else if (is360UI()) {
                sRomType = ROMType._360;
            } else if (isLetv()) {
                sRomType = ROMType.LETV;
            } else if (isVivo()) {
                sRomType = ROMType.VIVO;
            } else if (isSamsung()) {
                sRomType = ROMType.SAMSUNG;
            } else {
                sRomType = ROMType.OTHER;
            }
        }
        return sRomType;
    }

    /** ROM类型枚举 */
    public enum ROMType {
        /** 华为EMUI */
        EMUI,
        /** 小米MIUI */
        MIUI,
        /** 魅族Flyme */
        FLYME,
        /** OPPO ColorOS */
        COLOR_OS,
        /** 乐视EUI */
        LETV,
        /** vivo FuntouchOS */
        VIVO,
        /** 360 OS */
        _360,
        /** 三星OneUI */
        SAMSUNG,
        /** 其他ROM */
        OTHER
    }
}