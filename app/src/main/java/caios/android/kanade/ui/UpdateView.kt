package caios.android.kanade.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.play.core.ktx.AppUpdateResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import se.warting.inappupdate.compose.APP_UPDATE_REQUEST_CODE
import se.warting.inappupdate.compose.InAppUpdateState
import se.warting.inappupdate.compose.findActivity
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.DownloadLottie

@Composable
fun HeaderUpdate(
    updateState: InAppUpdateState,
    context: Context,
    rememberCoroutineScope: CoroutineScope,
    sheetState: (isShow: Boolean) -> Unit
) {
    BottomSheetHeader(
        iconPainter = painterResource(R.drawable.ic_baseline_update_24),
        iconTint = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
        title = "Software update",
        onCloseClick = {
            sheetState.invoke(false)
        }
    ) {
        when (val result = updateState.appUpdateResult) {
            is AppUpdateResult.NotAvailable -> {
                sheetState.invoke(false)
            }

            is AppUpdateResult.Available -> {
                Text(
                    text = stringResource(id = R.string.update_content),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 24.dp)
                )
                ElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 24.dp, end = 24.dp, bottom = 10.dp
                        ),
                    onClick = {
                        rememberCoroutineScope.launch {
                            result.startFlexibleUpdate(
                                context.findActivity(), APP_UPDATE_REQUEST_CODE
                            )
                            sheetState.invoke(false)
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.update_now))
                }

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }

            is AppUpdateResult.InProgress -> {
                val updateProgress: Long =
                    if (result.installState.totalBytesToDownload() == 0L) {
                        0L
                    } else {
                        (result.installState.bytesDownloaded() * 100L /
                                result.installState.totalBytesToDownload())
                    }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    DownloadLottie()
                }
                Text(
                    text = stringResource(id = R.string.downloading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                )
                val process = updateProgress.toFloat() / 100f
                LinearProgressIndicator(
                    progress = { process },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
                Text(
                    text = "${updateProgress}%",
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LaunchedEffect(key1 = Unit, block = {
                    sheetState.invoke(true)
                })
            }

            is AppUpdateResult.Downloaded -> {
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = stringResource(id = R.string.update_done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                )
                ElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    onClick = {
                        rememberCoroutineScope.launch {
                            sheetState.invoke(false)
                            result.completeUpdate()
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.install_now))
                }
            }
        }
    }
}

@Composable
fun BottomSheetHeader(
    iconPainter: Painter,
    title: String,
    onCloseClick: () -> Unit,
    iconTint: ColorFilter? = null,
    content: @Composable (ColumnScope.() -> Unit),
) {
    BottomSheetHeader(
        iconPainter = iconPainter,
        titleContent = {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                text = title,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium
            )
        },
        onCloseClick = onCloseClick,
        iconTint = iconTint,
        content = content
    )
}

@Composable
private fun BottomSheetHeader(
    iconPainter: Painter,
    titleContent: @Composable (RowScope.() -> Unit),
    onCloseClick: () -> Unit,
    iconTint: ColorFilter?,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 32.dp, top = 24.dp, end = 32.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = iconPainter,
                colorFilter = iconTint,
                contentDescription = null
            )
            titleContent.invoke(this)
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = null,
                )
            }
        }
        Column(
            content = content
        )
    }
}
