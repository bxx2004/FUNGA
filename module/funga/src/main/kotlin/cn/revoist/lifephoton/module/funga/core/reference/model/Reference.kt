package cn.revoist.lifephoton.module.funga.core.reference.model

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.ID
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.STRING
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/16 15:40
 * @description: None
 */
class Reference : SqlBaseEntity() {
    @BaseType(ID)
    val id = -1L
    @BaseType(STRING)
    val title = ""
    @BaseType(STRING)
    val summary = ""
    @BaseType(STRING)
    val citation = ""
    @BaseType(STRING)
    val url = ""
}