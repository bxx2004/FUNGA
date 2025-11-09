package cn.revoist.lifephoton.tools

import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.route.Api
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.RoutePage
import org.reflections.Reflections
import java.io.File
import kotlin.reflect.jvm.kotlinFunction


object FrontAPIPrinter {
    class FrontAPI(val path:String,val mName:String = path,val methods:List<String>,val api:Api?){
        override fun toString(): String {
            var r = ""
            val header = if (mName.startsWith("get")) {
                ""
            }else{
                ""
            }
            var exp = "/**\n"
            exp += "    @description ${api?.description?:"Null"}\n"
            if (api != null){
                for (param in api.params) {
                    exp += "    @param ${param.name} ${param.description}\n"
                }
            }

            var pa = "("
            if ("\\{.*?}".toRegex().find(path) != null){
                "\\{.*?}".toRegex().findAll(path).forEach {
                    pa += it.value.replace("{","").replace("}","") +":string,"
                    exp += "    @url $pa PathParam\n"
                }
                pa += "params:any)"
                exp += "  */"
                methods.forEach {
                    r+= "  $exp\n"
                    r+= "  async ${header}${wordToCase(mName)}$pa{\n"
                    r+= "    return await requests.${it}WithResponse(`${path.replace("{","\${")}`,params)\n"
                    r+= "  },\n"
                }
            }else{
                exp += "  */"
                methods.forEach {
                    r+= "  $exp\n"
                    r+= "  async ${header}${wordToCase(mName)}(params:any){\n"
                    r+= "    return await requests.${it}WithResponse(\"${path}\",params)\n"
                    r+= "  },\n"
                }
            }
            return r
        }
    }

    val result = HashMap<String,ArrayList<FrontAPI>>()
    fun generate(outPath:String){
        println("生产中···")
        val ref = Reflections("cn.revoist.lifephoton")
        //单页路由查找
        for (clazz in ref.getTypesAnnotatedWith(AutoRegister::class.java)) {
            clazz.getAnnotation(AutoRegister::class.java)?.let {
                val routeApi = clazz.kotlin.annotations.filterIsInstance<Api>().firstOrNull()
                val obj = clazz.getField("INSTANCE").get(null)
                if (obj is RoutePage){
                    val api = FrontAPI(
                        "/" + it.pluginId + "/" + obj.path,
                        clazz.simpleName.lowercase(),
                        obj.methods().map { it.value.lowercase() },
                        routeApi
                    )
                    if (result[it.pluginId] == null){
                        result[it.pluginId] = arrayListOf()
                    }
                    result[it.pluginId]?.add(api)
                }
            }
        }
        //查找容器
        for (clazz in ref.getTypesAnnotatedWith(RouteContainer::class.java)) {
            clazz.getAnnotation(RouteContainer::class.java)?.let { gateway->
                fun load(cla:Class<*>){
                    cla.declaredMethods.filter {
                        it.kotlinFunction?.annotations?.filterIsInstance<Route>()?.firstOrNull() != null
                    }.forEach {
                        val route = it.kotlinFunction!!.annotations.filterIsInstance<Route>().first()
                        val routeApi = it.kotlinFunction!!.annotations.filterIsInstance<Api>().firstOrNull()

                        val path = if (route.path == "&empty"){
                            formatConversion(it.name)
                        }else{
                            route.path
                        }

                        val api = FrontAPI(
                            "/" + gateway.pluginId + "/" + gateway.root + "/" + path,
                            formatConversion(gateway.root + "-" + path),
                            arrayListOf(route.method.lowercase()),routeApi
                        )
                        if (result[gateway.pluginId] == null){
                            result[gateway.pluginId] = arrayListOf()
                        }
                        result[gateway.pluginId]?.add(api)

                    }
                }
                load(clazz)
                load(clazz.superclass)
            }
        }

        result.forEach { t, u ->
            val f= File("$outPath/$t.ts")
            if (!f.exists()) {
                f.createNewFile()
            }
            var r = "import requests from \"../index.ts\";\n"
            r += "export default {\n"
            r += u.joinToString("\n") { it.toString() }
            r += "}\n"
            f.writeText(r)
            println("生产 ${t} 完毕···")
        }
        println("生产完成···")

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
    fun wordToCase(v:String):String{
        var r = ""
        var a = false
        for (c in v) {
            if (c == '-' || c == '/'){
                a = true
                continue
            }else{
                if (a){
                    r += c.uppercase()
                    a = false
                }else{
                    r += c
                }
            }
        }
        return r
    }
}