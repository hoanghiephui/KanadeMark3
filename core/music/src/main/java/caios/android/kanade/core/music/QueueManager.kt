package caios.android.kanade.core.music

import caios.android.kanade.core.model.music.Queue
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.ShuffleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

interface QueueManager {
    val queue: Flow<Queue>

    fun skipToNext(): Song
    fun skipToPrevious(): Song
    fun skipToItem(toIndex: Int): Song

    fun addItem(index: Int, song: Song)
    fun removeItem(index: Int)
    fun moveItem(fromIndex: Int, toIndex: Int)

    fun setShuffleMode(shuffleMode: ShuffleMode)
    fun setCurrentSong(song: Song)

    fun getCurrentSong(): Song?
    fun getCurrentQueue(): List<Song>
    fun getOriginalQueue(): List<Song>

    fun build(currentQueue: List<Song>, originalQueue: List<Song>, index: Int)
    fun clear()
}

class QueueManagerImpl @Inject constructor() : QueueManager {

    private val _currentQueue = MutableStateFlow(mutableListOf<Song>())
    private val _originalQueue = MutableStateFlow(mutableListOf<Song>())
    private val _index = MutableStateFlow(0)

    @get:JvmName("currentQueueValue")
    private val currentQueue get() = _currentQueue.value

    @get:JvmName("originalQueueValue")
    private val originalQueue get() = _originalQueue.value

    @get:JvmName("indexValue")
    private val index get() = _index.value

    override val queue: Flow<Queue>
        get() = combine(_currentQueue, _index, ::Queue)

    override fun skipToNext(): Song {
        _index.value = if (currentQueue.size > index + 1) index + 1 else 0
        return currentQueue[index]
    }

    override fun skipToPrevious(): Song {
        _index.value = if (index - 1 >= 0) index - 1 else currentQueue.size - 1
        return currentQueue[index]
    }

    override fun skipToItem(toIndex: Int): Song {
        _index.value = toIndex
        return currentQueue[index]
    }

    override fun addItem(index: Int, song: Song) {
        _currentQueue.value.add(index, song)
        _originalQueue.value.add(song)
        _index.value = if (index <= this.index) this.index + 1 else this.index
    }

    override fun removeItem(index: Int) {
        val song = currentQueue[this.index]

        _currentQueue.value.removeAt(index)
        _originalQueue.value.remove(song)
        _index.value = if (index <= this.index) this.index - 1 else this.index
    }

    override fun moveItem(fromIndex: Int, toIndex: Int) {
        val song = currentQueue[fromIndex]

        _currentQueue.value.apply { add(toIndex, removeAt(fromIndex)) }
        _index.value = currentQueue.indexOf(song)
    }

    override fun setShuffleMode(shuffleMode: ShuffleMode) {
        when(shuffleMode) {
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
        _index.value = currentQueue.indexOf(song)
    }

    override fun getCurrentSong(): Song? {
        return currentQueue.elementAtOrNull(index)
    }

    override fun getCurrentQueue(): List<Song> {
        return currentQueue.toList()
    }

    override fun getOriginalQueue(): List<Song> {
        return originalQueue.toList()
    }

    override fun build(currentQueue: List<Song>, originalQueue: List<Song>, index: Int) {
        _currentQueue.value = currentQueue.toMutableList()
        _originalQueue.value = originalQueue.toMutableList()
        _index.value = index
    }

    override fun clear() {
        _currentQueue.value = mutableListOf()
        _originalQueue.value = mutableListOf()
        _index.value = 0
    }
}