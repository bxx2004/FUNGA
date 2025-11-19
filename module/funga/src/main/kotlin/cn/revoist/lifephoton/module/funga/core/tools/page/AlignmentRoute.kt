package cn.revoist.lifephoton.module.funga.core.tools.page

import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.module.funga.core.info.service.InfoService
import cn.revoist.lifephoton.module.funga.core.tools.service.AlignmentService
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/5/28 20:12
 * @description: None
 */

@RouteContainer("funga","alignment")
object AlignmentRoute {
    @Route(GET)
    suspend fun alignment(call: RoutingCall){
        call.checkParameters("file","type","species","eValue")
        val fileId = call.queryParameters["file"]!!
        val type = call.parameters["type"]!!
        val species = call.parameters["species"]!!
        val eValue = call.parameters["eValue"]!!.toDouble()
        call.ok(AlignmentService.alignment(FileManagementAPI.findFileById(fileId)!!,species,type,eValue))
    }
    @Route(GET)
    suspend fun getAllAlignmentSpecies(call: RoutingCall){
        call.ok(InfoService.getAllDatabase().map { it.name })
    }
    @Route(GET)
    suspend fun isReadyAlignment(call: RoutingCall){
        call.ok(AlignmentService.isReady(call.queryParameters["id"]!!))
    }
    @Route(GET)
    suspend fun getAlignment(call: RoutingCall){
        call.ok(AlignmentService.getAlignment(call.queryParameters["id"]!!))
    }
}