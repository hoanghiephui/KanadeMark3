package com.podcast.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.podcast.EntryItem

const val DiscoverRoute = "discoverTop"

fun NavController.navigateToDiscover(navOptions: NavOptions? = null) {
    this.navigate(DiscoverRoute, navOptions)
}

fun NavGraphBuilder.discoverScreen(
    topMargin: Dp,
    navigateToFeedDetail: (String) -> Unit,
    navigateToFeedMore: (List<EntryItem>) -> Unit
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
            navigateToFeedMore = navigateToFeedMore
        )
    }
}
