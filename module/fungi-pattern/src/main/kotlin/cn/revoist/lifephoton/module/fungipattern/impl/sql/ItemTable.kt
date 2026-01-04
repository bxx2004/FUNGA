package cn.revoist.lifephoton.module.fungipattern.impl.sql

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/12/9 20:58
 * @description: None
 */
object ItemTable : Table<Nothing>("item") {
    val id = long("id").primaryKey()
    val name = varchar("name")
    val description = varchar("description")
    val company_id = long("company_id")
}