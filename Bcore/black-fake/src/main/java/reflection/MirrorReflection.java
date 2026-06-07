package reflection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 反射工具类，提供链式 API 访问 Android 隐藏 API。
 * <p>
 * 该类封装了 Java 反射机制，支持通过类名或 {@link Class} 对象定位类，
 * 并提供对方法、字段和构造器的便捷访问。所有反射操作会自动设置 {@code setAccessible(true)}
 * 以绕过访问权限检查。
 * <p>
 * 主要特性：
 * <ul>
 *   <li>链式调用：{@code MirrorReflection.on("className").field("fieldName").get(instance)}</li>
 *   <li>支持静态方法/字段和实例方法/字段的访问</li>
 *   <li>支持通过字符串名称指定参数类型（含基本类型）</li>
 *   <li>自动沿继承链查找方法和字段</li>
 *   <li>异常静默处理，查找失败返回 null 而非抛出异常</li>
 * </ul>
 * <p>
 * 该类是 BlackDex 中所有反射包装器的基础组件。
 *
 * @author canyie
 */
@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
public final class MirrorReflection {

    private Class<?> clazz;

    private MirrorReflection(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * 获取当前包装的 Class 对象。
     *
     * @return 当前包装的类
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 通过全限定类名创建反射包装器，使用默认类加载器。
     *
     * @param name 类的全限定名
     * @return MirrorReflection 实例
     */
    public static MirrorReflection on(String name) {
        return new MirrorReflection(findClass(name));
    }

    /**
     * 通过全限定类名和指定类加载器创建反射包装器。
     *
     * @param name 类的全限定名
     * @param loader 用于加载类的类加载器
     * @return MirrorReflection 实例
     */
    public static MirrorReflection on(String name, ClassLoader loader) {
        return new MirrorReflection(findClass(name, loader));
    }

    /**
     * 通过 Class 对象创建反射包装器。
     *
     * @param clazz 要包装的类
     * @param <T> 类的类型
     * @return MirrorReflection 实例
     */
    public static <T> MirrorReflection on(Class<T> clazz) {
        return new MirrorReflection(clazz);
    }

    /**
     * 将 Method 对象包装为 MethodWrapper。
     *
     * @param method 要包装的方法
     * @param <T> 返回值类型
     * @return MethodWrapper 实例
     */
    public static <T> MethodWrapper<T> wrap(Method method) {
        return new MethodWrapper<T>(method);
    }

    /**
     * 将静态方法包装为 StaticMethodWrapper。
     *
     * @param method 要包装的静态方法
     * @param <T> 返回值类型
     * @return StaticMethodWrapper 实例
     */
    public static <T> StaticMethodWrapper<T> wrapStatic(Method method) {
        return new StaticMethodWrapper<T>(method);
    }

    /**
     * 查找并包装当前类中的实例方法。
     *
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @param <T> 返回值类型
     * @return MethodWrapper 实例
     */
    public <T> MethodWrapper<T> method(String name, Class<?>... parameterTypes) {
        return method(clazz, name, parameterTypes);
    }

    /**
     * 通过类名和方法名查找并包装实例方法。
     *
     * @param className 类的全限定名
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @param <T> 返回值类型
     * @return MethodWrapper 实例
     */
    public static <T> MethodWrapper<T> method(String className, String name, Class<?>... parameterTypes) {
        return method(findClass(className), name, parameterTypes);
    }

    /**
     * 通过 Class 对象和方法名查找并包装实例方法。
     *
     * @param clazz 目标类
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @param <T> 返回值类型
     * @return MethodWrapper 实例
     */
    public static <T> MethodWrapper<T> method(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = getMethod(clazz, name, parameterTypes);
        if ((parameterTypes == null || parameterTypes.length == 0) && method == null) {
            method = findMethodNoChecks(clazz, name);
        }
        return wrap(method);
    }


    /**
     * 查找并包装当前类中的静态方法。
     *
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @param <T> 返回值类型
     * @return StaticMethodWrapper 实例
     */
    public <T> StaticMethodWrapper<T> staticMethod(String name, Class<?>... parameterTypes) {
        return staticMethod(clazz, name, parameterTypes);
    }

    public static <T> StaticMethodWrapper<T> staticMethod(String className, String name, Class<?>... parameterTypes) {
        return staticMethod(findClass(className), name, parameterTypes);
    }

    public static <T> StaticMethodWrapper<T> staticMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method method = getMethod(clazz, name, parameterTypes);
        if ((parameterTypes == null || parameterTypes.length == 0) && method == null) {
            method = findMethodNoChecks(clazz, name);
        }
        return wrapStatic(method);
    }

