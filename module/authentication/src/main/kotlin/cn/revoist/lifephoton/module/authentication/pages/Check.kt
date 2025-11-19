package cn.revoist.lifephoton.module.authentication.pages

import cn.revoist.lifephoton.ktors.UserSession
import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.route.RoutePage
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.message
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

/**
 * @author 6hisea
 * @date  2025/1/15 12:36
 * @description: None
 */
@AutoRegister("auth")
object Check : RoutePage("check",false,false){
    override fun methods(): List<HttpMethod> {
        return listOf(HttpMethod.Get)
    }

    override suspend fun onGet(call: RoutingCall) {
        val session = (call.sessions.get("user")?: UserSession("-1","-1")) as UserSession
        if (cn.revoist.lifephoton.module.authentication.data.Tools.checkToken(session)){
            call.sessions.set(session)
            call.message("Login")
        }else{
            call.error("Not login")
        }
    }
}