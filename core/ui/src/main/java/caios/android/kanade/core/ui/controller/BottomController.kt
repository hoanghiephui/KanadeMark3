package caios.android.kanade.core.ui.controller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.design.theme.KanadeTheme
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.music.MusicUiState
import caios.android.kanade.core.ui.music.Artwork
import caios.android.kanade.core.ui.util.marquee

@Composable
fun BottomController(
    uiState: MusicUiState,
    onClickPlay: () -> Unit,
    onClickPause: () -> Unit,
    onClickSkipToNext: () -> Unit,
    onClickSkipToPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier) {
        val (indicator, artwork, title, artist, buttons) = createRefs()
        val position by animateFloatAsState(
            targetValue = uiState.progressParent,
            label = "bottomSliderPosition",
        )
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .constrainAs(indicator) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                        width = Dimension.fillToConstraints
                    }
                    .height(2.dp),
            )
        } else {
            // Determinate progress bar.
            LinearProgressIndicator(
                progress = { position },
                modifier = Modifier
                    .constrainAs(indicator) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                        width = Dimension.fillToConstraints
                    }
                    .height(2.dp),
            )
        }

        Artwork(
            modifier = Modifier
                .constrainAs(artwork) {
                    top.linkTo(indicator.bottom)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)

                    height = Dimension.fillToConstraints
                }
                .aspectRatio(1f),
            artwork = uiState.song?.albumArtwork ?: Artwork.Unknown,
        )

        Text(
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(parent.top, 16.dp)
                    start.linkTo(artwork.end, 8.dp)
                    end.linkTo(buttons.start, 8.dp)
                    bottom.linkTo(artist.top)

                    width = Dimension.fillToConstraints
                },
            text = uiState.song?.title ?: stringResource(R.string.music_unknown_title),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier
                .marquee()
                .constrainAs(artist) {
                    top.linkTo(title.bottom)
                    start.linkTo(artwork.end, 8.dp)
                    end.linkTo(buttons.start, 8.dp)
                    bottom.linkTo(parent.bottom, 16.dp)

                    width = Dimension.fillToConstraints
                },
            text = uiState.song?.artist ?: stringResource(R.string.music_unknown_artist),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            modifier = Modifier.constrainAs(buttons) {
                top.linkTo(parent.top)
                end.linkTo(parent.end, 8.dp)
                bottom.linkTo(parent.bottom)
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            IconButton(
                modifier = Modifier
                    .size(45.dp)
                    .padding(8.dp)
                    .border(
                        width = 1.dp,
                        color = LocalContentColor.current,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(4.dp),
                onClick = { onClickSkipToPrevious() },
                enabled = uiState.queueItems.size > 1
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Skip to previous",
                )
            }

            IconButton(modifier = Modifier
                .size(54.dp)
                .padding(8.dp)
                .border(
                    width = 1.dp,
                    color = LocalContentColor.current,
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(4.dp),
                enabled = uiState.song != null,
                onClick = { if (uiState.isPlaying) onClickPause() else onClickPlay() }) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play or Pause",
                )
            }

            IconButton(
                modifier = Modifier
                    .size(45.dp)
                    .padding(8.dp)
                    .border(
                        width = 1.dp,
                        color = LocalContentColor.current,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .padding(4.dp),
                onClick = { onClickSkipToNext() },
                enabled = uiState.queueItems.size > 1
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Skip to next",
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    KanadeTheme {
        KanadeBackground {
            BottomController(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                uiState = MusicUiState(),
                onClickPlay = {},
                onClickPause = {},
                onClickSkipToNext = {},
                onClickSkipToPrevious = {},
            )
        }
    }
}
