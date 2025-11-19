package cn.revoist.lifephoton.module.aiassistant.core.page

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.role.RoleBaseEntity
import cn.revoist.lifephoton.module.aiassistant.impl.role.RoleTable
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.plugin.route.RouteContainer

/**
 * @author 6hisea
 * @date  2025/11/13 12:24
 * @description: None
 */
@RouteContainer("ai-assistant","role")
object RoleRoute : SqlBase(AIAssistant, RoleTable, RoleBaseEntity::class.java,true) {
}