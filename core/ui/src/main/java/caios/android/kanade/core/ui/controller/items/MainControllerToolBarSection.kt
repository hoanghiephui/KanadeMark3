package caios.android.kanade.core.ui.controller.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.KanadeBackground
import caios.android.kanade.core.design.theme.applyTonalElevation
import caios.android.kanade.core.ui.view.DropDownMenuItem
import caios.android.kanade.core.ui.view.DropDownMenuItemData

@Composable
internal fun MainControllerToolBarSection(
    onClickClose: () -> Unit,
    onClickSearch: () -> Unit,
    onClickMenuAddPlaylist: () -> Unit,
    onClickMenuArtist: () -> Unit,
    onClickMenuAlbum: () -> Unit,
    onClickMenuEqualizer: () -> Unit,
    onClickMenuEdit: () -> Unit,
    onClickMenuDetailInfo: () -> Unit,
    modifier: Modifier = Modifier,
    isPodcast: Boolean,
    isShowMenu: Boolean
) {
    var isExpandedMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClickClose.invoke() }
                .padding(4.dp),
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
        )

        Spacer(Modifier.weight(1f))

        Icon(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClickSearch.invoke() }
                .padding(4.dp),
            imageVector = Icons.Default.Search,
            contentDescription = null,
        )
        if (isShowMenu) {
            Box {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isExpandedMenu = !isExpandedMenu }
                        .padding(4.dp),
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                )

                DropdownMenu(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colorScheme.applyTonalElevation(
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                elevation = 3.dp,
                            ),
                        ),
                    expanded = isExpandedMenu,
                    onDismissRequest = { isExpandedMenu = false },
                ) {
                    val commonMenu = listOf(
                        DropDownMenuItemData(
                            text = R.string.controller_menu_add_playlist,
                            onClick = onClickMenuAddPlaylist,
                        ),
                        DropDownMenuItemData(
                            text = R.string.controller_menu_equalizer,
                            onClick = onClickMenuEqualizer,
                        ),
                        DropDownMenuItemData(
                            text = R.string.controller_menu_detail_info,
                            onClick = onClickMenuDetailInfo,
                        ),
                    )

                    val artistAlbumMenu = listOf(
                        DropDownMenuItemData(
                            text = R.string.controller_menu_artist,
                            onClick = onClickMenuArtist,
                        ),
                        DropDownMenuItemData(
                            text = R.string.controller_menu_album,
                            onClick = onClickMenuAlbum,
                        ),
                    )

                    val additionalMenu = listOf(
                        DropDownMenuItemData(
                            text = R.string.controller_menu_edit,
                            onClick = onClickMenuEdit,
                        )
                    )

                    val menus = if (isPodcast) {
                        commonMenu
                    } else {
                        commonMenu + artistAlbumMenu + additionalMenu
                    }

                    menus.map {
                        DropDownMenuItem(
                            text = it.text,
                            onClick = {
                                isExpandedMenu = false
                                it.onClick.invoke()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    KanadeBackground {
        MainControllerToolBarSection(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            onClickClose = { },
            onClickSearch = { },
            onClickMenuAddPlaylist = { },
            onClickMenuArtist = { },
            onClickMenuAlbum = { },
            onClickMenuEqualizer = { },
            onClickMenuEdit = { },
            onClickMenuDetailInfo = { },
            isPodcast = false,
            isShowMenu = true
        )
    }
}
