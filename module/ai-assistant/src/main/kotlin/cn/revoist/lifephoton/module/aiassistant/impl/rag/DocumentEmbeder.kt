package cn.revoist.lifephoton.module.aiassistant.impl.rag

import cn.revoist.lifephoton.module.aiassistant.core.entity.EmbedDocument
import cn.revoist.lifephoton.module.aiassistant.core.service.DocumentService
import cn.revoist.lifephoton.tools.submit
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment


/**
 * @author 6hisea
 * @date  2025/11/12 16:15
 * @description: None
 */
object DocumentEmbeder {
    private var isStarted = false
    private var isStarted2 = false
    private val parser = ApacheTikaDocumentParser()
    private val splitter = DocumentSplitters.recursive(1600,320)


    fun startPublicDocumentTask(){
        if (isStarted) return
        isStarted = true
        submit(-1,1000){
            val document = DocumentService.nextNotEmbedDocument(true)
            if (document.file == null){
                isStarted = false
                it.cancel()
            }else{
                document.knowledgeBase?.addDocument(loadText(document))
                DocumentService.markEmbed(document.docId,document.uploader,true)
            }
        }
    }


    fun startPrivateDocumentTask(){
        if (isStarted2) return
        isStarted2 = true
        submit(-1,1000){
            val document = DocumentService.nextNotEmbedDocument(false)
            if (document.file == null){
                isStarted2= false
                it.cancel()
            }else{
                document.knowledgeBase?.addDocument(loadText(document))
                DocumentService.markEmbed(document.docId,document.uploader,false)
            }
        }
    }
    fun loadText(ed: EmbedDocument): List<TextSegment>{
        val res = parser.parse(ed.file!!.inputStream())
        res.metadata().put("doc_id",ed.docId).put("uploader",ed.uploader).put("citation",ed.citation)
        return splitter.split(res)
    }

}