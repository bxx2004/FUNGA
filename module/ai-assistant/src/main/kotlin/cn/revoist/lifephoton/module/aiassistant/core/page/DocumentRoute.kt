package cn.revoist.lifephoton.module.aiassistant.core.page

import cn.revoist.lifephoton.module.aiassistant.core.request.UploadRequest
import cn.revoist.lifephoton.module.aiassistant.core.service.DocumentService
import cn.revoist.lifephoton.module.authentication.data.table.hasFriend
import cn.revoist.lifephoton.module.authentication.validateLogin
import cn.revoist.lifephoton.module.filemanagement.FileManagementAPI
import cn.revoist.lifephoton.plugin.data.json.jsonObject
import cn.revoist.lifephoton.plugin.data.sqltype.gson
import cn.revoist.lifephoton.plugin.pageSize
import cn.revoist.lifephoton.plugin.requestBody
import cn.revoist.lifephoton.plugin.route.Api
import cn.revoist.lifephoton.plugin.route.GET
import cn.revoist.lifephoton.plugin.route.POST
import cn.revoist.lifephoton.plugin.route.Route
import cn.revoist.lifephoton.plugin.route.RouteContainer
import cn.revoist.lifephoton.plugin.route.checkParameters
import cn.revoist.lifephoton.plugin.route.error
import cn.revoist.lifephoton.plugin.route.ok

import cn.revoist.lifephoton.plugin.route.sandbox
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.RoutingCall

/**
 * @author 6hisea
 * @date  2025/11/11 12:49
 * @description: None
 */
@RouteContainer("ai-assistant","document")
object DocumentRoute{

    @Route(GET)
    @Api("搜索文档")
    suspend fun search(call:RoutingCall){
        call.validateLogin {
            checkParameters("keyword")
            val keyword = call.request.queryParameters["keyword"]!!
            ok(DocumentService.search(it.id, keyword,pageSize()))
        }
    }

    @Route(GET)
    @Api("删除文档")
    suspend fun delete(call:RoutingCall){
        call.validateLogin {
            checkParameters("docId")
            val docId = call.request.queryParameters["docId"]!!
            DocumentService.deleteDocument(it.id, docId)
            ok(true)
        }
    }

    @Route(GET)
    @Api("获取统计")
    suspend fun statics(call:RoutingCall){
        call.validateLogin {
            ok(DocumentService.statics(it.id))
        }
    }
    @Route(POST)
    @Api("上传文档")
    suspend fun upload(call: RoutingCall){
        call.validateLogin { user->
            val request = call.requestBody(UploadRequest::class.java)
            val file = FileManagementAPI.findFileById(request.file)
            sandbox {
                return@sandbox if (!request.isZip){
                    DocumentService.uploadDocument(user.id,file!!,request.citation, gson.toJson(jsonObject {
                        put("name",request.sourceName)
                        put("link",request.sourceLink)
                    }),request.isPublic)
                    true
                }else{
                    DocumentService.uploadDocuments(user.id,file!!,request.isPublic)
                    true
                }
            }
        }
    }
    @Route(GET)
    @Api("获取自己上传的文档")
    suspend fun myself(call: RoutingCall){
        call.validateLogin { user ->
            sandbox {
                DocumentService.getDocuments(user.id,pageSize())
            }
        }
    }
    @Route(GET)
    @Api("获公共文档")
    suspend fun public(call: RoutingCall){
        call.validateLogin { user ->
            sandbox {
                DocumentService.getPublicDocuments(pageSize())
            }
        }
    }
    @Route(GET)
    @Api("获取别人分享给自己的文档")
    suspend fun share(call: RoutingCall){
        call.validateLogin { user ->
            sandbox {
                DocumentService.getShareDocuments(user.id,pageSize())
            }
        }
    }
    @Route(GET)
    @Api("通过文档ID阅读原文档")
    suspend fun download(call: RoutingCall){
        call.checkParameters("uploader","docId")
        call.validateLogin { user ->
            val uploader = call.parameters["uploader"]!!.toLong()
            val docId = call.parameters["docId"]!!
            var ba = DocumentService.getFile(user.id,uploader,docId,true)
            if (ba != null){
                call.response.header(HttpHeaders.ContentType, ContentType.Application.Pdf.toString())
                call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"document.pdf\"")
                call.respondBytes {
                    ba
                }
            }else{
                ba = DocumentService.getFile(user.id,uploader,docId,false)
                if (user.id == uploader || uploader.hasFriend(user.id)){
                    if (ba != null){
                        call.response.header(HttpHeaders.ContentType, ContentType.Application.Pdf.toString())
                        call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"document.pdf\"")
                        call.respondBytes {
                            ba
                        }
                    }else{
                        call.error("wait later, try again")
                    }
                }else {
                    error("You don't have permission")
                }
            }
        }
    }
}