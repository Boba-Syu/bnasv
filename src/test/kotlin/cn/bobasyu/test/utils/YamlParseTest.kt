package cn.bobasyu.test.utils

import cn.bobasyu.ServerConfig
import cn.bobasyu.base.ApplicationConfig
import io.vertx.junit5.VertxExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.InputStreamReader

@ExtendWith(VertxExtension::class)
class YamlParseTest {

    @Test
    fun parseTest() {
        val configStream: InputStream = this::class.java.classLoader.getResourceAsStream("application.yaml")!!
        configStream.use {
            val yaml = Yaml()
            val serverConfig = yaml.loadAs(InputStreamReader(it), HashMap::class.java)
            println(serverConfig)
        }
    }

    @Test
    fun parseConfigTest() {
        val config = ApplicationConfig("application.yaml")
        val serverConfig = config.getConfig("server", ServerConfig::class)
        println(serverConfig)
    }

    @Test
    fun parseConfigTest2() {
        val config = ApplicationConfig("application.yaml")
        val serverConfig = config[ServerConfig::class]
        println(serverConfig)
    }
}