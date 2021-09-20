package com.tory.module_adapter.base

import android.content.Context
import android.graphics.Point
import android.os.Looper
import android.os.SystemClock
import android.util.ArrayMap
import android.util.SparseArray
import android.util.SparseIntArray
import android.view.View
import android.view.ViewGroup
import androidx.core.os.TraceCompat
import androidx.core.util.forEach
import androidx.core.util.putAll
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tory.module_adapter.R
import com.tory.module_adapter.utils.LogUtils
import com.tory.module_adapter.utils.TimeRecorder
import com.tory.module_adapter.views.*


import java.lang.reflect.Constructor

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
class ModuleAdapterDelegate(
    private val moduleAdapter: IModuleAdapter,
    private val dataAdapter: IDataAdapter
) {
    internal val viewTypes = SparseArray<ViewType<*>>()
    internal val viewTypeMap = ArrayMap<Class<*>, IViewType<*>>()
    internal val groupTypes = ArrayMap<String, Set<ViewType<*>>>() // 分类
    internal val viewPoolSizes = mutableListOf<Pair<Int, Int>>()

    private val debugViewCount = SparseIntArray()
    private var isDebug: Boolean = false
    private var debugTag: String = ""
    private var recyclerView: RecyclerView? = null
    private var spanLookupCache: Pair<Int, GridLayoutManager.SpanSizeLookup>? = null

    // groupType的缓存
    private var groupPositionCache: SparseIntArray = SparseIntArray()
    private var isCacheGroupPosition: Boolean = true

    // viewType的缓存
    private var viewTypeCache = SparseArray<ViewType<*>>()
    private var isCacheViewType = true

    private var moduleCallback: IModuleCallback? = null

    private var viewTypeMax: Int = 0

    private var spaceDecoration: ModuleGridDelegateDecoration? = null

    private val tag: String
        get() = "$TAG $debugTag "

    init {
        register { ModuleEmptyView(it.context) }
        register { ModuleDividerView(it.context) }
        //register { MallNoMoreTipView(it.context) }
        register { ModuleSeparatorBarView(it.context) }
        //register { ModuleSeparatorBarWithImageView(it.context) }
        //register { ModuleLoadingContentView(it.context) }

        register { ModuleGroupSectionView(it.context) }

        (moduleAdapter as RecyclerView.Adapter<*>).registerAdapterDataObserver(
            ModuleCacheDataObserver(groupPositionCache, viewTypeCache)
        )
    }

    /**
     * 同步另外一个Adapter注册的view
     */
    fun syncWith(delegate: ModuleAdapterDelegate) {
        assertMainThread()
        check(recyclerView == null) { // 必需在adapter attach RecyclerView之前注册
            "$tag syncWith must before to attach RecyclerView (请在设置给RecyclerView之前注册组件)"
        }

        this.viewTypes.clear()
        this.viewTypes.putAll(delegate.viewTypes)
        this.viewTypeMap.clear()
        this.viewTypeMap.putAll(delegate.viewTypeMap)
        this.groupTypes.clear()
        this.groupTypes.putAll(delegate.groupTypes)
        this.viewTypeMax = delegate.viewTypeMax
        this.viewPoolSizes.clear()
        this.viewPoolSizes.addAll(delegate.viewPoolSizes)
        this.spaceDecoration = delegate.spaceDecoration?.copy(moduleAdapter)
    }

    fun generateViewTypeIndex(): Int = viewTypeMax++

    fun setModuleCallback(moduleCallback: IModuleCallback?) {
        this.moduleCallback = moduleCallback
    }

    fun setDebug(enable: Boolean) {
        this.isDebug = enable
    }

    fun setDebugTag(tag: String) {
        this.debugTag = tag
    }

    fun getDebugTag(): String {
        return this.debugTag
    }

    fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        val decoration = spaceDecoration
        if (decoration != null) {
            recyclerView.removeItemDecoration(decoration)
            recyclerView.addItemDecoration(decoration)
        }
    }

    fun onDetachedFromRecyclerView() {
        this.recyclerView = null
        val decoration = spaceDecoration
        if (decoration != null) {
            recyclerView?.removeItemDecoration(decoration)
            recyclerView?.let {
                it.removeItemDecoration(decoration)
            }
        }
    }

    fun onViewAttachedToWindow(itemView: View, position: Int) {
        val lp = itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams
            && getViewTypeByPosition(position)?.gridSize == 1) {
            lp.isFullSpan = true
        }
    }

    fun onViewDetachedFromWindow(itemView: View) {
    }

    /**
     * 注册类型
     */
    fun registerGroupType(groupType: String, viewType: ViewType<*>) {
        val types = groupTypes[groupType]
        if (types == null) {
            groupTypes[groupType] = setOf(viewType)
        } else {
            groupTypes[groupType] = types.plus(viewType)
        }
    }

    fun checkRegister(clazz: Class<*>) {
        if (!isDebug) return
        check(!checkClassType(clazz)) {
            "$tag register class $clazz not illegal, must not Primitive, Collection, Map (注册类型不合法，不允许为基本类型，String, Collection, Map)"
        }
        check(recyclerView == null) { // 必需在adapter attach RecyclerView之前注册
            "$tag register must before to attach RecyclerView (请在设置给RecyclerView之前注册组件)"
        }
    }

    private fun checkClassType(clazz: Class<*>): Boolean {
        return ILLEGAL_CLASS_TYPE.any { it?.isAssignableFrom(clazz) == true }
    }

    fun <T : Any> registerModelKeyGetter(clazz: Class<T>, getter: ModelKeyGetter<T>) {
        val viewType = viewTypeMap[clazz]
        check(viewType == null) {
            "please not repeat registerModelKeyGetter for clazz:$clazz (请不要重复注册)"
        }
        viewTypeMap[clazz] = ViewTypeGroup(
            modelKeyGetter = getter
        )
    }

    inline fun <reified V, reified M : Any> register(
        gridSize: Int = 1,
        groupType: String? = null,
        poolSize: Int = -1,
        enable: Boolean = true,
        modelKey: Any? = null, // 注册相同的class时需要以这个做区分
        itemSpace: ItemSpace? = null,
        noinline creator: (ViewGroup) -> V
    ) where V : IModuleView<M>, V : View {
        val clazzType = M::class.java
        register(clazzType, gridSize, groupType, poolSize, enable, modelKey, itemSpace, creator)
    }

    fun <V, M : Any> register(
        clazzType: Class<M>,
        gridSize: Int = 1,
        groupType: String? = null,
        poolSize: Int = -1,
        enable: Boolean = true,
        modelKey: Any? = null, // 注册相同的class时需要以这个做区分
        itemSpace: ItemSpace? = null,
        creator: (ViewGroup) -> V
    ) where V : IModuleView<M>, V : View {
        if (!enable) return
        checkRegister(clazzType)
        val realGroupType = groupType ?: clazzType.name
        val viewType = ViewType(
            clazzType, typeIndex = generateViewTypeIndex(),
            groupType = realGroupType,
            gridSize = gridSize,
            poolSize = poolSize
        ) { parent -> creator(parent) }
        registerGroupType(realGroupType, viewType)
        addViewType(viewType, modelKey)
        if (itemSpace != null) {
            registerItemSpace(realGroupType, gridSize, itemSpace)
        }
    }

    fun addViewType(viewType: ViewType<*>, modelKey: Any? = null) {
        if (modelKey != null) {
            val typeGroup = viewTypeMap[viewType.type]
            check(typeGroup is ViewTypeGroup<*>) {
                "$tag must registerModelKeyGetter before(请先注册modelKey的生成器，以便查找具体类型), modeKey:$modelKey"
            }
            typeGroup.viewTypes.put(viewType.typeIndex, viewType)
            typeGroup.modelKeyTypeMap.put(modelKey, viewType.typeIndex)
        } else {
            if (isDebug) {
                check(viewTypeMap[viewType.type] == null) {
                    "$tag please not register ${viewType.type} repeat(请不要重复注册)"
                }
            }
            viewTypeMap[viewType.type] = viewType
        }
        viewTypes.put(viewType.typeIndex, viewType)
        if (viewType.poolSize > 0) {
            viewPoolSizes.add(viewType.typeIndex to viewType.poolSize)
        }
    }

    private fun registerItemSpace(groupType: String, gridSize: Int, itemSpace: ItemSpace) {
        val decoration = spaceDecoration ?: ModuleGridDelegateDecoration(moduleAdapter).also {
            spaceDecoration = it
        }
        decoration.registerSpace(groupType, gridSize, itemSpace, isDebug)
    }

    /**
     * 获取类型
     */
    fun getViewTypeIndex(position: Int): Int {
        return getViewTypeByPosition(position)?.typeIndex ?: -1
    }

    /**
     * 通过adapter的viewType获取
     */
    private fun getViewTypeByIndex(viewTypeIndex: Int): ViewType<*>? {
        val type: ViewType<*>? = viewTypes.get(viewTypeIndex)
        if (type == null && isDebug) {
            throw IllegalStateException("$tag can not found viewType: viewType:$viewTypeIndex")
        } else if (type == null) {
            bmBug("getViewTypeByIndex is null, viewTypeIndex:$viewTypeIndex")
        }
        return type
    }

    /**
     * 获取adapter中的position的viewType
     */
    fun getViewTypeByPosition(position: Int): ViewType<*>? {
        if (isDebug) {
            assertMainThread()
        }
        if (isCacheViewType) {
            val viewType = viewTypeCache.get(position)
            if (viewType != null) {
                return viewType
            }
        }
        val model = dataAdapter.getItem(position)
        if (model == null && isDebug) {
            throw IllegalArgumentException("$tag getViewTypeByPosition getItem is null for position: $position")
        } else if (model == null) {
            bmBug("getViewTypeByPosition getItem is null for position: $position")
            return null
        }
        return getViewTypeByModel(model)
    }

    private fun getViewTypeByModel(model: Any): ViewType<*>? {
        val modelClazz = model.javaClass
        val type = viewTypeMap[modelClazz]
        val viewType = if (type is ViewType<*>) {
            return type
        } else if (type is ViewTypeGroup<*>) {
            val key = (type as ViewTypeGroup<Any>).modelKeyGetter(model)
            val index = type.modelKeyTypeMap[key] ?: return null
            type.viewTypes[index]
        } else null
        //val viewType = getViewTypeIndex(model.javaClass)
        if (viewType == null && isDebug) {
            throw IllegalArgumentException(
                "$tag getItemViewType can not found view type " +
                        "for ${model.javaClass.name} model: $model," +
                        " please check you register the Model"
            )
        } else if (viewType == null) {
            bmBug(
                "getItemViewType can not found view type " +
                        "for ${model.javaClass.name} model: $model," +
                        " please check you register the Model"
            )
        }
        return viewType
    }

    /**
     * 创建view
     */
    fun createView(parent: ViewGroup, viewType: Int): View {
        TraceCompat.beginSection("$tag createView viewType:$viewType")
        val startTime: Long = if (isDebug) {
            SystemClock.elapsedRealtimeNanos()
        } else 0L
        val type: ViewType<*> = getViewTypeByIndex(viewType) ?: return View(parent.context)
        val view: View = type.viewCreator.invoke(parent)

        val lp = view.layoutParams
        if (lp != null) { // 有LayoutParams时不需要重新沿用
            view.layoutParams = when (lp) {
                is RecyclerView.LayoutParams -> lp
                is ViewGroup.MarginLayoutParams -> RecyclerView.LayoutParams(lp)
                else -> RecyclerView.LayoutParams(lp)
            }
        } else { // 默认LayoutParams
            view.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        moduleCallback?.onViewCreated(parent, view, viewType)

        if (isDebug) {
            val timeSpent = SystemClock.elapsedRealtimeNanos() - startTime
            debugViewCount.put(viewType, debugViewCount[viewType] + 1)
            logd(
                "createView viewType:$viewType, " +
                        "view:${view.javaClass.simpleName}, viewCount:${debugViewCount[viewType]}," +
                        " timeSpent: ${TimeRecorder.nanoToMillis(timeSpent)}ms"
            )
        }
        TraceCompat.endSection()

        return view
    }

    fun bindHolder(
        view: View,
        viewHolder: RecyclerView.ViewHolder,
        viewType: Int,
        rvAdapter: IModuleAdapter
    ) {
        view.setTag(MODULE_ADAPTER_ITEM_HOLDER_TAG, object : IRvItemHolder {

            override fun getStartPosition(): Int = rvAdapter.getStartPosition()

            override fun getViewLayoutPosition(): Int = viewHolder.layoutPosition

            override fun getLayoutPosition(): Int {
                val adapterPosition = viewHolder.adapterPosition
                val resultPosition = if (adapterPosition >= 0) {
                    adapterPosition - rvAdapter.getStartPosition()
                } else {
                    //next version remove it
                    val layoutPosition = viewHolder.layoutPosition
                    bmBug(
                        "IRvItemHolder adapterPosition is invalid, adapterPosition:$adapterPosition, " +
                                "and try layoutPosition:$layoutPosition"
                    )
                    layoutPosition - rvAdapter.getStartPosition()
                }
                if (resultPosition < 0) {
                    bmBug(
                        "IRvItemHolder getLayoutPosition invalid, adapterPosition:$adapterPosition," +
                                "layoutPosition:${viewHolder.layoutPosition}, startPosition:${getStartPosition()}"
                    )
                }
                return resultPosition
            }


            override fun getGroupPosition(): Int {
                val type = getViewTypeByIndex(viewType) ?: return getLayoutPosition()
                val layoutPosition = getLayoutPosition()
                if (layoutPosition < 0) {
                    return -1
                }
                val groupPosition = findGroupPosition(type.groupType, layoutPosition)
                if (groupPosition < 0) {
                    val item = dataAdapter.getItem(layoutPosition)
                    bmBug(
                        "IRvItemHolder getGroupPosition is invalid, adapterPosition:${viewHolder.adapterPosition}, " +
                                "layoutPosition:${viewHolder.layoutPosition}, startPosition:${getStartPosition()}, resultPosition:$layoutPosition, " +
                                "groupType:${type.groupType}, clazz:${type.type}, item:$item"
                    )
                }
                return groupPosition
            }

            override fun getGroupCount(): Int {
                val type = getViewTypeByIndex(viewType) ?: return getItemCount()
                val groupType = type.groupType
                return rvAdapter.getGroupCount(groupType)
            }

            override fun getItemCount(): Int = rvAdapter.getItemCount()
        })
    }

    /**
     * 解绑
     */
    fun onViewRecycled(view: View) {
        if (view is IModuleLifecycle) {
            view.onViewRecycled()
        }
    }

    /**
     * 绑定数据
     */
    fun bindView(view: View, item: Any, position: Int) {
        if (view !is IModuleView<*>) {
            return
        }
        TraceCompat.beginSection("$tag bindView ${view.javaClass.simpleName}")
        val startTime = if (isDebug) SystemClock.elapsedRealtimeNanos() else 0L
        if (view is IModuleLifecycle) {
            view.onBind()
        }
        moduleCallback?.onBind(view, item, position)
        (view as IModuleView<Any>).update(item)
        moduleCallback?.onBindAfter(view, item, position)
        if (isDebug) {
            val timeSpent = SystemClock.elapsedRealtimeNanos() - startTime
            logd(
                "bindView position:$position groupPosition:${view.groupPosition}" +
                        ", view:${view.javaClass.simpleName} timeSpent: ${
                            TimeRecorder.nanoToMillis(
                                timeSpent
                            )
                        }ms"
            )
        }
        TraceCompat.endSection()
    }

    fun findGroupTypeByPosition(position: Int): String {
        val viewType = getViewTypeByPosition(position)
        return viewType?.groupType.orEmpty()
    }

    /**
     * 获取groupType
     */
    fun findGroupPosition(groupType: String, position: Int): Int {
        val types = groupTypes[groupType] ?: return -1
        return findCachedGroupPosition(types, position)
    }

    private fun findCachedGroupPosition(types: Set<ViewType<*>>, position: Int): Int {
        if (!checkPosition(position)) return -1
        if (!(getViewTypeByPosition(position) in types)) {
            // 在这里将类型不匹配的拦掉
            return -1
        }
        if (!isCacheGroupPosition) {
            return findGroupPosition(types, position)
        }
        val existing = groupPositionCache.get(position, -1)
        if (existing >= 0) {
            return existing
        }
        val groupPosition = findGroupPosition(types, position)
        groupPositionCache.put(position, groupPosition)
        return groupPosition
    }

    /**
     * position一定是对应types的类型
     */
    private fun findGroupPosition(types: Set<ViewType<*>>, position: Int): Int {
        if (position == 0) return 0
        var typePos = 0
        for (index in position - 1 downTo 0) {
            val item1 = dataAdapter.getItem(index)
            if (item1 is ModuleGroupSectionModel) {
                break
            }
            if (!(getViewTypeByPosition(index) in types)) {
                continue
            }
            // 数据必须是对应的类型
            typePos++
            if (isCacheGroupPosition) { // 找到之前的缓存
                val p = groupPositionCache.get(index, -1)
                if (p >= 0) {
                    typePos += p
                    break
                }
            }
        }
        return typePos
    }

    /**
     * 检查是否越界
     */
    private fun checkPosition(position: Int) = position >= 0 && position < dataAdapter.getCount()

    /**
     * 获取分组类型
     */
    fun getGroupTypes(groupType: String): List<Class<*>> {
        return groupTypes[groupType]?.map { it.type }.orEmpty()
    }

    /**
     * 分组数量
     */
    fun getGroupCount(groupType: String): Int {
        val types = groupTypes[groupType].orEmpty()
        if (types.isEmpty()) return 0
        return (0 until dataAdapter.getCount()).count {
            val type = getViewTypeByPosition(it)
            type != null && type in types
        }
    }

    /**
     * @return <Type, Size>
     */
    fun allRecyclerPoolSize(): List<Pair<Int, Int>> {
        return viewPoolSizes
    }

    // log
    fun logd(msg: String) {
        LogUtils.d("$tag $msg")
    }

    fun logw(msg: String, e: Throwable? = null) {
        LogUtils.w("$tag $msg", e)
    }

    fun loge(msg: String, e: Throwable? = null) {
        LogUtils.e("$tag $msg", e)
    }

    companion object {
        const val TAG = "ModuleAdapter"
        const val TYPE_NONE = 0
        private val CONSTRUCTOR_CACHE = ArrayMap<CKey, Constructor<*>>()

        private val ILLEGAL_CLASS_TYPE = arrayOf(
            Int::class.javaPrimitiveType,
            Long::class.javaPrimitiveType,
            Short::class.javaPrimitiveType,
            Float::class.javaPrimitiveType,
            Double::class.javaPrimitiveType,
            Byte::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            Char::class.javaPrimitiveType,
            Int::class.java,
            Long::class.java,
            Short::class.java,
            Float::class.java,
            Double::class.java,
            Byte::class.java,
            Boolean::class.java,
            Char::class.java,
            CharSequence::class.java,
            Collection::class.java,
            Map::class.java
        )


    }

    fun getGridLayoutManager(context: Context): GridLayoutManager {
        val pair = getGridSpanLookup()
        val spanCount = pair.first
        val glm = GridLayoutManager(context, spanCount)
        glm.orientation = RecyclerView.VERTICAL
        glm.spanSizeLookup = pair.second
        return glm
    }

    fun getSpanMargin(position: Int): Point? {
        val type = getViewTypeByPosition(position) ?: return null
        return type.margin
    }

    fun getGridSpanLookup(): Pair<Int, GridLayoutManager.SpanSizeLookup> {
        val cache = spanLookupCache
        if (cache != null) {
            return cache
        }
        var spanCount = 1
        viewTypes.forEach { _, viewType ->
            val size = viewType.gridSize
            if (size > 0 && spanCount % size != 0) {
                spanCount *= size
            }
        }

        val spanLockup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (spanCount == 1) return 1
                val type = getViewTypeByPosition(position) ?: return 1
                return spanCount / type.gridSize
            }
        }
        val pair = Pair(spanCount, spanLockup)
        spanLookupCache = pair
        return pair
    }

    private fun assertMainThread() {
        if (!isOnUiThread()) {
            throw java.lang.IllegalStateException("Expected to run on UI thread!")
        }
    }

    fun isOnUiThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }


}

