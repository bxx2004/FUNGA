package cn.revoist.lifephoton.module.aiassistant.impl.db

import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.DynamicCreateTable
import cn.revoist.lifephoton.tools.getProperty
import cn.revoist.lifephoton.tools.setProperty
import dev.langchain4j.data.message.ChatMessage
import org.ktorm.schema.Table
import kotlin.use

/**
 * @author 6hisea
 * @date  2025/11/11 10:25
 * @description: None
 */
fun Table<*>.createIfNotExists(plugin: Plugin) : Table<*>{
    val anno = this::class.annotations.filterIsInstance<DynamicCreateTable>().getOrNull(0)
    if (anno == null) {
        throw RuntimeException("Not found annotation: @DynamicCreateTable on ${this.javaClass.name}")
    }
    plugin.dataManager.useDatabase().useConnection{
        it.createStatement().use { statement ->
            statement.executeUpdate(anno.value.trimIndent().replace("{{tableName}}",this.tableName))
        }
    }
    return this
}
fun Long.computeTableIndex(): Int {
    // 每个表存放10000个用户的数据
    val usersPerTable = 2000

    // 计算表索引，从0开始
    // 使用除法运算，ID为1-10000在表0，10001-20000在表1，以此类推
    return ((this - 1) / usersPerTable).toInt()
}
fun ChatMessage.setAttribute(key: String, value: String) {
    val attributes = this.getProperty<Map<String, Any>>("attributes") ?: emptyMap()
    val updatedAttributes = attributes.toMutableMap()
    updatedAttributes[key] = value
    this.setProperty("attributes", updatedAttributes)
}