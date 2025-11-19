package cn.revoist.lifephoton.module.authentication.helper.sqlbase

/**
 * @author 6hisea
 * @date  2025/11/9 19:59
 * @description: None
 */
annotation class BaseType(
    val value:String,
    val max: Long = -1,
    val min: Long = -1,
    val options: Array<String> = [],
    val optionType: String = "string"
)
const val MARKDOWN="markdown"
const val DATE="date"
const val NUMBER="number"
const val BOOLEAN="boolean"
const val STRING="string"
const val RADIO="radio"
const val CHECKBOX="checkbox"
const val OPTION="option"
const val LIST="list"
const val ID="id"
const val FILE= "file"
data class BaseModel(
    val name: String,
    val type: String,
    val attributes: Map<String, Any>
)
fun String.parseBaseType():String{
    return when(this){
        "byte","short","int","long","float","double"-> "number"
        else -> this
    }
}