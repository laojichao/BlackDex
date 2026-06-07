package top.niunaijun.blackbox.utils.compat;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;

/**
 * ContentProvider调用兼容性工具类。
 * <p>
 * 处理不同Android版本中ContentProvider客户端获取和调用方式的变化：
 * <ul>
 *   <li>API 16 (JellyBean)：引入 {@code acquireUnstableContentProviderClient}，避免Provider崩溃导致调用方崩溃</li>
 *   <li>API 17 (JellyBean MR1)：ContentProviderClient引入 {@code call} 方法</li>
 *   <li>API 24 (Nougat)：ContentProviderClient的 {@code release} 方法改为 {@code close}</li>
 * </ul>
 * <p>
 * 支持获取失败时的重试机制，每次重试间隔100ms。
 */
public class ContentProviderCompat {

    /**
     * 调用ContentProvider的方法（带重试机制）。
     *
     * @param context    上下文
     * @param uri        ContentProvider的URI
     * @param method     方法名
     * @param arg        方法参数
     * @param extras     附加数据
     * @param retryCount 获取Provider客户端失败时的重试次数
     * @return 方法调用结果Bundle
     * @throws IllegalAccessException 获取Provider客户端失败时抛出
     */
    public static Bundle call(Context context, Uri uri, String method, String arg, Bundle extras, int retryCount) throws IllegalAccessException {
        if (VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getContentResolver().call(uri, method, arg, extras);
        }
        ContentProviderClient client = acquireContentProviderClientRetry(context, uri, retryCount);
        try {
            if (client == null) {
                throw new IllegalAccessException();
            }
            return client.call(method, arg, extras);
        } catch (RemoteException e) {
            throw new IllegalAccessException(e.getMessage());
        } finally {
            releaseQuietly(client);
        }
    }


    private static ContentProviderClient acquireContentProviderClient(Context context, Uri uri) {
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return context.getContentResolver().acquireUnstableContentProviderClient(uri);
            }
            return context.getContentResolver().acquireContentProviderClient(uri);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取ContentProviderClient（通过URI），带重试机制。
     *
     * @param context    上下文
     * @param uri        ContentProvider的URI
     * @param retryCount 重试次数
     * @return ContentProviderClient实例，获取失败返回null
     */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, Uri uri, int retryCount) {
        ContentProviderClient client = acquireContentProviderClient(context, uri);
        if (client == null) {
            int retry = 0;
            while (retry < retryCount && client == null) {
                SystemClock.sleep(100);
                retry++;
                client = acquireContentProviderClient(context, uri);
            }
        }
        return client;
    }

    /**
     * 获取ContentProviderClient（通过Authority名称），带重试机制。
     *
     * @param context    上下文
     * @param name       ContentProvider的Authority名称
     * @param retryCount 重试次数
     * @return ContentProviderClient实例，获取失败返回null
     */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, String name, int retryCount) {
        ContentProviderClient client = acquireContentProviderClient(context, name);
        if (client == null) {
            int retry = 0;
            while (retry < retryCount && client == null) {
                SystemClock.sleep(100);
                retry++;
                client = acquireContentProviderClient(context, name);
            }
        }
        return client;
    }

    private static ContentProviderClient acquireContentProviderClient(Context context, String name) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return context.getContentResolver().acquireUnstableContentProviderClient(name);
        }
        return context.getContentResolver().acquireContentProviderClient(name);
    }

    private static void releaseQuietly(ContentProviderClient client) {
        if (client != null) {
            try {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    client.close();
                } else {
                    client.release();
                }
            } catch (Exception ignored) {
            }
        }
    }
}