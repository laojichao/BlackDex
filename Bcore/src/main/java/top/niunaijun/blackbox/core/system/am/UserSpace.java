package top.niunaijun.blackbox.core.system.am;

/**
 * 虚拟环境中的用户空间。
 * <p>
 * 该类为每个用户维护独立的活跃服务列表（{@link ActiveServices}）和Activity栈（{@link ActivityStack}），
 * 实现不同用户之间虚拟环境的隔离。
 * </p>
 *
 * @see BActivityManagerService
 */
public class UserSpace {
    public final ActiveServices mActiveServices = new ActiveServices();
    public final ActivityStack mStack = new ActivityStack();
}
