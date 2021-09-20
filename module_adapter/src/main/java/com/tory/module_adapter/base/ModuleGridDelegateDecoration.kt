package com.tory.module_adapter.base

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * - Author: xutao
 * - Date: 7/27/21
 * - Email: xutao@shizhuang-inc.com
 * - Description:
 */
class ModuleGridDelegateDecoration(val moduleAdapter: IModuleAdapter) : RecyclerView.ItemDecoration() {

    private val spaceMap = mutableMapOf<String, MItemSpace>()

    fun copy(moduleAdapter: IModuleAdapter): ModuleGridDelegateDecoration {
        val decoration = ModuleGridDelegateDecoration(moduleAdapter)
        decoration.spaceMap.putAll(spaceMap)
        return decoration
    }

    /**
     * 注册间隔
     */
    fun registerSpace(groupType: String, gridSize: Int, itemSpace: ItemSpace, isDebug: Boolean) {
        val preSpace = spaceMap[groupType]
        if (preSpace != null) {
            if (isDebug && (preSpace.gridSize != gridSize || preSpace.itemSpace != itemSpace)) {
                throw IllegalStateException(
                    "同类型groupType:${groupType}注册的View的gridSize或则itemSpace不同，请检查, " +
                            "gridSize:$gridSize, pre gridSize:${preSpace.gridSize}, " +
                            "itemSpace:$itemSpace, pre itemSpace:${preSpace.gridSize}"
                )
            } else if (preSpace.gridSize != gridSize || preSpace.itemSpace != itemSpace) {
                // 重复的，不用再次注册
                return
            }
        }
        val avgItemOffset = (2 * itemSpace.edgeH + (gridSize - 1) * itemSpace.spaceH) * 1f / gridSize
        spaceMap[groupType] = MItemSpace(gridSize, itemSpace, avgItemOffset)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (view !is IModuleView<*>) {
            return
        }
        val position = parent.getChildAdapterPosition(view) - moduleAdapter.getStartPosition()
        if (position < 0 || position >= moduleAdapter.getItemCount()) {
            return
        }
        val groupType = moduleAdapter.getGroupTypeByPosition(position)
        val itemSpace = spaceMap[groupType] ?: return

        getOffsets(view, outRect, groupType, position, itemSpace)
    }

    private fun getOffsets(
        view: View,
        outRect: Rect, groupType: String, layoutPosition: Int,
        mItemSpace: MItemSpace
    ) {
        val groupPosition = moduleAdapter.getGroupPosition(groupType, layoutPosition)
        if (groupPosition < 0) return
        val gridSize = mItemSpace.gridSize
        val spaceH = mItemSpace.itemSpace.spaceH
        val spaceV = mItemSpace.itemSpace.spaceV
        val edgeH = mItemSpace.itemSpace.edgeH

        val row: Int = groupPosition / gridSize
        val lp = view.layoutParams
        val column: Int = if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.spanIndex
        } else {
            val perSpan = moduleAdapter.getSpanCount() / gridSize
            moduleAdapter.getSpanIndex(layoutPosition) / perSpan
        }
        if (row != 0) {
            outRect.top = spaceV
        }
        // p为每个Item都需要减去的间距
        val p = mItemSpace.avgItemOffset
        val left = edgeH + column * (spaceH - p)
        val right = p - left

        outRect.left = Math.round(left)
        outRect.right = Math.round(right)
    }

    private class MItemSpace(
        val gridSize: Int,
        val itemSpace: ItemSpace,
        val avgItemOffset: Float
    )
}