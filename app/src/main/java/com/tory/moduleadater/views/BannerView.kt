package com.tory.moduleadater.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.tory.module_adapter.views.AbsModuleMView
import com.tory.module_adapter.views.AbsModuleView
import com.tory.moduleadater.R
import com.tory.moduleadater.databinding.LayoutMockBannerViewBinding


data class BannerModel(
    val list: List<String> = emptyList()
)

class BannerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<BannerModel>(context, attrs) {

    override fun getLayoutId(): Int = R.layout.layout_mock_banner_view

}

data class Banner2Model(
    val item: BannerModel
)

class Banner2View @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<BannerModel>(context, attrs) {

    override fun getLayoutId(): Int = R.layout.layout_mock_banner_view

}