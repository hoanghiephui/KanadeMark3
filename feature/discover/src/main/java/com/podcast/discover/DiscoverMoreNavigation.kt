package com.podcast.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import caios.android.kanade.core.design.animation.NavigateAnimation

const val DiscoverMoreRoute = "DiscoverMoreRoute"

fun NavController.navigateToDiscoverMore() {
    this.navigate(route = DiscoverMoreRoute, builder = {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.discoverMoreScreen(
    navigateToFeedDetail: (String) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = DiscoverMoreRoute,
        enterTransition = { NavigateAnimation.Vertical.enter },
        exitTransition = { NavigateAnimation.Vertical.exit },
        popEnterTransition = { NavigateAnimation.Vertical.popEnter },
        popExitTransition = { NavigateAnimation.Vertical.popExit },
    ) {
        DiscoverMoreRouter(
            modifier = Modifier.fillMaxSize(),
            onClickPodcast = navigateToFeedDetail,
            onTerminate = terminate
        )
    }
}
