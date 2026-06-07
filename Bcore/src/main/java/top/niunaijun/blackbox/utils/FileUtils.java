package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.system.Os;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件操作工具类。
 * <p>
 * 提供文件读写、复制、删除、权限设置、软链接操作、Parcel序列化等常用文件操作。
 * 包含文件锁（{@link FileLock}）和文件权限常量（{@link FileMode}）的内部定义。
 */
public class FileUtils {

    /**
     * 统计文件或目录中的条目数量。
     *
     * @param file 文件或目录
     * @return 文件返回1；目录返回子条目数量；不存在返回-1
     */
    public static int count(File file) {
        if (!file.exists()) {
            return -1;
        }
        if (file.isFile()) {
            return 1;
        }
        if (file.isDirectory()) {
            String[] fs = file.list();
            return fs == null ? 0 : fs.length;
        }
        return 0;
    }

    /**
     * 获取文件名的扩展名（不含点号）。
     *
     * @param filename 文件名
     * @return 扩展名字符串，无扩展名返回空字符串
     */
    public static String getFilenameExt(String filename) {
        int dotPos = filename.lastIndexOf('.');
        if (dotPos == -1) {
            return "";
        }
        return filename.substring(dotPos + 1);
    }

    /**
     * 更改文件的扩展名。
     * <p>
     * 如果文件已有目标扩展名则返回原文件，否则创建新路径的File对象。
     *
     * @param f        原文件
     * @param targetExt 目标扩展名（不含点号）
     * @return 扩展名更改后的File对象
     */
    public static File changeExt(File f, String targetExt) {
        String outPath = f.getAbsolutePath();
        if (!getFilenameExt(outPath).equals(targetExt)) {
            int dotPos = outPath.lastIndexOf(".");
            if (dotPos > 0) {
                outPath = outPath.substring(0, dotPos + 1) + targetExt;
            } else {
                outPath = outPath + "." + targetExt;
            }
            return new File(outPath);
        }
        return f;
    }

    /**
     * 重命名文件。
     *
     * @param origFile 原文件
     * @param newFile  新文件
     * @return 重命名成功返回true
     */
    public static boolean renameTo(File origFile, File newFile) {
        return origFile.renameTo(newFile);
    }

    /**
     * 读取文件内容为字符串。
     *
     * @param fileName 文件路径
     * @return 文件内容字符串
     * @throws IOException 读取失败时抛出
     */
    public static String readToString(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }

    /**
     * 读取文件内容到Parcel对象。
     *
     * @param file 文件
     * @return 反序列化后的Parcel对象
     * @throws IOException 读取失败时抛出
     */
    public static Parcel readToParcel(File file) throws IOException {
        Parcel in = Parcel.obtain();
        byte[] bytes = toByteArray(file);
        in.unmarshall(bytes, 0, bytes.length);
        in.setDataPosition(0);
        return in;
    }

