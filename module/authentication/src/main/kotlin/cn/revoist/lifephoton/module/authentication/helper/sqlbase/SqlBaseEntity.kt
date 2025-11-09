package cn.revoist.lifephoton.module.authentication.helper.sqlbase

import cn.revoist.lifephoton.plugin.data.mapsWithColumn
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.tools.getProperty
import cn.revoist.lifephoton.tools.properties
import org.ktorm.database.Database
import org.ktorm.dsl.Query
import org.ktorm.dsl.count
import org.ktorm.dsl.delete
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.update
import org.ktorm.dsl.where
import org.ktorm.schema.Table

/**
 * @author 6hisea
 * @date  2025/11/7 17:22
 * @description: None
 */
abstract class SqlBaseEntity {
    val conditions: List<Condition> = arrayListOf()
}
class Condition(){
    lateinit var key: String
    lateinit var type:String
    lateinit var value: Any
}
fun <T>Database.insert(table: Table<*>, entity:T){
    insert(table){
        table.columns.forEach {
            if (it.name != "id"){
                set(it, entity!!.getProperty(it.name))
            }

        }
    }
}
fun <T: SqlBaseEntity>Database.delete(table: Table<*>, entity:T){
    if (entity.conditions.isNotEmpty()){
        delete(table){
            conditions(entity,table)
        }
    }

}
fun <T: SqlBaseEntity>Database.update(table: Table<*>, entity:T){
    if (entity.conditions.isEmpty()){
        return
    }
    update(table){
        entity.properties().forEach {
            if (it != "id" && it != "conditions"){
                set(table[it], entity.getProperty(it))
            }
        }
        where {
            conditions(entity,table)
        }
    }
}
fun <T: SqlBaseEntity>Database.select(table: Table<*>, entity:T,func: Query.()-> Query):List<Map<String,Any?>>{
    if (entity.conditions.isEmpty()){
        return mapsWithColumn(table,*entity.properties().map { table[it] }.toTypedArray()){
            func().limit(20)
        }
    }
    return mapsWithColumn(table,*entity.properties().map { table[it] }.toTypedArray()){
        func(where {
            conditions(entity,table)
        })
    }
}
fun <T: SqlBaseEntity> Database.count(table: Table<*>, entity: T): Long {
    if (entity.conditions.isEmpty()){
        return this.from(table)
            .select(count())
            .map { row ->
                row.getLong(1)  // 使用索引获取计数结果
            }.firstOrNull() ?: 0
    }
    return this.from(table)
        .select(count())
        .where {
            conditions(entity, table)
        }
        .map { row ->
            row.getLong(1)  // 使用索引获取计数结果
        }.firstOrNull() ?: 0
}