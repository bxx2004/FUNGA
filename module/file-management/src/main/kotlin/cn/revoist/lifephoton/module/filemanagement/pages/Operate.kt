package cn.revoist.lifephoton.module.filemanagement.pages

import cn.revoist.lifephoton.ktors.UserSession
import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.module.filemanagement.FileManagement
import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.module.filemanagement.FileManagementTable
import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.route.RoutePage
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.http.HttpMethod
import io.ktor.server.routing.RoutingCall
import io.ktor.server.sessions.sessions
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.update
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf

/**
 * @author 6hisea
 * @date  2025/7/28 19:37
 * @description: None
 */
@AutoRegister("file-management")
object Operate : RoutePage("operate") {
    override fun methods(): List<HttpMethod> {
        return listOf(HttpMethod.Get)
    }

    override suspend fun onGet(call: RoutingCall) {
        val id = call.queryParameters["id"]
        val type = call.queryParameters["type"]
        if (id == null) {
            call.error("id must not be null!")
            return
        }
        if (type == null) {
            call.error("type must not be null!")
            return
        }
        val user = if (call.isLogin()){
            Tools.findUserByToken((call.sessions.get("user") as UserSession).accessToken)
        }else{
            null
        }
        if (user == null) {
            call.error("please login!")
            return
        }
        val file = FileManagement.dataManager.useDefaultDatabase().sequenceOf(FileManagementTable).find {
            it.file_id eq id
        }
        if (file == null){
            call.error("file not found.")
            return
        }
        if (file.user_id != user.id){
            call.error("You don't have permission.")
            return
        }
        when (type) {
            "delete" -> {
                FileManagement.dataManager.useDefaultDatabase()
                    .delete(FileManagementTable){
                        (it.file_id eq id) and (it.user_id eq user.id)
                    }
                call.ok("delete success.")
            }
        }
    }
}