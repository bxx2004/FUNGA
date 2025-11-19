package cn.revoist.lifephoton.module.aiassistant.core.page

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.core.entity.DocumentMapper
import cn.revoist.lifephoton.module.aiassistant.impl.rag.DocumentTable
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.update
import cn.revoist.lifephoton.module.authentication.pages.sendMessage
import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall
import org.ktorm.dsl.eq
import org.ktorm.dsl.update

/**
 * @author 6hisea
 * @date  2025/11/11 21:14
 * @description: None
 */
@RouteContainer("ai-assistant","document-review")
object DocumentReviewRoute : SqlBase(AIAssistant, DocumentTable.public, DocumentMapper::class.java,true){
    @Route(GET)
    suspend fun review(call: RoutingCall){
        call.checkParameters("docId","status")
        val id = call.request.queryParameters["docId"]!!
        val status = call.request.queryParameters["status"]!!.toBoolean()
        call.validateLogin("ai-assistant.document-review.review") {
            AIAssistant.dataManager.useDatabase()
                .update(DocumentTable.public){
                    set(DocumentTable.public.review,status)
                    where {
                        DocumentTable.public.doc_id eq id
                    }
                }
            ok(true)
            if (status){
                it.sendMessage(1,"[AI Assistant] Public Document ${id}","Your public document upload has been approved","Thank you for your contribution!")
            }
        }
    }
}