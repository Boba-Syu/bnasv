plugins {
    kotlin("jvm") version "2.0.0"
}

group = "cn.bobasyu"
version = "1.0-SNAPSHOT"
repositories {
    maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public") }
    mavenCentral()
}

val vertxVersion = "4.4.0"
val jacksonVersion = "2.17.2"
val slf4jVersion = "1.5.6"

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.vertx:vertx-core:${vertxVersion}")
    implementation("io.vertx:vertx-lang-kotlin:${vertxVersion}")
    implementation("io.vertx:vertx-web:${vertxVersion}")
    implementation("io.vertx:vertx-auth-jwt:${vertxVersion}")
    implementation("io.vertx:vertx-auth-common:${vertxVersion}")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:${vertxVersion}")
    implementation("io.vertx:vertx-mysql-client:${vertxVersion}")

    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

    implementation("org.slf4j:slf4j-jdk14:${slf4jVersion}")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}