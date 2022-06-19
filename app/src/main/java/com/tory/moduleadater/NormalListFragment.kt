package com.tory.moduleadater

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tory.module_adapter.base.ItemSpace
import com.tory.module_adapter.base.NormalModuleAdapter
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.ModuleEmptyModel
import com.tory.module_adapter.views.ModuleGroupSectionModel
import com.tory.module_adapter.views.ModuleSeparatorBarModel
import com.tory.moduleadater.databinding.FragmentNormalRecyclerViewBinding
import com.tory.moduleadater.views.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class NormalListFragment : Fragment() {

    private var _binding: FragmentNormalRecyclerViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val listAdapter = NormalModuleAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registerViews()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNormalRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root

    }

    private fun registerViews() {
        listAdapter.register(itemSpace = ItemSpace(edgeH = 10.dp())) {
            BannerView(it.context)
        }
        listAdapter.register(
            gridSize = 5,
            poolSize = 10,
            itemSpace = ItemSpace(spaceH = 4.dp(), spaceV = 5.dp(), edgeH = 10.dp())
        ) {
            ItemOneView(it.context)
        }
        listAdapter.register {
            ItemTitleView(it.context)
        }
        listAdapter.registerModelKeyGetter<ItemTwoModel> { it.type }
        val itemSpace = ItemSpace(spaceH = 4.dp(), spaceV = 5.dp(), edgeH = 10.dp())
        listAdapter.register(
            gridSize = 3, poolSize = 20, groupType = "list", modelKey = ItemType.ONE,
            itemSpace = itemSpace
        ) {
            ItemTwoView(it.context)
        }
        listAdapter.register(
            gridSize = 3, poolSize = 20, groupType = "list", modelKey = ItemType.TWO,
            itemSpace = itemSpace
        ) {
            ItemTwoExtraView(it.context)
        }


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.adapter = listAdapter
        binding.recyclerView.layoutManager = listAdapter.getGridLayoutManager(requireContext())


        val list = mutableListOf<Any>()
        list.add(ModuleEmptyModel(height = 8.dp()))
        list.add(BannerModel())
        list.add(ModuleEmptyModel(height = 4.dp()))
        list.addAll(MutableList(10) { index ->
            ItemOneModel(index)
        })

        list.add(ItemTitleModel("title-1"))
        list.addAll(MutableList(9) {
            ItemTwoModel(1, ItemType.ONE)
        })

        list.add(ModuleGroupSectionModel())
        list.add(ItemTitleModel("title-2"))
        list.addAll(MutableList(12) {
            ItemTwoModel(2, ItemType.ONE)
        })

        list.add(ModuleGroupSectionModel())
        list.add(ItemTitleModel("title-3"))
        list.addAll(MutableList(100) {
            if (it % 8 == 0) {
                ItemTwoModel(3, ItemType.TWO)
            } else {
                ItemTwoModel(3, ItemType.ONE)
            }

        })
        listAdapter.setItems(list)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}