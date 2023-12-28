package caios.android.kanade.core.ui.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.design.theme.applyTonalElevation
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.design.theme.center
import caios.android.kanade.core.design.theme.start
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.ui.music.Artwork
import caios.android.kanade.core.ui.music.MultiArtwork
import caios.android.kanade.core.ui.util.marquee
import coil.size.Size
import kotlinx.collections.immutable.toImmutableList

@Composable
fun PodcastCoordinatorScaffold(
    data: CoordinatorData,
    onClickNavigateUp: () -> Unit,
    onClickMenu: () -> Unit,
    clickSubscribe: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    color: Color = MaterialTheme.colorScheme.surface,
    content: LazyListScope.() -> Unit,
) {
    var appBarAlpha by remember { mutableFloatStateOf(0f) }
    var topSectionHeight by remember { mutableIntStateOf(100) }

    Box(modifier.background(color)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
        ) {
            item {
                if (data is CoordinatorData.Podcast) {
                    ArtistArtworkSection(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .onGloballyPositioned { topSectionHeight = it.size.height },
                        data = data,
                        color = color,
                        alpha = 1f - appBarAlpha,
                        clickSubscribe = clickSubscribe
                    )
                }
            }

            content(this)
        }

        CoordinatorToolBar(
            modifier = Modifier.fillMaxWidth(),
            title = when (data) {
                is CoordinatorData.Album -> data.title
                is CoordinatorData.Artist -> data.title
                is CoordinatorData.Podcast -> data.title
                is CoordinatorData.Playlist -> data.title
            },
            color = MaterialTheme.colorScheme.applyTonalElevation(
                backgroundColor = MaterialTheme.colorScheme.surface,
                elevation = 3.dp,
            ),
            backgroundAlpha = appBarAlpha,
            onClickNavigateUp = onClickNavigateUp,
            onClickMenu = onClickMenu,
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collect {
            val index = listState.firstVisibleItemIndex
            val disableArea = topSectionHeight * 0.4
            val alpha =
                if (index == 0) (listState.firstVisibleItemScrollOffset.toDouble() - disableArea) / (topSectionHeight - disableArea) else 1

            appBarAlpha = (alpha.toFloat() * 3).coerceIn(0f..1f)
        }
    }
}

@Composable
private fun AlbumArtworkSection(
    data: CoordinatorData.Album,
    alpha: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val titleStyle = MaterialTheme.typography.headlineSmall
    val summaryStyle = MaterialTheme.typography.bodyMedium
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(modifier) {
        Artwork(
            modifier = Modifier
                .blur(16.dp)
                .fillMaxWidth(),
            artwork = data.artwork,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, color))),
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .size(224.dp)
                .aspectRatio(1f)
                .alpha(alpha),
            shape = RoundedCornerShape(8.dp),
        ) {
            Artwork(
                modifier = Modifier.fillMaxWidth(),
                artwork = data.artwork,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 6.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .marquee()
                    .alpha(alpha),
                text = data.title,
                style = titleStyle.center().bold(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .alpha(alpha)
                    .animateContentSize(),
                text = data.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (!expanded) 3 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(text = if (expanded) "Show less" else "Show more")
                }
            }
        }
    }
}

@Composable
private fun ArtistArtworkSection(
    data: CoordinatorData.Podcast,
    alpha: Float,
    color: Color,
    modifier: Modifier = Modifier,
    clickSubscribe: (Boolean) -> Unit
) {
    val titleStyle = MaterialTheme.typography.titleLarge
    val summaryStyle = MaterialTheme.typography.bodyMedium
    var selected by remember { mutableStateOf(false) }
    Box(modifier) {
        Artwork(
            modifier = Modifier.fillMaxWidth(),
            artwork = data.artwork,
            size = Size(500, 500)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, color))),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row {
                Artwork(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8)),
                    artwork = data.artwork,
                    size = Size(500, 500)
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = data.title,
                        style = titleStyle.start().bold(),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        text = data.author,
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis,
                        style = summaryStyle.start()
                    )



                    FilterChip(
                        onClick = {
                            clickSubscribe.invoke(selected)
                            selected = !selected
                        },
                        label = {
                            Text(stringResource(id = if (selected) R.string.subscribed else R.string.subscribe))
                        },
                        selected = selected,
                        leadingIcon = if (selected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Done icon",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Done icon",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        },
                    )
                }

            }

        }
    }
}

@Composable
private fun PlaylistArtworkSection(
    data: CoordinatorData.Playlist,
    alpha: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val titleStyle = MaterialTheme.typography.headlineSmall
    val summaryStyle = MaterialTheme.typography.bodyMedium

    Box(modifier) {
        MultiArtwork(
            modifier = Modifier.fillMaxWidth(),
            artworks = data.artworks.toImmutableList(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, color))),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .marquee()
                    .alpha(alpha),
                text = data.title,
                style = titleStyle.center().bold(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .alpha(alpha),
                text = data.summary,
                style = summaryStyle.center(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordinatorToolBar(
    title: String,
    color: Color,
    backgroundAlpha: Float,
    onClickNavigateUp: () -> Unit,
    onClickMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = color.copy(backgroundAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = if (backgroundAlpha > 0.9f) 4.dp else 0.dp,
    ) {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
            ),
            navigationIcon = {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(50))
                        .clickable { onClickNavigateUp() }
                        .padding(8.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            },
            title = {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .alpha(backgroundAlpha),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            actions = {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(50))
                        .clickable { onClickMenu.invoke() }
                        .padding(8.dp),
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
        )
    }
}

@Preview
@Composable
private fun FillSectionPreview1() {
    KanadeBackground {
        AlbumArtworkSection(
            data = CoordinatorData.Album(
                title = "UNDERTALE",
                summary = "toby fox",
                artwork = Artwork.Internal("UNDERTALE"),
            ),
            alpha = 1f,
            color = Color.Black,
        )
    }
}

@Preview
@Composable
private fun FillSectionPreview2() {
    KanadeBackground {
        ArtistArtworkSection(
            data = CoordinatorData.Podcast(
                title = "toby fox",
                summary = "UNDERTALE",
                artwork = Artwork.Internal("UNDERTALE"),
                author = "Demo"
            ),
            alpha = 1f,
            color = Color.Black,
            clickSubscribe = {}
        )
    }
}

@Preview
@Composable
private fun FillSectionPreview3() {
    KanadeBackground {
        PlaylistArtworkSection(
            data = CoordinatorData.Playlist(
                title = "toby fox",
                summary = "UNDERTALE",
                artworks = Artwork.dummies(),
            ),
            alpha = 1f,
            color = Color.Black,
        )
    }
}
