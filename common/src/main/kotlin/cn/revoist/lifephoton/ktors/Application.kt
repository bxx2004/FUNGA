package cn.revoist.lifephoton.ktors

import cn.revoist.lifephoton.Booster
import cn.revoist.lifephoton.plugin.getPlugins
import cn.revoist.lifephoton.plugin.initPluginProvider
import cn.revoist.lifephoton.plugin.loadConfig

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*


fun startEngine(args: Array<String>) {
    val config = CommandLineConfig(args)
    val server = EmbeddedServer(config.rootConfig, Netty) {
        takeFrom(config.engineConfig)
        loadConfiguration(config.rootConfig.environment.config)
    }
    initPluginProvider(server)
    server.application.configure()
    val prop = loadConfig("lifephoton")
    Booster.DB_URL = prop.getProperty("database.url","localhost")
    Booster.DB_PASSWORD = prop.getProperty("database.password","123456")
    Booster.DB_USERNAME = prop.getProperty("database.username","postgres")
    Booster.DB_NAME = prop.getProperty("database.name","lifephoton")
    Booster.pluginLoad()
    printInfo()
    server.start(true)
}
private fun printInfo() {
    var info = "\n===== LifePhoton Info =====\n"
    info += "> meta\n"
    info += "  version: ${Booster.VERSION}\n"
    info += "  author: Haixu Liu\n"
    info += "> plugins (count: ${getPlugins().size})\n"
    getPlugins().forEach {
        info += "  ${it.name}[${it.id}] author:${it.author} version:${it.version}\n"
    }
    info += "===== LifePhoton Info =====\n"
    println(info)
}


fun Application.module() {

}



































private fun NettyApplicationEngine.Configuration.loadConfiguration(config: ApplicationConfig) {
    val deploymentConfig = config.config("ktor.deployment")
    loadCommonConfiguration(deploymentConfig)
    deploymentConfig.propertyOrNull("runningLimit")?.getString()?.toInt()?.let {
        runningLimit = it
    }
    deploymentConfig.propertyOrNull("shareWorkGroup")?.getString()?.toBoolean()?.let {
        shareWorkGroup = it
    }
    deploymentConfig.propertyOrNull("responseWriteTimeoutSeconds")?.getString()?.toInt()?.let {
        responseWriteTimeoutSeconds = it
    }
    deploymentConfig.propertyOrNull("requestReadTimeoutSeconds")?.getString()?.toInt()?.let {
        requestReadTimeoutSeconds = it
    }
    deploymentConfig.propertyOrNull("tcpKeepAlive")?.getString()?.toBoolean()?.let {
        tcpKeepAlive = it
    }
    deploymentConfig.propertyOrNull("maxInitialLineLength")?.getString()?.toInt()?.let {
        maxInitialLineLength = it
    }
    deploymentConfig.propertyOrNull("maxHeaderSize")?.getString()?.toInt()?.let {
        maxHeaderSize = it
    }
    deploymentConfig.propertyOrNull("maxChunkSize")?.getString()?.toInt()?.let {
        maxChunkSize = it
    }
}

