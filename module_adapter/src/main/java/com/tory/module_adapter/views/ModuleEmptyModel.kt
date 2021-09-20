package com.tory.module_adapter.views


import android.graphics.Color
import com.tory.module_adapter.utils.dp


/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/4/7
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/4/7 xutao 1.0
 * Why & What is modified:
 */
interface IEmptyDivider

/**
 * 默认分割线
 */
data class ModuleDividerModel(
    val height: Int = 0.5f.dp(),
    val color: Int = Color.parseColor("#FFF1F1F5"),
    val tag: Any? = null,
    val start: Int = 0,
    val end: Int = 0
) : IEmptyDivider

/**
 * 默认灰色分割块
 */
data class ModuleSpaceModel(
    val height: Int = 8.dp(),
    val tag: Any? = null,
    val start: Int = 0,
    val end: Int = 0
) : IEmptyDivider

/**
 * 自定义颜色的分割块
 */
data class ModuleSeparatorBarModel(
    val height: Int = 8.dp(),
    val tag: Any? = null,
    val color: Int = 0
) : IEmptyDivider

/**
 * 空白分割块
 */
data class ModuleEmptyModel(
    val height: Int = 8.dp(),
    val tag: Any? = null
) : IEmptyDivider


/**
 * 分组间隔，插入到列表后，groupPosition会从0开始
 */
data class ModuleGroupSectionModel(val tag: Any = Any())

/**
 * 数组中插入数据
 */
fun IEmptyDivider.joinTo(list: List<Any>, hasStart: Boolean = false, hasEnd: Boolean = false): List<Any> {
    if (list.isEmpty()) return list
    val result = mutableListOf<Any>()
    val size = list.size
    list.forEachIndexed { index, any ->
        if (hasStart || index != 0) {
            result.add(this)
        }
        result.add(any)
        if (hasEnd && index == size - 1) {
            result.add(this)
        }
    }
    return result
}
