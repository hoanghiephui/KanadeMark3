package com.podcast.episodes

import android.content.Context
import androidx.lifecycle.viewModelScope
import caios.android.kanade.core.common.network.Dispatcher
import caios.android.kanade.core.common.network.KanadeDispatcher
import caios.android.kanade.core.design.BaseAdsViewModel
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.ScreenState
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.music.MusicController
import caios.android.kanade.core.repository.MusicRepository
import caios.android.kanade.core.repository.podcast.FeedDiscoveryRepository
import caios.android.kanade.core.repository.toSong
import com.applovin.sdk.AppLovinSdk
import com.podcast.core.network.util.PodcastDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(
    @Dispatcher(KanadeDispatcher.IO)
    private val ioDispatcher: CoroutineDispatcher,
    private val feedDiscoveryRepository: FeedDiscoveryRepository,
    private val musicController: MusicController,
    val download: PodcastDownloader,
    @Dispatcher(KanadeDispatcher.Default)
    private val defaultDispatcher: CoroutineDispatcher,
    adsSdk: AppLovinSdk,
    private val musicRepository: MusicRepository,
) : BaseAdsViewModel(adsSdk) {

    val screenState = feedDiscoveryRepository.loadAddItem().flowOn(ioDispatcher)
        .map { podcastFeedItemEntities ->
            ScreenState.Idle(
                podcastFeedItemEntities.sortedByDescending { it.publishDate } .map { it.toSong() }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScreenState.Loading,
        )

    fun onNewPlay(index: Int, queue: List<Song>) {
        musicController.playerEvent(
            PlayerEvent.NewPlay(
                index = index,
                queue = queue,
                playWhenReady = true,
            ),
        )
    }

    fun onAddToQuote(song: Song) {
        musicController.addToQueue(song)
    }

    fun playerEvent(event: PlayerEvent) {
        musicController.playerEvent(event)
    }


    fun downloadPodcast(
        song: Song,
        permission: Boolean,
        context: Context,
        downloadProgressListener: (Float, Int) -> Unit
    ): String {
        return if (permission) {
            download.downloadBook(song = song,
                downloadProgressListener = downloadProgressListener,
                onDownloadSuccess = {
                    insertIntoDB(song, download.getMediaFileName(song))
                })
            context.getString(R.string.downloading_podcast)
        } else {
            context.getString(R.string.storage_perm_error)
        }
    }

    fun onShufflePlay(songs: List<Song>) {
        viewModelScope.launch {
            musicRepository.setShuffleMode(ShuffleMode.ON)
            musicController.playerEvent(
                PlayerEvent.NewPlay(
                    index = Random().nextInt(songs.size),
                    queue = songs,
                    playWhenReady = true,
                ),
            )
        }
    }

    private fun insertIntoDB(song: Song, filename: String) =
        viewModelScope.launch(defaultDispatcher) {
            feedDiscoveryRepository.savePodcast(song, "${PodcastDownloader.FILE_FOLDER_PATH}/$filename")
        }
}
