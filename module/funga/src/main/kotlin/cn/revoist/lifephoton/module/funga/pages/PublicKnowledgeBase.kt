package cn.revoist.lifephoton.module.funga.pages

import cn.revoist.lifephoton.module.authentication.asEntity
import cn.revoist.lifephoton.module.authentication.getUser
import cn.revoist.lifephoton.module.authentication.isLogin
import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.ai.rag.EmbeddingStores
import cn.revoist.lifephoton.module.funga.data.table.LiteratureTable
import cn.revoist.lifephoton.plugin.match
import cn.revoist.lifephoton.plugin.paging
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall
import org.ktorm.dsl.desc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/11/5 17:27
 * @description: None
 */
@RouteContainer("funga","pkb")
object PublicKnowledgeBase {
    @Route(GET)
    suspend fun upload(call: RoutingCall) {
        call.checkParameters("file","title","citation")
        call.match { isLogin() }
            .then {
                val user = call.getUser().asEntity!!
                val fileId = call.queryParameters["file"]!!
                val title = call.queryParameters["title"]!!
                val citation = call.queryParameters["citation"]!!
                val file = FileManagementAPI.findFileById(fileId)!!
                FungaPlugin.dataManager.useDatabase()
                    .insert(LiteratureTable){
                        set(LiteratureTable.user_id,user.id)
                        set(LiteratureTable.title,title)
                        set(LiteratureTable.citation,citation)
                        set(LiteratureTable.upload_time,System.currentTimeMillis())
                    }
                EmbeddingStores.upload(file,user.id,citation,title)
                call.ok(true)
            }.default {
                error("Please login")
            }
    }
    @Route(GET)
    suspend fun getNews(call: RoutingCall) {
        call.ok(FungaPlugin.dataManager.useDatabase()
            .from(LiteratureTable)
            .select()
            .limit(20)
            .orderBy(LiteratureTable.upload_time.desc())
            .map {
                mapOf(
                    "user_id" to it.getLong("user_id"),
                    "title" to it.getString("title"),
                    "citation" to it.getString("citation"),
                    "upload_time" to it.getLong("upload_time"),
                )
            })
    }
    @Route(GET)
    suspend fun getMyself(call: RoutingCall) {
        call.match { isLogin() }
            .then {
                val user = call.getUser().asEntity!!
                call.paging(FungaPlugin.dataManager,FungaPlugin.dataManager.useDatabase()
                    .from(LiteratureTable)
                    .select()
                    .orderBy(LiteratureTable.upload_time.desc())
                    .where {
                        LiteratureTable.user_id eq user.id
                    }
                    .map {
                        mapOf(
                            "user_id" to it.getLong("user_id"),
                            "title" to it.getString("title"),
                            "citation" to it.getString("citation"),
                            "upload_time" to it.getLong("upload_time"),
                        )
                    })
            }.default {
                error("Please login")
            }
    }
}