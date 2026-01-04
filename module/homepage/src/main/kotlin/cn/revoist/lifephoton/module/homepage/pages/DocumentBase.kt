package cn.revoist.lifephoton.module.homepage.pages

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.module.homepage.data.entity.DocumentEntity
import cn.revoist.lifephoton.module.homepage.data.table.DocumentationTable
import cn.revoist.lifephoton.plugin.data.toMap
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.ok
import cn.revoist.lifephoton.system.Lifephoton
import io.ktor.server.routing.RoutingCall
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/11/7 18:22
 * @description: None
 */
@RouteContainer("homepage","documentation")
object DocumentBase : SqlBase(Lifephoton, DocumentationTable, DocumentEntity::class.java,true) {
    @Route(GET)
    suspend fun catalog(call: RoutingCall){
        call.ok(Lifephoton.dataManager.useDatabase()
            .from(DocumentationTable)
            .select(DocumentationTable.doc_id, DocumentationTable.children,DocumentationTable.title)
            .orderBy(DocumentationTable.doc_id.asc())
            .toMap(DocumentationTable))
    }
    @Route(GET)
    suspend fun content(call: RoutingCall){
        call.checkParameters("doc_id")
        call.ok(Lifephoton.dataManager.useDatabase()
            .from(DocumentationTable)
            .select(DocumentationTable.content, DocumentationTable.update_time)
            .where{
                DocumentationTable.doc_id.eq(call.parameters["doc_id"]!!)
            }
            .toMap(DocumentationTable))
    }
}