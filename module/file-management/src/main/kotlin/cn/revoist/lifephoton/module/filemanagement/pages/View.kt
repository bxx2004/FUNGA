package cn.revoist.lifephoton.module.filemanagement.pages

import cn.revoist.lifephoton.ktors.UserSession
import cn.revoist.lifephoton.module.authentication.data.Tools
import cn.revoist.lifephoton.module.authentication.data.table.hasFriend
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.module.filemanagement.FileManagement
import cn.revoist.lifephoton.module.filemanagement.FileManagementTable
import cn.revoist.lifephoton.plugin.anno.AutoRegister
import cn.revoist.lifephoton.plugin.route.RoutePage
import cn.revoist.lifephoton.plugin.route.error
import io.ktor.http.*
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import java.io.File

/**
 * @author 6hisea
 * @date  2025/1/22 12:30
 * @description: None
 */
@AutoRegister("file-management")
object View : RoutePage("view"){
    override fun methods(): List<HttpMethod> {
        return listOf(HttpMethod.Get)
    }

    override suspend fun onGet(call: RoutingCall) {
        val id = call.queryParameters["id"]
        if (id == null) {
            call.error("id must not be null!")
            return
        }
        val user = if (call.isLogin()){
            Tools.findUserByToken((call.sessions.get("user") as UserSession).accessToken)
        }else{
            null
        }

        val file = FileManagement.dataManager.useDefaultDatabase().sequenceOf(FileManagementTable).find {
            it.file_id eq id
        }
        if (file == null){
            call.error("file not found.")
            return
        }
        if (file.source != "public" || file.user_id != -1L){
            if (user == null){
                call.error("please login!")
                return
            }
            if (user.group == "admin" || user.id == file.user_id || file.user_id.hasFriend(user.id)){
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, file.name)
                        .toString()
                )
                call.respondFile(File(file.path))
            }else{
                call.error("You don't have permission!")
                return
            }
        }else{
            //写入文件名称
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, file.name)
                    .toString()
            )
            call.respondFile(File(file.path))
        }
    }
}