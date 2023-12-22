package caios.android.kanade.core.music

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import buildBundle
import caios.android.kanade.core.common.network.di.ApplicationScope
import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.ControlAction
import caios.android.kanade.core.model.player.ControlKey
import caios.android.kanade.core.model.player.PlayerEvent
import caios.android.kanade.core.model.player.PlayerState
import caios.android.kanade.core.model.player.RepeatMode
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface MusicController {
    val isInitialized: StateFlow<Boolean>
    val isAnalyzing: StateFlow<Boolean>
    val currentSong: StateFlow<Song?>
    val currentQueue: StateFlow<Queue?>
    val playerPosition: StateFlow<Long>
    val playerState: StateFlow<PlayerState>

    fun initialize()
    fun terminate()

    fun setAnalyzing(isAnalyzing: Boolean)

    fun setPlayerPlaying(isPlaying: Boolean)
    fun setPlayerState(state: Int)
    fun setPlayerItem(item: MediaMetadata)
    fun setPlayerPosition(position: Long)

    fun addToQueue(songs: List<Song>, index: Int? = null)
    fun addToQueue(songs: Song, index: Int? = null)
    fun removeFromQueue(index: Int)
    fun moveQueue(fromIndex: Int, toIndex: Int)

    fun playerEvent(event: PlayerEvent)
}

