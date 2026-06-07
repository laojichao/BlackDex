package top.niunaijun.blackbox.core;

import android.util.Log;

import androidx.annotation.Keep;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import dalvik.system.DexFile;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.entity.dump.DumpResult;
import top.niunaijun.blackbox.utils.DexUtils;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.compat.DexFileCompat;
import top.niunaijun.jnihook.MethodUtils;

import static top.niunaijun.blackbox.core.env.BEnvironment.EMPTY_JAR;

/**
 * VM（虚拟机）核心 Native 接口类。
 * <p>
 * 作为 Java 层与 Native 层（libblackdex.so）的桥梁，提供以下核心功能：
 * <ul>
     * <li>IO 路径重定向的 Native 注册</li>
     * <li>基于 DexFile cookie 的 DEX Dump（{@link #cookieDumpDex}）</li>
     * <li>基于 Hook 的 DEX Dump（{@link #hookDumpDex}）</li>
     * <li>Xposed 框架隐藏</li>
     * <li>方法查找与路径重定向回调</li>
 * </ul>
 *
 * @author Milk
 * @see IOCore
 * @see DexUtils
 */
public class VMCore {
    /** 日志标签 */
    public static final String TAG = "VMCoreJava";

    static {
        new File("");
        System.loadLibrary("blackdex");
    }

    /**
     * 初始化 Native 层，设置当前 Android API Level。
     *
     * @param apiLevel Android SDK 版本号
     */
    public static native void init(int apiLevel);

    /** 启用 Native 层 IO 重定向拦截 */
    public static native void enableIO();

    /**
     * 在 Native 层添加 IO 重定向规则。
     *
     * @param targetPath   原始路径
     * @param relocatePath 重定向目标路径
     */
    public static native void addIORule(String targetPath, String relocatePath);

    /** 隐藏 Xposed 框架特征，防止目标应用检测 */
    public static native void hideXposed();

    /**
     * 通过 DexFile cookie 执行 DEX Dump（Native 方法）。
     *
     * @param cookie    DexFile 的 cookie 值
     * @param dir       输出目录路径
     * @param fixMethod 是否修复 CodeItem
     */
    private static native void cookieDumpDex(long cookie, String dir, boolean fixMethod);

    /**
     * 通过 Hook 方式执行 DEX Dump（Native 方法）。
     *
     * @param dir 输出目录路径
     */
    private static native void hookDumpDex(String dir);

    /**
     * 通过 DexFile cookie 对 ClassLoader 中的所有 DEX 文件执行 Dump。
     * <p>
     * 使用线程池并行 Dump 多个 DEX，Dump 完成后自动修复 DEX 文件头。
     * 通过 {@link BDumpManager} 实时通知 Dump 进度。
     * </p>
     *
     * @param classLoader 目标应用的 ClassLoader
     * @param packageName 目标应用包名
     */
    public static void cookieDumpDex(ClassLoader classLoader, String packageName) {
        List<Long> cookies = DexFileCompat.getCookies(classLoader);
        File file = new File(BlackBoxCore.get().getDexDumpDir(), packageName);

        DumpResult result = new DumpResult();
        result.dir = file.getAbsolutePath();
        result.packageName = packageName;
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors <= 0 ? 1 : availableProcessors);
        CountDownLatch countDownLatch = new CountDownLatch(cookies.size());
        AtomicInteger atomicInteger = new AtomicInteger(0);

        BlackBoxCore.getBDumpManager().noticeMonitor(result.dumpProcess(cookies.size(), atomicInteger.getAndIncrement()));
        for (int i = 0; i < cookies.size(); i++) {
            long cookie = cookies.get(i);
            if (cookie == 0) {
                countDownLatch.countDown();
                BlackBoxCore.getBDumpManager().noticeMonitor(result.dumpProcess(cookies.size(), atomicInteger.getAndIncrement()));
                continue;
            }
            FileUtils.mkdirs(file);
            if (atomicInteger.get() == 1) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
            executorService.execute(() -> {
                cookieDumpDex(cookie, file.getAbsolutePath(), BlackBoxCore.get().isFixCodeItem());
                BlackBoxCore.getBDumpManager().noticeMonitor(result.dumpProcess(cookies.size(), atomicInteger.getAndIncrement()));
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ignored) {
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File dex : files) {
                if (dex.isFile() && dex.getAbsolutePath().endsWith(".dex")) {
                    DexUtils.fixDex(dex);
                }
            }
        }
    }

    /**
     * 获取调用方 UID（供 Native 层回调）。
     *
     * @param origCallingUid 原始调用方 UID
     * @return UID 值（当前直接返回原值）
     */
    @Keep
    public static int getCallingUid(int origCallingUid) {
//        if (origCallingUid > 0 && origCallingUid < Process.FIRST_APPLICATION_UID)
//            return origCallingUid;
//        // 非用户应用
//        if (origCallingUid > Process.LAST_APPLICATION_UID)
//            return origCallingUid;
//
//        Log.d(TAG, "origCallingUid: " + origCallingUid + " => " + BClient.getBaseVUid());
//        return BClient.getBaseVUid();
        return origCallingUid;
    }

    /**
     * 文件路径重定向回调（供 Native 层调用）。
     *
     * @param path 原始路径
     * @return 重定向后的路径
     */
    @Keep
    public static String redirectPath(String path) {
        return IOCore.get().redirectPath(path);
    }

    @Keep
    public static File redirectPath(File path) {
        return IOCore.get().redirectPath(path);
    }

    /**
     * 加载空 DEX 文件并返回其 cookie 数组（供 Native 层回调）。
     *
     * @return DexFile cookie 数组
     */
    @Keep
    public static long[] loadEmptyDex() {
        try {
            DexFile dexFile = new DexFile(EMPTY_JAR);
            List<Long> cookies = DexFileCompat.getCookies(dexFile);
            long[] longs = new long[cookies.size()];
            for (int i = 0; i < cookies.size(); i++) {
                longs[i] = cookies.get(i);
            }
            return longs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new long[]{};
    }

    /**
     * 通过类名、方法名和签名查找方法或构造方法（供 Native 层回调）。
     *
     * @param className  类名（JNI 内部格式，如 "Lcom/example/Foo;" 或 "com/example/Foo"）
     * @param methodName 方法名，构造方法为 "&lt;init&gt;"
     * @param signature  JNI 方法签名（如 "(I)V"）
     * @return 找到的 Method 或 Constructor，未找到返回 {@code null}
     */
    @Keep
    public static Object findMethod(String className, String methodName, String signature) {
        try {
            className = className.replace("/", ".");
            if (className.startsWith("L")) {
                className = className.substring(1);
            }
            if (className.endsWith(";")) {
                className = className.substring(0, className.length() - 1);
            }
            ClassLoader classLoader = BActivityThread.getApplication().getClassLoader();
            Class<?> aClass = Class.forName(className, false, classLoader);
            if ("<init>".equals(methodName)) {
                Constructor<?>[] constructors = aClass.getDeclaredConstructors();
                for (Constructor<?> constructor : constructors) {
                    String desc = MethodUtils.getDesc(constructor);
                    if (signature.equals(desc)) {
                        return constructor;
                    }
                }
            }

            try {
                Method[] declaredMethods = aClass.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if (declaredMethod.getName().equals(methodName)) {
                        String desc = MethodUtils.getDesc(declaredMethod);
                        if (desc.equals(signature)) {
                            return declaredMethod;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }

            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    String desc = MethodUtils.getDesc(method);
                    if (desc.equals(signature)) {
                        return method;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
