package cn.revoist.lifephoton

import cn.revoist.lifephoton.ktors.startEngine
import cn.revoist.lifephoton.plugin.Plugin
import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.anno.AutoUse
import cn.revoist.lifephoton.plugin.anno.CreateTable
import cn.revoist.lifephoton.plugin.getPlugin
import cn.revoist.lifephoton.plugin.loadConfig
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.RoutePage
import cn.revoist.lifephoton.plugin.usePlugin
import io.ktor.http.*
import net.axay.simplekotlinmail.delivery.MailerManager
import net.axay.simplekotlinmail.delivery.mailerBuilder
import org.ktorm.database.Database
import org.ktorm.schema.Table
import org.reflections.Reflections
import kotlin.coroutines.Continuation
import kotlin.reflect.jvm.kotlinFunction

/**
 * @path cn.revoist.lifephoton.configure.Booter
 * @author 6hisea
 * @date  2025/1/7 20:08
 * @description: None
 */
object Booster {
    fun main(args: Array<String>){
        startEngine(args)
    }
    var VERSION = "beta-1"
    var DB_URL = "127.0.0.1"
    var DB_NAME = "lifephoton"
    var DB_USERNAME = "root"
    var DB_PASSWORD = "123456"


    val database = lazy { try {
        Database.connect("jdbc:postgresql://${DB_URL}/${DB_NAME}","org.postgresql.Driver",
            DB_USERNAME, DB_PASSWORD)
    }catch (e:Exception){
        e.printStackTrace()
        null
    } }
    fun pluginLoad(){

        val ref = Reflections("cn.revoist.lifephoton")
        //自动使用插件
        for (clazz in ref.getTypesAnnotatedWith(AutoUse::class.java)) {
            clazz.getAnnotation(AutoUse::class.java)?.let {
                val plugin = clazz.getField("INSTANCE").get(null)
                if (plugin is Plugin) usePlugin(plugin)
            }
        }
        //自动注册路由
        for (clazz in ref.getTypesAnnotatedWith(AutoRegister::class.java)) {
            clazz.getAnnotation(AutoRegister::class.java)?.let {
                val obj = clazz.getField("INSTANCE").get(null)
                val plugin = getPlugin(it.pluginId)
                if (obj is RoutePage) plugin?.registerPage(obj)
            }
        }
        //自动注册路由组
        for (clazz in ref.getTypesAnnotatedWith(RouteContainer::class.java)) {
            clazz.getAnnotation(RouteContainer::class.java)?.let { gateway->
                val instance = clazz.getField("INSTANCE").get(null)
                val plugin = getPlugin(gateway.pluginId)
                fun load(cla: Class<*>){
                    cla.declaredMethods.filter {
                        it.kotlinFunction?.annotations?.filterIsInstance<Route>()?.firstOrNull() != null
                    }.forEach {
                        val route = it.kotlinFunction!!.annotations.filterIsInstance<Route>().first()
                        val path = if (route.path == "&empty"){
                            formatConversion(it.name)
                        }else{
                            route.path
                        }
                        plugin?.registerRoute(HttpMethod.parse(route.method.uppercase()),gateway.root + "/" + path ,route.auth,route.inject){
                            it.kotlinFunction!!.call(instance,call, Continuation<Unit>(call.coroutineContext) {})
                        }
                    }
                }
                load(clazz)
                load(clazz.superclass)
            }
        }
        //加载邮件系统
        val config = loadConfig("lifephoton")
        try {
            val mailer = mailerBuilder(config.getProperty("email.url"),config.getProperty("email.port").toInt(),config.getProperty("email.username"),config.getProperty("email.password"))
            MailerManager.defaultMailer = mailer
        }catch (e:Exception){
            println("请设置 EMAIL")
        }

        //自动创建表
        for (clazz in ref.getTypesAnnotatedWith(CreateTable::class.java)) {
            clazz.getAnnotation(CreateTable::class.java)?.let {
                val obj = clazz.getField("INSTANCE").get(null)
                val anno = clazz.kotlin.annotations.filterIsInstance<CreateTable>()[0]
                if (obj is Table<*>){
                    getPlugin(anno.plugin)?.dataManager?.useDatabase(anno.dbName)?.useConnection{
                        it.createStatement().use { statement ->
                            statement.executeUpdate(anno.value.trimIndent())
                        }
                    }
                }
            }
        }
    }
    private fun formatConversion(str:String):String {
        var result = ""
        for (char in str) {
            if (char.isUpperCase()){
                result += "-${char.lowercase()}"
            }else{
                result += char
            }
        }
        return result
    }
}