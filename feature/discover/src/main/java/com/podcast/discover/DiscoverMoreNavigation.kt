package com.podcast.discover

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import caios.android.kanade.core.common.network.extension.parcelableArrayList
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.podcast.EntryItem
import caios.android.kanade.core.ui.navigate
import kotlinx.collections.immutable.toImmutableList

const val DiscoverMoreRoute = "DiscoverMoreRoute"
const val DiscoverMoreData = "DiscoverMoreData"

fun NavController.navigateToDiscoverMore(items: List<EntryItem>) {
    val bundle = bundleOf(DiscoverMoreData to items)
    this.navigate(route = DiscoverMoreRoute, args = bundle, builder = {
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
        val items: List<EntryItem> =
            it.arguments?.parcelableArrayList(DiscoverMoreData) ?: return@composable
        DiscoverMoreRouter(
            modifier = Modifier.fillMaxSize(),
            feed = items.toImmutableList(),
            onClickPodcast = navigateToFeedDetail,
            onTerminate = terminate
        )
    }
}