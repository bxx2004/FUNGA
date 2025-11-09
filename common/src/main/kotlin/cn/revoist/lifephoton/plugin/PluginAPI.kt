package cn.revoist.lifephoton.plugin

import cn.revoist.lifephoton.plugin.data.DataManager
import cn.revoist.lifephoton.plugin.data.pool.DynamicPageInformation
import cn.revoist.lifephoton.plugin.data.pool.splitPage
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.io.File
import java.lang.reflect.Field
import java.util.Properties

private val plugins = ArrayList<Plugin>()
private lateinit var initServer: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>
/**
 * @author 6hisea
 * @date  2025/1/11 20:23
 * @description: None
 */
interface PluginAPI {
    val plugin:Plugin

}
fun hasPlugin(id:String):Boolean{
    return plugins.any { it.id == id }
}

fun getPlugin(id:String):Plugin?{
    return plugins.find { it.id == id }
}

fun usePlugin(plugin: Plugin) {
    plugin.setApplication(initServer.application)
    plugin.logger = initServer.application.log
    plugins.add(plugin)
    plugin.load()
}
fun getPlugins():List<Plugin>{
    return plugins
}
fun initPluginProvider(server:EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>){
    initServer = server
}

fun Any.properties():List<Field>{
    var clazz: Class<*>? = this::class.java
    val fields = java.util.ArrayList<Field>()
    while (clazz != null) {
        fields.addAll(clazz.declaredFields)
        clazz = clazz.superclass
    }
    fields.forEach {
        it.isAccessible = true
    }
    return fields
}
fun Any.property(name:String): Field?{
    return this.properties().find { it.name == name }
}
suspend inline fun <T>RoutingCall.requestBody(clazz:Class<T>):T{
    return try {
        gson.fromJson(receiveText(),clazz)
    }catch (e:Exception){
        clazz.getConstructor().newInstance()
        error("Request body is not valid")
    }
}
suspend inline fun RoutingCall.pageSize():Int{
    return (queryParameters["pageSize"]?:"20").toInt()
}
//普通分页必须取消缓存

suspend fun RoutingCall.dynamicPaging(manager: DataManager,dataGenerator:(requestPage:Int,size:Int)-> DynamicPageInformation){
    ok(
        manager.useDynamicPagination(pageSize()){pagination, size ->
            dataGenerator(pagination,size)
        }
    )
}

suspend fun RoutingCall.paging(manager:DataManager,data:List<Any>,lock:Boolean = false,cache:Boolean = true){
    val res = if (cache){
        val id = manager.usePaginationCache(request.uri)
        if (id != null){
            manager.getPage(id,1)?.toResponse(1)
        }else{
            manager.usePagination(data,pageSize(),lock,request.uri)
        }
    }else{
        manager.usePagination(data,pageSize(),lock)
    }
    ok(res)
}

class CheckBuilder(private val validate:suspend RoutingCall.() -> Boolean,private val call:RoutingCall){
    suspend fun then(func: suspend RoutingCall.()->Unit):CheckBuilder{
        if (validate(call)){
            func(call)
        }
        return this
    }
    suspend fun default(func: suspend RoutingCall.()->Unit):CheckBuilder{
        if (!validate(call)){
            func(call)
        }
        return this
    }
}

suspend fun RoutingCall.match(func: suspend RoutingCall.()->Boolean):CheckBuilder{
    return CheckBuilder(func,this)
}

fun loadConfig(id: String): Properties {
    val f = File("/data/LifePhoton/prop/${id}.properties")
    if (!f.parentFile.exists()) {
        f.parentFile.mkdirs()
    }

    val props = Properties()
    if (f.exists()) {
        f.inputStream().use { input ->
            props.load(input)
        }
    } else {
        // 如果文件不存在，创建空文件并返回空Properties
        f.createNewFile()
    }
    return props
}