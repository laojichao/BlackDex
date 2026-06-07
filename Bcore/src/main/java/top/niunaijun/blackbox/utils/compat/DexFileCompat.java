package top.niunaijun.blackbox.utils.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dalvik.system.DexFile;
import top.niunaijun.blackbox.utils.Reflector;

/**
 * DexFile兼容性工具类。
 * <p>
 * 通过反射访问ClassLoader内部的DexFile对象，提供以下功能：
 * <ul>
 *   <li>获取ClassLoader加载的所有类名列表</li>
 *   <li>获取DexFile的mCookie（native指针）列表</li>
 * </ul>
 * <p>
 * 兼容Android 6.0（M）前后的DexFile内部结构变化：
 * <ul>
 *   <li>Android 6.0+：mCookie为long[]数组（支持multidex）</li>
 *   <li>Android 6.0以下：mCookie为单个long值</li>
 * </ul>
 * <p>
 * 通过ClassLoader -> pathList -> dexElements -> dexFile的反射链访问内部结构。
 *
 * @author Milk
 */
public class DexFileCompat {
    /** 日志标签 */
    public static final String TAG = "DexFileCompat";

    /**
     * 获取ClassLoader中所有DexFile加载的类名列表。
     *
     * @param classLoader 目标ClassLoader
     * @return 类名列表
     */
    public static List<String> getClassNameList(ClassLoader classLoader) {
        List<String> allClass = new ArrayList<>();
        try {
            List<DexFile> dexFiles = getDexFiles(classLoader);
            for (DexFile dexFile : dexFiles) {
                Object object = Reflector.with(dexFile)
                        .field("mCookie")
                        .get();
                String[] classNameList = getClassNameList(object);
                allClass.addAll(Arrays.asList(classNameList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allClass;
    }

    private static String[] getClassNameList(Object cookie) {
        try {
            String[] list;
            if (BuildCompat.isM()) {
                list = Reflector.on(DexFile.class)
                        .method("getClassNameList", Object.class)
                        .call(cookie);
            } else {
                list = Reflector.on(DexFile.class)
                        .method("getClassNameList", long.class)
                        .call(cookie);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取ClassLoader中所有DexFile的Cookie（native指针）列表。
     *
     * @param classLoader 目标ClassLoader
     * @return Cookie值列表
     */
    public static List<Long> getCookies(ClassLoader classLoader) {
        List<Long> cookies = new ArrayList<>();
        List<DexFile> dexFiles = getDexFiles(classLoader);
        for (DexFile dexFile : dexFiles) {
            cookies.addAll(getCookies(dexFile));
        }
        return cookies;
    }

    /**
     * 获取单个DexFile的Cookie（native指针）列表。
     * <p>
     * Android 6.0+返回long[]中的所有值，6.0以下返回单个long。
     *
     * @param dexFile DexFile对象
     * @return Cookie值列表
     */
    public static List<Long> getCookies(DexFile dexFile) {
        List<Long> cookies = new ArrayList<>();
        if (dexFile == null)
            return cookies;
        try {
            Object object = Reflector.with(dexFile)
                    .field("mCookie")
                    .get();
            if (BuildCompat.isM()) {
                for (long l : (long[]) object) {
                    cookies.add(l);
                }
            } else {
                cookies.add((long) object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cookies;
    }

    private static List<DexFile> getDexFiles(ClassLoader classLoader) {
        List<DexFile> dexFiles = new ArrayList<>();
        Object[] dexElements = getDexElements(classLoader);
        for (Object dexElement : dexElements) {
            try {
                dexFiles.add(Reflector.with(dexElement)
                        .field("dexFile")
                        .<DexFile>get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dexFiles;
    }

    private static Object[] getDexElements(ClassLoader classLoader) {
        Object dexPathList = getDexPathList(classLoader);
        if (dexPathList == null) {
            return new Object[]{};
        }
        try {
            return Reflector.with(dexPathList)
                    .field("dexElements")
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[]{};
    }

    private static Object getDexPathList(ClassLoader classLoader) {
        try {
            return Reflector.on("dalvik.system.BaseDexClassLoader")
                    .field("pathList")
                    .get(classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
