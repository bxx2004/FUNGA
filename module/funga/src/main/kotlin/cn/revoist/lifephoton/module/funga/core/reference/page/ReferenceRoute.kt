package cn.revoist.lifephoton.module.funga.core.reference.page

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.module.funga.FungaPlugin
import cn.revoist.lifephoton.module.funga.core.reference.model.Reference
import cn.revoist.lifephoton.module.funga.core.reference.table.ReferenceTable
import cn.revoist.lifephoton.plugin.route.RouteContainer

/**
 * @author 6hisea
 * @date  2025/11/16 15:32
 * @description: None
 */
@RouteContainer("funga","reference")
object ReferenceRoute : SqlBase(FungaPlugin, ReferenceTable, Reference::class.java,true) {
}