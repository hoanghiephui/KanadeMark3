package com.podcast.discover.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import caios.android.kanade.core.common.network.extension.parcelableArrayList
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.ui.navigate
import kotlinx.collections.immutable.toImmutableList

const val OnlineDetailId = "onlineDetailId"
const val OnlineDetailUrl = "onlineDetailUrl"
const val OnlineDetailRoute = "onlineDetail"
const val FeedMoreRoute = "FeedMoreRoute"
const val FeedMoreData = "FeedMoreData"

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
    terminate: () -> Unit,
    showSnackBar: (String) -> Unit,
    onClickSeeAll: (List<Song>, String) -> Unit
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
            feedUrl = it.arguments?.getString(OnlineDetailUrl),
            onClickSeeAll = onClickSeeAll
        )
    }
}

///********************************************************/////////////////
fun NavController.navigateToFeedMore(
    feed: List<Song>,
    title: String
) {
    val bundle = bundleOf(
        FeedMoreData to feed,
        "Title" to title
    )
    this.navigate(route = FeedMoreRoute, args = bundle, builder = {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.feedMoreScreen(
    terminate: () -> Unit,
    showSnackBar: (String) -> Unit
) {
    composable(
        route = FeedMoreRoute,
        enterTransition = { NavigateAnimation.Vertical.enter },
        exitTransition = { NavigateAnimation.Vertical.exit },
        popEnterTransition = { NavigateAnimation.Vertical.popEnter },
        popExitTransition = { NavigateAnimation.Vertical.popExit },
    ) {
        val title = it.arguments?.getString("Title")
        val items: List<Song> =
            it.arguments?.parcelableArrayList(FeedMoreData) ?: return@composable
        FeedMoreRouter(
            onTerminate = terminate,
            showSnackBar = showSnackBar,
            feed = items.toImmutableList(),
            title = title
        )
    }
}