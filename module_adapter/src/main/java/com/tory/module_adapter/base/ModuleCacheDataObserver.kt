package com.tory.module_adapter.base

import android.util.SparseArray
import android.util.SparseIntArray
import androidx.recyclerview.widget.RecyclerView

class ModuleCacheDataObserver(
    private val groupPositionCache: SparseIntArray,
    private val viewTypeCache: SparseArray<ViewType<*>>
) : RecyclerView.AdapterDataObserver() {

    private fun invalidatePositionCache(startPosition: Int) {
        if (startPosition <= 0) {
            groupPositionCache.clear()
            viewTypeCache.clear()
            return
        }

        // 删除group
        var size = groupPositionCache.size()
        if (size > 0 && startPosition <= groupPositionCache.keyAt(size - 1)) {
            val keyIndex = indexKeyOfSparseArray(groupPositionCache, startPosition)
            if (size < 10 || keyIndex < size / 2) { // 如果删除的缓存少于一半或者10个，直接清除
                groupPositionCache.clear()
            } else { // 删除startPosition及之后的存储
                while (keyIndex <= size - 1) {
                    groupPositionCache.removeAt(size - 1)
                    size -= 1
                }
            }
        }

        // 删除viewType的缓存, 保险起见，直接清除
        size = viewTypeCache.size()
        if (size > 0 && startPosition <= viewTypeCache.keyAt(size - 1)) {
            viewTypeCache.clear()
        }
    }

    /**
     * 获取key值应该在的位置，详细可见 [SparseIntArray.put]
     */
    private fun indexKeyOfSparseArray(sparseArray: SparseIntArray, key: Int): Int {
        val i = sparseArray.indexOfKey(key)
        return if (i >= 0) i else i.inv()
    }

    override fun onChanged() {
        super.onChanged()
        invalidatePositionCache(0)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onItemRangeChanged(positionStart, itemCount)
        invalidatePositionCache(positionStart)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        super.onItemRangeChanged(positionStart, itemCount, payload)
        invalidatePositionCache(positionStart)
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        invalidatePositionCache(positionStart)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
        invalidatePositionCache(Math.min(fromPosition, toPosition))
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart, itemCount)
        invalidatePositionCache(positionStart)
    }
}