package caios.android.kanade.core.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions

@SuppressLint("RestrictedApi")
fun NavController.navigate(
    route: String,
    args: Bundle,
    builder: (NavOptionsBuilder.() -> Unit)? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    val routeLink =
        NavDeepLinkRequest.Builder.fromUri(NavDestination.createRoute(route).toUri()).build()

    val deepLinkMatch = graph.matchDeepLink(routeLink)
    if (deepLinkMatch != null) {
        val destination = deepLinkMatch.destination
        val id = destination.id
        navigate(id, args, builder?.let { navOptions(it) }, navigatorExtras)
    } else {
        navigate(route, builder?.let { navOptions(it) }, navigatorExtras)
    }
}