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

data class ItemOneModel(
    val index: Int
)


class ItemOneView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<ItemOneModel>(context, attrs) {


    val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, 40.dp())
        textView.setBackgroundColor(Color.GREEN)
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
    }

    override fun onChanged(model: ItemOneModel) {
        super.onChanged(model)
        textView.text = model.index.toString() + "-" + groupPosition
    }

}