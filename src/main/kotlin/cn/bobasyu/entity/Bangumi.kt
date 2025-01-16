package cn.bobasyu.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import java.io.Serializable
import java.time.LocalDate

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class BangumiSubjectType(
    @JsonValue
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
) : Serializable

data class BangumiSubject(
    val id: Int,

    val type: BangumiSubjectType,

    val name: String = "",

    @JsonProperty("name_cn")
    val nameCn: String = "",

    val summary: String = "",

    val date: LocalDate,

    val platform: String = "",

    val images: BangumiImages = BangumiImages(),

    val infobox: String = "",

    /**
     * 书籍条目的册数
     */
    val volumes: Int? = null,

    /**
     * 数据库中的章节数量
     */
    @JsonProperty("total_episodes")
    val totalEpisodes: Int? = null,
) : Serializable

data class BangumiSearchDto(
    val keyword: String,

    val types: List<BangumiSubjectType>? = null
) : Serializable
