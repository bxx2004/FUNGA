package cn.revoist.lifephoton.module.funga.core.textmining.page

import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.module.funga.core.textmining.service.TextMiningService
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/11/21 15:46
 * @description: None
 */
@RouteContainer("funga","text-mining")
object TextMiningRoute {
    @Route(GET)
    suspend fun push(call: RoutingCall){
        call.checkParameters("file","type")
        call.validateLogin { user->
            val fileId = queryParameters.get("file")!!
            val type = queryParameters.get("type")!!
            val file = FileManagementAPI.findFileById(fileId)!!
            call.ok(TextMiningService.pushTask(file,type,user.id))
        }
    }
    @Route(GET)
    suspend fun ready(call: RoutingCall){
        call.checkParameters("id")
        val id= call.queryParameters["id"]!!
        call.ok(TextMiningService.isReady(id))
    }
    @Route(GET)
    suspend fun get(call: RoutingCall){
        call.checkParameters("id")
        call.validateLogin { user ->
            val id= call.queryParameters["id"]!!
            call.ok(TextMiningService.getResult(id))
        }
    }
    @Route(GET)
    suspend fun history(call: RoutingCall){
        call.checkParameters("id")
        call.validateLogin { user ->
            call.ok(TextMiningService.getHistory20(user.id))
        }
    }
}