    /**
     * 将 Field 对象包装为 FieldWrapper。
     *
     * @param field 要包装的字段
     * @param <T> 字段值类型
     * @return FieldWrapper 实例
     */
    public static <T> FieldWrapper<T> wrap(Field field) {
        return new FieldWrapper<>(field);
    }

    /**
     * 查找并包装当前类中的字段。
     *
     * @param name 字段名
     * @param <T> 字段值类型
     * @return FieldWrapper 实例
     */
    public <T> FieldWrapper<T> field(String name) {
        return field(clazz, name);
    }

    public static <T> FieldWrapper<T> field(String className, String name) {
        return field(findClass(className), name);
    }

    public static <T> FieldWrapper<T> field(Class<?> clazz, String name) {
        return wrap(getField(clazz, name));
    }

    /**
     * 将 Constructor 对象包装为 ConstructorWrapper。
     *
     * @param constructor 要包装的构造器
     * @param <T> 构造的类型
     * @return ConstructorWrapper 实例
     */
    public static <T> ConstructorWrapper<T> wrap(Constructor<T> constructor) {
        return new ConstructorWrapper<>(constructor);
    }

    /**
     * 查找并包装当前类中的构造器。
     *
     * @param parameterTypes 构造器参数类型
     * @param <T> 构造的类型
     * @return ConstructorWrapper 实例
     */
    public <T> ConstructorWrapper<T> constructor(Class<?>... parameterTypes) {
        return wrap(getConstructor(clazz, parameterTypes));
    }

