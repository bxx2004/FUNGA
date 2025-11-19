package cn.revoist.lifephoton.module.aiassistant.impl.model

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.ID
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.STRING
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/13 17:32
 * @description: None
 */
class ModelBaseEntity : SqlBaseEntity() {
    @BaseType(ID)
    val id = -1L
    @BaseType(STRING)
    lateinit var model_name:String
    @BaseType(STRING)
    lateinit var permission:String
}