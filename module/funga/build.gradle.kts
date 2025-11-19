plugins {
    kotlin("jvm")
}

group = "cn.revoist.lifephoton.module.funga"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":module:authentication"))
    compileOnly(project(":module:ai-assistant"))
    compileOnly(project(":module:file-management"))
    implementation("io.milvus:milvus-sdk-java:2.5.9")
    compileOnly("dev.langchain4j:langchain4j:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}