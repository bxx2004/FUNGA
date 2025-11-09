package cn.revoist.lifephoton.module.funga.data.table

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/4/13 11:50
 * @description: None
 */
object LiteratureTable : Table<Nothing>("literatures") {
    val id = int("id").primaryKey()
    val user_id = long("user_id")
    val title = varchar("title")
    val citation = varchar("citation")
    val upload_time = long("upload_time")
}