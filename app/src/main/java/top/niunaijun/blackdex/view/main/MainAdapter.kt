package top.niunaijun.blackdex.view.main

import android.view.ViewGroup
import top.niunaijun.blackdex.data.entity.AppInfo
import top.niunaijun.blackdex.databinding.ItemPackageBinding
import top.niunaijun.blackdex.util.newBindingViewHolder
import top.niunaijun.blackdex.view.base.BaseAdapter

/**
 * 主界面应用列表适配器。
 *
 * 展示已安装应用的图标、名称和包名。
 *
 * @author wukaicheng
 */
class MainAdapter : BaseAdapter<ItemPackageBinding, AppInfo>() {

    /**
     * 创建列表项的 ViewBinding 实例。
     *
     * @param parent 父容器
     * @return ItemPackageBinding 实例
     */
    override fun getViewBinding(parent: ViewGroup): ItemPackageBinding {
        return newBindingViewHolder(parent, false)

    }

    /**
     * 绑定应用数据到列表项视图。
     *
     * @param binding 列表项的 ViewBinding
     * @param position 列表项位置
     * @param data 当前应用信息
     */
    override fun initView(binding: ItemPackageBinding, position: Int, data: AppInfo) {
        binding.icon.setImageDrawable(data.icon)
        binding.name.text = data.name
        binding.packageName.text = data.packageName
    }
}