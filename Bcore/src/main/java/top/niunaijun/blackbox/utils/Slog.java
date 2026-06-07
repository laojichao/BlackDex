/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.niunaijun.blackbox.utils;

import android.util.Log;

/**
 * 系统日志工具类。
 * <p>
 * 对 {@link android.util.Log} 的封装，提供各级别日志输出方法。
 * 来源于Android AOSP源码（android.util.Slog），用于在虚拟化框架中
 * 统一日志输出格式。
 * <p>
 * 支持的日志级别：VERBOSE、DEBUG、INFO、WARN、ERROR。
 */
public final class Slog {
    /** 系统日志缓冲区ID */
    public static final int LOG_ID_SYSTEM = 3;

    private Slog() {
    }

    /**
     * 输出VERBOSE级别日志。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @return 写入的字节数
     */
    public static int v(String tag, String msg) {
        return Log.println(Log.VERBOSE, tag, msg);
    }

    /**
     * 输出VERBOSE级别日志（带异常堆栈）。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @param tr  异常对象
     * @return 写入的字节数
     */
    public static int v(String tag, String msg, Throwable tr) {
        return Log.println(Log.VERBOSE, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    
    /**
     * 输出DEBUG级别日志。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @return 写入的字节数
     */
    public static int d(String tag, String msg) {
        return Log.println(Log.DEBUG, tag, msg);
    }

    
    /**
     * 输出DEBUG级别日志（带异常堆栈）。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @param tr  异常对象
     * @return 写入的字节数
     */
    public static int d(String tag, String msg, Throwable tr) {
        return Log.println(Log.DEBUG, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    
    /**
     * 输出INFO级别日志。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @return 写入的字节数
     */
    public static int i(String tag, String msg) {
        return Log.println(Log.INFO, tag, msg);
    }

    /**
     * 输出INFO级别日志（带异常堆栈）。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @param tr  异常对象
     * @return 写入的字节数
     */
    public static int i(String tag, String msg, Throwable tr) {
        return Log.println(Log.INFO, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    
    /**
     * 输出WARN级别日志。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @return 写入的字节数
     */
    public static int w(String tag, String msg) {
        return Log.println(Log.WARN, tag, msg);
    }

    
    /**
     * 输出WARN级别日志（带异常堆栈）。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @param tr  异常对象
     * @return 写入的字节数
     */
    public static int w(String tag, String msg, Throwable tr) {
        return Log.println(Log.WARN, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * 输出WARN级别日志（仅异常堆栈）。
     *
     * @param tag 日志标签
     * @param tr  异常对象
     * @return 写入的字节数
     */
    public static int w(String tag, Throwable tr) {
        return Log.println(Log.WARN, tag, Log.getStackTraceString(tr));
    }

    
    /**
     * 输出ERROR级别日志。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @return 写入的字节数
     */
    public static int e(String tag, String msg) {
        return Log.println(Log.ERROR, tag, msg);
    }

    
    /**
     * 输出ERROR级别日志（带异常堆栈）。
     *
     * @param tag 日志标签
     * @param msg 日志消息
     * @param tr  异常对象
     * @return 写入的字节数
     */
    public static int e(String tag, String msg, Throwable tr) {
        return Log.println(Log.ERROR, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * 以指定优先级输出日志。
     *
     * @param priority 日志优先级
     * @param tag      日志标签
     * @param msg      日志消息
     * @return 写入的字节数
     */
    public static int println(int priority, String tag, String msg) {
        return Log.println(priority, tag, msg);
    }
}

