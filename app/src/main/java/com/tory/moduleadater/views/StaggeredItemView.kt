package com.tory.moduleadater.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.updateLayoutParams
import com.tory.module_adapter.utils.dp
import com.tory.module_adapter.views.AbsModuleView


class StaggeredItemModel(val height: Int, val index: Int)

class StaggeredItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<StaggeredItemModel>(context, attrs) {

    val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        textView.setBackgroundColor(Color.CYAN)
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
    }

    override fun onChanged(model: StaggeredItemModel) {
        super.onChanged(model)
        updateLayoutParams {
            height = model.height
        }
        textView.text = model.index.toString()
    }

}