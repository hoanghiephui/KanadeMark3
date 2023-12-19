package com.podcast.discover.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.music.Album
import caios.android.kanade.core.model.music.Artist
import caios.android.kanade.core.model.music.Song

const val OnlineDetailId = "onlineDetailId"
const val OnlineDetailRoute = "onlineDetail/{$OnlineDetailId}"

fun NavController.navigateToOnlineDetail(feedId: String) {
    this.navigate("onlineDetail/$feedId") {
        launchSingleTop = true
    }
}

fun NavGraphBuilder.feedDetailScreen(
    navigateToSongDetail: (String, List<Long>) -> Unit,
    navigateToAlbumDetail: (Long) -> Unit,
    navigateToArtistMenu: (Artist) -> Unit,
    navigateToSongMenu: (Song) -> Unit,
    navigateToAlbumMenu: (Album) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = OnlineDetailRoute,
        arguments = listOf(
            navArgument(OnlineDetailId) { type = NavType.StringType },
        ),
        enterTransition = { NavigateAnimation.Vertical.enter },
        exitTransition = { NavigateAnimation.Vertical.exit },
        popEnterTransition = { NavigateAnimation.Vertical.popEnter },
        popExitTransition = { NavigateAnimation.Vertical.popExit },
    ) {
        OnlineFeedRoute(
            modifier = Modifier.fillMaxSize(),
            feedId = it.arguments?.getString(OnlineDetailId) ?: "",
            terminate = terminate,
        )
    }
}
