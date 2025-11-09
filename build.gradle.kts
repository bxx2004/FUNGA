val kotlin_version: String by project
val kotlinx_html_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.0"
}

group = "cn.revoist"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}
repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "io.ktor.plugin")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    application {
        mainClass.set("cn.revoist.lifephoton.Booster")
        val isDevelopment: Boolean = project.ext.has("development")
        applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
    }
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
    }
    dependencies {
        implementation("io.ktor:ktor-server-core-jvm")
        implementation("io.ktor:ktor-server-auth-jvm")
        implementation("io.ktor:ktor-server-content-negotiation-jvm")
        implementation("io.ktor:ktor-serialization-gson-jvm")
        implementation("io.ktor:ktor-server-sessions-jvm")
        implementation("io.ktor:ktor-server-host-common-jvm")
        implementation("io.ktor:ktor-server-status-pages-jvm")
        implementation("io.ktor:ktor-server-cors-jvm")
        implementation("io.ktor:ktor-server-html-builder-jvm")
        implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")
        implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.129-kotlin-1.4.20")
        implementation("io.ktor:ktor-server-netty-jvm")
        implementation("ch.qos.logback:logback-classic:$logback_version")
        implementation("io.ktor:ktor-server-config-yaml-jvm")
        implementation("org.ktorm:ktorm-core:4.1.1")
        implementation("org.ktorm:ktorm-support-postgresql:4.1.1")
        implementation("org.reflections:reflections:0.10.2")
        implementation("org.postgresql:postgresql:42.7.3")
        implementation("net.axay:simplekotlinmail-core:1.4.0")
        implementation("net.axay:simplekotlinmail-client:1.4.0")
        implementation("net.axay:simplekotlinmail-html:1.4.0")

        testImplementation("io.ktor:ktor-server-test-host-jvm")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    }
}
dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-gson-jvm")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-html-builder-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$kotlinx_html_version")
    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.129-kotlin-1.4.20")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    implementation ("org.ktorm:ktorm-core:4.1.1")
    implementation("org.ktorm:ktorm-support-postgresql:4.1.1")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("net.axay:simplekotlinmail-core:1.4.0")
    implementation("net.axay:simplekotlinmail-client:1.4.0")
    implementation("net.axay:simplekotlinmail-html:1.4.0")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
