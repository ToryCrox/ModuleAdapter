package com.tory.module_adapter.vlayout

import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.vlayout.DelegateAdapter
import com.alibaba.android.vlayout.VirtualLayoutManager

class VLayoutDelegateAdapter(layoutManager: VirtualLayoutManager?) : DelegateAdapter(layoutManager) {

    private var recyclerView: RecyclerView? = null

    private fun checkIllegalPosition(position: Int): Boolean {
        return position < 0 || position > itemCount - 1
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        val adaptersCount = adaptersCount
        for (i in 0 until adaptersCount) {
            findAdapterByIndex(i).onAttachedToRecyclerView(recyclerView)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        val adaptersCount = adaptersCount
        for (i in 0 until adaptersCount) {
            findAdapterByIndex(i).onDetachedFromRecyclerView(recyclerView)
        }
    }

    override fun addAdapter(adapter: Adapter<*>?) {
        super.addAdapter(adapter)
        bindAdapter(adapter)
    }

    override fun addAdapters(adapters: MutableList<Adapter<RecyclerView.ViewHolder>>?) {
        super.addAdapters(adapters)
        if (adapters?.isNotEmpty() == true) {
            adapters.forEach {
                bindAdapter(it)
            }
        }
    }

    override fun addAdapters(position: Int, adapters: MutableList<Adapter<RecyclerView.ViewHolder>>?) {
        super.addAdapters(position, adapters)
        if (adapters?.isNotEmpty() == true) {
            adapters.forEach {
                bindAdapter(it)
            }
        }
    }

    override fun addAdapter(position: Int, adapter: Adapter<*>?) {
        super.addAdapter(position, adapter)
        bindAdapter(adapter)
    }

    private fun bindAdapter(adapter: Adapter<*>?) {
        if (adapter is VLayoutDelegateInnerAdapter<*>) {
            adapter.attachParentAdapter(this)
        }
    }
}
