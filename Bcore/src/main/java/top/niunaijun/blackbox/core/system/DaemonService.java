package top.niunaijun.blackbox.core.system;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import top.niunaijun.blackbox.R;
import top.niunaijun.blackbox.utils.compat.BuildCompat;


/**
 * 前台守护服务，用于维持 BlackBox 框架进程的存活。
 * <p>
 * 在 Android 8.0 (Oreo) 及以上版本通过显示通知来保持前台服务状态，
 * 防止系统因省电策略杀死框架进程。使用 {@link #START_STICKY} 标志确保
 * 服务被系统终止后能自动重启。
 * </p>
 *
 * @author Milk
 */
public class DaemonService extends Service {
    public static final String TAG = "DaemonService";
    private static final int NOTIFY_ID = (int) (System.currentTimeMillis() / 1000);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initNotificationManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent innerIntent = new Intent(this, DaemonInnerService.class);
        startService(innerIntent);
        if (BuildCompat.isOreo()) {
            showNotification();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getPackageName() + ".blackbox")
                .setPriority(NotificationCompat.PRIORITY_MAX);
        startForeground(NOTIFY_ID, builder.build());
    }

    private void initNotificationManager() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ONE_ID = getPackageName() + ".blackbox";
        String CHANNEL_ONE_NAME = "blackbox";
        if (BuildCompat.isOreo()) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            nm.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * 内部辅助服务，用于辅助主守护服务启动后立即取消通知。
     * <p>
     * 该服务由 {@link DaemonService} 在 onStartCommand 中启动，
     * 启动后立即取消通知栏通知并停止自身。
     * </p>
     */
    public static class DaemonInnerService extends Service {
        @Override
        public void onCreate() {
            Log.i(TAG, "DaemonInnerService -> onCreate");
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i(TAG, "DaemonInnerService -> onStartCommand");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(NOTIFY_ID);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            Log.i(TAG, "DaemonInnerService -> onDestroy");
            super.onDestroy();
        }
    }
}
