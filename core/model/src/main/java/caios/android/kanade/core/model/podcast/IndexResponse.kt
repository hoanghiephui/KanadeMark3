package caios.android.kanade.core.model.podcast

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
data class IndexResponse(

	@Json(name="query")
	val query: String? = null,

	@Json(name="count")
	val count: Int? = null,

	@Json(name="feeds")
	val feeds: List<FeedsItem>? = null,

	@Json(name="description")
	val description: String? = null,

	@Json(name="status")
	val status: String? = null
)

@JsonClass(generateAdapter = true)
data class FeedsItem(

	@Json(name="episodeCount")
	val episodeCount: Int? = null,

	@Json(name="link")
	val link: String? = null,

	@Json(name="description")
	val description: String? = null,

	@Json(name="generator")
	val generator: String? = null,

	@Json(name="language")
	val language: String? = null,

	@Json(name="dead")
	val dead: Int? = null,

	@Json(name="medium")
	val medium: String? = null,

	@Json(name="title")
	val title: String? = null,

	@Json(name="type")
	val type: Int? = null,

	@Json(name="lastHttpStatus")
	val lastHttpStatus: Int? = null,

	@Json(name="ownerName")
	val ownerName: String? = null,

	@Json(name="inPollingQueue")
	val inPollingQueue: Int? = null,

	@Json(name="podcastGuid")
	val podcastGuid: String? = null,

	@Json(name="id")
	val id: Int? = null,

	@Json(name="locked")
	val locked: Int? = null,

	@Json(name="contentType")
	val contentType: String? = null,

	@Json(name="image")
	val image: String? = null,

	@Json(name="lastParseTime")
	val lastParseTime: Int? = null,

	@Json(name="lastGoodHttpStatusTime")
	val lastGoodHttpStatusTime: Int? = null,

	@Json(name="author")
	val author: String? = null,

	@Json(name="crawlErrors")
	val crawlErrors: Int? = null,

	@Json(name="originalUrl")
	val originalUrl: String? = null,

	@Json(name="artwork")
	val artwork: String? = null,

	@Json(name="priority")
	val priority: Int? = null,

	@Json(name="parseErrors")
	val parseErrors: Int? = null,

	@Json(name="url")
	val url: String? = null,

	@Json(name="explicit")
	val explicit: Boolean? = null,

	@Json(name="lastCrawlTime")
	val lastCrawlTime: Int? = null,

	@Json(name="imageUrlHash")
	val imageUrlHash: Long? = null,

	@Json(name="newestItemPubdate")
	val newestItemPubdate: Int? = null,

	@Json(name="lastUpdateTime")
	val lastUpdateTime: Int? = null,

	@Json(name="itunesId")
	val itunesId: Int? = null
)

@JsonClass(generateAdapter = true)
data class Categories(

	@Json(name="14")
	val jsonMember14: String? = null,

	@Json(name="9")
	val jsonMember9: String? = null,

	@Json(name="55")
	val jsonMember55: String? = null,

	@Json(name="25")
	val jsonMember25: String? = null,

	@Json(name="20")
	val jsonMember20: String? = null,

	@Json(name="77")
	val jsonMember77: String? = null,

	@Json(name="78")
	val jsonMember78: String? = null,

	@Json(name="10")
	val jsonMember10: String? = null,

	@Json(name="66")
	val jsonMember66: String? = null,

	@Json(name="65")
	val jsonMember65: String? = null,

	@Json(name="85")
	val jsonMember85: String? = null,

	@Json(name="53")
	val jsonMember53: String? = null,

	@Json(name="1")
	val jsonMember1: String? = null,

	@Json(name="7")
	val jsonMember7: String? = null,

	@Json(name="16")
	val jsonMember16: String? = null,

	@Json(name="61")
	val jsonMember61: String? = null,

	@Json(name="28")
	val jsonMember28: String? = null,

	@Json(name="29")
	val jsonMember29: String? = null,

	@Json(name="30")
	val jsonMember30: String? = null,

	@Json(name="26")
	val jsonMember26: String? = null,

	@Json(name="2")
	val jsonMember2: String? = null,

	@Json(name="33")
	val jsonMember33: String? = null,

	@Json(name="17")
	val jsonMember17: String? = null,

	@Json(name="36")
	val jsonMember36: String? = null,

	@Json(name="37")
	val jsonMember37: String? = null,

	@Json(name="22")
	val jsonMember22: String? = null,

	@Json(name="11")
	val jsonMember11: String? = null,

	@Json(name="3")
	val jsonMember3: String? = null,

	@Json(name="80")
	val jsonMember80: String? = null,

	@Json(name="81")
	val jsonMember81: String? = null,

	@Json(name="56")
	val jsonMember56: String? = null,

	@Json(name="57")
	val jsonMember57: String? = null,

	@Json(name="62")
	val jsonMember62: String? = null,

	@Json(name="86")
	val jsonMember86: String? = null,

	@Json(name="67")
	val jsonMember67: String? = null,

	@Json(name="21")
	val jsonMember21: String? = null
)
