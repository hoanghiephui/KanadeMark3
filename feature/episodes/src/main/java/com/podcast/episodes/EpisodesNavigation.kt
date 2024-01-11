package com.podcast.episodes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import caios.android.kanade.core.design.animation.NavigateAnimation
import caios.android.kanade.core.model.music.Song
import kotlin.reflect.KClass

const val EpisodesRoute = "Episodes"

fun NavController.navigateToEpisodes(navOptions: NavOptions? = null) {
    this.navigate(EpisodesRoute, navOptions)
}

fun NavGraphBuilder.episodesScreen(
    topMargin: Dp,
    navigateToSongMenu: (Song) -> Unit,
    navigateToSort: (KClass<*>) -> Unit,
    showSnackBar: (String) -> Unit
) {
    composable(
        route = EpisodesRoute,
        enterTransition = {
            when (initialState.destination.route) {
                "homeTop", "playlistTop", "artistTop", "albumTop" -> NavigateAnimation.Library.enter
                else -> NavigateAnimation.Vertical.popEnter
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                "homeTop", "playlistTop", "artistTop", "albumTop" -> NavigateAnimation.Library.exit
                else -> NavigateAnimation.Vertical.exit
            }
        },
    ) {
        EpisodesRouter(
            modifier = Modifier.fillMaxSize(),
            topMargin = topMargin,
            navigateToSongMenu = navigateToSongMenu,
            navigateToSort = navigateToSort,
            showSnackBar = showSnackBar
        )
    }
}
