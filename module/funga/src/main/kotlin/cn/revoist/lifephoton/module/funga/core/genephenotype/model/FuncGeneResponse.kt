package cn.revoist.lifephoton.module.funga.core.genephenotype.model

/**
 * @author 6hisea
 * @date  2025/4/20 20:39
 * @description: None
 */
data class FuncGeneResponse(val gene:String,val phenotypes:List<PhenotypeReferences>,val summary:String) {
}