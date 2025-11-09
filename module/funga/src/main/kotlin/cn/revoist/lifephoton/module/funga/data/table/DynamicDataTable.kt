package cn.revoist.lifephoton.module.funga.data.table

import cn.revoist.lifephoton.module.funga.data.entity.inneral.DynamicDataResult
import cn.revoist.lifephoton.module.funga.data.table.type.Source
import cn.revoist.lifephoton.plugin.data.sqltype.obj
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author 6hisea
 * @date  2025/8/27 14:08
 * @description: None
 */

object DynamicDataTable : Table<Nothing>("dynamic_data") {
    val id = long("id").primaryKey()
    val content = varchar("content")
    val source = obj<Source>("source")
    val result = obj<DynamicDataResult>("result")
    val date = long("date")
    val user_id = long("user_id")
}

object DynamicCacheTable : Table<Nothing>("dynamic_cache") {
    val id = long("id").primaryKey()
    val content = varchar("content")
    val source = obj<Source>("source")
    val result = obj<DynamicDataResult>("result")
    val date = long("date")
    val user_id = long("user_id")
}

object BigdataWallet : Table<Nothing>("big_data_wallet") {
    val id = long("id").primaryKey()
    val user_id = long("user_id")
    val point = double("point")
}
object BigdataWalletLog : Table<Nothing>("big_data_wallet_log") {
    val id = long("id").primaryKey()
    val user_id = long("user_id")
    val point = double("point")
    val method = double("method")
    val note = varchar("note")
}