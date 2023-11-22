package caios.android.kanade.core.common.network.extension

fun String.takeIfNotBlank(): String? = this.takeIf { it.isNotBlank() }
