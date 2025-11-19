package cn.revoist.lifephoton.module.funga.core.genephenotype.model

import dev.langchain4j.model.output.structured.Description

/**
 * @author 6hisea
 * @date  2025/7/11 16:28
 * @description: None
 */
@Description("基因功能排序的结果")
class SortedGeneResult {
    @Description("降序排列的基因名称列表，即第一个是功能最强的基因")
    val genes: List<String> = arrayListOf()
    @Description("降序排列对应的基因排序推理过程")
    val think: List<String> = arrayListOf()
    @Description("对应的参考文献")
    val references = arrayListOf<String>()
}