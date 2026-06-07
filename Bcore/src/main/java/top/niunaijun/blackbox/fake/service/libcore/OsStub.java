package top.niunaijun.blackbox.fake.service.libcore;

import java.lang.reflect.Method;

import reflection.libcore.io.Libcore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.core.IOCore;

/**
 * libcore.os.Os 对象代理，拦截系统底层文件操作实现路径重定向。
 * <p>
 * 通过替换 {@code Libcore.os} 静态字段中的 Os 对象，拦截所有以 "/" 开头的
 * 文件路径参数，使用 {@link IOCore#redirectPath(String)} 将真实路径重定向到
 * 虚拟环境的私有目录。
 * </p>
 * <p>
 * 这是虚拟环境文件隔离的核心机制之一，确保虚拟环境中应用的文件操作
 * 不会影响宿主系统的文件系统。
 * </p>
 *
 * @author Milk
 * @see ClassInvocationStub
 * @see IOCore
 */
public class OsStub extends ClassInvocationStub {
    public static final String TAG = "OsStub";
    private Object mBase;

    public OsStub() {
        mBase = Libcore.os.get();
    }

    @Override
    protected Object getWho() {
        return mBase;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        Libcore.os.set(proxyInvocation);
    }

    @Override
    protected void onBindMethod() {
    }

    /**
     * 检查环境是否异常（libcore.os 被替换为其他对象）。
     *
     * @return 如果 libcore.os 不是本代理实例则返回 true
     */
    @Override
    public boolean isBadEnv() {
        return Libcore.os.get() != getProxyInvocation();
    }

    /**
     * 方法调用拦截器，将参数中所有绝对路径重定向到虚拟环境目录。
     * <p>
     * 遍历方法参数数组，将以 "/" 开头的字符串参数通过 IOCore 进行路径重定向，
     * 然后委托给原始 Os 对象执行。
     * </p>
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null)
                    continue;
                if (args[i] instanceof String && ((String) args[i]).startsWith("/")) {
                    String orig = (String) args[i];
                    args[i] = IOCore.get().redirectPath(orig);
//                    if (!ObjectsCompat.equals(orig, args[i])) {
//                        Log.d(TAG, "redirectPath: " + orig + "  => " + args[i]);
//                    }
                }
            }
        }
        return super.invoke(proxy, method, args);
    }
}
