package com.podcast.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import caios.android.kanade.core.design.R.*
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.ui.navigate
import com.podcast.core.network.api.Genres

const val DiscoverMoreRoute = "DiscoverMoreRoute"
const val DiscoverMoreData_Genres = "DiscoverMoreData_Genres"
const val DiscoverMoreData_Title = "DiscoverMoreData_Title"
fun NavController.navigateToDiscoverMore(genres: Genres, title: Int) {
    val bundle = bundleOf(
        DiscoverMoreData_Genres to genres.id,
        DiscoverMoreData_Title to title
    )
    this.navigate(route = DiscoverMoreRoute, bundle, builder = {
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
        val title: Int = it.arguments?.getInt(DiscoverMoreData_Title) ?: string.discover
        DiscoverMoreRouter(
            modifier = Modifier.fillMaxSize(),
            onClickPodcast = navigateToFeedDetail,
            onTerminate = terminate,
            title = title
        )
    }
}
