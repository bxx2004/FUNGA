package cn.revoist.lifephoton.module.aiassistant.core.page

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.model.ModelBaseEntity
import cn.revoist.lifephoton.module.aiassistant.impl.model.ModelTable
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.plugin.route.RouteContainer

/**
 * @author 6hisea
 * @date  2025/11/13 17:32
 * @description: None
 */
@RouteContainer("ai-assistant","model")
object ModelRoute : SqlBase(AIAssistant, ModelTable, ModelBaseEntity::class.java,true) {
}