package cn.revoist.lifephoton.module.fungipattern.impl.sql

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/12/9 20:58
 * @description: None
 */
object CompanyTable : Table<Nothing>("company") {
    val id = long("id").primaryKey()
    val name = varchar("name")
    val description = varchar("description")
    val website = varchar("website")
    val phone_number = varchar("phone_number")
}