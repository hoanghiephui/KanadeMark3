package caios.android.kanade.core.music

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationManagerCompat
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.databinding.LayoutDefaultArtworkBinding
import caios.android.kanade.core.design.theme.Blue40
import caios.android.kanade.core.design.theme.Green40
import caios.android.kanade.core.design.theme.Orange40
import caios.android.kanade.core.design.theme.Purple40
import caios.android.kanade.core.design.theme.Teal40
import caios.android.kanade.core.model.NotificationConfigs
import caios.android.kanade.core.model.music.Artwork
import caios.android.kanade.core.model.music.Song
import caios.android.kanade.core.model.player.PlayerState
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.flow.first
import timber.log.Timber

class NotificationManager(
    private val service: Service,
    private val mediaSession: MediaSessionCompat,
    private val musicController: MusicController,
) {
    private val manager = NotificationManagerCompat.from(service.baseContext)
    private val notifyConfig = NotificationConfigs.music

    init {
        createNotificationChannel()
    }

    @SuppressLint("MissingPermission")
    suspend fun setForegroundService(isForeground: Boolean) {
        Timber.d("setForegroundService: $isForeground")

        val notification = createMusicNotification(
            context = service.baseContext,
            song = musicController.currentSong.first(),
        )

        if (isForeground) {
            service.startForeground(notifyConfig.notifyId, notification)
        } else {
            manager.notify(notifyConfig.notifyId, notification)
            service.stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun updateNotification() {
        val notification = createMusicNotification(
            context = service.baseContext,
            song = musicController.currentSong.first(),
        )

        manager.notify(notifyConfig.notifyId, notification)
    }

    @SuppressLint("WrongConstant")
    private suspend fun createMusicNotification(context: Context, song: Song?): Notification {
        val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("notify", true)
        }
        val mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val isPlaying = (musicController.playerState.first() == PlayerState.Playing)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val stopActionIntent = Intent(ACTION_PAUSE).apply { addFlags(0x01000000) }
        val stopActionPendingIntent = PendingIntent.getBroadcast(context, 99, stopActionIntent, pendingIntentFlags)

        val notificationBuilder = NotificationCompat.Builder(context, notifyConfig.channelId)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopActionPendingIntent),
            )
            .setLargeIcon(song?.albumArtwork?.toBitmap(context))
            .setSmallIcon(R.drawable.vec_songs_off)
            .setContentTitle(song?.title)
            .setContentText(song?.artist)
            .setAutoCancel(false)
            .setColorized(true)
            .setPriority(PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(mainPendingIntent)

        val playActionIntent = Intent(ACTION_PLAY).apply { addFlags(0x01000000) }
        val playActionPendingIntent = PendingIntent.getBroadcast(context, 99, playActionIntent, pendingIntentFlags)
        val playAction = NotificationCompat.Action(R.drawable.vec_play, null, playActionPendingIntent)

        val pauseActionIntent = Intent(ACTION_PAUSE).apply { addFlags(0x01000000) }
        val pauseActionPendingIntent = PendingIntent.getBroadcast(context, 99, pauseActionIntent, pendingIntentFlags)
        val pauseAction = NotificationCompat.Action(R.drawable.vec_pause, null, pauseActionPendingIntent)

        val skipToNextActionIntent = Intent(ACTION_SKIP_TO_NEXT).apply { addFlags(0x01000000) }
        val skipToNextActionPendingIntent = PendingIntent.getBroadcast(context, 99, skipToNextActionIntent, pendingIntentFlags)
        val skipToNextAction = NotificationCompat.Action(R.drawable.vec_skip_to_next, null, skipToNextActionPendingIntent)

        val skipToPreviousActionIntent = Intent(ACTION_SKIP_TO_PREVIOUS).apply { addFlags(0x01000000) }
        val skipToPreviousActionPendingIntent = PendingIntent.getBroadcast(context, 99, skipToPreviousActionIntent, pendingIntentFlags)
        val skipToPreviousAction = NotificationCompat.Action(R.drawable.vec_skip_to_previous, null, skipToPreviousActionPendingIntent)

        notificationBuilder.addAction(skipToPreviousAction)
        notificationBuilder.addAction(if (isPlaying) pauseAction else playAction)
        notificationBuilder.addAction(skipToNextAction)

        return notificationBuilder.build()
    }

    private fun createNotificationChannel() {
        if (manager.getNotificationChannel(notifyConfig.channelId) != null) return

        val channelName = service.baseContext.getString(R.string.notify_channel_music_name)
        val channelDescription = service.baseContext.getString(R.string.notify_channel_music_description)

        val channel = NotificationChannel(
            notifyConfig.channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = channelDescription
        }

        manager.createNotificationChannel(channel)
    }

    private suspend fun Artwork.toBitmap(context: Context): Bitmap? {
        try {
            val builder = when (this) {
                is Artwork.Internal -> {
                    val char1 = name.elementAtOrNull(0)?.uppercase() ?: "?"
                    val char2 = name.elementAtOrNull(1)?.uppercase() ?: char1

                    val backgroundColor = when (name.toList().sumOf { it.code } % 5) {
                        0 -> Blue40
                        1 -> Green40
                        2 -> Orange40
                        3 -> Purple40
                        4 -> Teal40
                        else -> throw IllegalArgumentException("Unknown album name.")
                    }

                    val binding = LayoutDefaultArtworkBinding.inflate(LayoutInflater.from(context))

                    binding.char1.text = char1
                    binding.char2.text = char2
                    binding.artworkLayout.setBackgroundColor(backgroundColor.toArgb())

                    return binding.root.toBitmap()
                }
                is Artwork.Web -> ImageRequest.Builder(context).data(url)
                is Artwork.MediaStore -> ImageRequest.Builder(context).data(uri)
                else -> return null
            }.allowHardware(false)

            val request = builder.build()
            val result = (ImageLoader(context).execute(request) as? SuccessResult)?.drawable

            return (result as? BitmapDrawable)?.bitmap
        } catch (e: Throwable) {
            Timber.e(e)
            return null
        }
    }

    private fun View.toBitmap(): Bitmap {
        measure(
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
        )

        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        layout(0, 0, measuredWidth, measuredHeight)
        draw(canvas)

        val scaleX = (0.7f * bitmap.width).toInt()
        val scaleY = (0.7f * bitmap.height).toInt()
        val startX = (bitmap.width - scaleX) / 2
        val startY = (bitmap.height - scaleY) / 2

        return Bitmap.createBitmap(bitmap, startX, startY, scaleX, scaleY, null, true)
    }

    companion object {
        const val ACTION_PLAY = "com.system.podcast.play"
        const val ACTION_PAUSE = "com.system.podcast.pause"
        const val ACTION_STOP = "com.system.podcast.stop"
        const val ACTION_SKIP_TO_NEXT = "com.system.podcast.skip_to_next"
        const val ACTION_SKIP_TO_PREVIOUS = "com.system.podcast.skip_to_previous"
    }
}
