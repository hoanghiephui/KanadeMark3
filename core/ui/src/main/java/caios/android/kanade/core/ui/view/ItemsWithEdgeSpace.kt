package caios.android.kanade.core.ui.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

inline fun <T> LazyGridScope.itemsWithEdgeSpace(
    @androidx.annotation.IntRange(from = 1) spanCount: Int,
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyGridItemScope.(item: T) -> Unit
) {
    require(spanCount > 0)

    val itemSize = items.size
    val rowCount = if (itemSize % spanCount == 0) itemSize / spanCount else itemSize / spanCount + 1

    (0 until rowCount).forEach { index ->
        item { Spacer(modifier = Modifier.fillMaxWidth()) }

        val childItems = items.subList(
            index * spanCount,
            minOf(index * spanCount + spanCount, itemSize)
        )
        items(childItems, key, span, contentType, itemContent)

        if (index != rowCount - 1 || itemSize % spanCount != 0) {
            item { Spacer(modifier = Modifier.fillMaxWidth()) }
        }
    }
}
