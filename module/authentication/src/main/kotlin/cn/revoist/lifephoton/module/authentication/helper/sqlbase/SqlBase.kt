package cn.revoist.lifephoton.module.authentication.helper.sqlbase

import cn.revoist.lifephoton.module.authentication.asEntity
import cn.revoist.lifephoton.module.authentication.getUser
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.data.pool.DynamicPageInformation
import cn.revoist.lifephoton.plugin.dynamicPaging
import cn.revoist.lifephoton.plugin.match
import cn.revoist.lifephoton.plugin.requestBody
import cn.revoist.lifephoton.plugin.route.Api
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.POST
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.ok
import cn.revoist.lifephoton.plugin.route.sandbox
import cn.revoist.lifephoton.tools.inferType
import io.ktor.server.request.uri
import io.ktor.server.routing.RoutingCall
import org.ktorm.dsl.and
import org.ktorm.dsl.desc
import org.ktorm.dsl.eq
import org.ktorm.dsl.gt
import org.ktorm.dsl.gte
import org.ktorm.dsl.inList
import org.ktorm.dsl.like
import org.ktorm.dsl.limit
import org.ktorm.dsl.lt
import org.ktorm.dsl.lte
import org.ktorm.dsl.notInList
import org.ktorm.dsl.notLike
import org.ktorm.dsl.orderBy
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import kotlin.math.ceil
import kotlin.reflect.jvm.kotlinProperty

/**
 * @author 6hisea
 * @date  2025/11/7 17:12
 * @description: None
 */
abstract class SqlBase(val plugin: Plugin, val table: Table<*>, val entityClass: Class<out SqlBaseEntity>,val selectable:Boolean = false) {
    @Route(GET)
    @Api("请求权限")
    suspend fun permission(call: RoutingCall){
        call.match { isLogin() }
            .then {
                val user = getUser().asEntity!!
                if (user.permissions.contains(request.uri.replace("/",".").replace("/permission","").replaceFirst(".","") + ".*")){
                    ok(listOf("insert","update","delete","select"))
                }else if (user.group == "admin"){
                    ok(listOf("insert","update","delete","select"))
                }else{
                    ok(listOf("insert","update","delete","select").filter {
                        user.permissions.contains(request.uri.replace("/",".").replace("/permission","").replaceFirst(".","") + ".$it")
                    })
                }
            }.default {
                if (selectable) {
                    ok(listOf("select"))
                }else{
                    ok(listOf<String>())
                }
            }
    }
    @Route(GET)
    @Api("查询数据模型")
    suspend fun query(call: RoutingCall) {
        val fields = entityClass.declaredFields
        val res = arrayListOf<BaseModel>()
        fields.forEach {
            val anno = it.kotlinProperty!!.annotations.filterIsInstance<BaseType>().firstOrNull()
            if (anno != null) {
                val attrs = hashMapOf<String, Any>()
                if (anno.min != anno.max && anno.min != -1L && anno.max != -1L && anno.value == "number") {
                    attrs["min"] = anno.min
                    attrs["max"] = anno.max
                }else{
                    attrs["min"] = Long.MIN_VALUE
                    attrs["max"] = Long.MAX_VALUE
                }
                if (anno.value == "radio" || anno.value == "checkbox"){
                    attrs["options"] = anno.options
                    attrs["optionType"] = anno.optionType
                }
                res.add(BaseModel(it.name,anno.value,attrs))
            }else{
                res.add(BaseModel(
                    it.name,
                    it.type.simpleName.lowercase().parseBaseType(),
                    hashMapOf()
                ))
            }
        }
        call.ok(res)
    }
    @Route(POST)
    @Api("插入实体数据")
    suspend fun insert(call: RoutingCall){
        call.validateLogin(call.request.uri.replace("/",".").replace("/permission","").replaceFirst(".","") + ".insert") { user ->
            sandbox {
                val request = call.requestBody(entityClass)
                plugin.dataManager.useDatabase()
                    .insert(table,request)
            }
        }
    }
    @Route(POST)
    @Api("删除实体数据")
    suspend fun delete(call: RoutingCall){
        call.validateLogin(call.request.uri.replace("/",".").replace("/permission","").replaceFirst(".","") + ".delete") { user ->
            sandbox {
                val request = call.requestBody(entityClass)
                plugin.dataManager.useDatabase()
                    .delete(table,request)
            }
        }
    }
    @Route(POST)
    @Api("更新/修改实体数据")
    suspend fun update(call: RoutingCall){
        call.validateLogin(call.request.uri.replace("/",".").replace("/permission","").replaceFirst(".","") + ".update") { user ->
            sandbox {
                val request = call.requestBody(entityClass)
                plugin.dataManager.useDatabase()
                    .update(table,request)
            }
        }
    }
    @Route(POST)
    @Api("查询实体数据")
    suspend fun select(call: RoutingCall){
        val request = call.requestBody(entityClass)
        suspend fun respond(){
            call.dynamicPaging(plugin.dataManager){requestPage, size ->
                val allCount = plugin.dataManager.useDatabase().count(table,request)
                val pages = ceil(allCount / size.toDouble()).toInt()
                return@dynamicPaging if (requestPage <= pages && requestPage >= 1) {
                    val offset = (requestPage - 1) * size
                    val pre = if (requestPage != 1){
                        requestPage-1
                    }else{
                        -1
                    }
                    val next = if (requestPage < pages){
                        requestPage + 1
                    }else{
                        -1
                    }
                    val data = plugin.dataManager.useDatabase()
                        .select(table,request){
                            limit(offset,size)
                            orderBy(table["id"].desc())
                        }
                    DynamicPageInformation(data,pages,pre,next)
                } else {
                    DynamicPageInformation(emptyList(),pages,-1,-1)
                }
            }
        }
        if (selectable) {
            respond()
        }else{
            call.validateLogin(call.request.uri.replace("/",".").replace("/permission","").replaceFirst(".","") + ".select") { user ->
                respond()
            }
        }
    }
}
fun <T: SqlBaseEntity>conditions(body:T, table: Table<*>):ColumnDeclaring<Boolean>{
    val res = arrayListOf<ColumnDeclaring<Boolean>>()
    body.conditions.forEach { condition ->
        when (condition.type) {
            "eq"->{
                res.add((table[condition.key] as Column<Any>) eq condition.value.inferType())
            }
            "gte"->{
                res.add((table[condition.key] as Column<Comparable<Any>>) gte condition.value.inferType() as Comparable<Any>)
            }
            "lte"->{
                res.add((table[condition.key] as Column<Comparable<Any>>) lte condition.value.inferType() as Comparable<Any>)
            }
            "gt"->{
                res.add((table[condition.key] as Column<Comparable<Any>>) gt condition.value.inferType() as Comparable<Any>)
            }
            "lt"->{
                res.add((table[condition.key] as Column<Comparable<Any>>) lt condition.value.inferType() as Comparable<Any>)
            }
            "like"->{
                res.add((table[condition.key] as Column<Any>) like condition.value.inferType().toString())
            }
            "!like"->{
                res.add((table[condition.key] as Column<Any>) notLike condition.value.inferType().toString())
            }
            "in"->{
                res.add((table[condition.key] as Column<Any>) inList condition.value as List<Any>)
            }
            "!in"->{
                res.add((table[condition.key] as Column<Any>) notInList condition.value as List<Any>)
            }
        }
    }
    return res.reduce { a, b -> a and b }
}