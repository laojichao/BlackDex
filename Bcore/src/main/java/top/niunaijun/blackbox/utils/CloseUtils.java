package top.niunaijun.blackbox.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * IO流关闭工具类。
 * <p>
 * 提供安全关闭 {@link Closeable} 资源的静态方法，自动处理null检查和异常抑制。
 * 支持同时关闭多个资源，适用于try-finally场景中的资源清理。
 *
 * @author sunwanquan
 */
public class CloseUtils {
    /**
     * 安全关闭一个或多个Closeable资源。
     * <p>
     * 对每个非null的资源调用close()方法，IOException会被静默忽略。
     *
     * @param closeables 要关闭的资源数组，可传入null
     */
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
