package com.tory.moduleadater.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tory.module_adapter.base.groupPosition
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.AbsModuleView

data class ItemTwoModel(
    val index: Int,
    val type: ItemType
)

enum class ItemType{
    ONE,
    TWO
}

class ItemTwoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<ItemTwoModel>(context, attrs) {


    val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, 50.dp())
        textView.setBackgroundColor(Color.GRAY)
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
    }

    override fun update(model: ItemTwoModel) {
        super.update(model)
        textView.text = model.index.toString() + "-" + groupPosition
    }

}

class ItemTwoExtraView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<ItemTwoModel>(context, attrs) {

    val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, 50.dp())
        textView.setBackgroundColor(Color.DKGRAY)
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
    }

    override fun update(model: ItemTwoModel) {
        super.update(model)
        textView.text = model.index.toString() + "-" + groupPosition
    }
}