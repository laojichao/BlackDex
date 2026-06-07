package top.niunaijun.blackbox.utils;



import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Java反射操作链式工具类。
 * <p>
 * 提供流畅的链式API来访问和操作类的构造方法、字段和方法。
 * 支持静态和实例成员的访问，自动处理 {@code setAccessible(true)}。
 * <p>
 * 使用示例：
 * <pre>
 * // 调用私有方法
 * Reflector.on("com.example.MyClass")
 *     .method("privateMethod", String.class)
 *     .call("arg");
 *
 * // 读取私有字段
 * Object value = Reflector.with(instance)
 *     .field("mSecret")
 *     .get();
 * </pre>
 * <p>
 * 还提供 {@link QuietReflector} 静默子类，反射失败时不抛出异常，
 * 而是将异常保存在 {@code mIgnored} 字段中，适合处理可能不存在的API。
 *
 * @author qiaopu
 */
public class Reflector {
    /** 日志标签 */
    public static final String LOG_TAG ="Reflector";

    /** 目标类类型 */
    protected Class<?> mType;
    /** 绑定的调用者实例（用于非静态成员） */
    protected Object mCaller;
    /** 当前选中的构造方法 */
    protected Constructor mConstructor;
    /** 当前选中的字段 */
    protected Field mField;
    /** 当前选中的方法 */
    protected Method mMethod;


    /**
     * 通过全限定类名加载类并创建Reflector实例。
     *
     * @param name 全限定类名
     * @return Reflector实例
     * @throws Exception 类加载失败时抛出
     */
    public static Reflector on(String name) throws Exception {
        return on(name, true, Reflector.class.getClassLoader());
    }
    
    /**
     * 通过全限定类名加载类并创建Reflector实例。
     *
     * @param name       全限定类名
     * @param initialize 是否初始化类的静态块
     * @return Reflector实例
     * @throws Exception 类加载失败时抛出
     */
    public static Reflector on(String name, boolean initialize) throws Exception {
        return on(name, initialize, Reflector.class.getClassLoader());
    }
    