data class CKey(val viewClz: Class<*>, val dataClz: Class<*>? = null)

/**
 * 数据存储器
 */
interface IDataAdapter {
    fun getItem(position: Int): Any?
    fun getCount(): Int
    fun remove(position: Int)
    fun addAll(index: Int, listData: List<Any>, clear: Boolean)
    fun notifyDataSetChange()
}


interface IViewType<T : Any>

/**
 * 组件类型
 */
class ViewType<T : Any>(
    val type: Class<T>,
    val typeIndex: Int,
    val groupType: String,
    val gridSize: Int = 1,
    val poolSize: Int = -1,
    val margin: Point? = null,
    val viewCreator: IViewCreator
) : IViewType<T>

class ViewTypeGroup<T : Any>(
    val viewTypes: SparseArray<ViewType<*>> = SparseArray(),
    val modelKeyTypeMap: MutableMap<Any, Int> = ArrayMap(),
    val modelKeyGetter: ModelKeyGetter<T>
) : IViewType<T>


class ItemSpace(
    val spaceH: Int = 0,
    val spaceV: Int = 0,
    val edgeH: Int = 0
)

typealias ModelKeyGetter<T> = (t: T) -> Any?

typealias IViewCreator = (parent: ViewGroup) -> View

interface IRvItemHolder {
    fun getStartPosition(): Int
    fun getViewLayoutPosition(): Int
    fun getLayoutPosition(): Int
    fun getGroupPosition(): Int
    fun getGroupCount(): Int
    fun getItemCount(): Int
}

