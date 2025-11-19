package cn.revoist.lifephoton.ktors

import cn.revoist.lifephoton.plugin.data.pool.NormalPage
import cn.revoist.lifephoton.plugin.data.pool.Page
import cn.revoist.lifephoton.plugin.event.events.AuthenticationEvent
import cn.revoist.lifephoton.plugin.event.events.RootPageRequestEvent
import cn.revoist.lifephoton.plugin.getPlugin
import cn.revoist.lifephoton.plugin.property
import cn.revoist.lifephoton.plugin.route.ErrorResponse
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Application.configureRouting() {
    val gson= Gson()
    install(StatusPages) {
        status(HttpStatusCode.NotAcceptable){ call, status->
            call.respond(ErrorResponse(message = status.toString()))
        }
        status(HttpStatusCode.NotFound){call, status->
            call.respond(ErrorResponse(message = status.toString()))
        }
        exception<Throwable> { call, cause ->
            call.respond(ErrorResponse(message = HttpStatusCode.InternalServerError.description+ ": " +cause.message + "\n" + cause.stackTraceToString()))
        }
    }
    fun pageResponse(session:String,number:Int):Page?{
        val plugin = getPlugin(session.split("-")[0])
        val code = session.split("-")[1]
        val response = plugin?.dataManager?.getPage(code,number)
        return response
    }
    routing {
        get("/") {
            val event = RootPageRequestEvent("/", hashMapOf("status" to "ok")).call() as RootPageRequestEvent
            if (!event.isCancelled) {
                call.respond(event.content)
            }
        }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
        get("/page/{session}/{number}"){
            val session = call.parameters["session"]
            val number = try {
                Integer.parseInt(call.parameters["number"]?:"1")
            }catch (e:NumberFormatException){
                1
            }
            if (session.isNullOrEmpty()){
                call.respond(ErrorResponse(message = "session is empty"))
                return@get
            }
            val page = pageResponse(session,number)
            if (page == null){
                call.respond(ErrorResponse(message = "Not page found."))
                return@get
            }
            if (page is NormalPage && page.property("lock") as Boolean){
                val userCookie = call.sessions.get("user") ?: UserSession("-1","-1")
                val event = AuthenticationEvent(userCookie as UserSession,false).call() as AuthenticationEvent
                if (event.truth){
                    call.respond(page.toResponse(number))
                }else{
                    call.respond(
                        ErrorResponse(false,"Please login")
                    )
                }
            }else{
                call.respond(page.toResponse(number))
            }
        }
    }
}