    /**
     * 设置文件或目录的权限。
     * <p>
     * Android 5.0+优先使用系统API {@link android.system.Os#chmod}，
     * 低版本回退到执行chmod命令。
     *
     * @param path 文件或目录路径
     * @param mode 权限模式（八进制），参见 {@link FileMode}
     * @throws Exception 设置权限失败时抛出
     */
    public static void chmod(String path, int mode) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Os.chmod(path, mode);
                return;
            } catch (Exception e) {
                // ignore
            }
        }

        File file = new File(path);
        String cmd = "chmod ";
        if (file.isDirectory()) {
            cmd += " -R ";
        }
        String cmode = String.format("%o", mode);
        Runtime.getRuntime().exec(cmd + cmode + " " + path).waitFor();
    }

    /**
     * 创建硬链接。
     * <p>
     * Android 5.0+优先使用系统API {@link android.system.Os#link}，
     * 低版本回退到执行ln命令。
     *
     * @param oldPath 源文件路径
     * @param newPath 链接文件路径
     * @throws Exception 创建链接失败时抛出
     */
    public static void createSymlink(String oldPath, String newPath) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Os.link(oldPath, newPath);
                return;
            } catch (Throwable e) {
                //ignore
            }
        }
        Runtime.getRuntime().exec("ln -s " + oldPath + " " + newPath).waitFor();
    }

    /**
     * 判断文件是否为符号链接（软链接）。
     *
     * @param file 待检查的文件
     * @return 是符号链接返回true
     * @throws IOException 获取规范路径失败时抛出
     */
    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    /**
     * 将Parcel对象序列化后写入文件。
     *
     * @param p    Parcel对象
     * @param file 目标文件
     * @throws IOException 写入失败时抛出
     */
    public static void writeParcelToFile(Parcel p, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(p.marshall());
        fos.close();
    }

    /**
     * 将Parcel对象序列化后写入输出流。
     *
     * @param p   Parcel对象
     * @param fos 文件输出流
     * @throws IOException 写入失败时抛出
     */
    public static void writeParcelToOutput(Parcel p, FileOutputStream fos) throws IOException {
        fos.write(p.marshall());
    }

    /**
     * 读取文件内容为字节数组。
     *
     * @param file 文件
     * @return 文件内容字节数组
     * @throws IOException 读取失败时抛出
     */
    public static byte[] toByteArray(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            return toByteArray(fileInputStream);
        } finally {
            closeQuietly(fileInputStream);
        }
    }

    /**
     * 从输入流读取全部内容为字节数组。
     *
     * @param inStream 输入流
     * @return 内容字节数组
     * @throws IOException 读取失败时抛出
     */
    public static byte[] toByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        return swapStream.toByteArray();
    }

    /**
     * 递归删除目录及其所有内容。
     * <p>
     * 会检测并跳过符号链接目录，避免误删链接目标。
     *
     * @param dir 目录
     * @return 成功删除的文件/目录数量
     */
    public static int deleteDir(File dir) {
        int count = 0;
        if (dir.isDirectory()) {
            boolean link = false;
            try {
                link = isSymlink(dir);
            } catch (Exception e) {
                //ignore
            }
            if (!link) {
                String[] children = dir.list();
                for (String file : children) {
                    count += deleteDir(new File(dir, file));
                }
            }
        }
        if (dir.delete()) {
            count++;
        }
        return count;
    }

    /**
     * 递归删除指定路径的目录及其所有内容。
     *
     * @param dir 目录路径
     * @return 成功删除的文件/目录数量
     */
    public static int deleteDir(String dir) {
        return deleteDir(new File(dir));
    }

    /**
     * 将输入流内容写入文件（带缓冲）。
     *
     * @param dataIns 输入流
     * @param target  目标文件
     * @throws IOException 写入失败时抛出
     */
    public static void writeToFile(InputStream dataIns, File target) throws IOException {
        final int BUFFER = 1024;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = dataIns.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, count);
        }
        bos.close();
    }

    /**
     * 将字节数组写入文件（使用NIO通道传输）。
     *
     * @param data   字节数组
     * @param target 目标文件
     * @throws IOException 写入失败时抛出
     */
    public static void writeToFile(byte[] data, File target) throws IOException {
        FileOutputStream fo = null;
        ReadableByteChannel src = null;
        FileChannel out = null;
        try {
            src = Channels.newChannel(new ByteArrayInputStream(data));
            fo = new FileOutputStream(target);
            out = fo.getChannel();
            out.transferFrom(src, 0, data.length);
        } finally {
            if (fo != null) {
                fo.close();
            }
            if (src != null) {
                src.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 将输入流内容复制到目标文件（静默异常）。
     *
     * @param inputStream 输入流
     * @param target      目标文件
     */
    public static void copyFile(InputStream inputStream, File target) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(target);
            byte[] data = new byte[4096];
            int len;
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            outputStream.flush();
        } catch (Throwable e) {
            //ignore
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    /**
     * 使用NIO通道复制文件。
     *
     * @param source 源文件
     * @param target 目标文件
     * @throws IOException 复制失败时抛出
     */
    public static void copyFile(File source, File target) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(source);
            outputStream = new FileOutputStream(target);
            FileChannel iChannel = inputStream.getChannel();
            FileChannel oChannel = outputStream.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                int r = iChannel.read(buffer);
                if (r == -1)
                    break;
                buffer.limit(buffer.position());
                buffer.position(0);
                oChannel.write(buffer);
            }
        } finally {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
        }
    }

    /**
     * 安静关闭Closeable资源（忽略异常）。
     *
     * @param closeable 要关闭的资源，可以为null
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 从字节数组指定位置读取一个int值。
     * <p>
     * 支持大端序和小端序两种字节排列方式。
     *
     * @param bytes  字节数组
     * @param value  起始偏移量
     * @param endian 字节序（{@link ByteOrder#BIG_ENDIAN} 或 {@link ByteOrder#LITTLE_ENDIAN}）
     * @return 读取的int值
     */
    public static int peekInt(byte[] bytes, int value, ByteOrder endian) {
        int v2;
        int v0;
        if (endian == ByteOrder.BIG_ENDIAN) {
            v0 = value + 1;
            v2 = v0 + 1;
            v0 = (bytes[v0] & 255) << 16 | (bytes[value] & 255) << 24 | (bytes[v2] & 255) << 8 | bytes[v2 + 1] & 255;
        } else {
            v0 = value + 1;
            v2 = v0 + 1;
            v0 = (bytes[v0] & 255) << 8 | bytes[value] & 255 | (bytes[v2] & 255) << 16 | (bytes[v2 + 1] & 255) << 24;
        }

        return v0;
    }

    private static boolean isValidExtFilenameChar(char c) {
        switch (c) {
            case '\0':
            case '/':
                return false;
            default:
                return true;
        }
    }

    /**
     * Check if given filename is valid for an ext4 filesystem.
     */
    public static boolean isValidExtFilename(String name) {
        return (name != null) && name.equals(buildValidExtFilename(name));
    }

    /**
     * Mutate the given filename to make it valid for an ext4 filesystem,
     * replacing any invalid characters with "_".
     */
    public static String buildValidExtFilename(String name) {
        if (TextUtils.isEmpty(name) || ".".equals(name) || "..".equals(name)) {
            return "(invalid)";
        }
        final StringBuilder res = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            if (isValidExtFilenameChar(c)) {
                res.append(c);
            } else {
                res.append('_');
            }
        }
        return res.toString();
    }

    /**
     * 创建目录（如果不存在）。
     *
     * @param path 目录File对象
     */
    public static void mkdirs(File path) {
        if (!path.exists())
            path.mkdirs();
    }

    /**
     * 创建目录（如果不存在）。
     *
     * @param path 目录路径字符串
     */
    public static void mkdirs(String path) {
        mkdirs(new File(path));
    }

    /**
     * 判断文件或目录是否存在。
     *
     * @param path 文件路径
     * @return 存在返回true
     */
    public static boolean isExist(String path) {
        return new File(path).exists();
    }

    /**
     * 判断文件是否可读。
     *
     * @param path 文件路径
     * @return 可读返回true
     */
    public static boolean canRead(String path) {
        return new File(path).canRead();
    }

    /**
     * 文件权限常量接口。
     * <p>
     * 定义了POSIX文件权限的八进制常量，包括：
     * <ul>
     *   <li>特殊权限位：SUID、SGID、Sticky</li>
     *   <li>用户权限位：读、写、执行</li>
     *   <li>组权限位：读、写、执行</li>
     *   <li>其他权限位：读、写、执行</li>
     *   <li>常用组合：MODE_755 (rwxr-xr-x)</li>
     * </ul>
     */
    public interface FileMode {
        int MODE_ISUID = 04000;
        int MODE_ISGID = 02000;
        int MODE_ISVTX = 01000;
        int MODE_IRUSR = 00400;
        int MODE_IWUSR = 00200;
        int MODE_IXUSR = 00100;
        int MODE_IRGRP = 00040;
        int MODE_IWGRP = 00020;
        int MODE_IXGRP = 00010;
        int MODE_IROTH = 00004;
        int MODE_IWOTH = 00002;
        int MODE_IXOTH = 00001;

        int MODE_755 = MODE_IRUSR | MODE_IWUSR | MODE_IXUSR
                | MODE_IRGRP | MODE_IXGRP
                | MODE_IROTH | MODE_IXOTH;
    }

    /**
     * 文件锁管理类（单例）。
     * <p>
     * 基于 {@link java.nio.channels.FileLock} 实现的文件排他锁机制，
     * 支持引用计数，多个线程可以对同一文件重复加锁，只有所有锁释放后才真正解锁。
     * <p>
     * 主要用于保护odex等编译产物文件的并发访问。
     */
    public static class FileLock {
        private static FileLock singleton;
        private Map<String, FileLockCount> mRefCountMap = new ConcurrentHashMap<String, FileLockCount>();

        /**
         * 获取FileLock单例实例。
         *
         * @return FileLock单例
         */
        public static FileLock getInstance() {
            if (singleton == null) {
                singleton = new FileLock();
            }
            return singleton;
        }

        /**
         * 增加文件锁的引用计数。
         *
         * @param filePath          锁文件路径
         * @param fileLock          NIO文件锁
         * @param randomAccessFile  随机访问文件
         * @param fileChannel       文件通道
         * @return 增加前的引用计数
         */
        private int RefCntInc(String filePath, java.nio.channels.FileLock fileLock, RandomAccessFile randomAccessFile,
                              FileChannel fileChannel) {
            int refCount;
            if (this.mRefCountMap.containsKey(filePath)) {
                FileLockCount fileLockCount = this.mRefCountMap.get(filePath);
                int i = fileLockCount.mRefCount;
                fileLockCount.mRefCount = i + 1;
                refCount = i;
            } else {
                refCount = 1;
                this.mRefCountMap.put(filePath, new FileLockCount(fileLock, refCount, randomAccessFile, fileChannel));

            }
            return refCount;
        }

        /**
         * 减少文件锁的引用计数。当计数降为0时从映射中移除。
         *
         * @param filePath 锁文件路径
         * @return 减少后的引用计数
         */
        private int RefCntDec(String filePath) {
            int refCount = 0;
            if (this.mRefCountMap.containsKey(filePath)) {
                FileLockCount fileLockCount = this.mRefCountMap.get(filePath);
                int i = fileLockCount.mRefCount - 1;
                fileLockCount.mRefCount = i;
                refCount = i;
                if (refCount <= 0) {
                    this.mRefCountMap.remove(filePath);
                }
            }
            return refCount;
        }

        /**
         * 对目标文件所在目录加排他锁。
         * <p>
         * 在目标文件同目录下创建"lock"文件并加排他锁，
         * 使用引用计数支持同一线程的重复加锁。
         *
         * @param targetFile 目标文件
         * @return 加锁成功返回true
         */
        public boolean LockExclusive(File targetFile) {

            if (targetFile == null) {
                return false;
            }
            try {
                File lockFile = new File(targetFile.getParentFile().getAbsolutePath().concat("/lock"));
                if (!lockFile.exists()) {
                    lockFile.createNewFile();
                }
                RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile.getAbsolutePath(), "rw");
                FileChannel channel = randomAccessFile.getChannel();
                java.nio.channels.FileLock lock = channel.lock();
                if (!lock.isValid()) {
                    return false;
                }
                RefCntInc(lockFile.getAbsolutePath(), lock, randomAccessFile, channel);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 释放目标文件所在目录的排他锁。
         * <p>
         * 减少引用计数，当计数降为0时真正释放NIO文件锁并关闭相关资源。
         *
         * @param targetFile 目标文件
         */
        public void unLock(File targetFile) {

            File lockFile = new File(targetFile.getParentFile().getAbsolutePath().concat("/lock"));
            if (!lockFile.exists()) {
                return;
            }
            if (this.mRefCountMap.containsKey(lockFile.getAbsolutePath())) {
                FileLockCount fileLockCount = this.mRefCountMap.get(lockFile.getAbsolutePath());
                if (fileLockCount != null) {
                    java.nio.channels.FileLock fileLock = fileLockCount.mFileLock;
                    RandomAccessFile randomAccessFile = fileLockCount.fOs;
                    FileChannel fileChannel = fileLockCount.fChannel;
                    try {
                        if (RefCntDec(lockFile.getAbsolutePath()) <= 0) {
                            if (fileLock != null && fileLock.isValid()) {
                                fileLock.release();
                            }
                            if (randomAccessFile != null) {
                                randomAccessFile.close();
                            }
                            if (fileChannel != null) {
                                fileChannel.close();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * 文件锁引用计数内部类。
         * <p>
         * 记录NIO文件锁、关联的RandomAccessFile和FileChannel，
         * 以及当前引用计数。
         */
        private class FileLockCount {
            FileChannel fChannel;
            RandomAccessFile fOs;
            java.nio.channels.FileLock mFileLock;
            int mRefCount;

            FileLockCount(java.nio.channels.FileLock fileLock, int mRefCount, RandomAccessFile fOs,
                          FileChannel fChannel) {
                this.mFileLock = fileLock;
                this.mRefCount = mRefCount;
                this.fOs = fOs;
                this.fChannel = fChannel;
            }
        }
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
