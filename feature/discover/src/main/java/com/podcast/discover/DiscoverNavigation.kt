package com.podcast.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.podcast.EntryItem
import com.podcast.core.network.api.Genres

const val DiscoverRoute = "discoverTop"

fun NavController.navigateToDiscover(navOptions: NavOptions? = null) {
    this.navigate(DiscoverRoute, navOptions)
}

fun NavGraphBuilder.discoverScreen(
    topMargin: Dp,
    navigateToFeedDetail: (String) -> Unit,
    navigateToFeedMore: (List<EntryItem>, genres: Genres, title: Int) -> Unit,
    navSearchWith: (id: Int) -> Unit,
    windowSize: WindowSizeClass
) {
    composable(
        route = DiscoverRoute,
        enterTransition = {
            when (initialState.destination.route) {
                "playlistTop", "songTop", "artistTop", "albumTop" -> NavigateAnimation.Library.enter
                else -> NavigateAnimation.Vertical.popEnter
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                "playlistTop", "songTop", "artistTop", "albumTop" -> NavigateAnimation.Library.exit
                else -> NavigateAnimation.Vertical.exit
            }
        },
    ) {
        DiscoverRouter(
            modifier = Modifier.fillMaxSize(),
            topMargin = topMargin,
            navigateToFeedDetail = navigateToFeedDetail,
            navigateToFeedMore = navigateToFeedMore,
            navSearchWith = navSearchWith,
            windowSize = windowSize
        )
    }
}
