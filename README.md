# ModuleAdapter

多类型RecyclerView方案

需要加入jitpack仓库:
```groovy
maven { url 'https://jitpack.io' }
```

依赖引入
```groovy
implementation 'com.github.ToryCrox.ModuleAdapter:module_adapter:0.2.0'
```


## 简单使用
- 创建Adapter，直接使用NormalModuleAdapter即可
private val listAdapter = NormalModuleAdapter()
- 创建Model，可以是任意模型类。但不能是基本类型，Map,List这种集合类
```kotlin
data class ItemOneModel(
    val index: Int
)
```
- 创建自定义View， 注意必须实现IModuleView，并且要与上一步定义的Model进行一一对应，这里可继承已实现IModuleView部分方法的AbsModuleView,。在自定义View专注于实现自己的功能，使组件拆分出来，也利于以后的复用。
```kotlin
class ItemOneView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsModuleView<ItemOneModel>(context, attrs) {


    private val textView = AppCompatTextView(context)

    init {
        addView(textView, LayoutParams.MATCH_PARENT, 40.dp())
        textView.setBackgroundColor(Color.GREEN)
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
    }

    /**
     * Adapter的onBindViewHolder时执行，
     */
    override fun update(model: ItemOneModel) {
        super.update(model)
    }

    /**
     * 数据有变化时执行
     */
    override fun onChanged(model: ItemOneModel) {
        super.onChanged(model)
        textView.text = model.index.toString() + "-" + groupPosition
    }

}
```
- 将自定义View注册到Adapter中
```kotlin
listAdapter.register {
    ItemOneView(it.context)
}
```
- 接口数据返回后，更新Adapter数据就完成了
```kotlin
private fun handleData(list: List<Any>) {
    listAdapter.setItems(list)
}
```

## 进阶使用

### 自定义view(业务组件)
在自定义view之前，我们要先有一个明确的概念，就是我们目前定义的是业务相关的组件，与通用的UI控件widget是不同，我们称之为业务组件，是有明确的业务场景的，所以千万不要轻易复用
1. 自定义View都需要继承IModuleView，但是为了方便大家使用，原则上应该使用AbsModuleView，例如: 
```kotlin
class AddCommentAnonymousView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AbsModuleView<AnonymousModel>(context, attrs, defStyleAttr)
```
2. 继承IModuleView或者AbsModuleView都需要对应的泛型，表示该View所需要的数据模型类型，该类型一定要使用自定义的类型，方便以后修改更新，具有明确的象征意义，例如商详: PmTitleModel(标题模型) -->  PmTitleView，标题组件，这样让我们的数据与组件的对应关系更加明确
3. 不管是IModuleView还是AbsModuleView，它们更新数据的方法都是update方法，只不过在AbsModuleView中，会对数据做一次对比，数据有变化时才调用onChange
```kotlin
override fun update(model: T) {
    val isChanged = data != model
    data = model
    if (isChanged) {
        onChanged(model)
    }
    subModuleViewhHelper?.update(model)
}

```
所以在使用AbsModuleView时要注意一下几点:
> 1. AbsModuleView中会存储有数据对象data，所以不需要再加上一个成员记录数据
> 2. 可以看到，在update中，是对model的内容进行对比，再决定是否调用onChanged的，所以建议将model定义为data class
> 3. 不管是RecyclerView自动回调，还是我们手动更新数据，记得一定要使用update而不是onChanged

4. 在某些情况下，即使我们不使用RecyclerView+ModuleAdapter来实现页面，而是直接将自定义View写到布局中，也建议直接继承AbsModuleView，主要有一下原因:
> - View与Model有明确的对应关系，体现数据驱动的思想
> - 更新数据统一使用update方法，方法比较统一，数据需要变动时修改量较少，不会出现以下更新入参混乱的情况
    
```kotlin
orderCustomerServiceView.render(model.customerServiceProcessItem,
    orderNum,
    model.statusInfo?.statusValue
        ?: 0)
orderShippingView.render(model.trackInfo)

```

### register注册组件
register方法有很多参数，但是默认只有最后一个参数是必须的，例如:
```kotlin
listAdapter.register(
    gridSize = 5,
    poolSize = 10,
    itemSpace = ItemSpace(spaceH = 4.dp(), spaceV = 5.dp(), edgeH = 10.dp())
) {
    ItemOneView(it.context)
}
```


大括号里面的是一个高阶函数，当RecyclerView需要创建一个新的Item的时候会回调，创建一个新的View，有一下几点需要了解和注意的点: 
1. ModuleAdapter是通过创建的IModuleView对应的泛型来进行类型匹配的，对应的泛型必须是确定类型，不能是Int, Long，String这种毫无意义的简单类型，也不能是List, Map这种不确定的类型
2. regisger方法调用必须在adapter设置给RecyclerView之前，否则一些设置会不生效，并被强制crash。
3. register方法里面一定要新创建View，不要将里面的View进行复用，全局变量的引用等操作
4. 同一种类型不允许重复注册，即同一个Modle类型不要对应多个View，如果有遇到这种情况，可以有一下两种方案
> 将Model进行包装成不同类型进行映射，如BannerView和Banner2View都需要使用到BannerModel，但我们可以将它们分别包装成不同的Model使用
```kotlin
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

    override fun getLayoutId(): Int = R.layout.layout_mock_banner2_view

}
```
5. 第二种方式是通过register的modelKey来区分，这个之后再来说明

接下来说一下register的其他参数

### gridSize
gridSize表示一行有几列，如商品流一行有两个，则传2，金刚位一行5个则传6， 默认值为1。
> 注意，使用NormalAdapter时需要自己给RecyclerView设置LayoutManager，需要使用NormalAdapter.getGridLayoutManager(context)

例如: gridSize=5， 那么最终显示的是一行5个
```kotlin
listAdapter.register(
    gridSize = 5,
    poolSize = 10,
    itemSpace = ItemSpace(spaceH = 4.dp(), spaceV = 5.dp(), edgeH = 10.dp())
) {
    ItemOneView(it.context)
}
```

### modelKey
这个主要用来解决同一个Model类型映射不同View的情景

例如： 两种不同类型的View公用同一个模型：
```kotlin
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
```