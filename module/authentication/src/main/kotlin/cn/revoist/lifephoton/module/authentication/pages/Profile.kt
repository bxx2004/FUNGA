package cn.revoist.lifephoton.module.authentication.pages

import cn.revoist.lifephoton.ktors.UserSession
import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.data.toPayloadResponse
import cn.revoist.lifephoton.plugin.route.RoutePage
import cn.revoist.lifephoton.plugin.route.error
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

/**
 * @author 6hisea
 * @date  2025/1/9 10:47
 * @description: None
 */
@AutoRegister("auth")
object Profile :RoutePage("profile/{user}",auth = true){
    override fun methods(): List<HttpMethod> {
        return listOf(HttpMethod.Get)
    }

    override suspend fun onGet(call: RoutingCall) {
        val username = call.parameters["user"]
        val ss = (call.sessions.get("user")?: UserSession("-1","-1")) as UserSession
        val user = Tools.findUserByToken(ss.accessToken)
        if (user == null){
            call.error("Please login first")
            return
        }
        if (username.isNullOrEmpty()){
            call.error("Please enter a valid username")
            return
        }
        if (username == "myself"){
            val user = call.sessions.get("user")!! as UserSession
            call.respond(
                Tools.findUserByToken(user.accessToken)!!.toPayloadResponse(
                excludes = arrayListOf("password","accessToken","refreshToken")
            ))
            return
        }else{
            if (user.group != "admin"){
                call.error("Your group isn't the admin")
                return
            }
        }
        val userEntity = Tools.getUserById(username.toLong())?.toPayloadResponse(
             excludes = arrayListOf("password","accessToken","refreshToken")
        )
        if (userEntity == null){
            call.error("user is not exists")
        }else{
            call.respond(userEntity)
        }
    }
}