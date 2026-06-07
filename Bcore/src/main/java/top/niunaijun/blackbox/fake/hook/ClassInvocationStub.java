package top.niunaijun.blackbox.fake.hook;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import top.niunaijun.blackbox.utils.MethodParameterUtils;

/**
 * 基于动态代理的方法拦截桩基类。
 * <p>
 * 实现 {@link InvocationHandler} 和 {@link IInjectHook}，通过 JDK 动态代理拦截目标对象的
 * 方法调用。方法拦截通过 {@link MethodHook} 注册表实现，支持注解驱动的自动注册：
 * </p>
 * <ul>
 *   <li>{@link ProxyMethod} - 将内部类标记为指定方法的 Hook 处理器</li>
 *   <li>{@link ProxyMethods} - 将内部类标记为多个方法的 Hook 处理器</li>
 *   <li>{@link ScanClass} - 扫描指定外部类的内部类作为 Hook 处理器</li>
 * </ul>
 * <p>
 * 注入流程：{@link #injectHook()} -> 获取原始对象 -> 创建动态代理 -> 调用
 * {@link #inject(Object, Object)} 完成替换 -> 注册所有注解标记的 MethodHook。
 * </p>
 *
 * @author Milk
 * @see MethodHook
 * @see ProxyMethod
 * @see BinderInvocationStub
 */
public abstract class ClassInvocationStub implements InvocationHandler, IInjectHook {
    public static final String TAG = ClassInvocationStub.class.getSimpleName();

    /** 方法名到 MethodHook 的映射表 */
    private Map<String, MethodHook> mMethodHookMap = new HashMap<>();
    /** 被代理的原始对象 */
    private Object mBase;
    /** 动态代理实例 */
    private Object mProxyInvocation;

    /**
     * 获取被代理的原始对象。
     *
     * @return 原始对象实例
     */
    protected abstract Object getWho();

    /**
     * 执行实际的代理注入操作（将 proxyInvocation 替换到目标位置）。
     *
     * @param baseInvocation  原始对象
     * @param proxyInvocation 动态代理对象
     */
    protected abstract void inject(Object baseInvocation, Object proxyInvocation);

    /**
     * 方法绑定回调，在注解扫描完成后调用，子类可覆写以手动注册 MethodHook。
     */
    protected void onBindMethod() {

    }

    protected Object getProxyInvocation() {
        return mProxyInvocation;
    }

    protected Object getBase() {
        return mBase;
    }

    /**
     * 执行 Hook 注入的完整流程：
     * <ol>
     *   <li>通过 {@link #getWho()} 获取原始对象</li>
     *   <li>创建 JDK 动态代理实例</li>
     *   <li>调用 {@link #inject(Object, Object)} 完成对象替换</li>
     *   <li>调用 {@link #onBindMethod()} 进行手动方法绑定</li>
     *   <li>扫描内部类和 {@link ScanClass} 指定的外部类，自动注册注解标记的 MethodHook</li>
     * </ol>
     */
    @Override
    public void injectHook() {
        mBase = getWho();
        mProxyInvocation = Proxy.newProxyInstance(mBase.getClass().getClassLoader(), MethodParameterUtils.getAllInterface(mBase.getClass()), this);
        inject(mBase, mProxyInvocation);

        onBindMethod();
        Class<?>[] declaredClasses = this.getClass().getDeclaredClasses();
        for (Class<?> declaredClass : declaredClasses) {
            initAnnotation(declaredClass);
        }
        ScanClass scanClass = this.getClass().getAnnotation(ScanClass.class);
        if (scanClass != null) {
            for (Class<?> aClass : scanClass.value()) {
                for (Class<?> declaredClass : aClass.getDeclaredClasses()) {
                    initAnnotation(declaredClass);
                }
            }
        }
    }

    /**
     * 解析类上的 {@link ProxyMethod} 或 {@link ProxyMethods} 注解并注册 MethodHook。
     *
     * @param clazz 待扫描注解的类
     */
    protected void initAnnotation(Class<?> clazz) {
        ProxyMethod proxyMethod = clazz.getAnnotation(ProxyMethod.class);
        if (proxyMethod != null) {
            final String name = proxyMethod.name();
            if (!TextUtils.isEmpty(name)) {
                try {
                    addMethodHook(name, (MethodHook) clazz.newInstance());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        ProxyMethods proxyMethods = clazz.getAnnotation(ProxyMethods.class);
        if (proxyMethods != null) {
            String[] value = proxyMethods.value();
            for (String name : value) {
                try {
                    addMethodHook(name, (MethodHook) clazz.newInstance());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册 MethodHook（使用 MethodHook 内部的 getMethodName 作为键）。
     *
     * @param methodHook 方法钩子实例
     */
    protected void addMethodHook(MethodHook methodHook) {
        mMethodHookMap.put(methodHook.getMethodName(), methodHook);
    }

    /**
     * 注册 MethodHook 到指定方法名。
     *
     * @param name       要拦截的方法名
     * @param methodHook 方法钩子实例
     */
    protected void addMethodHook(String name, MethodHook methodHook) {
        mMethodHookMap.put(name, methodHook);
    }

    /**
     * 动态代理的调用处理器。
     * <p>
     * 如果方法名在 MethodHook 注册表中找到匹配项，则按 beforeHook -> hook -> afterHook
     * 的顺序执行钩子链。beforeHook 返回非 null 时直接返回（短路）。
     * 未注册的方法直接委托给原始对象执行。
     * </p>
     *
     * @param proxy  代理实例
     * @param method 被调用的方法
     * @param args   方法参数
     * @return 方法执行结果
     * @throws Throwable 执行异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHook methodHook = mMethodHookMap.get(method.getName());
        if (methodHook == null) {
            try {
                return method.invoke(mBase, args);
            } catch (Throwable e) {
                throw e.getCause();
            }
        }

        Object result = methodHook.beforeHook(mBase, method, args);
        if (result != null) {
            return result;
        }
        result = methodHook.hook(mBase, method, args);
        result = methodHook.afterHook(result);
        return result;
    }
}
