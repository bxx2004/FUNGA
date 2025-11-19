package cn.revoist.lifephoton.module.funga.core.phenotype.model

import cn.revoist.lifephoton.module.funga.core.common.model.WithDatabasesRequest

/**
 * @author 6hisea
 * @date  2025/4/19 11:54
 * @description: None
 */
class OntologyRequest : WithDatabasesRequest(){
    lateinit var ontology:String
    var topK:Int = 20
}