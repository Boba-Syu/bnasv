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
    testImplementation(kotlin("test"))

    implementation("io.vertx:vertx-core:4.4.0")
    implementation("io.vertx:vertx-lang-kotlin:4.4.0")
    implementation("io.vertx:vertx-web:4.4.0")
    implementation("io.vertx:vertx-auth-jwt:4.4.0")
    implementation("io.vertx:vertx-auth-common:4.4.0")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.4.0")
    implementation("io.vertx:vertx-mysql-client:4.4.0")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    implementation("org.slf4j:slf4j-jdk14:1.5.6")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}