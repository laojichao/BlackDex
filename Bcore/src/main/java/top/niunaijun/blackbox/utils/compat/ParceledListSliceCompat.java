package top.niunaijun.blackbox.utils.compat;

import java.lang.reflect.Method;
import java.util.List;

import reflection.android.content.pm.ParceledListSlice;
import reflection.android.content.pm.ParceledListSliceJBMR2;

/**
 * ParceledListSlice兼容性工具类。
 * <p>
 * 处理Android 4.3（JellyBean MR2）前后 {@code ParceledListSlice} 类的变化。
 * <p>
 * ParceledListSlice是Android系统中用于分批传输大型Parcelable列表的机制，
 * 常用于PackageManager等系统服务的跨进程调用。
 * <ul>
 *   <li>API 18+：构造函数直接接受List参数</li>
 *   <li>API 18以下：需通过append逐个添加元素，最后设置lastSlice标记</li>
 * </ul>
 */
public class ParceledListSliceCompat {

    /**
     * 判断方法的返回类型是否为ParceledListSlice。
     *
     * @param method 方法对象
     * @return 是ParceledListSlice类型返回true
     */
    public static boolean isReturnParceledListSlice(Method method) {
		return method != null && method.getReturnType() == ParceledListSlice.REF.getClazz();
	}

	/**
	 * 判断对象是否为ParceledListSlice类型。
	 *
	 * @param obj 待检查的对象
	 * @return 是ParceledListSlice类型返回true
	 */
	public static boolean isParceledListSlice(Object obj) {
		return obj != null && obj.getClass() == ParceledListSlice.REF.getClazz();
	}

	/**
	 * 创建ParceledListSlice实例。
	 * <p>
	 * API 18+直接使用List构造；API 18以下通过append逐个添加元素，
	 * 最后设置lastSlice标记表示列表传输完毕。
	 *
	 * @param list 要包装的列表
	 * @return ParceledListSlice实例
	 */
	public static Object create(List<?> list) {
		if (ParceledListSliceJBMR2.constructor != null) {
			return ParceledListSliceJBMR2.constructor.newInstance(list);
		} else {
			Object slice = ParceledListSlice.constructor.newInstance();
			for (Object item : list) {
				ParceledListSlice.append.call(slice, item);
			}
			ParceledListSlice.setLastSlice.call(slice, true);
			return slice;
		}
	}

}
