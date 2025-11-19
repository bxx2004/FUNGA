package cn.revoist.lifephoton.module.funga.core.phenotype.model

import cn.revoist.lifephoton.module.aiassistant.AIAssistantAPI
import cn.revoist.lifephoton.module.funga.core.common.model.WithDatabasesRequest

/**
 * @author 6hisea
 * @date  2025/4/19 11:54
 * @description: None
 */
class PhenotypeRequest : WithDatabasesRequest(){
    var phenotype:String = ""
    var topK:Int = 20
    var minScore:Double = 0.8
    fun replacePhenotype(){
        if(phenotype.isEmpty()){ return }
        phenotype = AIAssistantAPI.chatModel("qwen-plus").chat("""
        请你用合适的英文语言描述我下面提到的表型，方便向量查询。语义必须相同。
        例如：co2 -> carbon source；热 -> heat
        你直接给我英文结果就行，无需使用箭头
        表型：${phenotype}
        """.trimIndent())
    }
}