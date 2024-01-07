package caios.android.kanade.feature.sort

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.LocalSystemBars
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.model.UserData
import caios.android.kanade.core.model.player.MusicOrder
import caios.android.kanade.core.model.player.MusicOrderOption
import caios.android.kanade.core.music.MusicViewModel
import caios.android.kanade.core.ui.dialog.showAsButtonSheet
import kotlinx.collections.immutable.toImmutableList
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDialog(
    order: MusicOrder,
    onChangedSortOrder: (MusicOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = LocalSystemBars.current.bottom),
    ) {
        TopAppBar(modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
            title = {
                Text(
                    text = stringResource(R.string.sort_title),
                    style = MaterialTheme.typography.titleMedium.bold(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            })
        SortOrderSection(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            order = order.order,
            onClickOrder = { onChangedSortOrder.invoke(order.copy(order = it)) },
        )

        HorizontalDivider(Modifier.fillMaxWidth())

        SortOptionSection(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            order = order,
            options = (
                    when (order.option) {
                        is MusicOrderOption.Song -> MusicOrderOption.Song.entries
                        is MusicOrderOption.Artist -> MusicOrderOption.Artist.entries
                        is MusicOrderOption.Album -> MusicOrderOption.Album.entries
                        is MusicOrderOption.Playlist -> MusicOrderOption.Playlist.entries
                    } as List<MusicOrderOption>
                    ).toImmutableList(),
            onClickOption = { onChangedSortOrder.invoke(order.copy(option = it)) },
        )
    }
}

fun Activity.showSortDialog(
    musicViewModel: MusicViewModel,
    userData: UserData?,
    type: KClass<*>,
) {
    showAsButtonSheet(userData) { _ ->
        SortDialog(
            modifier = Modifier.fillMaxWidth(),
            order = when (type) {
                MusicOrderOption.Song::class -> musicViewModel.uiState.songOrder
                MusicOrderOption.Artist::class -> musicViewModel.uiState.artistOrder
                MusicOrderOption.Album::class -> musicViewModel.uiState.albumOrder
                MusicOrderOption.Playlist::class -> musicViewModel.uiState.playlistOrder
                else -> throw IllegalArgumentException("Unknown type: $type")
            },
            onChangedSortOrder = musicViewModel::setSortOrder,
        )
    }
}