    /**
     * 通过全限定类名、指定ClassLoader加载类并创建Reflector实例。
     *
     * @param name       全限定类名
     * @param initialize 是否初始化类的静态块
     * @param loader     类加载器
     * @return Reflector实例
     * @throws Exception 类加载失败时抛出
     */
    public static Reflector on(String name, boolean initialize, ClassLoader loader) throws Exception {
        try {
            return on(Class.forName(name, initialize, loader));
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 通过Class对象创建Reflector实例。
     *
     * @param type 目标类的Class对象
     * @return Reflector实例
     */
    public static Reflector on(Class<?> type) {
        Reflector reflector = new Reflector();
        reflector.mType = type;
        return reflector;
    }
    
    /**
     * 通过已有实例创建Reflector并自动绑定该实例。
     *
     * @param caller 目标对象实例
     * @return 已绑定实例的Reflector
     * @throws Exception 绑定失败时抛出
     */
    public static Reflector with(Object caller) throws Exception {
        return on(caller.getClass()).bind(caller);
    }
    
    protected Reflector() {
    
    }
    
    /**
     * 选择指定参数类型的构造方法。
     *
     * @param parameterTypes 构造方法参数类型数组
     * @return 当前Reflector实例（链式调用）
     * @throws Exception 找不到匹配的构造方法时抛出
     */
    public Reflector constructor(Class<?>... parameterTypes) throws Exception {
        try {
            mConstructor = mType.getDeclaredConstructor(parameterTypes);
            mConstructor.setAccessible(true);
            mField = null;
            mMethod = null;
            return this;
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 使用选中的构造方法创建新实例。
     *
     * @param initargs 构造方法参数
     * @param <R>      返回值类型
     * @return 新创建的实例
     * @throws Exception 创建失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <R> R newInstance(Object... initargs) throws Exception {
        if (mConstructor == null) {
            throw new Exception("Constructor was null!");
        }
        try {
            return (R) mConstructor.newInstance(initargs);
        } catch (InvocationTargetException e) {
            throw new Exception("Oops!", e.getTargetException());
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 验证调用者实例是否为目标类型的实例。
     *
     * @param caller 调用者实例
     * @return 验证通过的调用者实例
     * @throws Exception 类型不匹配时抛出
     */
    protected Object checked(Object caller) throws Exception {
        if (caller == null || mType.isInstance(caller)) {
            return caller;
        }
        throw new Exception("Caller [" + caller + "] is not a instance of type [" + mType + "]!");
    }
    
    /**
     * 检查成员是否已选择，以及非静态成员是否有调用者。
     *
     * @param caller 调用者实例
     * @param member 目标成员（Field或Method）
     * @param name   成员名称（用于错误信息）
     * @throws Exception 成员为null或缺少调用者时抛出
     */
    protected void check(Object caller, Member member, String name) throws Exception {
        if (member == null) {
            throw new Exception(name + " was null!");
        }
        if (caller == null && !Modifier.isStatic(member.getModifiers())) {
            throw new Exception("Need a caller!");
        }
        checked(caller);
    }
    
    /**
     * 绑定调用者实例，用于后续的非静态成员访问。
     *
     * @param caller 调用者实例
     * @return 当前Reflector实例（链式调用）
     * @throws Exception 调用者不是目标类型实例时抛出
     */
    public Reflector bind(Object caller) throws Exception {
        mCaller = checked(caller);
        return this;
    }
    
    /**
     * 解绑调用者实例。
     *
     * @return 当前Reflector实例（链式调用）
     */
    public Reflector unbind() {
        mCaller = null;
        return this;
    }
    
    /**
     * 选择指定名称的字段。
     * <p>
     * 会自动搜索整个类继承链（包括父类的私有字段）。
     *
     * @param name 字段名
     * @return 当前Reflector实例（链式调用）
     * @throws Exception 找不到字段时抛出
     */
    public Reflector field(String name) throws Exception {
        try {
            mField = findField(name);
            mField.setAccessible(true);
            mConstructor = null;
            mMethod = null;
            return this;
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 在类继承链中查找指定名称的字段。
     *
     * @param name 字段名
     * @return 找到的Field对象
     * @throws NoSuchFieldException 整个继承链中都找不到时抛出
     */
    protected Field findField(String name) throws NoSuchFieldException {
        try {
            return mType.getField(name);
        } catch (NoSuchFieldException e) {
            for (Class<?> cls = mType; cls != null; cls = cls.getSuperclass()) {
                try {
                    return cls.getDeclaredField(name);
                } catch (NoSuchFieldException ex) {
                    // Ignored
                }
            }
            throw e;
        }
    }
    
    /**
     * 读取已绑定实例的字段值。
     *
     * @param <R> 返回值类型
     * @return 字段值
     * @throws Exception 读取失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <R> R get() throws Exception {
        return get(mCaller);
    }
    
    /**
     * 读取指定实例的字段值。
     *
     * @param caller 目标实例（静态字段传null）
     * @param <R>    返回值类型
     * @return 字段值
     * @throws Exception 读取失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <R> R get(Object caller) throws Exception {
        check(caller, mField, "Field");
        try {
            return (R) mField.get(caller);
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 设置已绑定实例的字段值。
     *
     * @param value 要设置的值
     * @return 当前Reflector实例（链式调用）
     * @throws Exception 设置失败时抛出
     */
    public Reflector set(Object value) throws Exception {
        return set(mCaller, value);
    }
    
    /**
     * 设置指定实例的字段值。
     *
     * @param caller 目标实例（静态字段传null）
     * @param value  要设置的值
     * @return 当前Reflector实例（链式调用）
     * @throws Exception 设置失败时抛出
     */
    public Reflector set(Object caller, Object value) throws Exception {
        check(caller, mField, "Field");
        try {
            mField.set(caller, value);
            return this;
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 选择指定名称和参数类型的方法。
     * <p>
     * 会自动搜索整个类继承链（包括父类的私有方法）。
     *
     * @param name           方法名
     * @param parameterTypes 方法参数类型数组
     * @return 当前Reflector实例（链式调用）
     * @throws Exception 找不到方法时抛出
     */
    public Reflector method(String name, Class<?>... parameterTypes) throws Exception {
        try {
            mMethod = findMethod(name, parameterTypes);
            mMethod.setAccessible(true);
            mConstructor = null;
            mField = null;
            return this;
        } catch (NoSuchMethodException e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 在类继承链中查找指定名称和参数类型的方法。
     *
     * @param name           方法名
     * @param parameterTypes 方法参数类型数组
     * @return 找到的Method对象
     * @throws NoSuchMethodException 整个继承链中都找不到时抛出
     */
    protected Method findMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        try {
            return mType.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            for (Class<?> cls = mType; cls != null; cls = cls.getSuperclass()) {
                try {
                    return cls.getDeclaredMethod(name, parameterTypes);
                } catch (NoSuchMethodException ex) {
                    // Ignored
                }
            }
            throw e;
        }
    }
    
    /**
     * 使用已绑定的实例调用选中的方法。
     *
     * @param args 方法参数
     * @param <R>  返回值类型
     * @return 方法返回值
     * @throws Exception 调用失败时抛出
     */
    public <R> R call(Object... args) throws Exception {
        return callByCaller(mCaller, args);
    }
    
    /**
     * 使用指定调用者实例调用选中的方法。
     *
     * @param caller 调用者实例（静态方法传null）
     * @param args   方法参数
     * @param <R>    返回值类型
     * @return 方法返回值
     * @throws Exception 调用失败时抛出
     */
    @SuppressWarnings("unchecked")
    public <R> R callByCaller(Object caller, Object... args) throws Exception {
        check(caller, mMethod, "Method");
        try {
            return (R) mMethod.invoke(caller, args);
        } catch (InvocationTargetException e) {
            throw new Exception("Oops!", e.getTargetException());
        } catch (Throwable e) {
            throw new Exception("Oops!", e);
        }
    }
    
    /**
     * 静默反射操作子类。
     * <p>
     * 继承 {@link Reflector}，所有反射操作失败时不抛出异常，
     * 而是将异常保存在 {@link #mIgnored} 字段中，操作返回null或this。
     * <p>
     * 适用场景：访问可能不存在的Android隐藏API或不同ROM的私有方法。
     * 可通过 {@link #getIgnored()} 检查最后一次操作是否出错。
     */
    public static class QuietReflector extends Reflector {

        /** 最后一次操作的异常，成功时为null */
        protected Throwable mIgnored;

        /**
         * 通过全限定类名创建静默Reflector实例。
         *
         * @param name 全限定类名
         * @return QuietReflector实例（类加载失败时type为null）
         */
        public static QuietReflector on(String name) {
            return on(name, true, QuietReflector.class.getClassLoader());
        }
    
        public static QuietReflector on(String name, boolean initialize) {
            return on(name, initialize, QuietReflector.class.getClassLoader());
        }
    
        public static QuietReflector on(String name, boolean initialize, ClassLoader loader) {
            Class<?> cls = null;
            try {
                cls = Class.forName(name, initialize, loader);
                return on(cls, null);
            } catch (Throwable e) {
//                Log.w(LOG_TAG, "Oops!", e);
                return on(cls, e);
            }
        }
    
        public static QuietReflector on(Class<?> type) {
            return on(type, (type == null) ? new Exception("Type was null!") : null);
        }
    
        private static QuietReflector on(Class<?> type, Throwable ignored) {
            QuietReflector reflector = new QuietReflector();
            reflector.mType = type;
            reflector.mIgnored = ignored;
            return reflector;
        }
    
        public static QuietReflector with(Object caller) {
            if (caller == null) {
                return on((Class<?>) null);
            }
            return on(caller.getClass()).bind(caller);
        }
        
        protected QuietReflector() {
            
        }
    
        /**
         * 获取最后一次操作的异常。
         *
         * @return 异常对象，无异常时返回null
         */
        public Throwable getIgnored() {
            return mIgnored;
        }
    
        /**
         * 判断是否应跳过当前操作（type为null或已有异常）。
         *
         * @return 应跳过返回true
         */
        protected boolean skip() {
            return skipAlways() || mIgnored != null;
        }
        
        /**
         * 判断是否应始终跳过操作（type为null）。
         *
         * @return type为null时返回true
         */
        protected boolean skipAlways() {
            return mType == null;
        }
    
        @Override
        public QuietReflector constructor(Class<?>... parameterTypes) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.constructor(parameterTypes);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }
    
        @Override
        public <R> R newInstance(Object... initargs) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.newInstance(initargs);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }
    
        @Override
        public QuietReflector bind(Object obj) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.bind(obj);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }
    
        @Override
        public QuietReflector unbind() {
            super.unbind();
            return this;
        }
    
        @Override
        public QuietReflector field(String name) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.field(name);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }
    
        @Override
        public <R> R get() {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.get();
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }
    
        @Override
        public <R> R get(Object caller) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.get(caller);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }
    
        @Override
        public QuietReflector set(Object value) {
            if (skip()) {
                return this;
            }
            try {
                mIgnored = null;
                super.set(value);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }
    
        @Override
        public QuietReflector set(Object caller, Object value) {
            if (skip()) {
                return this;
            }
            try {
                mIgnored = null;
                super.set(caller, value);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }
    
        @Override
        public QuietReflector method(String name, Class<?>... parameterTypes) {
            if (skipAlways()) {
                return this;
            }
            try {
                mIgnored = null;
                super.method(name, parameterTypes);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return this;
        }
    
        @Override
        public <R> R call(Object... args)  {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.call(args);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }
    
        @Override
        public <R> R callByCaller(Object caller, Object... args) {
            if (skip()) {
                return null;
            }
            try {
                mIgnored = null;
                return super.callByCaller(caller, args);
            } catch (Throwable e) {
                mIgnored = e;
//                Log.w(LOG_TAG, "Oops!", e);
            }
            return null;
        }
    }
}
