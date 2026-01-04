package cn.revoist.lifephoton.module.fungipattern.impl.sql

import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/12/9 20:49
 * @description: None
 */
class MetaTable(val name: String) : Table<Nothing>("${name}_meta") {
    val id = long("id").primaryKey()
    val company_id = long("company_id")
    val item_id = long("item_id")
    val indate = long("indate")
    val usedate = long("usedate")
    val wechat_user = varchar("wechat_user")
}