package cn.revoist.lifephoton.module.funga.core.textannotation.model

/**
 * @author 6hisea
 * @date  2025/11/10 15:29
 * @description: None
 */
class TextAnnotationResult (
    val interactions: List<Interaction>,
    val phenotypes:List<Phenotype>
)
class Interaction(
    val gene1:String,
    val type:String,
    val gene2:String
)
class Phenotype(val gene: String,val phenotype:String)
