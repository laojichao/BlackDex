package top.niunaijun.blackdex.biz.cache


import android.content.Context
import android.text.TextUtils
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * 基于 SharedPreferences 的属性委托（Property Delegate）。
 *
 * 利用 Kotlin 的 [ReadWriteProperty] 接口，将任意属性自动持久化到
 * SharedPreferences 中，支持 Int、Long、Float、String、Boolean 五种基本类型。
 * 如需支持自定义对象，可继承本类并重写 [findData] / [putData] 方法。
 *
 * @param Data 属性的数据类型（受五种基本类型约束）
 * @param context 应用 Context，用于获取 SharedPreferences 实例
 * @param default 属性的默认值
 * @param spName SharedPreferences 文件名，为 null 时使用类名作为文件名
 *
 * @author mini
 */
open class AppSharedPreferenceDelegate<Data>(context: Context, private val default: Data, spName: String? = null) : ReadWriteProperty<Any, Data?> {

    /** SharedPreferences 实例，延迟初始化 */
    private val mSharedPreferences by lazy {
        val tmpCacheName = if (TextUtils.isEmpty(spName)) {
            AppSharedPreferenceDelegate::class.java.simpleName
        } else {
            spName
        }
        return@lazy context.getSharedPreferences(tmpCacheName, Context.MODE_PRIVATE)
    }

    /**
     * 读取属性值，从 SharedPreferences 中获取与属性名对应的值。
     *
     * @param thisRef 持有属性的对象
     * @param property 属性的元数据（使用 [KProperty.name] 作为 key）
     * @return 持久化的属性值，未找到时返回 [default]
     */
    override fun getValue(thisRef: Any, property: KProperty<*>): Data {
        return findData(property.name, default)
    }

    /**
     * 写入属性值，将值持久化到 SharedPreferences 中。
     *
     * @param thisRef 持有属性的对象
     * @param property 属性的元数据
     * @param value 要写入的新值，为 null 时移除对应 key
     */
    override fun setValue(thisRef: Any, property: KProperty<*>, value: Data?) {
        putData(property.name, value)
    }

    /**
     * 从 SharedPreferences 中读取指定 key 的值。
     *
     * @param key SharedPreferences 的 key（属性名）
     * @param default 未找到时返回的默认值
     * @return 读取到的值，类型不匹配时返回 [default]
     * @throws IllegalArgumentException 当数据类型不在支持范围内时抛出
     */
    protected fun findData(key: String, default: Data): Data {
        with(mSharedPreferences) {
            val result: Any? = when (default) {
                is Int -> getInt(key, default)
                is Long -> getLong(key, default)
                is Float -> getFloat(key, default)
                is String -> getString(key, default)
                is Boolean -> getBoolean(key, default)
                else -> throw IllegalArgumentException("This type $default can not be saved into sharedPreferences")
            }
            return result as? Data ?: default
        }
    }

    /**
     * 将值写入 SharedPreferences。
     *
     * @param key SharedPreferences 的 key（属性名）
     * @param value 要写入的值，为 null 时从 SharedPreferences 中移除该 key
     * @throws IllegalArgumentException 当数据类型不在支持范围内时抛出
     */
    protected fun putData(key: String, value: Data?) {
        mSharedPreferences.edit {
            if (value == null) {
                remove(key)
            } else {
                when (value) {
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> throw IllegalArgumentException("This type $default can not be saved into Preferences")
                }
            }
        }
    }
}