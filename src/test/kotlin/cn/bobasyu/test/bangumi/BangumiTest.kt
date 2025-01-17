package cn.bobasyu.test.bangumi;

import cn.bobasyu.bangumi.BangumiRepository
import cn.bobasyu.base.ApplicationContext
import cn.bobasyu.bangumi.entity.BangumiSearchDto
import cn.bobasyu.bangumi.entity.BangumiSubject
import cn.bobasyu.bangumi.entity.BangumiSubjectType
import cn.bobasyu.utils.parseJson
import cn.bobasyu.utils.parseJsonToList
import cn.bobasyu.utils.toJson
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(VertxExtension::class)
class BangumiTest {

    fun applicationText(vertx: Vertx): ApplicationContext = ApplicationContext(vertx)

    @Test
    fun searchTest(vertx: Vertx, testContext: VertxTestContext) {
        val bangumiRepository = BangumiRepository(applicationText(vertx))
        val bangumiSearchDto = BangumiSearchDto(keyword = "86", types = listOf(BangumiSubjectType.ANIMATION))
        val resp = bangumiRepository.searchByKeyword(bangumiSearchDto)
        println(resp)
        testContext.completeNow()
    }

    @Test
    fun searchByIdTest(vertx: Vertx, textContext: VertxTestContext) {
        val bangumiRepository = BangumiRepository(applicationText(vertx))
        val resp = bangumiRepository.searchById(331887)
        println(resp)
        textContext.completeNow()
    }

    @Test
    fun calender(vertx: Vertx, testContext: VertxTestContext) {
        val bangumiRepository = BangumiRepository(applicationText(vertx))
        val resp = bangumiRepository.calendar()
        println(resp)
        testContext.completeNow()
    }

    @Test
    fun jsonTest() {
        val bangumiSubject = BangumiSubject(
            id = 0,
            type = BangumiSubjectType.ANIMATION,
            date = LocalDate.now(),
        )
        print(bangumiSubject.toJson())
    }

    @Test
    fun jsonTest2() {
        val bangumiSubject = BangumiSubject(
            id = 0,
            type = BangumiSubjectType.ANIMATION,
            date = LocalDate.now(),
        )
        val json = """
            {"id":0,"type":2,"name":"","name_cn":"","summary":"","date":"2024-12-05","platform":"","images":{"large":"","common":"","medium":"","small":"","grid":""},"infoBox":"","volumes":null,"total_episodes":null}
        """.trimIndent()
        assertEquals(bangumiSubject, json.parseJson(BangumiSubject::class.java))
    }

