package caios.android.kanade.core.common.network.util

import android.content.Context
import android.content.Intent
import androidx.core.app.ShareCompat.IntentBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun shareLink(context: Context, text: String) {
    val intent: Intent = IntentBuilder(context)
        .setType("text/plain")
        .setText(text)
        .setChooserTitle("Share URL")
        .createChooserIntent()
    context.startActivity(intent)
}

fun shareFeedLink(context: Context, title: String, url: String) {
    val text: String = (title
            + "\n\n"
            + "https://antennapod.org/deeplink/subscribe/?url="
            + URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
            + "&title="
            + URLEncoder.encode(title, StandardCharsets.UTF_8.toString()))
    shareLink(context, text)
}