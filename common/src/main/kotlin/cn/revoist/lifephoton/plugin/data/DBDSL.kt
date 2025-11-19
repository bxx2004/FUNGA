package cn.revoist.lifephoton.plugin.data

import cn.revoist.lifephoton.plugin.data.entity.Map
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.plugin.properties
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import java.sql.ResultSetMetaData
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.kotlinProperty

/**
 * @author 6hisea
 * @date  2025/1/22 19:09
 * @description: None
 */
fun Database.maps(table: Table<*>, vararg columns: Column<*>, func: Query.()-> Query? = {null}):List<HashMap<String,Any?>>{
    val selected = ArrayList<Column<*>>()
    table.columns.forEach {
        if (!columns.map { it.name }.contains(it.name)){
            selected.add(it)
        }
    }
    val re = ArrayList<HashMap<String,Any?>>()
    var a= from(table).select(selected)
    a = func(a)?:a
    a.forEach { row->
        val r = HashMap<String,Any?>()
        selected.forEach {
            r[it.name] = row[it]
        }
        re.add(r)
    }
    return re
}

fun Database.first(table: Table<*>, vararg columns: Column<*>, func: Query.()-> Query? = {null}):HashMap<String,Any?>{
    val selected = ArrayList<Column<*>>()
    table.columns.forEach {
        if (!columns.map { it.name }.contains(it.name)){
            selected.add(it)
        }
    }
    val re = HashMap<String,Any?>()
    var a=  from(table).select(selected).limit(1)
    a = func(a)?:a
    a.forEach { row->
        selected.forEach {
            re[it.name] = row[it]
        }
    }
    return re
}


fun Database.mapsWithColumn(table: Table<*>, vararg columns: Column<*>, func: Query.()-> Query? = {null}):List<HashMap<String,Any?>>{
    val selected = ArrayList<Column<*>>()
    table.columns.forEach {
        if (columns.map { it.name }.contains(it.name)){
            selected.add(it)
        }
    }
    val re = ArrayList<HashMap<String,Any?>>()
    var a=  from(table).select(selected)
    a = func(a)?:a
    a.forEach { row->
        val r = HashMap<String,Any?>()
        selected.forEach {
            r[it.name] = row[it]
        }
        re.add(r)
    }
    return re
}

private fun findLabel(meta:ResultSetMetaData):List<String>{
    return (1..meta.columnCount).map { meta.getColumnLabel(it) }
}

fun Query.toMap(table: Table<*>):List<kotlin.collections.Map<String,Any?>>{
    val re = arrayListOf<kotlin.collections.Map<String,Any?>>()
    map {
        val res = hashMapOf<String,Any?>()
        table.columns.forEach { col ->
            res[col.name] = it[col]
        }
        re.add(res)
    }
    return re
}


fun <T :Table<out Any>,R>Database.bind(table:T,entityZ:Class<R>,func: QuerySource.()->Query):List<R>{
    val query = func(from(table))

    val result = ArrayList<R>()

    query.forEach { row ->
        val entity = entityZ.getConstructor().newInstance()!!
        entity.properties().forEach { property ->
            val mapAnnotation = property.kotlinProperty!!.findAnnotations(Map::class).firstOrNull()?:Map()
            val mapName = if (mapAnnotation.colName == "&empty") property.name else mapAnnotation.colName
            val convert = if (mapAnnotation.convert != "&empty") {
                entity::class.java.getDeclaredMethod(mapAnnotation.convert)
            }else{
                null
            }
            val value = row[table[mapName]]
            if (convert == null) {
                try {
                    property.set(entity, value)
                }catch (e:IllegalArgumentException){
                    property.set(entity, gson.fromJson(gson.toJson(value),property.type))
                }

            }else{
                convert.invoke(entity, value)
            }
        }
        result.add(entity)
    }

    return result
}


fun Database.count(table: Table<*>,func: () -> ColumnDeclaring<Boolean>): Long {
    return this.from(table)
        .select(count())
        .where {
            func()
        }
        .map { row ->
            row.getLong(1)  // 使用索引获取计数结果
        }.firstOrNull() ?: 0
}
