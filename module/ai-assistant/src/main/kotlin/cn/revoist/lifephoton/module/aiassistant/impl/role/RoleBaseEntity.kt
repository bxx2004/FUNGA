package cn.revoist.lifephoton.module.aiassistant.impl.role

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.*
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/13 12:25
 * @description: None
 */
class RoleBaseEntity : SqlBaseEntity(){
    @BaseType(ID)
    val id = -1L
    @BaseType(STRING)
    var role_name = "default"
    @BaseType(STRING)
    val prompt = "You are a bioinformatics assistant on the FUNGA platform, and you will be able to accurately infer relevant knowledge of bioinformatics and correct errors."
    @BaseType(BOOLEAN)
    val status = true
    @BaseType(STRING)
    var permission = ""
}