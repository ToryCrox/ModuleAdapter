package com.tory.module_adapter.base

import android.view.View
import android.view.ViewGroup

interface IModuleView<T> {
    /**
     * 数据更新
     *
     * @param model
     */
    fun update(model: T)
}

/**
 * 做IModuleView子view使用，作数据分发使用
 */
interface ISubModuleView<T> : IModuleView<T> {
    val parent: IModuleView<T>
}

/**
 * 描述 IModuleView 的生命周期
 */
interface IModuleLifecycle {
    /**
     * 当绑定到 adapter 时
     */
    fun onBind()

    /**
     * 当被回收时
     */
    fun onViewRecycled()
}

/**
 * 监听moduleView的创建和更新进行统一监听
 */
interface IModuleCallback {
    /**
     * view创建好以后
     */
    fun onViewCreated(viewParent: ViewGroup, view: View, viewType: Int)

    /**
     * 绑定数据之前
     */
    fun onBind(view: View, item: Any, position: Int)

    /**
     * 绑定数据之后
     */
    fun onBindAfter(view: View, item: Any, position: Int)
}

open class AbsModuleCallback : IModuleCallback {

    override fun onViewCreated(viewParent: ViewGroup, view: View, viewType: Int) = Unit

    override fun onBind(view: View, item: Any, position: Int) = Unit

    override fun onBindAfter(view: View, item: Any, position: Int) = Unit
}

typealias OnViewItemClick<T> = (model: T, position: Int) -> Unit
