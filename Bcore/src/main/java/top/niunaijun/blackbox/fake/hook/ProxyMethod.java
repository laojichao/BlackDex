package top.niunaijun.blackbox.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法代理注解，将标记的 {@link MethodHook} 子类绑定到指定的方法名。
 * <p>
 * 标注在继承 {@link MethodHook} 的内部类上，{@link ClassInvocationStub} 在注入时
 * 会扫描这些注解并自动注册对应的 MethodHook 到方法拦截映射表中。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @ProxyMethod(name = "getPackageInfo")
 * public static class GetPackageInfo extends MethodHook {
 *     @Override
 *     protected Object hook(Object who, Method method, Object[] args) {
 *         // 拦截逻辑
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author Milk
 * @see ClassInvocationStub#initAnnotation(Class)
 * @see MethodHook
 * @see ProxyMethods
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyMethod {
    /**
     * 要拦截的目标方法名。
     *
     * @return 方法名字符串
     */
    String name();
}
