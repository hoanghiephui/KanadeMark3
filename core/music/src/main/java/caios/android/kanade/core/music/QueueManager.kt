package caios.android.kanade.core.music

import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.ShuffleMode
import caios.android.kanade.core.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

interface QueueManager {
    val queue: Flow<Queue>

    fun skipToNext(): Song?
    fun skipToPrevious(): Song?
    fun skipToItem(toIndex: Int): Song?

    fun addItem(index: Int, song: Song)
    fun addItems(index: Int, songs: List<Song>)
    fun removeItem(index: Int)
    fun moveItem(fromIndex: Int, toIndex: Int)

    fun setShuffleMode(shuffleMode: ShuffleMode)
    fun setCurrentSong(song: Song)

    fun getCurrentSong(): Song?
    fun getCurrentSongPreview(): Song?
    fun getCurrentQueue(): List<Song>
    fun getOriginalQueue(): List<Song>
    fun getIndex(): Int

    fun build(currentQueue: List<Song>, originalQueue: List<Song>, index: Int)
    fun preview(currentQueue: Song)
    fun clear()

    val isPreview: Boolean
}

class QueueManagerImpl @Inject constructor(
    private val musicRepository: MusicRepository,
) : QueueManager {

    private val _currentQueue = MutableStateFlow(mutableListOf<Long>())
    private val _originalQueue = MutableStateFlow(mutableListOf<Long>())
    private val _index = MutableStateFlow(0)
    private val _currentPreview = MutableStateFlow<Song?>(null)
    private val _isPreview = MutableStateFlow(false)

    @get:JvmName("currentQueueValue")
    private val currentQueue get() = _currentQueue.value

    @get:JvmName("originalQueueValue")
    private val originalQueue get() = _originalQueue.value

    @get:JvmName("indexValue")
    private val index get() = _index.value

    @get:JvmName("currentPreviewValue")
    private val currentPreview get() = _currentPreview.value

    override val queue: Flow<Queue>
        get() = combine(_currentQueue, _index) { queue, index ->
            Queue(
                items = queue.mapNotNull { musicRepository.getSong(it) },
                index = index,
            )
        }

    override fun skipToNext(): Song? {
        _index.value = if (currentQueue.size > index + 1) index + 1 else 0
        return currentQueue.elementAtOrNull(index)?.let { musicRepository.getSong(it) }
    }

    override fun skipToPrevious(): Song? {
        _index.value = if (index - 1 >= 0) index - 1 else currentQueue.size - 1
        return currentQueue.elementAtOrNull(index)?.let { musicRepository.getSong(it) }
    }

    override fun skipToItem(toIndex: Int): Song? {
        _index.value = toIndex
        return currentQueue.elementAtOrNull(index)?.let { musicRepository.getSong(it) }
    }

    override fun addItem(index: Int, song: Song) {
        val current = currentQueue.toMutableList()
        val original = originalQueue.toMutableList()

        _currentQueue.value = current.apply { add(index, song.id) }
        _originalQueue.value = original.apply { add(song.id) }
        _index.value = if (index <= this.index) this.index + 1 else this.index
    }

    override fun addItems(index: Int, songs: List<Song>) {
        val current = currentQueue.toMutableList()
        val original = originalQueue.toMutableList()

        _currentQueue.value = current.apply { addAll(index, songs.map { it.id }) }
        _originalQueue.value = original.apply { addAll(songs.map { it.id }) }
        _index.value = if (index <= this.index) this.index + songs.size else this.index
    }

    override fun removeItem(index: Int) {
        val song = currentQueue[this.index]
        val current = currentQueue.toMutableList()
        val original = originalQueue.toMutableList()

        _currentQueue.value = current.apply { removeAt(index) }
        _originalQueue.value = original.apply { remove(song) }
        _index.value = if (index <= this.index) this.index - 1 else this.index
    }

    override fun moveItem(fromIndex: Int, toIndex: Int) {
        val song = currentQueue[index]
        val current = currentQueue.toMutableList()

        _currentQueue.value = current.apply { add(toIndex, removeAt(fromIndex)) }
        _index.value = currentQueue.indexOf(song)
    }

    override fun setShuffleMode(shuffleMode: ShuffleMode) {
        when (shuffleMode) {
            ShuffleMode.ON -> {
                val shuffledQueue = originalQueue.shuffled()
                val shuffledIndex = shuffledQueue.indexOf(currentQueue[index])

                _currentQueue.value = shuffledQueue.toMutableList()
                _index.value = shuffledIndex
            }
            ShuffleMode.OFF -> {
                val originalIndex = originalQueue.indexOf(currentQueue[index])

                _currentQueue.value = originalQueue.toMutableList()
                _index.value = originalIndex
            }
        }
    }

    override fun setCurrentSong(song: Song) {
        _index.value = currentQueue.indexOf(song.id)
    }

    override fun getCurrentSong(): Song? {
        return currentQueue.elementAtOrNull(index)?.let { musicRepository.getSong(it) }
    }

    override fun getCurrentSongPreview(): Song? {
        return currentPreview
    }

    override fun getCurrentQueue(): List<Song> {
        return currentQueue.mapNotNull { musicRepository.getSong(it) }
    }

    override fun getOriginalQueue(): List<Song> {
        return originalQueue.mapNotNull { musicRepository.getSong(it) }
    }

    override fun getIndex(): Int {
        return index
    }

    override fun build(currentQueue: List<Song>, originalQueue: List<Song>, index: Int) {
        Timber.d("queue build: current=${currentQueue.size}, originalQueue=${originalQueue.size}, index=$index")
        _currentQueue.value = currentQueue.map { it.id }.toMutableList()
        _originalQueue.value = originalQueue.map { it.id }.toMutableList()
        _index.value = index
        _isPreview.value = false
    }

    override fun preview(currentQueue: Song) {
        _currentPreview.value = currentQueue
        _isPreview.value = true
    }

    override fun clear() {
        _currentQueue.value = mutableListOf()
        _originalQueue.value = mutableListOf()
        _index.value = 0
    }

    override val isPreview: Boolean
        get() = _isPreview.value
}
