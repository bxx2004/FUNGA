package cn.revoist.lifephoton.module.funga.core.genephenotype.model

import cn.revoist.lifephoton.module.funga.core.common.model.WithDatabasesRequest

/**
 * @author 6hisea
 * @date  2025/4/13 19:48
 * @description: None
 */
class ImputationPredictGenesRequest : WithDatabasesRequest() {
    lateinit var genes:List<String>
    lateinit var geneList:List<String>
    var degree = 1
}