package caios.android.kanade.core.model.music

data class Artist(
    val artist: String,
    val artistId: Long,
    val albums: List<Album>,
    val artwork: Artwork,
    val description: String? = null,
    val author: String? = null,
    val urlAvatar: String? = null
) {
    val songs: List<Song>
        get() = albums.flatMap { it.songs }

    val duration: Long
        get() = albums.sumOf { it.duration }

    companion object {
        fun dummy(id: Long = 0): Artist {
            return Artist(
                artist = "CAIOS$id",
                artistId = id,
                albums = Album.dummies(5),
                artwork = Artwork.Internal("${id}Artist"),
            )
        }

        fun dummies(count: Int): List<Artist> {
            return (0 until count).map { dummy(it.toLong()) }
        }
    }
}
