package cn.revoist.lifephoton.module.funga.core.common.model

/**
 * @author 6hisea
 * @date  2025/4/16 12:33
 * @description: None
 */
class SentenceRequest : WithDatabasesRequest(){
    lateinit var sentence:String
    var topK:Int = 20
    var minScore:Double = 0.8
}