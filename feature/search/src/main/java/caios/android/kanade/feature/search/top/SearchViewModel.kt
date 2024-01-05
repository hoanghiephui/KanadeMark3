package caios.android.kanade.feature.search.top

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.common.network.Result
import caios.android.kanade.core.common.network.asFlowResult
import caios.android.kanade.core.common.network.data
import caios.android.kanade.core.common.network.extension.safeCollect
import caios.android.kanade.core.common.network.mapResultData
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.entity.YTMusicSearch
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Playlist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.podcast.PodcastSearchResult
import caios.android.kanade.core.music.MusicController
import caios.android.kanade.core.music.YTMusic
import caios.android.kanade.core.repository.MusicRepository
import caios.android.kanade.core.repository.UserDataRepository
import caios.android.kanade.core.repository.podcast.FyyDRepository
import caios.android.kanade.core.repository.podcast.IndexRepository
import caios.android.kanade.core.repository.podcast.PodcastSearcherRepository
import caios.android.kanade.core.ui.error.ErrorsDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Stable
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicController: MusicController,
    private val musicRepository: MusicRepository,
    private val userDataRepository: UserDataRepository,
    private val ytMusic: YTMusic,
    private val searcherRepository: PodcastSearcherRepository,
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    private val errorsDispatcher: ErrorsDispatcher,
    private val fyyDRepository: FyyDRepository,
    private val indexRepository: IndexRepository
) : ViewModel() {

    private val _screenState =
        MutableStateFlow<ScreenState<SearchUiState>>(ScreenState.Idle(SearchUiState()))
    val screenState
        get() = _screenState.asStateFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Idle(SearchUiState())
        )

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest {
                (screenState.value as? ScreenState.Idle)?.also { state ->
                    _screenState.value =
                        ScreenState.Idle(state.data.copy(isEnableYTMusic = it.isEnableYTMusic))
                }
            }
        }
    }

    fun onNewPlay(songs: List<Song>, index: Int) {
        musicController.playerEvent(
            PlayerEvent.NewPlay(
                index = index,
                queue = songs,
                playWhenReady = true,
            ),
        )
    }

    suspend fun search(
        keywords: List<String>,
        isSearchPodcast: Boolean,
        idSearchBy: Int
    ) {
        if (!isSearchPodcast) {
            _screenState.value = ScreenState.Loading
            _screenState.value = kotlin.runCatching {
                searchLibrary(keywords)
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(message = R.string.search_title) },
            )
        } else {
            if (keywords.all { it.isEmpty() }) {
                _screenState.emit(ScreenState.Idle(SearchUiState()))
                return
            }
            searchPodcast(keywords, SearchType.fromInt(idSearchBy))
                .safeCollect(
                    onEach = {
                        if (it is Result.Loading) {
                            _screenState.emit(ScreenState.Loading)
                        } else {
                            val state = SearchUiState(
                                keywords = keywords,
                                resultSongs = emptyList(),
                                resultArtists = emptyList(),
                                resultAlbums = emptyList(),
                                resultPlaylists = emptyList(),
                                resultYTMusic = emptyList(),
                                resultSongsRangeMap = emptyMap(),
                                resultArtistsRangeMap = emptyMap(),
                                resultAlbumsRangeMap = emptyMap(),
                                resultPlaylistsRangeMap = emptyMap(),
                                isEnableYTMusic = false,
                                resultSearchPodcast = it.data ?: emptyList()
                            )
                            _screenState.emit(ScreenState.Idle(state))
                        }
                    },
                    onError = errorsDispatcher::dispatch
                )
        }
        keywords.forEach {
            Timber.d("SEARCH: $it")
        }
    }

    private fun searchPodcast(keywords: List<String>, idSearchBy: SearchType) =
        when (idSearchBy) {
            SearchType.SEARCH_BY_APPLE -> {
                asFlowResult {
                    searcherRepository.searchPodcast(keywords.last())
                }.mapResultData { response ->
                    response.results?.map {
                        PodcastSearchResult(
                            title = it.collectionName.toString(),
                            imageUrl = it.artworkUrl100.toString(),
                            feedUrl = it.feedUrl.toString(),
                            author = it.artistName.toString(),
                            id = it.collectionId ?: 0,
                            trackCount = it.trackCount ?: 0
                        )
                    }
                }
            }

            SearchType.SEARCH_BY_FYYD -> {
                asFlowResult {
                    fyyDRepository.searchPodcast(keywords.last())
                }.mapResultData { response ->
                    response.data.map {
                        PodcastSearchResult(
                            title = it.title,
                            imageUrl = it.thumbImageURL,
                            feedUrl = it.xmlUrl,
                            author = it.author,
                            id = it.id,
                            trackCount = it.countEpisodes ?: 0
                        )
                    }
                }
            }

            SearchType.SEARCH_BY_INDEX -> {
                asFlowResult {
                    indexRepository.searchPodcast(keywords.last())
                }.mapResultData { response ->
                    response.feeds?.map {
                        PodcastSearchResult(
                            title = it.title ?: "",
                            imageUrl = it.image ?: "",
                            feedUrl = it.url ?: "",
                            author = it.author ?: "",
                            id = it.id ?: 0,
                            trackCount = it.episodeCount ?: 0
                        )
                    }
                }
            }

            else -> {
                asFlowResult {
                    searcherRepository.searchPodcast(keywords.last())
                }.mapResultData { response ->
                    response.results?.map {
                        PodcastSearchResult(
                            title = it.collectionName.toString(),
                            imageUrl = it.artworkUrl100.toString(),
                            feedUrl = it.feedUrl.toString(),
                            author = it.artistName.toString(),
                            id = it.collectionId ?: 0,
                            trackCount = it.trackCount ?: 0
                        )
                    }
                }
            }

        }

    private suspend fun searchLibrary(keywords: List<String>) = withContext(ioDispatcher) {
        val config = musicRepository.config.first()
        val songs = musicRepository.sortedSongs(config)
        val artists = musicRepository.sortedArtists(config)
        val albums = musicRepository.sortedAlbums(config)
        val playlists = musicRepository.sortedPlaylists(config)
        val userData = userDataRepository.userData.firstOrNull()

        val searchSongsJob = searchSongs(keywords, songs)
        val searchArtistsJob = searchArtists(keywords, artists)
        val searchAlbumsJob = searchAlbums(keywords, albums)
        val searchPlaylistsJob = searchPlaylists(keywords, playlists)
        val searchYTMusicJob = if (userData?.isEnableYTMusic == true) searchYTMusic(keywords) else null

        val (resultSongs, resultSongsRangeMap) = searchSongsJob.await()
        val (resultArtists, resultArtistsRangeMap) = searchArtistsJob.await()
        val (resultAlbums, resultAlbumsRangeMap) = searchAlbumsJob.await()
        val (resultPlaylists, resultPlaylistsRangeMap) = searchPlaylistsJob.await()
        val resultYTMusic = searchYTMusicJob?.await() ?: emptyList()

        delay(100)

        return@withContext SearchUiState(
            keywords = keywords,
            resultSongs = resultSongs,
            resultArtists = resultArtists,
            resultAlbums = resultAlbums,
            resultPlaylists = resultPlaylists,
            resultYTMusic = resultYTMusic,
            resultSongsRangeMap = resultSongsRangeMap,
            resultArtistsRangeMap = resultArtistsRangeMap,
            resultAlbumsRangeMap = resultAlbumsRangeMap,
            resultPlaylistsRangeMap = resultPlaylistsRangeMap,
            isEnableYTMusic = userData?.isEnableYTMusic ?: false,
        )
    }

    private fun searchSongs(keywords: List<String>, songs: List<Song>) = viewModelScope.async {
        if (keywords.all { it.isEmpty() }) return@async (emptyList<Song>() to emptyMap<Long, IntRange>())

        val resultSongs = mutableListOf<Song>()
        val resultRangeMap = mutableMapOf<Long, IntRange>()

        for (song in songs) {
            for (keyword in keywords) {
                val regex = Regex("(?i)$keyword")

                if (regex.containsMatchIn(song.title)) {
                    resultSongs.add(song)
                    resultRangeMap[song.id] = (regex.find(song.title)!!.range)
                }
            }
        }

        return@async (resultSongs to resultRangeMap)
    }

    private fun searchArtists(keywords: List<String>, artists: List<Artist>) = viewModelScope.async {
        if (keywords.all { it.isEmpty() }) return@async (emptyList<Artist>() to emptyMap<Long, IntRange>())

        val resultArtists = mutableListOf<Artist>()
        val resultRangeMap = mutableMapOf<Long, IntRange>()

        for (artist in artists) {
            for (keyword in keywords) {
                val regex = Regex("(?i)$keyword")

                if (regex.containsMatchIn(artist.artist)) {
                    resultArtists.add(artist)
                    resultRangeMap[artist.artistId] = (regex.find(artist.artist)!!.range)
                }
            }
        }

        return@async (resultArtists to resultRangeMap)
    }

    private fun searchAlbums(keywords: List<String>, albums: List<Album>) = viewModelScope.async {
        if (keywords.all { it.isEmpty() }) return@async (emptyList<Album>() to emptyMap<Long, IntRange>())

        val resultAlbums = mutableListOf<Album>()
        val resultRangeMap = mutableMapOf<Long, IntRange>()

        for (album in albums) {
            for (keyword in keywords) {
                val regex = Regex("(?i)$keyword")

                if (regex.containsMatchIn(album.album)) {
                    resultAlbums.add(album)
                    resultRangeMap[album.albumId] = (regex.find(album.album)!!.range)
                }
            }
        }

        return@async (resultAlbums to resultRangeMap)
    }

    private fun searchPlaylists(keywords: List<String>, playlists: List<Playlist>) = viewModelScope.async {
        if (keywords.all { it.isEmpty() }) return@async (emptyList<Playlist>() to emptyMap<Long, IntRange>())

        val resultPlaylists = mutableListOf<Playlist>()
        val resultRangeMap = mutableMapOf<Long, IntRange>()

        for (playlist in playlists) {
            for (keyword in keywords) {
                val regex = Regex("(?i)$keyword")

                if (regex.containsMatchIn(playlist.name)) {
                    resultPlaylists.add(playlist)
                    resultRangeMap[playlist.id] = (regex.find(playlist.name)!!.range)
                }
            }
        }

        return@async (resultPlaylists to resultRangeMap)
    }

    private fun searchYTMusic(keywords: List<String>) = viewModelScope.async {
        if (keywords.all { it.isEmpty() } || keywords.isEmpty()) return@async emptyList()

        return@async ytMusic.search(keywords.first()).fold(
            onSuccess = { it },
            onFailure = { emptyList() },
        )
    }

    enum class SearchType(val type: Int) {
        SEARCH_BY_APPLE(1) {
            override fun toString(): String {
                return type.toString()
            }
        },
        SEARCH_BY_FYYD(2) {
            override fun toString(): String {
                return type.toString()
            }
        },
        SEARCH_BY_INDEX(3) {
            override fun toString(): String {
                return type.toString()
            }
        };

        companion object {
            fun fromInt(value: Int) = entries.first { it.type == value }
        }
    }
}

@Stable
data class SearchUiState(
    val isEnableYTMusic: Boolean = false,
    val keywords: List<String> = emptyList(),
    val resultSongs: List<Song> = emptyList(),
    val resultArtists: List<Artist> = emptyList(),
    val resultAlbums: List<Album> = emptyList(),
    val resultPlaylists: List<Playlist> = emptyList(),
    val resultYTMusic: List<YTMusicSearch> = emptyList(),
    val resultSongsRangeMap: Map<Long, IntRange> = emptyMap(),
    val resultArtistsRangeMap: Map<Long, IntRange> = emptyMap(),
    val resultAlbumsRangeMap: Map<Long, IntRange> = emptyMap(),
    val resultPlaylistsRangeMap: Map<Long, IntRange> = emptyMap(),
    val resultSearchPodcast: List<PodcastSearchResult> = emptyList()
)
