package cn.revoist.lifephoton.module.funga.core.info.page

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.info.model.DBInfoBaseEntity
import cn.revoist.lifephoton.module.funga.core.info.service.InfoService
import cn.revoist.lifephoton.module.funga.core.info.table.DBInfoTable
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/11/17 10:57
 * @description: None
 */
@RouteContainer("funga","db-info")
object DBInfoRoute : SqlBase(FungaPlugin, DBInfoTable, DBInfoBaseEntity::class.java,true) {
    @Route(GET)
    suspend fun getAllDatabase(call: RoutingCall){
        call.ok(InfoService.getAllDatabase())
    }
}