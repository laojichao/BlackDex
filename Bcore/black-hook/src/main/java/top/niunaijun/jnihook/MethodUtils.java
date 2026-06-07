package top.niunaijun.jnihook;

/**
 * JNI Hook 方法描述工具类。
 * <p>
 * 提供将 Java 反射的 {@link Method} 和 {@link Constructor} 转换为 JNI 方法签名描述符的功能，
 * 主要用于 ART 方法 Hook 时获取准确的方法描述符以定位 ARTMethod 结构体。
 * </p>
 *
 * @author Milk
 */
@Keep
public class MethodUtils {

    /**
     * 获取方法所属类的内部名称（JNI 格式，以 '/' 分隔）。
     * <p>供 Native 层调用，用于构建 JNI 方法描述符。</p>
     *
     * @param method 目标方法
     * @return 类的内部名称，如 "java/lang/String"
     */
    // native call
    public static String getDeclaringClass(final Method method) {
        return method.getDeclaringClass().getName().replace(".", "/");
    }

    /**
     * 获取方法的名称。
     * <p>供 Native 层调用，用于匹配 ARTMethod 结构体中的方法名。</p>
     *
     * @param method 目标方法
     * @return 方法名称
     */
    // native call
    public static String getMethodName(final Method method) {
        return method.getName();
    }

    /**
     * 获取方法的 JNI 描述符（参数类型 + 返回值类型）。
     * <p>格式示例：{@code (Ljava/lang/String;I)V}，供 Native 层用于精确匹配 ARTMethod。</p>
     *
     * @param method 目标方法
     * @return JNI 格式的方法描述符
     */
    // native call
    public static String getDesc(final Method method) {
        final StringBuffer buf = new StringBuffer();
        buf.append("(");
        final Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; ++i) {
            buf.append(getDesc(types[i]));
        }
        buf.append(")");
        buf.append(getDesc(method.getReturnType()));
        return buf.toString();
    }

    /**
     * 获取构造方法的 JNI 描述符（参数类型，返回值固定为 V）。
     * <p>构造方法返回值在 JNI 描述符中始终为 {@code V}。</p>
     *
     * @param method 目标构造方法
     * @return JNI 格式的构造方法描述符
     */
    // native call
    public static String getDesc(final Constructor<?> method) {
        final StringBuffer buf = new StringBuffer();
        buf.append("(");
        final Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; ++i) {
            buf.append(getDesc(types[i]));
        }
        buf.append(")V");
        return buf.toString();
    }

    /**
     * 获取单个类型的 JNI 描述符。
     * <p>
     * 基本类型返回单字母代码（如 int -> "I"），
     * 数组类型以 "[" 前缀递归拼接，
     * 对象类型以 "L" + 类路径 + ";" 格式返回。
     * </p>
     *
     * @param returnType 类型的 Class 对象
     * @return JNI 格式的类型描述符
     */
    private static String getDesc(final Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        }
        if (returnType.isArray()) {
            return "[" + getDesc(returnType.getComponentType());
        }
        return "L" + getType(returnType) + ";";
    }

    /**
     * 获取参数类型的 JNI 内部表示。
     * <p>处理数组类型和对象类型的路径转换（"." 替换为 "/"）。</p>
     *
     * @param parameterType 参数类型的 Class 对象
     * @return JNI 格式的类型内部表示
     */
    private static String getType(final Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + getDesc(parameterType.getComponentType());
        }
        if (!parameterType.isPrimitive()) {
            final String clsName = parameterType.getName();
            return clsName.replaceAll("\\.", "/");
        }
        return getPrimitiveLetter(parameterType);
    }

    /**
     * 获取基本类型的 JNI 单字母描述符。
     *
     * @param type 基本类型的 Class 对象
     * @return JNI 基本类型描述符（如 "I"、"V"、"Z" 等）
     * @throws IllegalStateException 如果传入的类型不是基本类型
     */
    private static String getPrimitiveLetter(final Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        }
        if (Void.TYPE.equals(type)) {
            return "V";
        }
        if (Boolean.TYPE.equals(type)) {
            return "Z";
        }
        if (Character.TYPE.equals(type)) {
            return "C";
        }
        if (Byte.TYPE.equals(type)) {
            return "B";
        }
        if (Short.TYPE.equals(type)) {
            return "S";
        }
        if (Float.TYPE.equals(type)) {
            return "F";
        }
        if (Long.TYPE.equals(type)) {
            return "J";
        }
        if (Double.TYPE.equals(type)) {
            return "D";
        }
        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }
}