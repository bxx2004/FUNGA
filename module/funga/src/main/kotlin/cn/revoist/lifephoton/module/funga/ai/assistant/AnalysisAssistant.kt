package cn.revoist.lifephoton.module.funga.ai.assistant

import cn.revoist.lifephoton.module.funga.ai.ModelStore
import cn.revoist.lifephoton.module.funga.data.entity.ai.GeneResult
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V

/**
 * @author 6hisea
 * @date  2025/4/20 15:01
 * @description: None
 */
interface AnalysisAssistant {
    @UserMessage("""
角色定义
- 你是一个以表型为导向的基因挖掘专家，擅长基于生物数据库和文献证据判断表型描述的关联性。你的任务是根据关联规则分析两组表型术语的关系。
任务定义
- 你应该逐项分析第一组表型为x，然后再逐项分析第二组表型为y，如果x与y关联，则输出y
- 关联规则极其重要，请仔细推敲。
- 不满足关联规则请你不要主观判断


关联规则(满足任意一条即可)
- y的产生导致x的产生，如死亡必定导致生长停滞
- y是x的上游部分，并x是唯一导致y产生的条件时
- x与y是公认的、在权威数据库或文献中可互换使用的完全同义词。
- x与y在同一细胞结构发挥功能，且y必须是导致x的原因

输出规则
- 必须全部的x都能够被y关联，否则输出无
- 按照第二组原始表述输出
- 无关联返回无即可
- [!]严格遵守规则，禁止主观推测。仅根据已知的、明确的数据库或经典文献中的直接因果关系或包含关系进行判断。如果找不到直接、明确的证据证明y导致x，则视为无关联。禁止进行功能性的、机制性的推测。
- 输出的推理过程无需举例和参考文献，使用[英文]输出。[不用提到无关的表型][不要加特殊符号]，只需要输出关注的表型推断过程，使用SCI科研论文方式简短描述。
- 无关联的不用输出结果和推理
当前任务
第一组：{{inputPhenotype}}
第二组：{{dbPhenotype}}
    """)
    fun isPhenotypeRelatedAll(@V("inputPhenotype") inputPhenotype:List<String>, @V("dbPhenotype") dbPhenotype:List<String>):GeneResult
    @UserMessage("""
角色定义
- 你是一个以表型为导向的基因挖掘专家，擅长基于生物数据库和文献证据判断表型描述的相同性。你的任务是根据关联规则分析两组表型术语的关系。

任务定义
- 你应该依次遍历第一组表型for x in one，然后再依次遍历第二组表型为for y in two，如果x与y关联，则输出y
- 你不可以通过通路分析,不可以通过因果关系分析
- 禁止主观推测，所有的结果必须有文献支持

关联规则(满足任意一条即可)(必须有直接的证据表明)
- x与y是公认的、在权威数据库或文献中可互换使用的完全同义词。
- y是x的上游表型，并有且仅有y才能影响x,x不能导致y产生
- x与y具有显著的生物学关联，必须极其显著

输出规则
- !"禁止输出无关的表型极其推理过程"
- 按照第二组原始表述输出
- 禁止进行功能性的、机制性的推测。
- 输出的推理过程无需举例和参考文献，使用[英文]输出。[不用提到无关的表型][不要加特殊符号]，只需要输出关注的表型推断过程，使用SCI科研论文方式简短描述。

当前任务
第一组：{{inputPhenotype}}
第二组：{{dbPhenotype}}
    """)
    fun isPhenotypeRelatedAny(@V("inputPhenotype") inputPhenotype:List<String>, @V("dbPhenotype") dbPhenotype:List<String>): GeneResult

    companion object{
        val INSTANCE = AiServices.builder(AnalysisAssistant::class.java)
            .chatLanguageModel(ModelStore.qwenMax)
            .build()
    }
}
