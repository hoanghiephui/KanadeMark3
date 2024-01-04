package caios.android.kanade.core.model.podcast

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class FyydResponse(
    val status: Int,
    val msg: String,
    val meta: MetaData,
    val data: List<SearchHit>
)

@JsonClass(generateAdapter = true)
data class SearchHit(
    val title: String,
    val id: Int,
    @Json(name = "xmlURL")
    val xmlUrl: String,
    @Json(name = "htmlURL")
    val htmlUrl: String? = null,
    @Json(name = "imgURL")
    val imageUrl: String,
    val status: Int,
    val slug: String,
    val layoutImageUrl: String? = null,
    val thumbImageURL: String,
    val smallImageURL: String,
    val microImageURL: String,
    val language: String,
    val lastpoll: String? = null,
    val generator: String? = null,
    val categories: IntArray,
    @Json(name = "lastpub")
    val lastPubDate: Date,
    val rank: Int,
    @Json(name = "url_fyyd")
    val urlFyyd: String,
    val description: String,
    val subtitle: String,
    val author: String,
    @Json(name = "episode_count")
    val countEpisodes: Int? = null
)

@JsonClass(generateAdapter = true)
data class MetaData(
    val paging: Paging,
    @Json(name = "API_INFO") val apiInfo: ApiInfo,
    @Json(name = "SERVER") val server: String,
    val duration: Int
)

@JsonClass(generateAdapter = true)
data class Paging(
    val count: Int,
    val page: Int,
    @Json(name = "first_page")
    val firstPage: Int? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null,
    @Json(name = "next_page")
    val nextPage: Int? = null,
    @Json(name = "prev_page")
    val prevPage: Int? = null
)

@JsonClass(generateAdapter = true)
data class ApiInfo(
    @Json(name = "API_VERSION")
    val apiVersion: Double
)
