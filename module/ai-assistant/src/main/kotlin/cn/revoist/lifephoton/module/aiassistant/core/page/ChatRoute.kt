package cn.revoist.lifephoton.module.aiassistant.core.page

import cn.revoist.lifephoton.module.aiassistant.core.entity.ChatOption
import cn.revoist.lifephoton.module.aiassistant.core.service.ChatServices
import cn.revoist.lifephoton.module.aiassistant.core.service.ChatServices.wrapper
import cn.revoist.lifephoton.module.aiassistant.impl.rag.FixContentInjector.DocumentTransformer
import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.plugin.data.json.jsonObject
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.plugin.requestBody
import cn.revoist.lifephoton.plugin.route.Api
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.POST
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.RoutingCall
import kotlinx.coroutines.CompletableDeferred

/**
 * @author 6hisea
 * @date  2025/11/13 16:31
 * @description: None
 */
@RouteContainer("ai-assistant","chat")
object ChatRoute {
    @Route(GET)
    @Api("清空消息")
    suspend fun clear(call: RoutingCall){
        call.validateLogin { user->
            checkParameters("memoryId")
            val memoryId = request.queryParameters["memoryId"]!!
            ok(ChatServices.clear(user.id,memoryId))
        }
    }
    @Route(GET)
    @Api("删除历史对话")
    suspend fun delete(call: RoutingCall){
        call.validateLogin { user->
            checkParameters("memoryId")
            val memoryId = request.queryParameters["memoryId"]!!
            ok(ChatServices.deleteHistory(user.id,memoryId))
        }
    }
    @Route(GET)
    @Api("获取历史对话")
    suspend fun histories(call: RoutingCall){
        call.validateLogin { user->
            ok(ChatServices.getHistoriesId(user.id))
        }
    }
    @Route(GET)
    @Api("获取历史对话")
    suspend fun history(call: RoutingCall){
        call.validateLogin { user->
            checkParameters("memoryId")
            val memoryId = request.queryParameters["memoryId"]!!
            ok(ChatServices.getHistory(user.id,memoryId))
        }
    }
    @Route(GET)
    @Api("申请新的MemoryID")
    suspend fun apply(call: RoutingCall){
        call.validateLogin { user->
            ok(ChatServices.applyMemory(user.id))
        }
    }
    @Route(POST)
    @Api("对话")
    suspend fun chat(call: RoutingCall){
        call.validateLogin { user->
            val body = requestBody(ChatOption::class.java)
            if (ChatServices.checkPermission(user.id,body)){

                val token = ChatServices.chat(user.id,body)

                val completion = CompletableDeferred<Any>()
                call.respondOutputStream{
                    try {
                        token.onRetrieved {
                            write(
                                gson.toJson(it.map {
                                    val segment = it.textSegment()
                                    val content = segment.text()
                                    val metadata = hashMapOf<String, Any?>()
                                    for (metadataKey in arrayListOf("uploader","citation","doc_id")) {
                                        val metadataValue = segment.metadata().toMap()[metadataKey]
                                        metadata[metadataKey] = metadataValue
                                    }
                                    DocumentTransformer(metadata,content)
                                }).wrapper("@knowledgebase")
                            )
                            flush()
                        }.onPartialThinking {
                            write(it.text().wrapper("@think"))
                            flush()
                        }.onPartialResponse {
                            write(it.wrapper("@chat"))
                            flush()
                        }.beforeToolExecution {
                            write(gson.toJson(
                                jsonObject {
                                    put("name",it.request().name())
                                    //需解析
                                    put("arguments", it.request().arguments())
                                }
                            ).wrapper("@tool-start"))
                            flush()
                        }.onToolExecuted {
                            write(gson.toJson(jsonObject {
                                put("name",it.request().name())
                                //需解析转换
                                put("result",it.result())
                            }).wrapper("@tool-finish"))
                            flush()
                        }.onError {
                            it.printStackTrace()
                            write((it.message?:"error").wrapper("@error"))
                            flush()
                            completion.complete(1)
                            close()
                        }.onCompleteResponse {
                            completion.complete(1)
                            close()
                        }.start()
                        // 等待流完成或出错
                        try {
                            completion.await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            close()
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                        close()
                    }
                }
            }else{
                error("You don't have permission.")
            }
        }
    }
}