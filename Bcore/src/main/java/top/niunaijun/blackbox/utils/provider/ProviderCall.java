package top.niunaijun.blackbox.utils.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;

import top.niunaijun.blackbox.utils.compat.ContentProviderCompat;
import top.niunaijun.blackbox.BlackBoxCore;

/**
 * ContentProvider调用封装工具类。
 * <p>
 * 提供简洁的ContentProvider跨进程调用API，支持直接调用和Builder模式构建调用参数。
 * <p>
 * 核心功能：
 * <ul>
 *   <li>{@link #callSafely} - 安全调用（异常静默处理）</li>
 *   <li>{@link #call} - 标准调用（抛出异常）</li>
 *   <li>{@link Builder} - 流式构建调用参数</li>
 * </ul>
 * <p>
 * 底层通过 {@link ContentProviderCompat} 实现，支持获取Provider客户端失败时的重试机制。
 *
 * @author Milk
 */
public class ProviderCall {
    /**
     * 安全调用ContentProvider方法（异常静默处理）。
     *
     * @param authority   ContentProvider的Authority
     * @param methodName  方法名
     * @param arg         参数
     * @param bundle      附加数据
     * @return 调用结果Bundle，失败返回null
     */
    public static Bundle callSafely(String authority, String methodName, String arg, Bundle bundle) {
        try {
            return call(authority, BlackBoxCore.get().getContext(), methodName, arg, bundle, 5);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 调用ContentProvider方法。
     *
     * @param authority   ContentProvider的Authority
     * @param context     上下文
     * @param method      方法名
     * @param arg         参数
     * @param bundle      附加数据
     * @param retryCount  重试次数
     * @return 调用结果Bundle
     * @throws IllegalAccessException 获取Provider客户端失败时抛出
     */
    public static Bundle call(String authority, Context context, String method, String arg, Bundle bundle, int retryCount) throws IllegalAccessException {
        Uri uri = Uri.parse("content://" + authority);
        return ContentProviderCompat.call(context, uri, method, arg, bundle, retryCount);
    }

    /**
     * ProviderCall的Builder模式构建类。
     * <p>
     * 提供流式API来构建ContentProvider调用参数，支持设置方法名、参数、
     * 附加数据和重试次数。
     * <p>
     * 使用示例：
     * <pre>
     * Bundle result = new ProviderCall.Builder(context, authority)
     *     .methodName("query")
     *     .arg("arg1")
     *     .addArg("key1", "value1")
     *     .retry(3)
     *     .callSafely();
     * </pre>
     */
    public static final class Builder {

        private Context context;

        private Bundle bundle = new Bundle();

        private String method;
        private String auth;
        private String arg;
        private int retryCount = 5;

        /**
         * 构造Builder实例。
         *
         * @param context 上下文
         * @param auth    ContentProvider的Authority
         */
        public Builder(Context context, String auth) {
            this.context = context;
            this.auth = auth;
        }

        /**
         * 设置方法名。
         *
         * @param name 方法名
         * @return Builder实例（链式调用）
         */
        public Builder methodName(String name) {
            this.method = name;
            return this;
        }

        /**
         * 设置方法参数。
         *
         * @param arg 参数字符串
         * @return Builder实例（链式调用）
         */
        public Builder arg(String arg) {
            this.arg = arg;
            return this;
        }

        /**
         * 添加一个键值对到附加数据Bundle。
         * <p>
         * 支持的类型：Boolean、Integer、String、Serializable、Bundle、Parcelable、int[]。
         *
         * @param key   键名
         * @param value 值（支持多种类型，为null时忽略）
         * @return Builder实例（链式调用）
         * @throws IllegalArgumentException 不支持的值类型时抛出
         */
        public Builder addArg(String key, Object value) {
            if (value != null) {
                if (value instanceof Boolean) {
                    bundle.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    bundle.putInt(key, (Integer) value);
                } else if (value instanceof String) {
                    bundle.putString(key, (String) value);
                } else if (value instanceof Serializable) {
                    bundle.putSerializable(key, (Serializable) value);
                } else if (value instanceof Bundle) {
                    bundle.putBundle(key, (Bundle) value);
                } else if (value instanceof Parcelable) {
                    bundle.putParcelable(key, (Parcelable) value);
                } else if (value instanceof int[]) {
                    bundle.putIntArray(key, (int[]) value);
                } else {
                    throw new IllegalArgumentException("Unknown type " + value.getClass() + " in Bundle.");
                }
            }
            return this;
        }

        /**
         * 设置获取Provider客户端失败时的重试次数。
         *
         * @param retryCount 重试次数
         * @return Builder实例（链式调用）
         */
        public Builder retry(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        /**
         * 执行ContentProvider调用。
         *
         * @return 调用结果Bundle
         * @throws IllegalAccessException 获取Provider客户端失败时抛出
         */
        public Bundle call() throws IllegalAccessException {
            return ProviderCall.call(auth, context, method, arg, bundle, retryCount);
        }

        /**
         * 安全执行ContentProvider调用（异常静默处理）。
         *
         * @return 调用结果Bundle，失败返回null
         */
        public Bundle callSafely() {
            try {
                return call();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
