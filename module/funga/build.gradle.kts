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
    compileOnly(project(":module:file-management"))
    implementation("dev.langchain4j:langchain4j:1.0.0-beta3"){
        exclude("org.apache.pdfbox")
    }
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0-beta3"){
        exclude("org.apache.pdfbox")
    }
    implementation("dev.langchain4j:langchain4j-ollama:1.0.0-beta3"){
        exclude("org.apache.pdfbox")
    }
    implementation("dev.langchain4j:langchain4j-milvus:1.0.0-beta3"){
        exclude("org.apache.pdfbox")
    }
    implementation("dev.langchain4j:langchain4j-easy-rag:1.0.0-beta3"){
        exclude("org.apache.pdfbox")
    }
    implementation("dev.langchain4j:langchain4j-document-parser-apache-pdfbox:1.0.0-beta3")
    implementation("org.apache.pdfbox:pdfbox:2.0.32")
    implementation("io.milvus:milvus-sdk-java:2.5.9")
}

tasks.test {
    useJUnitPlatform()
}