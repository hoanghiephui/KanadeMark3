package caios.android.kanade.core.common.network.extension

fun <T> MutableList<T>.addIfAbsent(item: T) {
    if (!contains(item)) {
        add(item)
    }
}
