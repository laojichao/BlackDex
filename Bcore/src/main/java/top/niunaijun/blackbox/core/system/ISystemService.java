package top.niunaijun.blackbox.core.system;

/**
 * 系统服务接口，所有 BlackBox 框架的系统服务必须实现此接口。
 * <p>
 * 定义了系统就绪回调方法，各服务在 {@code systemReady()} 中完成
 * 自身的初始化逻辑（如加载配置、扫描已安装应用等）。
 * </p>
 *
 * @author Milk
 * @see BActivityManagerService
 * @see BPackageManagerService
 * @see BUserManagerService
 * @see BStorageManagerService
 */
public interface ISystemService {
    /**
     * 系统就绪回调，在 BlackBox 系统启动完成后由框架自动调用。
     */
    void systemReady();
}
