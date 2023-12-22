package caios.android.kanade.core.model.podcast

import kotlinx.parcelize.Parcelize
import com.squareup.moshi.JsonClass
import android.os.Parcelable
import com.squareup.moshi.Json

@JsonClass(generateAdapter = true)
@Parcelize
data class ItunesTopPodcastResponse(

	@Json(name="feed")
	val feed: Feed? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Id(

	@Json(name="label")
	val label: String? = null,

	@Json(name="attributes")
	val attributes: Attributes? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ImReleaseDate(

	@Json(name="attributes")
	val attributes: Attributes? = null,

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Author(

	@Json(name="name")
	val name: Name? = null,

	@Json(name="uri")
	val uri: Uri? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ImName(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Attributes(

	@Json(name="label")
	val label: String? = null,

	@Json(name="scheme")
	val scheme: String? = null,

	@Json(name="term")
	val term: String? = null,

	@Json(name="im:id")
	val imId: String? = null,

	@Json(name="rel")
	val rel: String? = null,

	@Json(name="href")
	val href: String? = null,

	@Json(name="type")
	val type: String? = null,

	@Json(name="amount")
	val amount: String? = null,

	@Json(name="currency")
	val currency: String? = null,

	@Json(name="height")
	val height: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class LinkItem(

	@Json(name="attributes")
	val attributes: Attributes? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Rights(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Category(

	@Json(name="attributes")
	val attributes: Attributes? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class EntryItem(

	@Json(name="summary")
	val summary: Summary? = null,

	@Json(name="im:artist")
	val imArtist: ImArtist? = null,

	@Json(name="im:name")
	val imName: ImName? = null,

	@Json(name="im:contentType")
	val imContentType: ImContentType? = null,

	@Json(name="im:image")
	val imImage: List<ImImageItem>? = null,

	@Json(name="rights")
	val rights: Rights? = null,

	@Json(name="im:price")
	val imPrice: ImPrice? = null,

	@Json(name="link")
	val link: Link? = null,

	@Json(name="id")
	val id: Id? = null,

	@Json(name="title")
	val title: Title? = null,

	@Json(name="category")
	val category: Category? = null,

	@Json(name="im:releaseDate")
	val imReleaseDate: ImReleaseDate? = null,
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Icon(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Feed(

	@Json(name="entry")
	val entry: List<EntryItem>? = null,

	@Json(name="author")
	val author: Author? = null,

	@Json(name="rights")
	val rights: Rights? = null,

	@Json(name="icon")
	val icon: Icon? = null,

	@Json(name="link")
	val link: List<LinkItem>? = null,

	@Json(name="id")
	val id: Id? = null,

	@Json(name="title")
	val title: Title? = null,

	@Json(name="updated")
	val updated: Updated? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Title(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Updated(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ImArtist(

	@Json(name="attributes")
	val attributes: Attributes? = null,

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ImImageItem(

	@Json(name="attributes")
	val attributes: Attributes? = null,

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Name(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ImContentType(

	@Json(name="attributes")
	val attributes: Attributes? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class ImPrice(

	@Json(name="attributes")
	val attributes: Attributes? = null,

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Summary(

	@Json(name="label")
	val label: String? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Link(

	@Json(name="attributes")
	val attributes: Attributes? = null
) : Parcelable

@JsonClass(generateAdapter = true)
@Parcelize
data class Uri(

	@Json(name="label")
	val label: String? = null
) : Parcelable
