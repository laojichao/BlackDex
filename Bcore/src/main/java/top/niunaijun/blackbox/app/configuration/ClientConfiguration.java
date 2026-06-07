package top.niunaijun.blackbox.app.configuration;

import java.io.File;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.FileUtils;

/**
 * 虚拟环境客户端配置抽象基类。
 * <p>
 * 定义虚拟化框架的可配置项，包括宿主包名、DEX Dump 目录、CodeItem 修复开关等。
 * 宿主应用需继承此类并实现 {@link #getHostPackageName()} 以提供必要的配置信息。
 *
 * @author Milk
 * @see BlackBoxCore
 */
public abstract class ClientConfiguration {
    /** 外部存储根目录 */
    private File mExternalDir;

    /**
     * 初始化配置，获取外部存储目录。
     * <p>由 {@link BlackBoxCore#doAttachBaseContext} 自动调用，不要手动调用。</p>
     */
    public final void init() {
        mExternalDir = BlackBoxCore.getContext().getExternalCacheDir().getParentFile();
    }

    /**
     * 获取宿主应用包名（必须实现）。
     *
     * @return 宿主应用包名
     */
    public abstract String getHostPackageName();

    /**
     * 获取 DEX Dump 输出目录路径。
     * <p>默认位于外部存储的 "dump" 子目录下。可重写此方法自定义输出路径。</p>
     *
     * @return Dump 目录的绝对路径
     */
    public String getDexDumpDir() {
        File dump = new File(mExternalDir, "dump");
        FileUtils.mkdirs(dump);
        return dump.getAbsolutePath();
    }

    /**
     * 是否启用 CodeItem 修复。
     * <p>修复 DEX 文件中的 CodeItem 偏移量以提高脱壳完整性，默认关闭。</p>
     *
     * @return 启用返回 {@code true}，默认 {@code false}
     */
    public boolean isFixCodeItem() {
        return false;
    }

    /**
     * 是否启用基于 Hook 的 DEX Dump 模式。
     * <p>Hook 模式通过拦截 DEX 加载函数实现更精确的 Dump，默认启用。</p>
     *
     * @return 启用返回 {@code true}，默认 {@code true}
     */
    public boolean isEnableHookDump() {
        return true;
    }
}
