plugins {
    kotlin("jvm")
}

group = "cn.revoist.lifephoton.app.tester"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":module:authentication"))
    implementation(project(":module:file-management"))
    //implementation(project(":module:genome"))
    //implementation(project(":module:funga"))
    implementation(project(":module:announcement"))
   // implementation(project(":module:mating-type-imputation"))
}

tasks.test {
    useJUnitPlatform()
}