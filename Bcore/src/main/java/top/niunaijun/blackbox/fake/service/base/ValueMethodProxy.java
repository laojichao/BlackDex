package top.niunaijun.blackbox.fake.service.base;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.MethodHook;

/**
 * 固定返回值的方法代理，用于拦截指定方法并返回预设的固定值。
 * <p>
 * 简化了需要返回固定值（如 0、null、true 等）的 MethodHook 实现，
 * 无需为每个方法单独创建子类。
 * </p>
 *
 * @author Milk
 * @see MethodHook
 */
public class ValueMethodProxy extends MethodHook {

    /** 预设的返回值 */
    Object mValue;
    /** 绑定的方法名 */
    String mName;

    /**
     * 构造固定返回值方法代理。
     *
     * @param name  要拦截的方法名
     * @param value 拦截后返回的固定值
     */
    public ValueMethodProxy(String name, Object value) {
		mValue = value;
		mName = name;
	}

    /**
     * 获取绑定的方法名。
     *
     * @return 方法名
     */
    public String getName() {
		return mName;
	}

	@Override
	protected Object hook(Object who, Method method, Object[] args) throws Throwable {
		return mValue;
	}
}
