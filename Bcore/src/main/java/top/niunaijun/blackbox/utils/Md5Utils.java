package top.niunaijun.blackbox.utils;

/**
 * MD5摘要计算工具类。
 * <p>
 * 支持对字符串、文件和输入流计算MD5哈希值，
 * 返回32位小写十六进制字符串。
 */
public class Md5Utils {

    /** 十六进制字符查找表 */
    private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };


    /**
     * 计算字符串的MD5摘要。
     *
     * @param input 输入字符串
     * @return 32位小写十六进制MD5字符串，输入为null时返回null
     */
    public static String md5(String input) {
        if (input == null)
            return null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] inputByteArray = input.getBytes("utf-8");
            messageDigest.update(inputByteArray);
            byte[] resultByteArray = messageDigest.digest();
            return byteArrayToHex(resultByteArray);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算文件的MD5摘要。
     *
     * @param file 文件对象
     * @return 32位小写十六进制MD5字符串，文件不存在或非文件时返回null
     */
    public static String md5(File file) {
        try {
            if (!file.isFile()) {

                return null;
            }

            FileInputStream in = new FileInputStream(file);

            String result = md5(in);

            in.close();

            return result;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 计算输入流的MD5摘要。
     * <p>
     * 读取完成后会自动关闭输入流。
     *
     * @param in 输入流
     * @return 32位小写十六进制MD5字符串，异常时返回null
     */
    public static String md5(InputStream in) {

        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                messagedigest.update(buffer, 0, read);
            }

            in.close();

            String result = byteArrayToHex(messagedigest.digest());

            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param byteArray 字节数组
     * @return 十六进制字符串
     */
    private static String byteArrayToHex(byte[] byteArray) {

        char[] resultCharArray = new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        return new String(resultCharArray);

    }

}
