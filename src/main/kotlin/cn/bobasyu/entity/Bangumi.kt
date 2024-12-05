package cn.bobasyu.entity

import com.fasterxml.jackson.annotation.JsonAlias
import io.vertx.core.json.JsonObject
import java.time.LocalDate

enum class BangumiSubjectType(
    val code: Int,
    val description: String,
) {
    BOOK(1, "书籍"),
    ANIMATION(2, "动画"),
    MUSIC(3, "音乐"),
    GAME(4, "游戏"),
    OTHERS(6, "其他")
}

data class BangumiImages(
    val large: String = "",
    val common: String = "",
    val medium: String = "",
    val small: String = "",
    val grid: String = "",
)

data class BangumiSubject(
    val id: Int,

    val type: BangumiSubjectType,

    val name: String = "",

    @JsonAlias("name_cn")
    val nameCn: String = "",

    val summary: String,

    val date: LocalDate,

    val platform: String,

    val images: BangumiImages = BangumiImages(),

    val infoBox: JsonObject = JsonObject(),

    /**
     * 书籍条目的册数
     */
    val volumes: Int? = null,
    /**
     * 数据库中的章节数量
     *
     *
     */
    @JsonAlias("total_episodes")
    val totalEpisodes: Int? = null,
)

data class BangumiSearchDto(
    val keyword: String,
    val types: List<BangumiSubjectType>? = null
)