    /**
     * 通过字符串形式的参数类型名查找并包装构造器，支持基本类型名称。
     *
     * @param parameterTypes 参数类型名称的字符串数组
     * @param <T> 构造的类型
     * @return ConstructorWrapper 实例
     */
    public <T> ConstructorWrapper<T> constructorStringClass(String... parameterTypes) {
        Class<?>[] classes = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            classes[i] = getClassFormString(parameterTypes[i]);
        }
        return constructor(classes);
    }


    /**
     * 通过全限定类名查找类，未找到时返回 null。
     *
     * @param name 类的全限定名
     * @return Class 对象，未找到返回 null
     */
    public static Class<?> findClassOrNull(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }


    /**
     * 通过全限定类名和类加载器查找类，未找到时返回 null。
     *
     * @param name 类的全限定名
     * @param loader 类加载器
     * @return Class 对象，未找到返回 null
     */
    public static Class<?> findClassOrNull(String name, ClassLoader loader) {
        try {
            return Class.forName(name, true, loader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }


    /**
     * 通过全限定类名查找类，未找到时打印异常堆栈并返回 null。
     *
     * @param name 类的全限定名
     * @return Class 对象，未找到返回 null
     */
    public static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 通过全限定类名和类加载器查找类，未找到时打印异常堆栈并返回 null。
     *
     * @param name 类的全限定名
     * @param loader 类加载器
     * @return Class 对象，未找到返回 null
     */
    public static Class<?> findClass(String name, ClassLoader loader) {
        try {
            return Class.forName(name, true, loader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在类中查找方法（带参数类型检查）。
     *
     * @param clazz 目标类
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @return 找到的 Method 对象，未找到返回 null
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        return findMethod(clazz, name, parameterTypes);
    }

    /**
     * 在类中通过方法名查找方法（不指定参数类型）。
     *
     * @param clazz 目标类
     * @param name 方法名
     * @return 找到的 Method 对象，未找到返回 null
     */
    public static Method getMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name);
    }

    private static String getParameterTypesMessage(Class<?>[] parameterTypes) {
        if (parameterTypes == null || parameterTypes.length == 0) {
            return "()";
        }
        StringBuilder sb = new StringBuilder("(");
        boolean isFirst = true;
        for (Class<?> type : parameterTypes) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(",");
            }
            sb.append(type.getName());
        }
        return sb.append(')').toString();
    }


    /**
     * 在类及其父类链中查找方法，自动设置可访问性。
     *
     * @param clazz 起始查找类
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @return 找到的 Method 对象，未找到返回 null
     */
    public static Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        checkForFindMethod(clazz, name, parameterTypes);
        return findMethodNoChecks(clazz, name, parameterTypes);
    }


    /**
     * 在类及其父类链中查找方法（不做参数检查），自动设置可访问性。
     *
     * @param clazz 起始查找类
     * @param name 方法名
     * @param parameterTypes 方法参数类型
     * @return 找到的 Method 对象，未找到返回 null
     */
    public static Method findMethodNoChecks(Class<?> clazz, String name, Class<?>... parameterTypes) {
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }


    /**
     * 在类中通过方法名匹配查找第一个方法（不检查参数类型）。
     *
     * @param clazz 目标类
     * @param name 方法名
     * @return 找到的 Method 对象，未找到返回 null
     */
    public static Method findMethodNoChecks(Class<?> clazz, String name) {
        try {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(name)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void checkForFindMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == null) {
                    throw new NullPointerException("parameterTypes[" + i + "] == null");
                }
            }
        }

    }


    /**
     * 在类中查找字段，自动设置可访问性。
     *
     * @param clazz 目标类
     * @param name 字段名
     * @return 找到的 Field 对象，未找到返回 null
     */
    public static Field getField(Class<?> clazz, String name) {
        return findField(clazz, name);
    }

    /**
     * 在类中查找字段，自动设置可访问性。
     *
     * @param clazz 目标类
     * @param name 字段名
     * @return 找到的 Field 对象，未找到返回 null
     */
    public static Field findField(Class<?> clazz, String name) {
        return findFieldNoChecks(clazz, name);
    }

    /**
     * 在类及其父类链中查找字段，自动设置可访问性。
     *
     * @param clazz 起始查找类
     * @param name 字段名
     * @return 找到的 Field 对象，未找到返回 null
     */
    public static Field findFieldNoChecks(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 在类中查找构造器。
     *
     * @param clazz 目标类
     * @param parameterTypes 构造器参数类型
     * @param <T> 构造的类型
     * @return 找到的 Constructor 对象，未找到返回 null
     */
    public static <T> Constructor<T> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        return findConstructor(clazz, parameterTypes);
    }

    /**
     * 在类中查找构造器（带参数类型检查）。
     *
     * @param clazz 目标类
     * @param parameterTypes 构造器参数类型
     * @param <T> 构造的类型
     * @return 找到的 Constructor 对象，未找到返回 null
     */
    public static <T> Constructor<T> findConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        checkForFindConstructor(clazz, parameterTypes);
        return findConstructorNoChecks(clazz, parameterTypes);
    }

    /**
     * 在类中查找构造器（不做参数检查），自动设置可访问性。
     *
     * @param clazz 目标类
     * @param parameterTypes 构造器参数类型
     * @param <T> 构造的类型
     * @return 找到的 Constructor 对象，未找到返回 null
     */
    public static <T> Constructor<T> findConstructorNoChecks(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException ignored) {
        }
        return null;
    }

    private static void checkForFindConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == null) {
                    throw new NullPointerException("parameterTypes[" + i + "] == null");
                }
            }
        }
    }

    /**
     * 判断指定实例是否为当前包装类的实例。
     *
     * @param instance 待检查的对象
     * @return 如果是当前类的实例返回 true
     */
    public boolean isInstance(Object instance) {
        return clazz.isInstance(instance);
    }

    /**
     * 获取当前类的修饰符。
     *
     * @return 修饰符整数值
     */
    public int getModifiers() {
        return clazz.getModifiers();
    }

    /**
     * 判断当前类是否为 Lambda 类。
     *
     * @return 如果类名中包含 "$$Lambda$" 返回 true
     */
    public boolean isLambdaClass() {
        return isLambdaClass(clazz);
    }


    /**
     * 判断指定类是否为 Lambda 类。
     *
     * @param clazz 待检查的类
     * @return 如果类名中包含 "$$Lambda$" 返回 true
     */
    public static boolean isLambdaClass(Class<?> clazz) {
        return clazz.getName().contains("$$Lambda$");
    }

    /**
     * 判断指定类是否为动态代理类。
     *
     * @param clazz 待检查的类
     * @return 如果是代理类返回 true
     */
    public static boolean isProxyClass(Class<?> clazz) {
        return Proxy.isProxyClass(clazz);
    }

    /**
     * 抛出未检查异常，绕过编译器的受检异常检查。
     *
     * @param e 要抛出的异常
     * @param <T> 异常类型
     * @throws T 始终抛出传入的异常
     */
    public static <T extends Throwable> void throwUnchecked(Throwable e) throws T {
        throw (T) e;
    }

    /**
     * 反射成员包装器基类，封装 {@link AccessibleObject} 和 {@link Member} 的公共操作。
     * 构造时自动设置 {@code setAccessible(true)}。
     *
     * @param <M> 成员类型（Method、Field 或 Constructor）
     */
    public static class MemberWrapper<M extends AccessibleObject & Member> {
        M member;

        MemberWrapper(M member) {
            if (member == null)
                return;
            member.setAccessible(true);
            this.member = member;
        }


        /**
         * 获取被包装的原始成员对象。
         *
         * @return 被包装的 Method/Field/Constructor 对象
         */
        public final M unwrap() {
            return member;
        }

        /**
         * 获取成员的修饰符。
         *
         * @return 修饰符整数值
         */
        public final int getModifiers() {
            return member.getModifiers();
        }

        /**
         * 获取声明该成员的类。
         *
         * @return 声明类的 Class 对象
         */
        public final Class<?> getDeclaringClass() {
            return member.getDeclaringClass();
        }
    }

    /**
     * 实例方法包装器，提供对实例方法的便捷调用。
     *
     * @param <T> 方法返回值类型
     */
    public static class MethodWrapper<T> extends MemberWrapper<Method> {
        MethodWrapper(Method method) {
            super(method);
        }

        /**
         * 调用实例方法，异常时静默返回 null。
         *
         * @param instance 目标对象实例
         * @param args 方法参数
         * @return 方法返回值，异常时返回 null
         */
        public T call(Object instance, Object... args) {
            try {
                return (T) member.invoke(instance, args);
            } catch (Throwable ignored) {
            }
            return null;
        }

        /**
         * 调用实例方法，异常时向上抛出。
         *
         * @param instance 目标对象实例
         * @param args 方法参数
         * @return 方法返回值
         * @throws Throwable 方法调用过程中可能抛出的异常
         */
        public T callWithException(Object instance, Object... args) throws Throwable {
            return (T) member.invoke(instance, args);
        }
    }

    /**
     * 静态方法包装器，提供对静态方法的便捷调用。
     *
     * @param <T> 方法返回值类型
     */
    public static class StaticMethodWrapper<T> extends MemberWrapper<Method> {
        StaticMethodWrapper(Method method) {
            super(method);
        }

        /**
         * 调用静态方法，异常时静默返回 null。
         *
         * @param args 方法参数
         * @return 方法返回值，异常时返回 null
         */
        public T call(Object... args) {
            return (T) member.invoke(null, args);
        }
    }

    /**
     * 字段包装器，提供对字段的便捷读写操作。
     *
     * @param <T> 字段值类型
     */
    public static class FieldWrapper<T> extends MemberWrapper<Field> {
        FieldWrapper(Field field) {
            super(field);
        }

        /**
         * 获取实例字段的值，异常时静默返回 null。
         *
         * @param instance 目标对象实例（静态字段传 null）
         * @return 字段值，异常时返回 null
         */
        public T get(Object instance) {
            try {
                return (T) member.get(instance);
            } catch (Throwable ignored) {
                return null;
            }
        }

        /**
         * 获取静态字段的值。
         *
         * @return 字段值，异常时返回 null
         */
        public T get() {
            return get(null);
        }

        /**
         * 设置实例字段的值，异常时静默忽略。
         *
         * @param instance 目标对象实例（静态字段传 null）
         * @param value 要设置的值
         */
        public void set(Object instance, Object value) {
            try {
                member.set(instance, value);
            } catch (Throwable ignored) {
            }
        }

        /**
         * 设置静态字段的值。
         *
         * @param value 要设置的值
         */
        public void set(Object value) {
            set(null, value);
        }

        /**
         * 获取字段的声明类型。
         *
         * @return 字段类型的 Class 对象
         */
        public Class<?> getType() {
            return member.getType();
        }
    }

    /**
     * 构造器包装器，提供对构造器的便捷调用。
     *
     * @param <T> 构造的类型
     */
    public static class ConstructorWrapper<T> extends MemberWrapper<Constructor<T>> {
        ConstructorWrapper(Constructor<T> constructor) {
            super(constructor);
        }

        /**
         * 使用指定参数创建对象实例，异常时静默返回 null。
         *
         * @param args 构造器参数
         * @return 创建的对象实例，异常时返回 null
         */
        public T newInstance(Object... args) {
            try {
                return member.newInstance(args);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    /**
     * 将字符串形式的类名转换为 Class 对象，支持基本类型名称。
     *
     * @param clazz 类名字符串
     * @return 对应的 Class 对象
     */
    static Class<?> getClassFormString(String clazz) {
        Class<?> type = getProtoType(clazz);
        if (type == null) {
            try {
                type = Class.forName(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return type;
    }

    /**
     * 将基本类型名称转换为对应的 Class 对象。
     *
     * @param typeName 基本类型名称（如 "int"、"boolean" 等）
     * @return 对应的基本类型 Class 对象，如果不是基本类型返回 null
     */
    static Class<?> getProtoType(String typeName) {
        if (typeName.equals("int")) {
            return Integer.TYPE;
        }
        if (typeName.equals("long")) {
            return Long.TYPE;
        }
        if (typeName.equals("boolean")) {
            return Boolean.TYPE;
        }
        if (typeName.equals("byte")) {
            return Byte.TYPE;
        }
        if (typeName.equals("short")) {
            return Short.TYPE;
        }
        if (typeName.equals("char")) {
            return Character.TYPE;
        }
        if (typeName.equals("float")) {
            return Float.TYPE;
        }
        if (typeName.equals("double")) {
            return Double.TYPE;
        }
        if (typeName.equals("void")) {
            return Void.TYPE;
        }
        return null;
    }
}