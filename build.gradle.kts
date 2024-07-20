plugins {
    kotlin("jvm") version "2.0.0"
}

group = "cn.bobasyu"
version = "1.0-SNAPSHOT"

repositories {
    maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public") }
    mavenCentral()
}

dependencies {
    implementation("io.vertx:vertx-core:4.5.3")
    implementation("io.vertx:vertx-lang-kotlin:4.5.3")
    implementation("io.vertx:vertx-web:4.5.3")
    implementation("io.vertx:vertx-auth-jwt:4.5.3")
    implementation("io.vertx:vertx-auth-common:4.5.3")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.5.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}