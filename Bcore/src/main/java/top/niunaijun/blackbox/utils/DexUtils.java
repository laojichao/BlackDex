package top.niunaijun.blackbox.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

/**
 * DEX文件修复工具类。
 * <p>
 * 用于修复DEX文件的头部信息，包括：
 * <ul>
 *   <li>文件大小头（file_size）：位于偏移32字节处</li>
 *   <li>SHA-1签名（signature）：位于偏移12字节处，长度20字节</li>
 *   <li>Adler32校验和（checksum）：位于偏移8字节处，长度4字节</li>
 * </ul>
 * <p>
 * 在虚拟化场景中，修改过的DEX文件需要重新计算这三个字段才能被系统正确加载。
 *
 * @author Milk
 */
public class DexUtils {

    /**
     * 修复DEX文件的头部信息。
     * <p>
     * 读取DEX文件内容，依次修复文件大小头、SHA-1签名和Adler32校验和，
     * 然后将修复后的内容写回文件。
     *
     * @param dex 待修复的DEX文件，为null时直接返回
     */
    public static void fixDex(File dex) {
        if (dex == null)
            return;
        FileInputStream in = null;
        FileOutputStream out = null;
        File dexN = new File(dex.getAbsolutePath() + "_fix.dex");
        try {
            in = new FileInputStream(dex);
            out = new FileOutputStream(dexN);
            byte[] bytes = new byte[in.available()];
            int read = in.read(bytes);
            if (read > 0) {
                fixFileSizeHeader(bytes);
                calcSignature(bytes);
                calcChecksum(bytes);
            }
            out.write(bytes);
            out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            CloseUtils.close(in, out);
            FileUtils.deleteDir(dex);
            FileUtils.renameTo(dexN, dex);
            FileUtils.deleteDir(dexN);
        }
    }

    /**
     * 计算并写入Adler32校验和。
     * <p>
     * 对DEX文件从偏移12字节开始到文件末尾的数据计算Adler32值，
     * 将结果以小端序写入偏移8~11字节处。
     *
     * @param var0 DEX文件字节数组
     */
    private static void calcChecksum(byte[] var0) {
        Adler32 var2 = new Adler32();
        var2.update(var0, 12, var0.length - 12);
        int var1 = (int) var2.getValue();
        var0[8] = (byte) var1;
        var0[9] = (byte) (var1 >> 8);
        var0[10] = (byte) (var1 >> 16);
        var0[11] = (byte) (var1 >> 24);
    }

    /**
     * 计算并写入SHA-1签名。
     * <p>
     * 对DEX文件从偏移32字节开始到文件末尾的数据计算SHA-1哈希，
     * 将20字节的哈希值写入偏移12~31字节处。
     *
     * @param bytes DEX文件字节数组
     */
    private static void calcSignature(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        md.update(bytes, 32, bytes.length - 32);

        try {
            int amt = md.digest(bytes, 12, 20);
            if (amt != 20) {
                throw new RuntimeException("unexpected digest write: " + amt +
                        " bytes");
            }
        } catch (DigestException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 修复DEX文件的大小头字段。
     * <p>
     * 将文件实际长度以大端序写入偏移32字节处（file_size字段）。
     *
     * @param dexBytes DEX文件字节数组
     */
    private static void fixFileSizeHeader(byte[] dexBytes) {
        byte[] newfs = intToByte(dexBytes.length);
        byte[] refs = new byte[4];
        for (int i = 0; i < 4; i++) {
            refs[i] = newfs[newfs.length - 1 - i];
        }
        System.arraycopy(refs, 0, dexBytes, 32, 4);
    }

    /**
     * 将int值转换为大端序的4字节数组。
     *
     * @param number 要转换的整数
     * @return 4字节大端序字节数组
     */
    private static byte[] intToByte(int number) {
        byte[] b = new byte[4];
        for (int i = 3; i >= 0; i--) {
            b[i] = (byte) (number % 256);
            number >>= 8;
        }
        return b;
    }
}
