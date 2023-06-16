package caios.android.kanade.core.repository

import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.LastQueue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.MusicConfig
import caios.android.kanade.core.model.player.MusicOrder
import caios.android.kanade.core.model.player.RepeatMode
import caios.android.kanade.core.model.player.ShuffleMode
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    val config: Flow<MusicConfig>
    val lastQueue: Flow<LastQueue>

    val songs: List<Song>
    val artists: List<Artist>
    val albums: List<Album>

    fun sortedSongs(musicConfig: MusicConfig): List<Song>
    fun sortedArtists(musicConfig: MusicConfig): List<Artist>
    fun sortedAlbums(musicConfig: MusicConfig): List<Album>

    fun getSong(songId: Long): Song?
    fun getArtist(artistId: Long): Artist?
    fun getAlbum(albumId: Long): Album?

    suspend fun saveQueue(currentQueue: List<Song>, originalQueue: List<Song>, index: Int)
    suspend fun saveProgress(progress: Long)

    suspend fun fetchSongs(musicConfig: MusicConfig? = null)
    suspend fun fetchArtists(musicConfig: MusicConfig? = null)
    suspend fun fetchAlbums(musicConfig: MusicConfig? = null)
    suspend fun fetchArtistArtwork()
    suspend fun fetchAlbumArtwork()

    suspend fun setShuffleMode(mode: ShuffleMode)
    suspend fun setRepeatMode(mode: RepeatMode)

    suspend fun setSongOrder(musicOrder: MusicOrder)
    suspend fun setArtistOrder(musicOrder: MusicOrder)
    suspend fun setAlbumOrder(musicOrder: MusicOrder)
}
