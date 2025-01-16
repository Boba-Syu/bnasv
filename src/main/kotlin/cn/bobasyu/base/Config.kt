package cn.bobasyu.base

import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.toJson
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotations

annotation class ConfigName(
    val value: String
)

/**
 * 应用全局配置项获取
 */
class ApplicationConfig(
    path: String
) {
    private val inputStream: InputStream by lazy {
        this::class.java.classLoader.getResourceAsStream(path)!!
    }

    private val yaml = Yaml()

    private val configMap: HashMap<String, Any> by lazy {
        yaml.loadAs(InputStreamReader(inputStream), HashMap::class.java)
    }

    /**
     * 获取配置
     */
    operator fun <T : Any> get(clazz: KClass<T>): T {
        val configNameList: List<ConfigName> = clazz.findAnnotations(ConfigName::class)
        val key: String = when {
            configNameList.isEmpty() -> clazz.simpleName!!
            else -> configNameList[0].value
        }
        when {
            configMap.containsKey(key) -> return getConfig(key, clazz)
            else -> throw CommonBaseException("config [${key}] not found")
        }
    }

    /**
     * 获取配置
     */
    fun <T : Any> getConfig(key: String, clazz: KClass<T>): T {
        val value = configMap[key]
        return value!!.toJson().parseJson(clazz.java)
    }

}