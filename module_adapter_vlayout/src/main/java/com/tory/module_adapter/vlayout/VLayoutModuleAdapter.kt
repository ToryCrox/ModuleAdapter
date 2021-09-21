package com.tory.module_adapter.vlayout

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.LinearLayoutHelper
import com.tory.module_adapter.base.*

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description: 组件化Adapter，适用于vlayout
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
class VLayoutModuleAdapter(
    private val calDiff: Boolean = false,
    private val adapterIndex: Int = 0,
    private var layoutHelper: LayoutHelper? = null
) : VLayoutDelegateInnerAdapter<Any>(), IModuleImplAdapter {

    override val delegate: ModuleAdapterDelegate = ModuleAdapterDelegate(
            this,
            object : IDataAdapter {
                override fun getItem(position: Int): Any? = list.getOrNull(position)
                override fun getCount(): Int = list.size
                override fun remove(position: Int) {
                    list.removeAt(position)
                }

                override fun addAll(index: Int, listData: List<Any>, clear: Boolean) {
                    if (clear) list.clear()
                    list.addAll(index, listData)
                }

                override fun notifyDataSetChange() {
                    notifyDataSetChanged()
                }
            })

    private var spanSizeLookup: ExtendGridLayoutHelper.SpanSizeLookup? = null
    private var spanCount: Int = 1

    private var isFixPreLayout: Boolean = false

    /**
     * 请不要重写，此方法，通过
     */
    override fun onCreateLayoutHelper(): LayoutHelper {
        val lh = layoutHelper
        if (lh != null) return lh
        val pair = delegate.getGridSpanLookup()
        spanCount = pair.first
        val lookup = pair.second
        if (spanCount > 1) {
            return ExtendGridLayoutHelper(spanCount).also {
                it.setAutoExpand(false)
                spanSizeLookup = object : ExtendGridLayoutHelper.SpanSizeLookup() {

                    override fun invalidateSpanIndexCache() {
                        super.invalidateSpanIndexCache()
                        lookup.invalidateSpanIndexCache()
                    }

                    override fun getSpanSize(position: Int): Int {
                        return lookup.getSpanSize(position - startPosition)
                    }
                }
                it.setSpanSizeLookup(spanSizeLookup)
                //it.setIsFixPreLayout(isFixPreLayout)
                layoutHelper = it
            }
        }
        return LinearLayoutHelper().also {
            layoutHelper = it
        }
    }

    /**
     * 在有动画时，getSpanIndex, getMargin获取的position有错误，例如收藏删除时，下面推荐的位置有错误
     */
    fun setFixPreLayout(isFixPreLayout: Boolean) {
        this.isFixPreLayout = isFixPreLayout
    }

    override fun setDebug(debug: Boolean) {
        delegate.setDebug(debug)
    }

    override fun setDebugTag(deubTag: String) {
        delegate.setDebugTag(deubTag)
    }

    override fun isEmpty() = list.isEmpty()

    override fun indexOf(item: Any): Int {
        return list.indexOf(item)
    }

    override fun indexOf(predicate: (Any) -> Boolean): Int {
        return list.indexOfFirst(predicate)
    }

    override fun getItems(): List<Any> {
        return list
    }

    override fun setItems(items: List<Any>) {
        if (calDiff && list.size > 0) {
            val result = DiffUtil.calculateDiff(RvDiffCallback(
                    list,
                    items))
            list.clear()
            list.addAll(items)
            result.dispatchUpdatesTo(this)
        } else if (items != list) {
            list.clear()
            list.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun setItemsWithDiff(items: List<Any>, callback: DiffUtil.Callback?, updateCallback: ListUpdateCallback?) {
        if (list.size > 0) {
            val result = DiffUtil.calculateDiff(callback ?: RvDiffCallback(list, items))
            list.clear()
            list.addAll(items)
            if (updateCallback != null) {
                result.dispatchUpdatesTo(updateCallback)
            } else {
                result.dispatchUpdatesTo(this)
            }
        } else if (items != list) {
            list.clear()
            list.addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun getGroupPosition(groupType: String, position: Int): Int {
        return delegate.findGroupPosition(groupType, position)
    }

    override fun getGroupTypeByPosition(position: Int): String {
        return delegate.findGroupTypeByPosition(position)
    }

    override fun getGroupTypes(groupType: String): List<Class<*>> {
        return delegate.getGroupTypes(groupType)
    }

    override fun getGroupCount(groupType: String): Int {
        return delegate.getGroupCount(groupType)
    }

    override fun getStartPosition(): Int {
        if (layoutHelper == null) {
            delegate.logw("getStartPosition can not found layoutHelper")
        }
        return layoutHelper?.range?.lower ?: 0
    }

    override fun getGridLayoutManager(context: Context): GridLayoutManager {
        return delegate.getGridLayoutManager(context)
    }

    override fun getSpanCount(): Int {
        return delegate.getGridSpanLookup().first
    }

    override fun getSpanSize(position: Int): Int {
        val spanLookup = delegate.getGridSpanLookup().second
        return spanLookup.getSpanSize(position)
    }

    override fun getSpanIndex(position: Int): Int {
        val (spanCount, spanLookup) = delegate.getGridSpanLookup()
        return spanLookup.getSpanIndex(position, spanCount)
    }

    override fun getSpanGroupIndex(position: Int): Int {
        val (spanCount, spanLookup) = delegate.getGridSpanLookup()
        return spanLookup.getSpanGroupIndex(position, spanCount)
    }

    override fun refresh(oldItem: Any, newItem: Any?) {
        val index = list.indexOf(oldItem)
        if (index < 0) {
            return
        }
        if (newItem != null) {
            if (oldItem != newItem) {
                list.removeAt(index)
                list.add(index, newItem)
            }
            notifyItemChanged(index)
        } else {
            notifyItemRemoved(index)
        }
    }

    override fun setModuleCallback(moduleCallback: IModuleCallback?) {
        delegate.setModuleCallback(moduleCallback)
    }

    override fun syncWith(adapter: IModuleAdapter) {
        if (adapter is IModuleImplAdapter) {
            delegate.syncWith(adapter.delegate)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return delegate.getViewTypeIndex(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        delegate.onAttachedToRecyclerView(recyclerView)
        for ((type, maxSize) in delegate.allRecyclerPoolSize()) {
            if (type >= 0 && maxSize > 5) {
                delegate.logd("setMaxRecycledViews type:$type, maxSize:$maxSize")
                setMaxRecycledViews(adapterIndex, type, maxSize)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        delegate.onDetachedFromRecyclerView()
    }

    /**
     * 如果需要一个类对应多个View需要先注册这个生成器类
     */
    inline fun <reified T : Any> registerModelKeyGetter(noinline getter: ModelKeyGetter<T>) {
        val clazz = T::class.java
        registerModelKeyGetter(clazz, getter)
    }

    override fun <T : Any> registerModelKeyGetter(clazz: Class<T>, getter: ModelKeyGetter<T>) {
        delegate.registerModelKeyGetter(clazz, getter)
    }


    /**
     * 注册类型
     * 注: 1. 不能重复注册相同的model类, 在Fragment里面使用报错的，将register提前到view创建之前，或者使用FragmentStateAdapter
     * 2. 必需在add到RecyclerView之前调用
     * @param gridSize 每行所占的网格数
     * @param groupType 类型归类，获取position需要, 在View中通过groupPosition获取对应位置，通过ModuleGroupSectionModel可以用来分割
     * @param poolSize 设置缓存大小
     * @param groupMargin 设置两边的margin，目前只对vLayout有效
     * @param enable 该注册是否生效，方便书写，防止大堆if else
     */
    inline fun <reified V, reified M : Any> register(
            gridSize: Int = 1,
            groupType: String? = null,
            poolSize: Int = -1,
            enable: Boolean = true,
            modelKey: Any? = null, // 注册相同的class时需要以这个做区分
            itemSpace: ItemSpace? = null,
            noinline creator: (ViewGroup) -> V
    ) where V : IModuleView<M>, V : View {
        delegate.register(gridSize = gridSize,
                groupType = groupType,
                poolSize = poolSize,
                enable = enable,
                modelKey = modelKey,
                itemSpace = itemSpace,
                creator = creator)
    }

    override fun <V, M : Any> register(
            clazzType: Class<M>,
            gridSize: Int,
            groupType: String?,
            poolSize: Int,
            enable: Boolean,
            modelKey: Any?, // 注册相同的class时需要以这个做区分
            creator: (ViewGroup) -> V
    ) where V : IModuleView<M>, V : View {
        delegate.register(clazzType = clazzType,
                gridSize = gridSize,
                groupType = groupType,
                poolSize = poolSize,
                enable = enable,
                modelKey = modelKey,
                creator = creator)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        return CommonViewHolder(delegate.createView(parent, viewType)).also {
            delegate.bindHolder(it.itemView, it, viewType, this)
        }
    }


    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        val item = getItem(position) ?: return
        delegate.bindView(holder.itemView, item, position)
    }


    override fun onViewRecycled(holder: CommonViewHolder) {
        super.onViewRecycled(holder)
        delegate.onViewRecycled(holder.itemView)
    }

    class CommonViewHolder(val containerView: View) : RecyclerView.ViewHolder(containerView)
}
