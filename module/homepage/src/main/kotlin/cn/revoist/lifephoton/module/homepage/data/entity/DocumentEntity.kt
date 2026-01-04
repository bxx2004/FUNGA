package cn.revoist.lifephoton.module.homepage.data.entity

import cn.revoist.lifephoton.module.authentication.helper.sqlbase.BaseType
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.DATE
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.ID
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.LIST
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.MARKDOWN
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.STRING
import cn.revoist.lifephoton.module.authentication.helper.sqlbase.SqlBaseEntity

/**
 * @author 6hisea
 * @date  2025/11/20 11:12
 * @description: None
 */
class DocumentEntity : SqlBaseEntity() {
    @BaseType(ID)
    val id = -1L
    @BaseType(STRING)
    val title = ""
    @BaseType(STRING)
    val doc_id = ""
    @BaseType(DATE)
    val update_time = -1L
    @BaseType(MARKDOWN)
    val content = ""
    @BaseType(LIST)
    val children = listOf<String>()
}