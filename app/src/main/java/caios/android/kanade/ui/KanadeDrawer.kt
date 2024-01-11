package caios.android.kanade.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavDestination
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.theme.bold
import caios.android.kanade.core.model.UserData
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.music.Artwork
import caios.android.kanade.navigation.LibraryDestination
import caios.android.kanade.navigation.isLibraryDestinationInHierarchy
import kotlinx.coroutines.launch

@Composable
fun PodcastDrawer(
    state: DrawerState,
    userData: UserData?,
    currentSong: Song?,
    currentDestination: NavDestination?,
    onClickItem: (LibraryDestination) -> Unit,
    navigateToQueue: () -> Unit,
    navigateToMediaScan: () -> Unit,
    navigateToDownloadInput: () -> Unit,
    navigateToSetting: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToSupport: () -> Unit,
    navigateToBillingPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Column(
            modifier = modifier
                .width(256.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            NavigationDrawerHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp),
                song = currentSong,
            )

            Spacer(Modifier.height(16.dp))

            NavigationDrawerItem(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Home),
                label = stringResource(R.string.navigation_home),
                icon = Icons.Default.Home,
                onClick = { onClickItem.invoke(LibraryDestination.Home) },
            )

            NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_podcast),
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Discover),
                icon = Icons.Default.Podcasts,
                onClick = { onClickItem.invoke(LibraryDestination.Discover) },
            )

            NavigationDrawerItem(
                state = state,
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Song),
                label = stringResource(R.string.navigation_song),
                icon = Icons.Filled.MusicNote,
                onClick = { onClickItem.invoke(LibraryDestination.Song) },
            )

            NavigationDrawerItem(
                state = state,
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Artist),
                label = stringResource(R.string.navigation_artist),
                icon = Icons.Filled.Person,
                onClick = { onClickItem.invoke(LibraryDestination.Artist) },
            )

            NavigationDrawerItem(
                state = state,
                isSelected = currentDestination.isLibraryDestinationInHierarchy(LibraryDestination.Album),
                label = stringResource(R.string.navigation_album),
                icon = Icons.Default.Album,
                onClick = { onClickItem.invoke(LibraryDestination.Album) },
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
            )

            NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_playlist),
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                onClick = { onClickItem.invoke(LibraryDestination.Playlist) },
            )

            NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_queue),
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                onClick = navigateToQueue,
            )

            NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_scan),
                icon = Icons.Default.Scanner,
                onClick = navigateToMediaScan,
            )

            NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_download),
                icon = Icons.Outlined.CloudDownload,
                onClick = navigateToDownloadInput,
            )

            NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_setting),
                icon = Icons.Default.Settings,
                onClick = navigateToSetting,
            )

            /*NavigationDrawerItem(
                state = state,
                label = stringResource(R.string.navigation_app_info),
                icon = Icons.Outlined.Info,
                onClick = navigateToAbout,
            )*/

            /* NavigationDrawerItem(
                 state = state,
                 label = stringResource(R.string.navigation_support),
                 icon = Icons.Default.Redeem,
                 onClick = navigateToSupport,
             )*/

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
            )

            NavigationDrawerPlusItem(
                state = state,
                isPlusMode = userData?.isPlusMode == true,
                isDeveloperMode = userData?.isDeveloperMode == true,
                onClick = navigateToBillingPlus,
            )
        }
    }
}

@Composable
private fun NavigationDrawerItem(
    state: DrawerState,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val containerColor: Color
    val contentColor: Color

    if (isSelected) {
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        contentColor = MaterialTheme.colorScheme.primary
    } else {
        containerColor = Color.Transparent
        contentColor = MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .padding(end = 16.dp)
            .clip(
                RoundedCornerShape(
                    topEnd = 32.dp,
                    bottomEnd = 32.dp,
                ),
            )
            .background(containerColor)
            .clickable {
                scope.launch {
                    state.close()
                    onClick.invoke()
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun NavigationDrawerHeader(
    song: Song?,
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier) {
        val (albumArtwork, artistArtwork, shadow, title, artist) = createRefs()

        Artwork(
            modifier = Modifier.constrainAs(albumArtwork) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)

                width = Dimension.fillToConstraints
            },
            artwork = song?.albumArtwork ?: Artwork.Unknown,
            isLockAspect = false,
        )

        Box(
            modifier = Modifier
                .constrainAs(shadow) {
                    top.linkTo(albumArtwork.top)
                    start.linkTo(albumArtwork.start)
                    end.linkTo(albumArtwork.end)
                    bottom.linkTo(albumArtwork.bottom)

                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    ),
                ),
        )

        Card(
            modifier = Modifier
                .size(56.dp)
                .constrainAs(artistArtwork) {
                    start.linkTo(parent.start, 16.dp)
                    bottom.linkTo(parent.bottom, 24.dp)
                },
            shape = RoundedCornerShape(50),
        ) {
            Artwork(
                modifier = Modifier.fillMaxSize(),
                artwork = song?.artistArtwork ?: Artwork.Unknown,
            )
        }

        Text(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(artistArtwork.top, 4.dp)
                start.linkTo(artistArtwork.end, 16.dp)
                end.linkTo(parent.end, 24.dp)
                bottom.linkTo(artist.top)

                width = Dimension.fillToConstraints
            },
            text = song?.title ?: stringResource(R.string.music_unknown_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            modifier = Modifier.constrainAs(artist) {
                top.linkTo(title.bottom)
                start.linkTo(title.start)
                end.linkTo(title.end)
                bottom.linkTo(artistArtwork.bottom, 4.dp)

                width = Dimension.fillToConstraints
            },
            text = song?.artist ?: stringResource(R.string.music_unknown_artist),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NavigationDrawerPlusItem(
    isPlusMode: Boolean,
    isDeveloperMode: Boolean,
    state: DrawerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val titleStyle = MaterialTheme.typography.titleMedium.bold()
    val title: AnnotatedString
    val description: String

    if (isPlusMode) {
        title = buildAnnotatedString {
            withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
                append("Podcast+")
            }
        }
        description = stringResource(R.string.navigation_kanade_plus_purchased_description)
    } else {
        title = buildAnnotatedString {
            append("Buy ")
            withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
                append("Podcast+")
            }
        }
        description = stringResource(R.string.navigation_kanade_plus_description)
    }

    Row(
        modifier = modifier
            .clickable {
                scope.launch {
                    if (!isPlusMode || isDeveloperMode) {
                        state.close()
                        onClick.invoke()
                    }
                }
            }
            .padding(top = 8.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = titleStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
