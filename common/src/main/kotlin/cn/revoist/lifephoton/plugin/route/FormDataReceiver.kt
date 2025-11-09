package cn.revoist.lifephoton.plugin.route

import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray

/**
 * @author 6hisea
 * @date  2025/1/10 15:51
 * @description: None
 */
suspend fun RoutingCall.receiveMultiData(limit:Long=-1,func:suspend (data:HashMap<String,Any>) -> Unit){
    val result = HashMap<String,Any>()
    receiveMultipart(limit).forEachPart {
        if (it is PartData.FileItem) {
            result[it.name!!] = it.provider().readRemaining().readByteArray()
        }
        if (it is PartData.FormItem) {
            result[it.name!!] = it.value
        }
    }
    func(result)
}
suspend fun RoutingCall.sandbox(func:suspend ()->Any?){
    try {
        val res = func()
        if (res != null && res != Unit){
            ok(res)
        }else{
            ok("success")
        }

    }catch (e:Exception){
        error("Exception: ${e.message}")
        e.printStackTrace()
    }
}