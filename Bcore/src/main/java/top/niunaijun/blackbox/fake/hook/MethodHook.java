package top.niunaijun.blackbox.fake.hook;

import java.lang.reflect.Method;

/**
 * 方法钩子抽象基类，定义方法拦截的三阶段处理模型。
 * <p>
 * 钩子执行顺序：
 * <ol>
 *   <li>{@link #beforeHook} - 前置处理，返回非 null 时短路（跳过 hook 和原始方法）</li>
 *   <li>{@link #hook} - 核心拦截逻辑（子类必须实现）</li>
 *   <li>{@link #afterHook} - 后置处理，可对 hook 的返回值进行转换</li>
 * </ol>
 * </p>
 * <p>
 * 通过 {@link ProxyMethod} 或 {@link ProxyMethods} 注解标记的内部类必须继承此类，
 * 并实现 {@link #hook} 方法定义具体拦截逻辑。
 * </p>
 *
 * @author Milk
 * @see ClassInvocationStub
 * @see ProxyMethod
 */
public abstract class MethodHook {
    /**
     * 获取此钩子绑定的方法名。默认返回 null，通常通过注解指定。
     *
     * @return 方法名，null 表示由注解配置
     */
    protected String getMethodName() {
        return null;
    }

    /**
     * 后置处理钩子，在 hook 方法执行后调用。
     *
     * @param result hook 方法的返回值
     * @return 最终返回给调用者的结果
     * @throws Throwable 处理异常
     */
    protected Object afterHook(Object result) throws Throwable {
        return result;
    }

    /**
     * 前置处理钩子，在 hook 方法执行前调用。
     * <p>
     * 返回非 null 时将直接作为方法返回值，跳过 hook 和原始方法调用（短路机制）。
     * </p>
     *
     * @param who    被代理的原始对象
     * @param method 被拦截的方法
     * @param args   方法参数（可修改）
     * @return 非 null 时短路返回，null 时继续执行 hook
     * @throws Throwable 处理异常
     */
    protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
        return null;
    }

    /**
     * 核心拦截方法（子类必须实现）。
     * <p>
     * 在此方法中实现具体的拦截逻辑，如参数替换、返回值伪造、调用转发等。
     * </p>
     *
     * @param who    被代理的原始对象
     * @param method 被拦截的方法
     * @param args   方法参数（可修改）
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    protected abstract Object hook(Object who, Method method, Object[] args) throws Throwable;
}
