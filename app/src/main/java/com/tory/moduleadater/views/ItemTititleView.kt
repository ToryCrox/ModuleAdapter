package com.tory.moduleadater.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.AbsModuleView

data class ItemTitleModel(
    val title: String
)


class ItemTitleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<ItemTitleModel>(context, attrs) {


    val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, 40.dp())
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
    }

    override fun onChanged(model: ItemTitleModel) {
        super.onChanged(model)
        textView.text = model.title
    }

}