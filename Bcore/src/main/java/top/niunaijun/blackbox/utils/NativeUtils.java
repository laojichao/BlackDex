package top.niunaijun.blackbox.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Native库（SO文件）提取工具类。
 * <p>
 * 从APK（ZIP格式）文件中提取与设备CPU架构匹配的native动态库（.so文件），
 * 并复制到指定目录。支持以下CPU架构：
 * <ul>
 *   <li>当前设备的CPU_ABI</li>
 *   <li>armeabi（作为回退方案）</li>
 * </ul>
 * <p>
 * 如果SO文件已存在且大小相同，则跳过复制以提高性能。
 *
 * @author Milk
 */
public class NativeUtils {
    /** 日志标签 */
    public static final String TAG = "VirtualM";

    /**
     * 从APK文件中提取native库到指定目录。
     * <p>
     * 优先查找与设备CPU_ABI匹配的SO库，如果未找到则尝试armeabi架构。
     *
     * @param apk          APK文件
     * @param nativeLibDir native库输出目录
     * @throws Exception 提取过程中的异常
     */
    public static void copyNativeLib(File apk, File nativeLibDir) throws Exception {
        long startTime = System.currentTimeMillis();
        if (!nativeLibDir.exists()) {
            nativeLibDir.mkdirs();
        }
        try (ZipFile zipfile = new ZipFile(apk.getAbsolutePath())) {
            if (findAndCopyNativeLib(zipfile, Build.CPU_ABI, nativeLibDir)) {
                return;
            }

            findAndCopyNativeLib(zipfile, "armeabi", nativeLibDir);
        } finally {
            //Log.d(TAG, "Done! +" + (System.currentTimeMillis() - startTime) + "ms");
        }
    }


    /**
     * 在ZIP文件中查找并复制指定CPU架构的SO文件。
     * <p>
     * 遍历ZIP条目，找到匹配"lib/{cpuArch}/"前缀的.so文件并复制到目标目录。
     * 如果APK中完全没有lib/目录，则视为无native库（返回true快速跳过）。
     *
     * @param zipfile      ZIP文件
     * @param cpuArch      目标CPU架构
     * @param nativeLibDir 输出目录
     * @return 找到并复制了SO文件返回true
     * @throws Exception 复制过程中的异常
     */
    private static boolean findAndCopyNativeLib(ZipFile zipfile, String cpuArch, File nativeLibDir) throws Exception {
        //Log.d(TAG, "Try to copy plugin's cup arch: " + cpuArch);
        boolean findLib = false;
        boolean findSo = false;
        byte buffer[] = null;
        String libPrefix = "lib/" + cpuArch + "/";
        ZipEntry entry;
        Enumeration e = zipfile.entries();

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            String entryName = entry.getName();
            if (!findLib && !entryName.startsWith("lib/")) {
                continue;
            }
            findLib = true;
            if (!entryName.endsWith(".so") || !entryName.startsWith(libPrefix)) {
                continue;
            }

            if (buffer == null) {
                findSo = true;
                //Log.d(TAG, "Found plugin's cup arch dir: " + cpuArch);
                buffer = new byte[8192];
            }

            String libName = entryName.substring(entryName.lastIndexOf('/') + 1);
            //Log.d(TAG, "verify so " + libName);
//            File abiDir = new File(nativeLibDir, cpuArch);
//            if (!abiDir.exists()) {
//                abiDir.mkdirs();
//            }

            File libFile = new File(nativeLibDir, libName);
            if (libFile.exists() && libFile.length() == entry.getSize()) {
                //Log.d(TAG, libName + " skip copy");
                continue;
            }
            FileOutputStream fos = new FileOutputStream(libFile);
            //Log.d(TAG, "copy so " + entry.getName() + " of " + cpuArch);
            copySo(buffer, zipfile.getInputStream(entry), fos);
        }

        if (!findLib) {
            //Log.d(TAG, "Fast skip all!");
            return true;
        }

        return findSo;
    }

    /**
     * 使用缓冲流将输入内容复制到输出流。
     *
     * @param buffer 缓冲区
     * @param input  输入流
     * @param output 输出流
     * @throws IOException 复制过程中的IO异常
     */
    private static void copySo(byte[] buffer, InputStream input, OutputStream output) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(input);
        BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
        int count;

        while ((count = bufferedInput.read(buffer)) > 0) {
            bufferedOutput.write(buffer, 0, count);
        }
        bufferedOutput.flush();
        bufferedOutput.close();
        output.close();
        bufferedInput.close();
        input.close();
    }
}
