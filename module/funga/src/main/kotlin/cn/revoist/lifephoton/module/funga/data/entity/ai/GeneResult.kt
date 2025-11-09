package cn.revoist.lifephoton.module.funga.data.entity.ai

import dev.langchain4j.model.output.structured.Description

/**
 * @author 6hisea
 * @date  2025/7/9 16:55
 * @description: None
 */

class GeneResult {
    @Description("每个有关联的表型和其对应的推理过程,没结果不要胡编乱造")
    val result: Map<String, String> = mapOf()
    @Description("对本次分析简短的科研总结")
    val summary : String = ""
}