package cn.revoist.lifephoton.module.funga.core.info.page

import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.info.service.InfoService
import cn.revoist.lifephoton.module.funga.data.table.GenePhenotypeAnalysisTable
import cn.revoist.lifephoton.plugin.data.toMap
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.ok
import io.ktor.server.routing.*
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/4/13 13:42
 * @description: None
 */
@RouteContainer("funga","info")
object InfoRoute {

    @Route(GET)
    suspend fun allAnalysisSummary(call: RoutingCall){
        call.validateLogin { user ->
            ok(InfoService.getAllAnalysisSummary(user.id))
        }
    }
}