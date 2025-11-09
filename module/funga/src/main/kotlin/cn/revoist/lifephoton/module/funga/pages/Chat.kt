package cn.revoist.lifephoton.module.funga.pages

import cn.revoist.lifephoton.module.authentication.asEntity
import cn.revoist.lifephoton.module.authentication.getUser
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.module.funga.ai.chat.TalkBot
import cn.revoist.lifephoton.module.funga.data.entity.request.ChatRequest
import cn.revoist.lifephoton.plugin.requestBody
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.POST
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.ok
import cn.revoist.lifephoton.tools.submit
import dev.langchain4j.data.message.ChatMessageType
import io.ktor.http.ContentType
import io.ktor.server.response.respondOutputStream
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.RoutingCall
import kotlinx.coroutines.CompletableDeferred
import kotlinx.io.IOException

/**
 * @author 6hisea
 * @date  2025/5/25 12:06
 * @description: None
 */
@RouteContainer("funga","chat")
object Chat {
    @Route(GET)
    suspend fun clear(call: RoutingCall) {
        val userId = if (call.isLogin()){
            call.getUser().asEntity!!.id
        }else{
            if (call.queryParameters["tempId"] == null){
                -1
            }else{
                call.queryParameters["tempId"]!!.toInt()
            }
        }.toInt()
        TalkBot.INSTANCE.evictChatMemory(userId)
        call.ok("ok")
    }
    @Route(GET)
    suspend fun memory(call: RoutingCall){
        val userId = if (call.isLogin()){
            call.getUser().asEntity!!.id
        }else{
            if (call.queryParameters["tempId"] == null){
                -1
            }else{
                call.queryParameters["tempId"]!!.toInt()
            }
        }.toInt()
        val memory = TalkBot.INSTANCE.getChatMemory(userId)
        call.ok(
            if (memory != null){
                memory.messages().filter { it.type() != ChatMessageType.SYSTEM }
            }else{
                arrayListOf()
            }
        )
    }
    @Route(POST)
    suspend fun chat(call: RoutingCall){
        val req = call.requestBody(ChatRequest::class.java)
        val userId = if (call.isLogin()){
            call.getUser().asEntity!!.id
        }else{
            req.tempId
        }.toInt()
        val stream = TalkBot.INSTANCE.chat(userId,req.message)
        var b = false
        call.respondOutputStream {
            val completion = CompletableDeferred<Any>()
            submit(-1,10){
                write("<heart></heart>".encodeToByteArray())
                flush()
                if (b) it.cancel()
            }
            stream.onPartialResponse { res ->
                b =true
                try {
                    write(res.encodeToByteArray())
                    flush()
                } catch (e: IOException) {
                    completion.completeExceptionally(e)
                }
            }.onError {
                completion.complete(it)
            }.onCompleteResponse{
                completion.complete(it)
            }.start()

            // 等待流完成或出错
            try {
                completion.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
