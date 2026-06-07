  package top.niunaijun.blackdex.view.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * 通用 RecyclerView 适配器基类。
 *
 * 封装了数据管理（增删改查）、点击/长按事件监听，
 * 子类只需实现 [getViewBinding] 和 [initView] 即可快速构建列表。
 *
 * @param T ViewBinding 类型，用于绑定列表项布局
 * @param D 数据项类型
 *
 * @author wukaicheng
 */
abstract class BaseAdapter<T : ViewBinding, D> : RecyclerView.Adapter<BaseAdapter.ViewHolder<T>>() {

    /** 当前列表数据源 */
    var dataList: MutableList<D> = ArrayList()

    /** 列表项点击回调 */
    private var onItemClick: ((position: Int, binding: T, data: D) -> Unit)? = null

    /** 列表项长按回调 */
    private var onLongClick: ((position: Int, binding: T, data: D) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return ViewHolder(getViewBinding(parent))
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val bean = dataList[position]
        val binding = holder.bindIng
        initView(binding, position, bean)

        binding.root.setOnClickListener {
            if (onItemClick != null) {
                onItemClick!!(position, holder.bindIng, bean)
            }
        }
        binding.root.setOnLongClickListener {
            if (onLongClick != null) {
                onLongClick!!(position, holder.bindIng, bean)
            }
            true
        }
    }

    /**
     * 替换全部数据并刷新列表。
     *
     * @param newDataList 新的数据列表
     */
    open fun replaceData(newDataList: List<D>) {
        this.dataList = arrayListOf<D>().apply {
            newDataList.forEach {
                this.add(it)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * 追加一批数据到列表末尾。
     *
     * @param list 要追加的数据列表
     */
    open fun addData(list: List<D>) {
        val index = this.dataList.size
        this.dataList.addAll(list)
        notifyItemRangeInserted(index, list.size)
    }

    /**
     * 追加单条数据到列表末尾。
     *
     * @param bean 要追加的数据项
     */
    open fun addData(bean: D) {
        val index = this.dataList.size
        this.dataList.add(bean)
        notifyItemRangeInserted(index, 1)
    }

    /**
     * 更新指定位置的数据项。
     *
     * @param bean 新的数据项
     * @param position 要更新的位置索引
     */
    open fun updateData(bean: D, position: Int) {
        if (dataList.size > position) {
            dataList[position] = bean
            notifyItemChanged(position)
        }
    }

    /**
     * 根据数据对象移除列表中的对应项。
     *
     * @param bean 要移除的数据项
     * @return 被移除项的索引位置，未找到时返回 -1
     */
    open fun removeData(bean: D): Int {
        val position: Int = this.dataList.indexOf(bean)
        if (position >= 0) {
            removeDataAt(position)
        }
        return position
    }

    /**
     * 移除指定位置的数据项。
     *
     * @param position 要移除的位置索引
     */
    open fun removeDataAt(position: Int) {
        if (position >= 0) {
            this.dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size - position)
        }
    }

    /**
     * 设置列表项点击监听器。
     *
     * @param function 回调函数，参数为 (位置, ViewBinding, 数据)
     */
    fun setOnItemClick(function: (position: Int, binding: T, data: D) -> Unit) {
        this.onItemClick = function

    }

    /**
     * 设置列表项长按监听器。
     *
     * @param function 回调函数，参数为 (位置, ViewBinding, 数据)
     */
    fun setOnItemLongClick(function: (position: Int, binding: T, data: D) -> Unit) {
        this.onLongClick = function

    }

    /**
     * 获取 LayoutInflater 实例。
     *
     * @param parent 父容器，用于获取 Context
     * @return LayoutInflater 实例
     */
    fun getLayoutInflater(parent: ViewGroup): LayoutInflater {
        return LayoutInflater.from(parent.context)
    }

    /**
     * 创建列表项的 ViewBinding 实例。
     *
     * @param parent 父容器
     * @return ViewBinding 实例
     */
    abstract fun getViewBinding(parent: ViewGroup): T

    /**
     * 绑定数据到列表项视图。
     *
     * @param binding 当前列表项的 ViewBinding
     * @param position 列表项位置索引
     * @param data 当前列表项对应的数据
     */
    abstract fun initView(binding: T, position: Int, data: D)


    /**
     * 通用 ViewHolder，持有 ViewBinding 引用。
     *
     * @param T ViewBinding 类型
     * @param bindIng 当前列表项的 ViewBinding 实例
     */
    class ViewHolder<T : ViewBinding>(val bindIng: T) : RecyclerView.ViewHolder(bindIng.root)

}