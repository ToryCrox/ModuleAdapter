package com.tory.moduleadater

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tory.module_adapter.base.ItemSpace
import com.tory.module_adapter.base.NormalModuleAdapter
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.ModuleEmptyModel
import com.tory.moduleadater.databinding.FragmentStaggeredListBinding
import com.tory.moduleadater.views.BannerModel
import com.tory.moduleadater.views.BannerView
import com.tory.moduleadater.views.StaggeredItemModel
import com.tory.moduleadater.views.StaggeredItemView

class StaggeredListFragment: Fragment() {

    private var _binding: FragmentStaggeredListBinding? = null

    private val binding: FragmentStaggeredListBinding
        get() = _binding!!

    private val listAdapter = NormalModuleAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerViews()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStaggeredListBinding.inflate(inflater, container, false)
        return binding.root
    }


    private fun registerViews() {
        listAdapter.register(itemSpace = ItemSpace(edgeH = 10.dp())) {
            BannerView(it.context)
        }

        listAdapter.register(gridSize = 2,
            itemSpace = ItemSpace(edgeH = 10.dp(), spaceV = 8.dp(), spaceH = 8.dp())) {
            StaggeredItemView(it.context)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = listAdapter
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)

        val list = mutableListOf<Any>()
        list.add(ModuleEmptyModel())
        list.add(BannerModel())

        val heightList = intArrayOf(80.dp(), 90.dp(), 100.dp(), 110.dp())
        list.addAll(MutableList(100) {
            StaggeredItemModel(index = it, height = heightList.random())
        })
        listAdapter.setItems(list)
    }
}