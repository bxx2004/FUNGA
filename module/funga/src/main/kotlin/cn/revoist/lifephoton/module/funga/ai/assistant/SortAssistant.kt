package cn.revoist.lifephoton.module.funga.ai.assistant

import cn.revoist.lifephoton.module.funga.ai.ModelStore
import cn.revoist.lifephoton.module.funga.data.entity.ai.SortedGeneResult
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V

/**
 * @author 6hisea
 * @date  2025/7/11 16:15
 * @description: None
 */
interface SortAssistant {
    @SystemMessage("你是一个基因功能排序的小助手，从大量候选基因中筛选出与表型最可能相关的基因，并按照其关联性、重要性或潜在功能进行排序的过程，结合多种数据源和分析方法，旨在缩小研究范围，帮助研究者聚焦于最有可能的目标基因。")
    @UserMessage("""
        信息如下：
        - 表达相关性：基因在特定组织或条件下的表达水平（如RNA-seq数据）。
        - 功能注释：已知的基因功能（如GO注释、KEGG通路）。
        - 网络分析：基因在蛋白质相互作用（PPI）网络或共表达网络中的位置（如枢纽基因可能更重要）。
        - 遗传变异关联：与疾病相关的突变或单核苷酸多态性（SNP）（如GWAS结果）。
        - 保守性：跨物种的进化保守性可能提示功能重要性。
        - 文献挖掘：已有研究中基因被提及的频率或关联强度。  
        
        
        参考数据如下(无需按部就班摘抄下面的内容，根据生物知识自由发挥。):
        - 基因与对应调控的表型信息: {{gene}}
        - 需要排序的表型: {{phenotype}}
        - 物种: {{db}}
        
        要求：英文回答：给出相关的参考文献；参考文献不要乱生成，没有就不生成。
    """)
    fun sort(@V("gene") gene: Map<String,List<String>>, @V("phenotype") phenotype: String, @V("db") db: String): SortedGeneResult

    companion object{
        val INSTANCE = AiServices.builder(SortAssistant::class.java)
            .chatLanguageModel(ModelStore.deepSeekV3)
            .build()
    }
}