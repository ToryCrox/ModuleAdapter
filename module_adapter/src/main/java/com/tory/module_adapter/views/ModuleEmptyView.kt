package com.tory.module_adapter.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description: 分割线
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
class ModuleDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleView<ModuleDividerModel>(context, attrs, defStyleAttr) {

    private val dividerView = View(context)

    init {
        addView(dividerView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    override fun onChanged(model: ModuleDividerModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = model.height
        }
        updatePaddingRelative(start = model.start, end = model.end)
        dividerView.setBackgroundColor(model.color)
    }
}

/**
 * 分割块，自定义高度和颜色
 */
class ModuleSeparatorBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleMView<ModuleSeparatorBarModel>(context, attrs, defStyleAttr) {

    override fun onChanged(model: ModuleSeparatorBarModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = model.height
        }
        setBackgroundColor(model.color)
    }
}

/**
 * 空白分割块
 */
class ModuleEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleMView<ModuleEmptyModel>(context, attrs, defStyleAttr) {

    override fun onChanged(model: ModuleEmptyModel) {
        super.onChanged(model)
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height = model.height
        }
    }


    override fun onDraw(canvas: Canvas?) = Unit

}


class ModuleGroupSectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AbsModuleMView<ModuleGroupSectionModel>(context, attrs, defStyleAttr) {

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas?) = Unit
}
