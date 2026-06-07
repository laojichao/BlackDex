package top.niunaijun.blackbox.proxy;

import java.util.Locale;

import top.niunaijun.blackbox.BlackBoxCore;

/**
 * 代理组件清单管理工具类。
 * <p>
 * 负责生成和管理虚拟化框架中代理组件的标识信息，包括：
 * <ul>
 *   <li>代理ContentProvider的Authority</li>
 *   <li>代理Activity的完整类名</li>
 *   <li>代理FileProvider标识</li>
 *   <li>虚拟化进程名称</li>
 *   <li>系统调用Provider绑定标识</li>
 * </ul>
 * 所有代理组件标识均基于宿主包名动态生成，以确保命名唯一性。
 *
 * @author Milk
 */
public class ProxyManifest {
    /** 预分配的代理组件数量上限 */
    public static final int FREE_COUNT = 100;

    /**
     * 判断给定字符串是否为代理组件标识。
     * <p>
     * 匹配系统调用Provider名称或包含"proxy_content_provider_"的Authority。
     *
     * @param msg 待检查的字符串（通常是Authority或组件名）
     * @return 如果是代理组件标识则返回true
     */
    public static boolean isProxy(String msg) {
        return getBindProvider().equals(msg) || msg.contains("proxy_content_provider_");
    }

    /**
     * 获取系统调用Provider的完整组件名。
     * <p>
     * 格式为："{宿主包名}.blackbox.SystemCallProvider"
     *
     * @return 系统调用Provider的完整组件名
     */
    public static String getBindProvider() {
        return BlackBoxCore.getHostPkg() + ".blackbox.SystemCallProvider";
    }

    /**
     * 获取指定索引的代理ContentProvider的Authority。
     * <p>
     * 格式为："{宿主包名}.proxy_content_provider_{index}"
     *
     * @param index 代理组件索引，范围0~FREE_COUNT-1
     * @return 代理ContentProvider的Authority字符串
     */
    public static String getProxyAuthorities(int index) {
        return String.format(Locale.CHINA, "%s.proxy_content_provider_%d", BlackBoxCore.getHostPkg(), index);
    }

    /**
     * 获取指定索引的代理Activity的完整类名。
     * <p>
     * 格式为："top.niunaijun.blackbox.proxy.ProxyActivity$P{index}"
     *
     * @param index 代理Activity索引，范围0~FREE_COUNT-1
     * @return 代理Activity的完整类名
     */
    public static String getProxyActivity(int index) {
        return String.format(Locale.CHINA, "top.niunaijun.blackbox.proxy.ProxyActivity$P%d", index);
    }

    /**
     * 获取代理FileProvider的完整组件名。
     * <p>
     * 格式为："{宿主包名}.blackbox.FileProvider"
     *
     * @return 代理FileProvider的完整组件名
     */
    public static String getProxyFileProvider() {
        return BlackBoxCore.getHostPkg() + ".blackbox.FileProvider";
    }

    /**
     * 获取虚拟化进程的进程名。
     * <p>
     * 格式为："{宿主包名}:p{bPid}"
     *
     * @param bPid 黑盒虚拟进程ID
     * @return 虚拟化进程名
     */
    public static String getProcessName(int bPid) {
        return BlackBoxCore.getHostPkg() + ":p" + bPid;
    }
}
