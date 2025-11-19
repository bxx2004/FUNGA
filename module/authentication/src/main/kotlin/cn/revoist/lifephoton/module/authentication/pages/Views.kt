package cn.revoist.lifephoton.module.authentication.pages

import cn.revoist.lifephoton.module.authentication.asEntity
import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.module.authentication.getUser
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.module.authentication.username
import cn.revoist.lifephoton.plugin.match
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.*

/**
 * @author 6hisea
 * @date  2025/3/6 12:31
 * @description: None
 */
@RouteContainer("auth","views")
object Views {
    @Route(GET)
    suspend fun isAccessView(call:RoutingCall){
         val id = call.queryParameters["id"]
        call.match {
            isLogin()
        }.then {
            call.ok(getUser().asEntity?.group == "admin" || getUser().asEntity?.permissions?.contains("view.$id") == true || getUser().asEntity?.permissions?.contains("view.*") == true)
        }.default {
            call.ok(true)
        }
    }
    @Route(GET)
    suspend fun getUsername(call:RoutingCall){
        call.checkParameters("id")
        val id = call.queryParameters["id"]!!
        call.ok(id.toLong().username())
    }
}