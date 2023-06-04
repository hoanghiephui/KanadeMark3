package caios.android.kanade.feature.artist.top

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.model.Artist
import caios.android.kanade.core.ui.AsyncLoadContents

@Composable
internal fun ArtistTopRoute(
    topMargin: Dp,
    modifier: Modifier = Modifier,
    viewModel: ArtistTopViewModel = hiltViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(screenState) {
        ArtistTopScreen(
            artists = it ?: emptyList(),
            modifier = modifier,
            contentPadding = PaddingValues(top = topMargin),
        )
    }
}

@Composable
internal fun ArtistTopScreen(
    artists: List<Artist>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        items(
            items = artists,
            key = { it.artistId },
        ) {
            Text(
                text = it.artist,
                color = Color.Gray,
            )
        }
    }
}
