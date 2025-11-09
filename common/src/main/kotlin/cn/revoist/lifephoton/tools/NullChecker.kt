package cn.revoist.lifephoton.tools

import cn.revoist.lifephoton.plugin.properties
import cn.revoist.lifephoton.plugin.route.error
import io.ktor.server.routing.*

/**
 * @author 6hisea
 * @date  2025/3/2 20:46
 * @description: None
 */
suspend inline fun RoutingCall.checkNotNull(vararg datas:Any?){
    if (!datas.all { it != null }){
        error("Parameter not null.")
        return
    }
}
suspend inline fun RoutingCall.checkRequest(requestBody:Any){
    for (property in requestBody.properties()) {
        if (property.get(requestBody) == null){
            error("Parameter ${property.name} must be not null.")
            return
        }
    }
}
fun Any.inferType(): Any {
    if (this is String) {
        if (this.isEmpty()) return this

        // 布尔值
        when (this.lowercase()) {
            "true", "false" -> return this.toBoolean()
        }

        // 数字类型推断
        try {
            val longValue = this.toLong()

            // 根据数值范围选择最合适的类型
            return when {
                longValue in Byte.MIN_VALUE..Byte.MAX_VALUE -> longValue.toByte()
                longValue in Short.MIN_VALUE..Short.MAX_VALUE -> longValue.toShort()
                longValue in Int.MIN_VALUE..Int.MAX_VALUE -> longValue.toInt()
                else -> longValue
            }
        } catch (e: NumberFormatException) {
            // 尝试浮点数
            try {
                val doubleValue = this.toDouble()
                return if (doubleValue % 1 == 0.0) {
                    // 如果是整数形式的浮点数，转换为Long
                    doubleValue.toLong()
                } else {
                    // 根据精度选择Float或Double
                    if (doubleValue in -Float.MAX_VALUE..Float.MAX_VALUE) {
                        doubleValue.toFloat()
                    } else {
                        doubleValue
                    }
                }
            } catch (e: NumberFormatException) {
                // 保持为字符串
            }
        }
    }
    return this
}