package cn.revoist.lifephoton.module.funga.core.genephenotype.model

import cn.revoist.lifephoton.module.funga.data.table.type.Source

/**
 * @author 6hisea
 * @date  2025/4/22 18:33
 * @description: None
 */
class AnalysisResult {
    lateinit var input:Input
    lateinit var outer:List<SearchContainer<List<FuncGene>>>
    lateinit var func:List<SearchContainer<List<FuncGene>>>
    lateinit var predict:List<SearchContainer<PredictGene>>
    lateinit var graph:List<SearchContainer<List<InteractionGene>>>
}
class Input{
    lateinit var phenotypes:List<String>
    lateinit var genes:List<String>
    lateinit var type:String
}
class FuncGene{
    lateinit var gene:String
    lateinit var phenotypes:List<PhenotypeReferences>
    lateinit var summary:String
}
class PredictGene{
    lateinit var genes:HashMap<String,Int>
    lateinit var interactions:List<InteractionGene>
}
class InteractionGene{
    lateinit var gene1:String
    lateinit var gene2:String
    lateinit var type:String
    lateinit var source:Source
    lateinit var references:List<String>
}
class SearchContainer<T>{
    lateinit var database:String
    var data:T?=null
}