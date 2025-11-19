package cn.revoist.lifephoton.module.aiassistant.core.service

import cn.revoist.lifephoton.module.aiassistant.AIAssistant
import cn.revoist.lifephoton.module.aiassistant.impl.role.RoleTable
import cn.revoist.lifephoton.module.authentication.data.Tools
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.inList
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * @author 6hisea
 * @date  2025/11/13 12:29
 * @description: None
 */
object RoleServices {
    fun getPrompt(user_id : Long, role_name : String) : String {
        return AIAssistant.dataManager.useDatabase()
            .from(RoleTable)
            .select(RoleTable.prompt)
            .where {
                (RoleTable.role_name eq role_name) and (RoleTable.permission inList Tools.getUserById(user_id)!!.permissions) and (RoleTable.status eq true)
            }.map {
                it.getString(1)
            }.firstOrNull()?:"You are a bioinformatics assistant on the FUNGA platform, and you will be able to accurately infer relevant knowledge of bioinformatics and correct errors."
    }
}