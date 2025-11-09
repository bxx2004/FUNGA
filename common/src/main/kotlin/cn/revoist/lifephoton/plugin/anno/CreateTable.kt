package cn.revoist.lifephoton.plugin.anno

/**
 * @author 6hisea
 * @date  2025/11/7 17:04
 * @description: None
 */
annotation class CreateTable(
    val plugin:String,

    val value :String,
    val dbName:String = "lifephoton",
)