    @Test
    fun jsonTest3() {
        val json = """
            [ {
              "date" : "2021-10-02",
              "platform" : "TV",
              "images" : {
                "small" : "https://lain.bgm.tv/r/200/pic/cover/l/85/0e/331887_rZdb9.jpg",
                "grid" : "https://lain.bgm.tv/r/100/pic/cover/l/85/0e/331887_rZdb9.jpg",
                "large" : "https://lain.bgm.tv/pic/cover/l/85/0e/331887_rZdb9.jpg",
                "medium" : "https://lain.bgm.tv/r/800/pic/cover/l/85/0e/331887_rZdb9.jpg",
                "common" : "https://lain.bgm.tv/r/400/pic/cover/l/85/0e/331887_rZdb9.jpg"
              },
              "image" : "https://lain.bgm.tv/pic/cover/l/85/0e/331887_rZdb9.jpg",
              "summary" : "東部戦線第一戦区第一防衛戦隊、通称スピアヘッド戦隊。\r\nサンマグノリア共和国から“排除”された〈エイティシックス〉の少年少女たちで構成された彼らは、ギア－デ帝国が投入した無人兵器〈レギオン〉との過酷な戦いに身を投じていた。\r\nそして次々と数を減らしていくスピアヘッド戦隊に課せられた、成功率 0％、任務期間無制限の「特別偵察任務」。それは母国からの実質上の死刑宣告であったが、\r\nリーダーのシンエイ・ノウゼン、ライデン・シュガ、セオト・リッカ、アンジュ・エマ、クレナ・ククミラの 5 人は、それでも前に進み続けること、戦い続けることを選択する。\r\n希望や未来を追い求めようとしたわけではない。\r\n“戦場”が、彼らにとって唯一の居場所となっていたのだから。\r\nそしてその願いは皮肉にも、知らぬ間に足を踏み入れた新天地で叶うことになるのだった。",
              "name" : "86―エイティシックス― 第2クール",
              "name_cn" : "86 -不存在的战区- 第2部分",
              "tags" : [ {
                "name" : "A-1Pictures",
                "count" : 2555,
                "total_cont" : 0
              }, {
                "name" : "轻小说改",
                "count" : 2136,
                "total_cont" : 0
              }, {
                "name" : "科幻",
                "count" : 1646,
                "total_cont" : 0
              }, {
                "name" : "战争",
                "count" : 1479,
                "total_cont" : 0
              }, {
                "name" : "2021年10月",
                "count" : 1423,
                "total_cont" : 0
              }, {
                "name" : "泽野弘之",
                "count" : 1329,
                "total_cont" : 0
              }, {
                "name" : "机战",
                "count" : 1203,
                "total_cont" : 0
              }, {
                "name" : "战斗",
                "count" : 1011,
                "total_cont" : 0
              }, {
                "name" : "TV",
                "count" : 919,
                "total_cont" : 0
              }, {
                "name" : "轻改",
                "count" : 667,
                "total_cont" : 0
              }, {
                "name" : "恋爱",
                "count" : 665,
                "total_cont" : 0
              }, {
                "name" : "2021",
                "count" : 543,
                "total_cont" : 0
              }, {
                "name" : "石井俊匡",
                "count" : 285,
                "total_cont" : 0
              }, {
                "name" : "-86-eightsix-",
                "count" : 260,
                "total_cont" : 0
              }, {
                "name" : "小说改",
                "count" : 178,
                "total_cont" : 0
              }, {
                "name" : "续作",
                "count" : 41,
                "total_cont" : 0
              }, {
                "name" : "热血",
                "count" : 36,
                "total_cont" : 0
              }, {
                "name" : "86",
                "count" : 33,
                "total_cont" : 0
              }, {
                "name" : "神作",
                "count" : 31,
                "total_cont" : 0
              }, {
                "name" : "催泪",
                "count" : 28,
                "total_cont" : 0
              }, {
                "name" : "治愈",
                "count" : 24,
                "total_cont" : 0
              }, {
                "name" : "萝卜",
                "count" : 23,
                "total_cont" : 0
              }, {
                "name" : "2021年",
                "count" : 22,
                "total_cont" : 0
              }, {
                "name" : "日本",
                "count" : 22,
                "total_cont" : 0
              }, {
                "name" : "大野敏哉",
                "count" : 19,
                "total_cont" : 0
              }, {
                "name" : "网恋奔现",
                "count" : 16,
                "total_cont" : 0
              }, {
                "name" : "2022",
                "count" : 13,
                "total_cont" : 0
              }, {
                "name" : "机甲",
                "count" : 10,
                "total_cont" : 0
              }, {
                "name" : "奇幻",
                "count" : 10,
                "total_cont" : 0
              }, {
                "name" : "季番",
                "count" : 9,
                "total_cont" : 0
              } ],
              "infobox" : [ {
                "key" : "中文名",
                "value" : "86 -不存在的战区- 第2部分"
              }, {
                "key" : "别名",
                "value" : [ {
                  "v" : "86: Eighty Six 2nd Season"
                }, {
                  "v" : "86: Eighty Six Part 2"
                } ]
              }, {
                "key" : "话数",
                "value" : "12"
              }, {
                "key" : "放送开始",
                "value" : "2021年10月2日"
              }, {
                "key" : "放送星期",
                "value" : "星期六"
              }, {
                "key" : "官方网站",
                "value" : "https://anime-86.com/"
              }, {
                "key" : "播放电视台",
                "value" : "TOKYO MX"
              }, {
                "key" : "播放结束",
                "value" : "2022年3月19日"
              }, {
                "key" : "导演",
                "value" : "石井俊匡"
              }, {
                "key" : "Copyright",
                "value" : "© 2020 安里アサト / KADOKAWA / Project-86"
              }, {
                "key" : "原作",
                "value" : "安里アサト（電擊文庫 刊）"
              }, {
                "key" : "人物设定",
                "value" : "川上哲也；副：猪口美緒、杉生祐一"
              }, {
                "key" : "主题歌演出",
                "value" : "amazarashi、リーガルリリー"
              }, {
                "key" : "主动画师",
                "value" : "笠原由博"
              }, {
                "key" : "企画",
                "value" : "岩上敦宏、徳田直巳、春山ゆきお"
              }, {
                "key" : "製作",
                "value" : "Project-86（Aniplex、KADOKAWA、アスキー・メディアワークス、BANDAI SPIRITS）"
              }, {
                "key" : "制片人",
                "value" : "中山信宏、清瀬貴央、都真由"
              }, {
                "key" : "动画制片人",
                "value" : "藤井翔太"
              }, {
                "key" : "总制片人",
                "value" : "三宅将典、高林初、野瀬和也；制作统括：柏田真一郎、加藤淳、清田穣二"
              } ],
              "rating" : {
                "rank" : 273,
                "total" : 10372,
                "count" : {
                  "1" : 26,
                  "2" : 16,
                  "3" : 34,
                  "4" : 67,
                  "5" : 205,
                  "6" : 584,
                  "7" : 2150,
                  "8" : 4303,
                  "9" : 2086,
                  "10" : 901
                },
                "score" : 7.9
              },
              "collection" : {
                "on_hold" : 398,
                "dropped" : 173,
                "wish" : 2544,
                "collect" : 15262,
                "doing" : 875
              },
              "id" : 331887,
              "eps" : 12,
              "meta_tags" : [ "科幻", "机战", "TV", "日本", "小说改" ],
              "volumes" : 0,
              "series" : false,
              "locked" : false,
              "nsfw" : false,
              "type" : 2
            }, {
              "date" : "2021-04-10",
              "platform" : "TV",
              "images" : {
                "small" : "https://lain.bgm.tv/r/200/pic/cover/l/a4/b3/302189_SfN7e.jpg",
                "grid" : "https://lain.bgm.tv/r/100/pic/cover/l/a4/b3/302189_SfN7e.jpg",
                "large" : "https://lain.bgm.tv/pic/cover/l/a4/b3/302189_SfN7e.jpg",
                "medium" : "https://lain.bgm.tv/r/800/pic/cover/l/a4/b3/302189_SfN7e.jpg",
                "common" : "https://lain.bgm.tv/r/400/pic/cover/l/a4/b3/302189_SfN7e.jpg"
              },
              "image" : "https://lain.bgm.tv/pic/cover/l/a4/b3/302189_SfN7e.jpg",
              "summary" : "为了应对吉亚迪所开发出的完全独立无人战斗兵器“军团”的入侵，其邻国圣格诺利亚共和国开发了无人战斗兵器毁灭之力。但是，无人战斗机只是空有名号，实际是没有被认可为”人“的人们——86——驾驶，被当作道具来使用。\r\n由”86“所组成的部队“先锋战队”的队长少年・辛恩，在只能等待着死亡的令人绝望的战场上为了某个目的而战斗着。在那里，共和国军队的精英・蕾娜就任了新任指挥管制官。她小时候有着被86所救助过的经历，因此想把被作为“人形猪”而遭到轻视的他们作为正常人类来对待。\r\n只不过是为了战斗而被作为道具使用的少年以及被寄予了未来期望的精英才女，本不应当有所交集的两人，在激烈的战斗中看到了未来——",
              "name" : "86―エイティシックス―",
              "name_cn" : "86 -不存在的战区-",
              "tags" : [ {
                "name" : "A-1Pictures",
                "count" : 2863,
                "total_cont" : 0
              }, {
                "name" : "轻小说改",
                "count" : 2322,
                "total_cont" : 0
              }, {
                "name" : "2021年4月",
                "count" : 1789,
                "total_cont" : 0
              }, {
                "name" : "科幻",
                "count" : 1771,
                "total_cont" : 0
              }, {
                "name" : "泽野弘之",
                "count" : 1726,
                "total_cont" : 0
              }, {
                "name" : "战斗",
                "count" : 1550,
                "total_cont" : 0
              }, {
                "name" : "战争",
                "count" : 1341,
                "total_cont" : 0
              }, {
                "name" : "TV",
                "count" : 1115,
                "total_cont" : 0
              }, {
                "name" : "轻改",
                "count" : 918,
                "total_cont" : 0
              }, {
                "name" : "机战",
                "count" : 814,
                "total_cont" : 0
              }, {
                "name" : "2021",
                "count" : 772,
                "total_cont" : 0
              }, {
                "name" : "恋爱",
                "count" : 514,
                "total_cont" : 0
              }, {
                "name" : "石井俊匡",
                "count" : 333,
                "total_cont" : 0
              }, {
                "name" : "小说改",
                "count" : 244,
                "total_cont" : 0
              }, {
                "name" : "大野敏哉",
                "count" : 88,
                "total_cont" : 0
              }, {
                "name" : "日本",
                "count" : 48,
                "total_cont" : 0
              }, {
                "name" : "热血",
                "count" : 38,
                "total_cont" : 0
              }, {
                "name" : "長谷川育美",
                "count" : 36,
                "total_cont" : 0
              }, {
                "name" : "86",
                "count" : 33,
                "total_cont" : 0
              }, {
                "name" : "萝卜",
                "count" : 27,
                "total_cont" : 0
              }, {
                "name" : "2021年",
                "count" : 22,
                "total_cont" : 0
              }, {
                "name" : "-86-eightsix-",
                "count" : 21,
                "total_cont" : 0
              }, {
                "name" : "治愈",
                "count" : 20,
                "total_cont" : 0
              }, {
                "name" : "催泪",
                "count" : 20,
                "total_cont" : 0
              }, {
                "name" : "神作",
                "count" : 17,
                "total_cont" : 0
              }, {
                "name" : "机甲",
                "count" : 15,
                "total_cont" : 0
              }, {
                "name" : "奇幻",
                "count" : 13,
                "total_cont" : 0
              }, {
                "name" : "长谷川育美",
                "count" : 10,
                "total_cont" : 0
              }, {
                "name" : "2020s",
                "count" : 10,
                "total_cont" : 0
              }, {
                "name" : "A-1_Pictures",
                "count" : 9,
                "total_cont" : 0
              } ],
              "infobox" : [ {
                "key" : "中文名",
                "value" : "86 -不存在的战区-"
              }, {
                "key" : "别名",
                "value" : [ {
                  "v" : "86 -不存在的地域-"
                }, {
                  "v" : "86-エイティシックス-"
                } ]
              }, {
                "key" : "话数",
                "value" : "11"
              }, {
                "key" : "放送开始",
                "value" : "2021年4月10日"
              }, {
                "key" : "放送星期",
                "value" : "星期六"
              }, {
                "key" : "官方网站",
                "value" : "https://anime-86.com/"
              }, {
                "key" : "播放电视台",
                "value" : "TOKYO MX"
              }, {
                "key" : "其他电视台",
                "value" : "とちぎテレビ / 群馬テレビ / BS11 / 読売テレビ / CTV"
              }, {
                "key" : "播放结束",
                "value" : "2021年6月19日"
              }, {
                "key" : "导演",
                "value" : "石井俊匡"
              }, {
                "key" : "Copyright",
                "value" : "© 2020 安里アサト / KADOKAWA / Project-86"
              }, {
                "key" : "原作",
                "value" : "安里アサト（電擊文庫 刊）"
              }, {
                "key" : "人物设定",
                "value" : "川上哲也；副：猪口美緒"
              }, {
                "key" : "主题歌演出",
                "value" : "ヒトリエ、SawanoHiroyuki[nZk]:mizuki"
              }, {
                "key" : "主动画师",
                "value" : "笠原由博"
              }, {
                "key" : "企画",
                "value" : "岩上敦宏、徳田直巳、春山ゆきお"
              }, {
                "key" : "製作",
                "value" : "Project-86（Aniplex、KADOKAWA、アスキー・メディアワークス、BANDAI SPIRITS）"
              }, {
                "key" : "制片人",
                "value" : "中山信宏、清瀬貴央、都真由"
              }, {
                "key" : "动画制片人",
                "value" : "藤井翔太"
              }, {
                "key" : "总制片人",
                "value" : "三宅将典、高林初、野瀬和也；制作统括：柏田真一郎、加藤淳、清田穣二"
              } ],
              "rating" : {
                "rank" : 849,
                "total" : 11071,
                "count" : {
                  "1" : 34,
                  "2" : 22,
                  "3" : 51,
                  "4" : 110,
                  "5" : 321,
                  "6" : 1203,
                  "7" : 3460,
                  "8" : 3979,
                  "9" : 1266,
                  "10" : 625
                },
                "score" : 7.5
              },
              "collection" : {
                "on_hold" : 762,
                "dropped" : 498,
                "wish" : 3621,
                "collect" : 16358,
                "doing" : 1401
              },
              "id" : 302189,
              "eps" : 11,
              "meta_tags" : [ "科幻", "TV", "日本", "战斗", "小说改" ],
              "volumes" : 0,
              "series" : false,
              "locked" : false,
              "nsfw" : false,
              "type" : 2
            } ]
        """.trimIndent()
        val resp = json.parseJsonToList(BangumiSubject::class.java)
        println(resp)
    }
}
