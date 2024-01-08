package caios.android.kanade.core.ui.music

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.Blue40
import caios.android.kanade.core.design.theme.Green40
import caios.android.kanade.core.design.theme.Orange40
import caios.android.kanade.core.design.theme.Purple40
import caios.android.kanade.core.design.theme.Teal40
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.ui.util.extraSize
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size

@Composable
fun Artwork(
    artwork: Artwork,
    modifier: Modifier = Modifier,
    isLockAspect: Boolean = true,
    size: Size = Size.ORIGINAL
) {
    val artworkModifier = if (isLockAspect) modifier.aspectRatio(1f) else modifier

    when (artwork) {
        is Artwork.Internal -> ArtworkFromInternal(artwork, artworkModifier)
        is Artwork.MediaStore -> ArtworkFromMediaStore(artwork, artworkModifier)
        is Artwork.Web -> ArtworkFromWeb(artwork, artworkModifier, size)
        is Artwork.Unknown -> ArtworkFromUnknown(artworkModifier)
    }
}

@Composable
fun ArtworkFromWeb(
    artwork: Artwork.Web,
    modifier: Modifier = Modifier,
    size: Size = Size.ORIGINAL
) {
    SubcomposeAsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(artwork.url)
            .crossfade(true)
            .size(size)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                ArtworkFromInternal(
                    modifier = modifier,
                    artwork = Artwork.Internal("PO")
                )
                CircularProgressIndicator()
            }
        },
        error = {
            ArtworkFromInternal(
                modifier = modifier,
                artwork = Artwork.Internal("PO")
            )
        }
    )
}

@Composable
private fun ArtworkFromMediaStore(
    artwork: Artwork.MediaStore,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(artwork.uri)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun ArtworkFromUnknown(
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.logo_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun ArtworkFromInternal(
    artwork: Artwork.Internal,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val name = remember { artwork.name.replace(Regex("\\s+"), "") }
    val char1 = remember { name.elementAtOrNull(0)?.uppercase() ?: "?" }
    val char2 = remember { name.elementAtOrNull(1)?.uppercase() ?: char1 }

    val backgroundColor = when (name.toList().sumOf { it.code } % 5) {
        0 -> Blue40
        1 -> Green40
        2 -> Orange40
        3 -> Purple40
        4 -> Teal40
        else -> throw IllegalArgumentException("Unknown album name.")
    }

    BoxWithConstraints(
        modifier = modifier
            .background(backgroundColor)
            .clipToBounds(),
    ) {
        val boxHeight = with(LocalDensity.current) { constraints.maxHeight.toDp() }
        val fontSize = with(density) { (boxHeight * 1.4f).toSp() }
        val xOffset = boxHeight * 0.15f
        val yOffset = boxHeight * 0.05f
        val xOffsetPlus = boxHeight * 0.02f
        val yOffsetPlus = boxHeight * 0.02f

        Text(
            modifier = Modifier
                .fillMaxHeight()
                .extraSize(0.3f, 0.3f)
                .aspectRatio(1f)
                .align(Alignment.Center)
                .offset(-xOffset + xOffsetPlus, -yOffset + yOffsetPlus)
                .rotate(20f),
            text = char1,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.3f),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = Modifier
                .fillMaxHeight()
                .extraSize(0.3f, 0.3f)
                .aspectRatio(1f)
                .align(Alignment.Center)
                .offset(xOffset + xOffsetPlus, yOffset + yOffsetPlus)
                .rotate(20f),
            text = char2,
            style = TextStyle(
                color = Color.White.copy(alpha = 0.3f),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Artwork(
        modifier = Modifier,
        artwork = Artwork.Internal("ABC"),
    )
}
