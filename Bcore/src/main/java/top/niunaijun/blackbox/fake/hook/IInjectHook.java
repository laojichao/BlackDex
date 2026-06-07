package top.niunaijun.blackbox.fake.hook;

/**
 * Hook 注入接口，定义代理注入的标准契约。
 * <p>
 * 所有系统服务代理和 Instrumentation 代理均实现此接口，
 * 由 {@link HookManager} 统一管理其生命周期。
 * </p>
 *
 * @author Milk
 * @see HookManager
 */
public interface IInjectHook {
    /**
     * 执行 Hook 注入操作，将代理对象替换到目标位置。
     */
    void injectHook();

    /**
     * 检查当前运行环境是否异常（代理被外部篡改）。
     *
     * @return 如果环境异常（需要重新注入）返回 true
     */
    boolean isBadEnv();
}
