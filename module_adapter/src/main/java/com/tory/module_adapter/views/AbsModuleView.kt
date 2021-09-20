package com.tory.module_adapter.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.os.TraceCompat
import com.tory.module_adapter.base.IModuleView
import com.tory.module_adapter.utils.inflate

/**
 * Author: xutao
 * Version V1.0
 * Date: 2019-12-19
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2019-12-19 xutao 1.0
 * Why & What is modified:
 */
abstract class AbsModuleView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IModuleView<T> {

    protected var data: T? = null

    init {
        val layoutId = this.getLayoutId()
        if (layoutId != 0) {
            TraceCompat.beginSection("inflate:${javaClass.simpleName}")
            inflate(layoutId, true)
            TraceCompat.endSection()
        }
    }

    @LayoutRes
    open fun getLayoutId(): Int = 0

    override fun update(model: T) {
        val isChanged = data != model
        data = model
        if (isChanged) {
            onChanged(model)
        }
    }

    open fun onChanged(model: T) {}
}