class MusicControllerImpl @Inject constructor(
    private val musicRepository: MusicRepository,
    private val queueManager: QueueManager,
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
) : MusicController {

    private var _isInitialized = MutableStateFlow(false)
    private var _isAnalyzing = MutableStateFlow(false)
    private var _currentSong = MutableStateFlow<Song?>(null)
    private var _currentQueue = MutableStateFlow<Queue?>(null)
    private var _playerPosition = MutableStateFlow(0L)
    private var _playerState = MutableStateFlow(PlayerState.Initialize)

    override val isInitialized = _isInitialized.asStateFlow()
    override val isAnalyzing = _isAnalyzing.asStateFlow()
    override val currentSong = _currentSong.asStateFlow()
    override val currentQueue = _currentQueue.asStateFlow()
    override val playerPosition = _playerPosition.asStateFlow()
    override val playerState = _playerState.asStateFlow()

    private var isTryingConnect = false
    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaController: MediaControllerCompat? = null

    private val transportStack = mutableListOf<TransportControlEvent>()
    private val transportControls get() = mediaController?.transportControls

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Timber.d("Connected to MediaBrowser.")

            mediaController = mediaBrowser?.let { MediaControllerCompat(context, it.sessionToken) }
            isTryingConnect = false

            scope.launch {
                onInitialize(PlayerEvent.Initialize(false))
            }
        }

        override fun onConnectionFailed() {
            Timber.d("Connection failed to MediaBrowser.")

            mediaBrowser = null
            mediaController = null
            isTryingConnect = false
        }
    }

    private val transportLooper = scope.launch(start = CoroutineStart.LAZY) {
        while (isActive) {
            try {
                val action = transportStack.firstOrNull()

                if (action != null && transportControls != null) {
                    action.action.invoke(transportControls!!)
                    transportStack.removeFirstOrNull()
                }

                delay(100)
            } catch (e: Throwable) {
                Timber.w(e, "Cannot send transport event.")
            }
        }
    }

    init {
        scope.launch {
            queueManager.queue.collect {
                _currentQueue.value = it

                if (it.items.isNotEmpty()) {
                    musicRepository.saveQueue(
                        currentQueue = queueManager.getCurrentQueue(),
                        originalQueue = queueManager.getOriginalQueue(),
                        index = it.index,
                    )
                }
            }
        }
    }

    override fun initialize() {
        Timber.d("Initialize MusicController. isInitialized: ${isInitialized.value}")

        if (isInitialized.value) return

        transportLooper.start()
        createConnection()
    }

    override fun terminate() {
        Timber.d("Terminate MusicController.")

        transportLooper.cancel()
        terminateConnection()

        scope.launch {
            delay(500)
            _isInitialized.value = false
        }
    }

    override fun setAnalyzing(isAnalyzing: Boolean) {
        _isAnalyzing.value = isAnalyzing
    }

    override fun setPlayerPlaying(isPlaying: Boolean) {
        when (isPlaying) {
            true -> _playerState.value = PlayerState.Playing
            false -> _playerState.value = PlayerState.Paused
        }
    }

    override fun setPlayerState(state: Int) {
        when (state) {
            ExoPlayer.STATE_BUFFERING -> _playerState.value = PlayerState.Buffering
            ExoPlayer.STATE_READY -> _playerState.value = PlayerState.Ready
            ExoPlayer.STATE_ENDED -> onComplete()
        }

        _isInitialized.value = true
    }

    override fun setPlayerItem(item: MediaMetadata) {
        _currentSong.value = queueManager.getCurrentSong() ?: queueManager.getCurrentSongPreview()
    }

    override fun setPlayerPosition(position: Long) {
        _playerPosition.value = position

        if (isInitialized.value) {
            scope.launch {
                musicRepository.saveProgress(position)
            }
        }
    }

    override fun addToQueue(songs: List<Song>, index: Int?) {
        val i = if (currentQueue.value?.items?.isEmpty() == true) 0 else index
        queueManager.addItems(i ?: currentQueue.value?.items?.size ?: 0, songs)
    }

    override fun addToQueue(songs: Song, index: Int?) {
        val i = if (currentQueue.value?.items?.isEmpty() == true) 0 else index
        queueManager.addItem(i ?: currentQueue.value?.items?.size ?: 0, songs)
    }

    override fun removeFromQueue(index: Int) {
        queueManager.removeItem(index)
    }

    override fun moveQueue(fromIndex: Int, toIndex: Int) {
        queueManager.moveItem(fromIndex, toIndex)
    }

    override fun playerEvent(event: PlayerEvent) {
        scope.launch {
            Timber.d("playerEvent: $event")

            when (event) {
                is PlayerEvent.Initialize -> onInitialize(event)
                is PlayerEvent.NewPlay -> onNewPlay(event)
                is PlayerEvent.Play -> onPlay(event)
                is PlayerEvent.Pause -> onPause(event)
                is PlayerEvent.PauseTransient -> onPauseTransient(event)
                is PlayerEvent.Stop -> onStop(event)
                is PlayerEvent.SkipToNext -> onSkipToNext(event)
                is PlayerEvent.SkipToPrevious -> onSkipToPrevious(event)
                is PlayerEvent.SkipToQueue -> onSkipToQueue(event)
                is PlayerEvent.Seek -> onSeek(event)
                is PlayerEvent.Repeat -> onRepeatModeChanged(event)
                is PlayerEvent.Shuffle -> onShuffleModeChanged(event)
                is PlayerEvent.Dack -> onDack(event)
                is PlayerEvent.PreviewPlay -> onPreviewPlay(event)
            }
        }
    }

    private suspend fun onNewPlay(event: PlayerEvent.NewPlay) {
        val config = musicRepository.config.first()
        val originalQueue = event.queue
        val originalItem = originalQueue[event.index]
        val currentQueue = if (config.shuffleMode == ShuffleMode.ON) originalQueue.shuffled() else originalQueue
        val currentIndex = if (config.shuffleMode == ShuffleMode.ON) currentQueue.indexOf(originalItem) else event.index

        queueManager.build(
            currentQueue = currentQueue,
            originalQueue = originalQueue,
            index = currentIndex,
        )

        val args = buildBundle {
            putBoolean(ControlKey.PLAY_WHEN_READY, event.playWhenReady)
        }

        event.transport { sendCustomAction(ControlAction.NEW_PLAY, args) }
    }
    private fun onPreviewPlay(event: PlayerEvent.PreviewPlay) {
        queueManager.preview(
            currentQueue = event.queue
        )

        val args = buildBundle {
            putBoolean(ControlKey.PLAY_WHEN_READY, event.playWhenReady)
        }

        event.transport { sendCustomAction(ControlAction.PREVIEW_PLAY, args) }
    }

    private fun onPlay(event: PlayerEvent.Play) {
        event.transport { play() }
    }

    private fun onPause(event: PlayerEvent.Pause) {
        event.transport { pause() }
    }

    private fun onPauseTransient(event: PlayerEvent.PauseTransient) {
        event.transport { sendCustomAction(ControlAction.PAUSE_TRANSIENT, null) }
    }

    private fun onStop(event: PlayerEvent.Stop) {
        event.transport { stop() }
    }

    private fun onSkipToNext(event: PlayerEvent.SkipToNext) {
        event.transport { skipToNext() }
    }

    private fun onSkipToPrevious(event: PlayerEvent.SkipToPrevious) {
        event.transport { skipToPrevious() }
    }

    private fun onSkipToQueue(event: PlayerEvent.SkipToQueue) {
        queueManager.skipToItem(event.index)

        val args = buildBundle {
            putBoolean(ControlKey.PLAY_WHEN_READY, event.playWhenReady ?: (playerState.value == PlayerState.Playing))
        }

        event.transport { sendCustomAction(ControlAction.NEW_PLAY, args) }
    }

    private fun onSeek(event: PlayerEvent.Seek) {
        val duration = currentSong.value?.duration ?: 0
        val position = (duration * event.progress).toLong()

        event.transport { seekTo(position) }
    }

    private suspend fun onRepeatModeChanged(event: PlayerEvent.Repeat) {
        musicRepository.setRepeatMode(event.repeatMode)
    }

    private suspend fun onShuffleModeChanged(event: PlayerEvent.Shuffle) {
        musicRepository.setShuffleMode(event.shuffleMode)
        queueManager.setShuffleMode(event.shuffleMode)
    }

    private fun onDack(event: PlayerEvent.Dack) {
        val args = buildBundle {
            putBoolean(ControlKey.IS_ENABLED, event.isEnabled)
        }

        event.transport { sendCustomAction(ControlAction.DACK, args) }
    }

    private suspend fun onInitialize(event: PlayerEvent.Initialize) {
        if (isInitialized.value) return
        if (queueManager.getCurrentSong() != null) return

        val lastQueue = musicRepository.lastQueue.first()
        val args = buildBundle {
            putBoolean(ControlKey.PLAY_WHEN_READY, event.playWhenReady)
            putLong(ControlKey.PROGRESS, lastQueue.progress)
        }

        queueManager.build(
            currentQueue = lastQueue.currentItems.mapNotNull { musicRepository.getSong(it) },
            originalQueue = lastQueue.originalItems.mapNotNull { musicRepository.getSong(it) },
            index = lastQueue.index,
        )

        event.transport { sendCustomAction(ControlAction.INITIALIZE, args) }
    }

    private fun onComplete() {
        scope.launch {
            val config = musicRepository.config.first()

            when (config.repeatMode) {
                RepeatMode.ALL -> playerEvent(PlayerEvent.SkipToNext)
                RepeatMode.ONE -> {
                    currentSong.value?.let { musicRepository.addToPlayHistory(it) }
                    playerEvent(PlayerEvent.Seek(0f))
                }
                RepeatMode.OFF -> {
                    val index = queueManager.getIndex()
                    val queue = queueManager.getCurrentQueue()

                    if (queue.size <= index + 1) {
                        playerEvent(PlayerEvent.Pause)
                    }

                    playerEvent(PlayerEvent.SkipToNext)
                }
            }
        }
    }

    private fun PlayerEvent.transport(action: MediaControllerCompat.TransportControls.() -> Unit) {
        transportStack.add(TransportControlEvent(this, action))
    }

    private fun createConnection() {
        if (isTryingConnect) return

        isTryingConnect = true

        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            connectionCallback,
            null,
        )

        mediaBrowser?.connect()
    }

    private fun terminateConnection() {
        mediaBrowser?.disconnect()

        mediaBrowser = null
        mediaController = null
    }

    private data class TransportControlEvent(
        val event: PlayerEvent,
        val action: MediaControllerCompat.TransportControls.() -> Unit,
    )
}
