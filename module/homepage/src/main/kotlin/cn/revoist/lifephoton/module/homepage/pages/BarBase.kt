package cn.revoist.lifephoton.module.homepage.pages

import cn.revoist.lifephoton.module.homepage.data.entity.BarEntity
import cn.revoist.lifephoton.module.homepage.data.table.BarTable
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBase
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.system.Lifephoton

/**
 * @author 6hisea
 * @date  2025/11/7 18:22
 * @description: None
 */
@RouteContainer("homepage","bar")
object BarBase : SqlBase(Lifephoton, BarTable, BarEntity::class.java,true) {

}