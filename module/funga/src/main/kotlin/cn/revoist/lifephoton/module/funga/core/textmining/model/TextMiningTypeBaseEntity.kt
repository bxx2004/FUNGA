package cn.revoist.lifephoton.module.funga.core.textmining.model

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.ID
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.MARKDOWN
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.STRING
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/21 15:47
 * @description: None
 */
class TextMiningTypeBaseEntity : SqlBaseEntity(){
    @BaseType(ID)
    val id = -1
    @BaseType(STRING)
    val name = ""
    @BaseType(MARKDOWN)
    val prompt = ""
    @BaseType(MARKDOWN)
    val format = ""
}