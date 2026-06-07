package top.niunaijun.blackbox.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多方法代理注解，将标记的 {@link MethodHook} 子类绑定到多个方法名。
 * <p>
 * 当同一个 Hook 逻辑需要拦截多个方法时使用，{@link ClassInvocationStub} 会为
 * value 数组中的每个方法名都注册同一个 MethodHook 实例。
 * </p>
 *
 * @author Milk
 * @see ProxyMethod
 * @see ClassInvocationStub#initAnnotation(Class)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProxyMethods {
    /**
     * 要拦截的目标方法名数组。
     *
     * @return 方法名数组
     */
    String[] value() default {};
}
