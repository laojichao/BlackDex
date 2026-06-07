package top.niunaijun.blackbox.fake.hook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类扫描注解，指定 {@link ClassInvocationStub} 子类需要额外扫描的外部类。
 * <p>
 * 当 MethodHook 内部类定义在外部类（而非 ClassInvocationStub 子类自身）中时，
 * 使用此注解声明需要扫描的外部类，ClassInvocationStub 在注入时会扫描这些类的
 * 内部类并注册其 {@link ProxyMethod} / {@link ProxyMethods} 注解。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @ScanClass(ActivityManagerCommonProxy.class)
 * public class IActivityManagerProxy extends ClassInvocationStub {
 *     // 会扫描 ActivityManagerCommonProxy 的内部类中的 ProxyMethod 注解
 * }
 * }</pre>
 * </p>
 *
 * @author Milk
 * @see ClassInvocationStub#injectHook()
 * @see ProxyMethod
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanClass {
    /**
     * 需要扫描的外部类数组。
     *
     * @return 待扫描的类数组
     */
    Class<?>[] value() default {};
}