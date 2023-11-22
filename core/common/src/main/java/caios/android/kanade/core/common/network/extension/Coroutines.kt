package caios.android.kanade.core.common.network.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren

fun CoroutineScope.cancelChildren() = this.coroutineContext.cancelChildren()
