package caios.android.kanade.core.model

data class MusicConfig(
    val shuffleMode: ShuffleMode,
    val repeatMode: RepeatMode,
    val songOrder: MusicOrder,
    val artistOrder: MusicOrder,
    val albumOrder: MusicOrder,
)
