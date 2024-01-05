package com.podcast.discover.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.navigate

const val OnlineDetailId = "onlineDetailId"
const val OnlineDetailUrl = "onlineDetailUrl"
const val OnlineDetailRoute = "onlineDetail"

fun NavController.navigateToOnlineDetail(
    feedId: String,
    feedUrl: String? = null
) {
    val bundle = bundleOf(
        OnlineDetailId to feedId,
        OnlineDetailUrl to feedUrl
    )
    this.navigate(route = OnlineDetailRoute, args = bundle, builder = {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.feedDetailScreen(
    navigateToSongDetail: (String, List<Long>) -> Unit,
    navigateToAlbumDetail: (Long) -> Unit,
    navigateToArtistMenu: (Artist) -> Unit,
    navigateToSongMenu: (Song) -> Unit,
    navigateToAlbumMenu: (Album) -> Unit,
    terminate: () -> Unit,
    showSnackBar: (String) -> Unit
) {
    composable(
        route = OnlineDetailRoute,
        enterTransition = { NavigateAnimation.Vertical.enter },
        exitTransition = { NavigateAnimation.Vertical.exit },
        popEnterTransition = { NavigateAnimation.Vertical.popEnter },
        popExitTransition = { NavigateAnimation.Vertical.popExit },
    ) {
        OnlineFeedRoute(
            modifier = Modifier.fillMaxSize(),
            feedId = it.arguments?.getString(OnlineDetailId) ?: "",
            terminate = terminate,
            showSnackBar = showSnackBar,
            feedUrl = it.arguments?.getString(OnlineDetailUrl)
        )
    }
}
