package cn.revoist.lifephoton.module.funga.core.tools.page

import cn.revoist.lifephoton.module.funga.core.common.model.GeneListWithDatabaseRequest
import cn.revoist.lifephoton.module.funga.core.tools.service.IDMappingService
import cn.revoist.lifephoton.plugin.requestBody
import cn.revoist.lifephoton.plugin.route.POST
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/11/15 16:14
 * @description: None
 */
@RouteContainer("funga","id-mapping")
object IDMappingRoute {
    @Route(POST)
    suspend fun idMapping(call: RoutingCall){
        val body = call.requestBody(GeneListWithDatabaseRequest::class.java)
        call.ok(IDMappingService.idMapping(body))
    }
}