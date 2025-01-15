package cn.bobasyu.entity

import cn.bobasyu.utils.BaseCodec
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.vertx.core.eventbus.EventBus
import java.io.Serializable
import java.time.LocalDate

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class BangumiCalendarWeekdayEnum(
    @JsonValue
    val id: Int,
    val en: String,
    val cn: String,
    val ja: String
) {
    MONDAY(1, "Mon", "星期一", "月曜日"),
    TUESDAY(2, "Tue", "星期二", "火曜日"),
    WEDNESDAY(3, "Wed", "星期三", "水曜日"),
    THURSDAY(4, "Thu", "星期四", "木曜日"),
    FRIDAY(5, "Fri", "星期五", "金曜日"),
    SATURDAY(6, "Sat", "星期六", "土曜日"),
    SUNDAY(7, "Sum", "星期日", "日曜日")
}

data class BangumiCalendarItem(
    val id: Int,
    val url: String,
    val type: Int,
    val name: String,
    @JsonProperty("name_cn")
    val nameCn: String,
    val summary: String,
    /**
     * 放送星期
     */
    @JsonProperty("air_weekday")
    val airWeekday: BangumiCalendarWeekdayEnum,
    /**
     * 放送开始日期
     */
    @JsonProperty("air_date")
    val airDate: LocalDate,
    val images: BangumiImages
) : Serializable


data class BangumiCalendar(
    val items: List<BangumiCalendarItem>
) : Serializable

fun EventBus.registerBangumiCalendarCodecs(): EventBus = this.apply {
    registerDefaultCodec(BangumiCalendar::class.java, BaseCodec(BangumiCalendar::class.java))
    registerDefaultCodec(BangumiCalendarItem::class.java, BaseCodec(BangumiCalendarItem::class.java))
    registerDefaultCodec(BangumiCalendarWeekdayEnum::class.java, BaseCodec(BangumiCalendarWeekdayEnum::class.java))
}