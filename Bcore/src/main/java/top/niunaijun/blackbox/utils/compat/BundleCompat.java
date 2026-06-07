package top.niunaijun.blackbox.utils.compat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;

import reflection.android.os.BaseBundle;
import reflection.android.os.BundleICS;

/**
 * Bundle和Intent的IBinder操作兼容性工具类。
 * <p>
 * 处理Android 4.3（API 18）前后Bundle中Binder操作API的变化：
 * <ul>
 *   <li>API 18+：使用 {@link Bundle#getBinder} 和 {@link Bundle#putBinder}</li>
 *   <li>API 18以下：通过反射调用隐藏的getIBinder/putIBinder方法</li>
 * </ul>
 * <p>
 * 还提供了清除Bundle内部ParcelledData缓存的方法，用于强制Bundle重新序列化。
 */
public class BundleCompat {
    /**
     * 从Bundle中获取IBinder对象。
     *
     * @param bundle Bundle对象
     * @param key    键名
     * @return IBinder对象
     */
    public static IBinder getBinder(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= 18) {
            return bundle.getBinder(key);
        } else {
            return reflection.android.os.Bundle.getIBinder.call(bundle, key);
        }
    }

    /**
     * 向Bundle中存入IBinder对象。
     *
     * @param bundle Bundle对象
     * @param key    键名
     * @param value  IBinder对象
     */
    public static void putBinder(Bundle bundle, String key, IBinder value) {
        if (Build.VERSION.SDK_INT >= 18) {
            bundle.putBinder(key, value);
        } else {
            reflection.android.os.Bundle.putIBinder.call(bundle, key, value);
        }
    }

    /**
     * 向Intent中存入IBinder对象（通过Bundle包装）。
     * <p>
     * 由于Intent不直接支持IBinder，通过创建内部Bundle来间接存储。
     *
     * @param intent Intent对象
     * @param key    键名
     * @param value  IBinder对象
     */
    public static void putBinder(Intent intent, String key, IBinder value) {
        Bundle bundle = new Bundle();
        putBinder(bundle, "binder", value);
        intent.putExtra(key, bundle);
    }

    /**
     * 从Intent中获取IBinder对象（从Bundle包装中提取）。
     *
     * @param intent Intent对象
     * @param key    键名
     * @return IBinder对象，未找到返回null
     */
    public static IBinder getBinder(Intent intent, String key) {
        Bundle bundle = intent.getBundleExtra(key);
        if (bundle != null) {
            return getBinder(bundle, "binder");
        }
        return null;
    }

    /**
     * 清除Bundle内部的ParcelledData缓存。
     * <p>
     * Bundle在首次序列化后会将结果缓存在mParcelledData字段中。
     * 当通过反射修改了Bundle的内容后，需要清除该缓存以确保
     * 下次序列化时使用最新的数据。
     * <p>
     * 兼容Android 4.0以下（BundleICS）和Android 4.0以上（BaseBundle）两种实现。
     *
     * @param bundle 要清除缓存的Bundle
     */
    public static void clearParcelledData(Bundle bundle) {
        Parcel obtain = Parcel.obtain();
        obtain.writeInt(0);
        obtain.setDataPosition(0);
        Parcel parcel;
        if (BaseBundle.REF.getClazz() != null) {
            parcel = BaseBundle.mParcelledData.get(bundle);
            if (parcel != null) {
                parcel.recycle();
            }
            BaseBundle.mParcelledData.set(bundle, obtain);
        } else if (BundleICS.REF.getClazz() != null) {
            parcel = BundleICS.mParcelledData.get(bundle);
            if (parcel != null) {
                parcel.recycle();
            }
            BundleICS.mParcelledData.set(bundle, obtain);
        }
    }
}
