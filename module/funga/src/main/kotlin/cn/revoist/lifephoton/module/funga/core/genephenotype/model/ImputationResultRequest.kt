package cn.revoist.lifephoton.module.funga.core.genephenotype.model

import cn.revoist.lifephoton.module.funga.core.common.model.WithDatabasesRequest

/**
 * @author 6hisea
 * @date  2025/4/22 14:07
 * @description: None
 */
class ImputationResultRequest : WithDatabasesRequest(){
    lateinit var id:String
    lateinit var type:String
    var genes:List<String> = emptyList()
    var degree:Int = 0
    var prompt:String = ""
}