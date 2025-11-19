package cn.revoist.lifephoton.module.authentication

import cn.revoist.lifephoton.ktors.UserSession
import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.module.authentication.data.entity.UserDataEntity
import cn.revoist.lifephoton.module.authentication.data.table.UserDataTable
import cn.revoist.lifephoton.plugin.data.count
import cn.revoist.lifephoton.plugin.event.events.AuthenticationEvent
import cn.revoist.lifephoton.plugin.match
import cn.revoist.lifephoton.plugin.route.error
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder
import org.ktorm.dsl.desc
import org.ktorm.dsl.from
import org.ktorm.dsl.gte
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select

/**
 * @author 6hisea
 * @date  2025/3/4 20:28
 * @description: None
 */
suspend fun RoutingCall.isLogin():Boolean{
    val userCookie = sessions.get("user") ?: return false
    val event = AuthenticationEvent(userCookie as UserSession,false).call() as AuthenticationEvent
    return event.truth
}
suspend fun RoutingCall.getUser():UserSession{
    val userCookie = sessions.get("user")
    if (userCookie != null && userCookie is UserSession) {
        return userCookie
    }
    throw RuntimeException("not user")
}
val UserSession.asEntity:UserDataEntity?
    get() = Tools.findUserByToken(accessToken)

private val nameCache = HashMap<Long, String>()

fun Long.username():String?{
    return nameCache.computeIfAbsent(this){
        Tools.getUserById(this)?.username?:"Unknown"
    }
}
fun userSize(): Long{
    return Auth.dataManager.useDatabase().count(UserDataTable){
        UserDataTable.id gte 1
    }
}
fun lastUserId(): Long{
    return Auth.dataManager.useDatabase().from(UserDataTable)
        .select(UserDataTable.id)
        .limit(1)
        .orderBy(UserDataTable.id.desc())
        .map {
            it.getLong(1)
        }.first()
}
suspend fun RoutingCall.validateLogin(permission: String? =null, func: suspend RoutingCall.(UserDataEntity)->Unit){
    match { isLogin() }
        .then {
            val user = getUser().asEntity!!
            if (permission != null) {
                if (user.permissions.contains(permission) || user.group == "admin"){
                    func(this,user)
                }else{
                    error("You dont have permission: $permission")
                }
            }else{
                func(this,user)
            }
        }.default {
            error("Please login")
        }
}
fun Long.asUser(): UserDataEntity?{
    return Tools.getUserById(this)
}