val MODULE_ADAPTER_ITEM_HOLDER_TAG = R.id.module_adapter_item_holder_tag

val IModuleView<*>.rvItemHolder: IRvItemHolder?
    get() = if (this is ISubModuleView) {
        this.parent.rvItemHolder
    } else if (this is View) {
        val tag = getTag(MODULE_ADAPTER_ITEM_HOLDER_TAG)
        if (tag is IRvItemHolder) tag else null
    } else {
        bmBug("can not find IRvItemHolder, $this")
        null
    }

/**
 * 对于VLayout的多Adapter，表示该Adapter的开始的position
 */
val IModuleView<*>.startPosition: Int
    get() = rvItemHolder?.getStartPosition() ?: 0

/**
 * ViewHolder的layoutPosition, 不考虑startPosition
 */
val IModuleView<*>.viewLayoutPosition: Int
    get() = rvItemHolder?.getViewLayoutPosition() ?: -1

/**
 * ViewHolder的adapterPosition - startPosition
 */
val IModuleView<*>.layoutPosition: Int
    get() = rvItemHolder?.getLayoutPosition() ?: -1

/**
 * 分组的position
 */
val IModuleView<*>.groupPosition: Int
    get() = rvItemHolder?.getGroupPosition() ?: -1

/**
 * 该分组的数量
 */
val IModuleView<*>.groupCount: Int
    get() = rvItemHolder?.getGroupCount() ?: -1

/**
 * 该Adapter的itemCount
 */
val IModuleView<*>.itemCount: Int
    get() = rvItemHolder?.getItemCount() ?: 0

class RvDiffCallback(private val oldList: List<Any>, private val newList: List<Any>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList.getOrNull(oldItemPosition) == newList.getOrNull(newItemPosition)

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList.getOrNull(oldItemPosition) == newList.getOrNull(newItemPosition)
}


fun bmBug(msg: String, e: Throwable? = null) {
    LogUtils.e("", "ModuleAdapter bmBug $msg", e)
}