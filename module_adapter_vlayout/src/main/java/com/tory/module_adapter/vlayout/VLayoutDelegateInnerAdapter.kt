package com.tory.module_adapter.vlayout

import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.Cantor
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.LayoutHelper
import com.alibaba.android.vlayout.layout.LinearLayoutHelper


abstract class VLayoutDelegateInnerAdapter<Item>
    : DelegateAdapter.Adapter<VLayoutModuleAdapter.CommonViewHolder>() {
    companion object {
        private val TAG = VLayoutDelegateInnerAdapter::class.java.simpleName
    }

    val list: ArrayList<Item> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private var parentAdapter: VLayoutDelegateAdapter? = null

    override fun onCreateLayoutHelper(): LayoutHelper {
        return LinearLayoutHelper()
    }

    open fun setItems(items: List<Item>) {
        clearItems(false)
        appendItems(items)
    }

    fun insertItems(items: List<Item>, startPos: Int) {
        if (startPos < 0 || startPos > list.size) {
            return
        }
        list.addAll(startPos, items)
        notifyItemRangeInserted(startPos, items.size)
    }

    fun clearItems() {
        clearItems(true)
    }

    fun clearItems(notify: Boolean) {
        list.clear()
        if (notify) {
            notifyDataSetChanged()
        }
    }

    fun appendItems(items: List<Item>) {
        if (items.isEmpty()) {
            return
        }
        val positionStart = list.size
        list.addAll(items)
        val itemCount = list.size - positionStart

        if (positionStart == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(positionStart, itemCount)
        }
    }


    fun getItem(position: Int): Item? {
        return list.getOrNull(position)
    }

    fun insertItem(position: Int, item: Item) {
        val p = when {
            position < 0 -> 0
            position > list.size -> list.size
            else -> position
        }
        list.add(p, item)
        notifyItemInserted(p)
    }

    fun autoInsertItems(items: List<Item>) {
        if (list.isEmpty()) {
            setItems(items)
        } else {
            appendItems(items)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun attachParentAdapter(parentAdapter: VLayoutDelegateAdapter) {
        this.parentAdapter = parentAdapter
    }

    fun detachParentAdapter(parentAdapter: VLayoutDelegateAdapter) {
        if (parentAdapter === this.parentAdapter) {
            this.parentAdapter = null
        }
    }

    fun getOffsetPosition(position: Int): Int {
        return if (parentAdapter == null) {
            position
        } else {
            parentAdapter?.findOffsetPosition(position) ?: position
        }
    }

    private fun getParentOffsetPosition(layoutPosition: Int, position: Int): Int {
        return if (parentAdapter == null) {
            position
        } else {
            parentAdapter?.findOffsetPosition(layoutPosition) ?: position
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    fun setMaxRecycledViews(adapterIndex: Int, viewType: Int, max: Int) {
        val realType = Cantor.getCantor(viewType.toLong(), adapterIndex.toLong())
        recyclerView.recycledViewPool.setMaxRecycledViews(realType.toInt(), max)
    }
}
