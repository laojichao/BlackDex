package top.niunaijun.blackbox.fake.service;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import reflection.android.app.ActivityThread;
import reflection.android.app.servertransaction.ClientTransaction;
import reflection.android.app.servertransaction.LaunchActivityItem;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.proxy.record.ProxyActivityRecord;
import top.niunaijun.blackbox.utils.compat.BuildCompat;

/**
 * ActivityThread.mH 的 Handler.Callback 代理，拦截 Activity 启动消息。
 * <p>
 * 通过替换 ActivityThread 中 mH（主线程 Handler）的 mCallback 字段，
 * 在 Activity 启动前拦截 LAUNCH_ACTIVITY / EXECUTE_TRANSACTION 消息。
 * 检测到目标 Intent 中包含虚拟环境的 ProxyActivityRecord 时，
 * 如果当前应用尚未初始化，则触发 {@link BActivityThread#bindApplication} 完成虚拟环境绑定。
 * </p>
 * <p>
 * 兼容 Android P (9.0) 前后的消息差异：
 * <ul>
 *   <li>9.0+：消息类型为 EXECUTE_TRANSACTION，Intent 在 ClientTransaction 的 LaunchActivityItem 中</li>
 *   <li>9.0 以下：消息类型为 LAUNCH_ACTIVITY，Intent 在 ActivityClientRecord 中</li>
 * </ul>
 * </p>
 *
 * @author Milk
 * @see IInjectHook
 * @see BActivityThread
 * @see ProxyActivityRecord
 */
public class HCallbackProxy implements IInjectHook, Handler.Callback {
    public static final String TAG = "HCallbackStub";
    /** 其他框架注册的原始 Callback，优先转发 */
    private Handler.Callback mOtherCallback;
    /** 防止重入标志 */
    private AtomicBoolean mBeing = new AtomicBoolean(false);

    private Handler.Callback getHCallback() {
        return reflection.android.os.Handler.mCallback.get(getH());
    }

    private Handler getH() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return ActivityThread.mH.get(currentActivityThread);
    }

    /**
     * 注入 Hook：将 ActivityThread.mH 的 mCallback 替换为本实例。
     * 保存原始 Callback 引用以便转发非拦截消息。
     */
    @Override
    public void injectHook() {
        mOtherCallback = getHCallback();
        if (mOtherCallback != null && (mOtherCallback == this || mOtherCallback.getClass().getName().equals(this.getClass().getName()))) {
            mOtherCallback = null;
        }
        reflection.android.os.Handler.mCallback.set(getH(), this);
    }

    @Override
    public boolean isBadEnv() {
        Handler.Callback hCallback = getHCallback();
        return hCallback != null && hCallback != this;
    }

    /**
     * 处理 Handler 消息，拦截 Activity 启动相关的消息。
     * <p>
     * 使用 AtomicBoolean 防止重入，拦截到启动消息后将消息重新发送到队列头部，
     * 并在处理前完成虚拟环境的应用绑定。
     * </p>
     *
     * @param msg Handler 消息
     * @return true 表示消息已处理，false 转发给原始 Callback 或 Handler.handleMessage
     */
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (!mBeing.getAndSet(true)) {
            try {
                if (BuildCompat.isPie()) {
                    if (msg.what == ActivityThread.H.EXECUTE_TRANSACTION.get()) {
                        if (handleLaunchActivity(msg.obj)) {
                            getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                            return true;
                        }
                    }
                } else {
                    if (msg.what == ActivityThread.H.LAUNCH_ACTIVITY.get()) {
                        if (handleLaunchActivity(msg.obj)) {
                            getH().sendMessageAtFrontOfQueue(Message.obtain(msg));
                            return true;
                        }
                    }
                }
                if (mOtherCallback != null) {
                    return mOtherCallback.handleMessage(msg);
                }
                return false;
            } finally {
                mBeing.set(false);
            }
        }
        return false;
    }

    private Object getLaunchActivityItem(Object clientTransaction) {
        List<Object> mActivityCallbacks = ClientTransaction.mActivityCallbacks.get(clientTransaction);

        for (Object obj : mActivityCallbacks) {
            if (LaunchActivityItem.REF.getClazz().getName().equals(obj.getClass().getCanonicalName())) {
                return obj;
            }
        }
        return null;
    }

    private boolean handleLaunchActivity(Object client) {
        Object r;
        if (BuildCompat.isPie()) {
            // ClientTransaction
            r = getLaunchActivityItem(client);
        } else {
            // ActivityClientRecord
            r = client;
        }
        if (r == null)
            return false;

        Intent intent;
        if (BuildCompat.isPie()) {
            intent = LaunchActivityItem.mIntent.get(r);
        } else {
            intent = ActivityThread.ActivityClientRecord.intent.get(r);
        }

        if (intent == null)
            return false;

        ProxyActivityRecord stubRecord = ProxyActivityRecord.create(intent);
        ActivityInfo activityInfo = stubRecord.mActivityInfo;
        if (activityInfo != null) {
            // bind
            if (!BActivityThread.currentActivityThread().isInit()) {
                BActivityThread.currentActivityThread().bindApplication(activityInfo.packageName,
                        activityInfo.processName);
                return false;
            }
        }
        return false;
    }
}
