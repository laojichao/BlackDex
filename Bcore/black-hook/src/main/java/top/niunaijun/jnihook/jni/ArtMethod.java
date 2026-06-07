package top.niunaijun.jnihook.jni;

/**
 * ART 方法结构体偏移量计算类。
 * <p>
 * 通过 JNI 调用 Native 层来获取当前设备 ART 运行时中 {@code art::ArtMethod} 结构体的
 * 各字段偏移量（如 accessFlags、dexMethodIndex、entryPoint 等）。
 * 这些偏移量是进行 ART 方法 Hook 的基础数据，不同 Android 版本和 CPU 架构的偏移量各不相同，
 * 因此需要在运行时动态计算。
 * </p>
 *
 * @author Milk
 */
public class ArtMethod {

    /**
     * 计算并缓存 ART 方法结构体的第一组偏移量。
     * <p>Native 方法，用于初始化 Hook 所需的 ArtMethod 字段偏移数据。</p>
     */
    public static final native void nativeOffset();

    /**
     * 计算并缓存 ART 方法结构体的第二组偏移量。
     * <p>Native 方法，作为 {@link #nativeOffset()} 的补充，获取额外的偏移信息。</p>
     */
    public static final native void nativeOffset2();
}
