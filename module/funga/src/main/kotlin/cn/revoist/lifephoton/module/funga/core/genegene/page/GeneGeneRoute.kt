package cn.revoist.lifephoton.module.funga.core.genegene.page

import cn.revoist.lifephoton.module.funga.core.genegene.services.GeneGeneService
import cn.revoist.lifephoton.module.funga.core.common.model.GeneListWithDatabaseRequest
import cn.revoist.lifephoton.module.funga.core.common.model.GeneWithDatabasesRequest
import cn.revoist.lifephoton.plugin.requestBody
import cn.revoist.lifephoton.plugin.route.POST
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.ok
import cn.revoist.lifephoton.tools.checkNotNull
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/11/15 16:06
 * @description: None
 */
@RouteContainer("funga","gene-gene")
object GeneGeneRoute {
    @Route(POST)
    suspend fun getInteractionsByGeneList(call: RoutingCall){
        val request = call.requestBody(GeneListWithDatabaseRequest::class.java)
        call.checkNotNull(request.genes)
        call.ok(
            GeneGeneService.getInteractionsByGeneList(request)
        )
    }
    @Route(POST)
    suspend fun getInteractionsById(call: RoutingCall){
        val request = call.requestBody(GeneWithDatabasesRequest::class.java)
        call.checkNotNull(request.gene)
        call.ok(
            GeneGeneService.getInteractionsById(request)
        )
    }
}