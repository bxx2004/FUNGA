package cn.revoist.lifephoton.module.authentication.data.table

import cn.revoist.lifephoton.module.authentication.Auth
import cn.revoist.lifephoton.module.authentication.data.entity.UserDataEntity
import cn.revoist.lifephoton.plugin.anno.CreateTable
import org.ktorm.dsl.and
import org.ktorm.dsl.asIterable
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long

/**
 * @author 6hisea
 * @date  2025/8/24 15:49
 * @description: None
 */
@CreateTable("auth", dbName = "auth", value = """
    CREATE TABLE IF NOT EXISTS friend (
    id SERIAL PRIMARY KEY,
    "from" BIGINT NOT NULL,
    "to" BIGINT NOT NULL
    )
""")
object FriendTable : Table<Nothing>("friend") {
    val id = int("id").primaryKey()
    val from = long("from")
    val to = long("to")
}
fun UserDataEntity.hasFriend(id:Long):Boolean{
    return Auth.dataManager.useDatabase().from(FriendTable)
        .select()
        .where {
           (FriendTable.from eq this.id) and (FriendTable.to eq id)
        }.asIterable().toList().isNotEmpty()
}
fun Long.hasFriend(id:Long):Boolean{
    return Auth.dataManager.useDatabase().from(FriendTable)
        .select()
        .where {
            (FriendTable.from eq this) and (FriendTable.to eq id)
        }.asIterable().toList().isNotEmpty()
}
fun Long.whoShareMe():List<Long>{
    return Auth.dataManager.useDatabase().from(FriendTable)
        .select(
            FriendTable.from
        )
        .where {
            FriendTable.to eq this
        }.map {
            it[FriendTable.from]!!
        }.toList()
}