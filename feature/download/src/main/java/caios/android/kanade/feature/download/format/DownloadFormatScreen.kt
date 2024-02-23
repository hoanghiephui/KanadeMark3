package caios.android.kanade.feature.download.format

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import caios.android.kanade.core.common.network.util.StringUtil.toHttpsUrl
import caios.android.kanade.core.common.network.util.ToastUtil
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.download.VideoInfo
import caios.android.kanade.core.ui.AsyncLoadContents
import caios.android.kanade.core.ui.TrackScreenViewEvent
import caios.android.kanade.core.ui.dialog.SimpleAlertDialog
import caios.android.kanade.core.ui.view.KanadeTopAppBar
import caios.android.kanade.feature.download.format.items.DownloadCautionItem
import caios.android.kanade.feature.download.format.items.DownloadFormatItem
import caios.android.kanade.feature.download.format.items.DownloadFormatSubTitle
import caios.android.kanade.feature.download.format.items.DownloadFormatVideoPreviewSection
import caios.android.kanade.feature.download.format.items.DownloadProgressDialog
import com.hippo.unifile.UniFile
import kotlinx.coroutines.launch

@Composable
internal fun DownloadFormatRoute(
    videoInfo: VideoInfo,
    navigateToBillingPlus: () -> Unit,
    navigateToTagEdit: (Long) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadFormatViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LaunchedEffect(videoInfo) {
        viewModel.fetch(context, videoInfo)
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        DownloadFormatScreen(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            isPlusUser = it.isPlusUser,
            videoInfo = it.videoInfo,
            downloadState = it.downloadState,
            saveUniFile = it.saveUniFile,
            onBillingPlus = navigateToBillingPlus,
            onClickTagEdit = navigateToTagEdit,
            onDownload = viewModel::download,
            onUpdateSaveUri = viewModel::updateSaveUri,
            onTerminate = terminate,
        )
    }

    TrackScreenViewEvent("DownloadFormatScreen")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadFormatScreen(
    isPlusUser: Boolean,
    videoInfo: VideoInfo,
    downloadState: DownloadFormatUiState.DownloadState?,
    saveUniFile: UniFile?,
    onBillingPlus: () -> Unit,
    onClickTagEdit: (Long) -> Unit,
    onDownload: (Context, VideoInfo, VideoInfo.Format, Boolean, UniFile) -> Unit,
    onUpdateSaveUri: (Context, Uri) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (videoInfo.formats.isNullOrEmpty()) {
        onTerminate.invoke()
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appBarState = rememberTopAppBarState()
    val gridState = rememberLazyGridState()
    val behavior = TopAppBarDefaults.pinnedScrollBehavior(appBarState)

    var selectedFormat by remember { mutableStateOf<SelectedItem>(SelectedItem.Suggested) }
    var isSaveUriError by remember { mutableStateOf(false) }
    var isShowBackAlert by remember { mutableStateOf(false) }
    var isShowProgressDialog by remember { mutableStateOf(false) }

    val audioFormats = videoInfo.formats!!.filter { it.acodec != "none" && it.vcodec == "none" }.reversed()
    val videoFormats = videoInfo.formats!!.filter { it.acodec != "none" && it.vcodec != "none" }.reversed()
    val suggestFormat = audioFormats.maxByOrNull { it.tbr ?: 0.0 }

    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                context.contentResolver.takePersistableUriPermission(it, flag)
                onUpdateSaveUri.invoke(context, it)
            }
        },
    )

    DisposableEffect(downloadState) {
        if (downloadState is DownloadFormatUiState.DownloadState.Progress) {
            isShowProgressDialog = true
        }

        if (downloadState is DownloadFormatUiState.DownloadState.Failed) {
            isShowProgressDialog = false
            ToastUtil.show(context, R.string.download_progress_toast_error)
        }

        if (downloadState is DownloadFormatUiState.DownloadState.Complete) {
            ToastUtil.show(context, R.string.download_progress_toast_complete)
        }

        onDispose { }
    }

    BackHandler(!isShowBackAlert && !isShowProgressDialog) {
        isShowBackAlert = true
    }

    if (isShowBackAlert) {
        SimpleAlertDialog(
            title = R.string.common_caution,
            message = R.string.download_format_alert_message,
            positiveText = R.string.common_back,
            negativeText = R.string.common_cancel,
            onPositiveClick = { onTerminate.invoke() },
            onNegativeClick = { isShowBackAlert = false },
            onDismiss = { isShowBackAlert = false },
        )
    }

    if (isShowProgressDialog) {
        val progress: Float
        val songId: Long?

        when (downloadState) {
            is DownloadFormatUiState.DownloadState.Complete -> {
                progress = 1f
                songId = downloadState.songId
            }
            is DownloadFormatUiState.DownloadState.Progress -> {
                progress = downloadState.progress
                songId = null
            }
            else -> return
        }

        DownloadProgressDialog(
            progress = progress,
            title = videoInfo.title,
            author = videoInfo.uploader ?: videoInfo.channel,
            thumbnail = videoInfo.thumbnail?.toHttpsUrl(),
            onClickTagEdit = { songId?.let { onClickTagEdit.invoke(it) } },
            onDismiss = {
                isShowProgressDialog = false
                onTerminate.invoke()
            },
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            KanadeTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.download_format_title),
                behavior = behavior,
                onTerminate = { isShowBackAlert = true },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    if (!isPlusUser) {
                        ToastUtil.show(context, R.string.billing_plus_toast_require_plus)
                        onBillingPlus.invoke()

                        return@FloatingActionButton
                    }

                    if (saveUniFile == null) {
                        scope.launch {
                            isSaveUriError = true
                            gridState.animateScrollToItem(1)
                        }

                        return@FloatingActionButton
                    }

                    val format = when (val selected = selectedFormat) {
                        is SelectedItem.Suggested -> suggestFormat!!
                        is SelectedItem.Audio -> audioFormats[selected.index]
                        is SelectedItem.Video -> videoFormats[selected.index]
                    }

                    onDownload.invoke(context, videoInfo, format, selectedFormat !is SelectedItem.Video, saveUniFile)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                )
            }
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize(),
            state = gridState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            columns = GridCells.Adaptive(144.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                DownloadFormatVideoPreviewSection(
                    title = videoInfo.title,
                    author = videoInfo.uploader ?: videoInfo.channel,
                    duration = videoInfo.duration?.toLong(),
                    thumbnail = videoInfo.thumbnail?.toHttpsUrl(),
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                CompositionLocalProvider(LocalTextInputService provides null) {
                    OutlinedTextField(
                        modifier = Modifier
                            .focusable(false)
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        value = saveUniFile?.filePath ?: "",
                        onValueChange = { onUpdateSaveUri.invoke(context, it.toUri()) },
                        isError = isSaveUriError,
                        readOnly = true,
                        label = { Text(stringResource(R.string.download_format_save_path)) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    isSaveUriError = false
                                    safLauncher.launch(saveUniFile?.uri)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }

            if (suggestFormat != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DownloadFormatSubTitle(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            bottom = 4.dp,
                        ),
                        title = R.string.download_format_suggestion,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    DownloadFormatItem(
                        modifier = Modifier.fillMaxWidth(),
                        format = suggestFormat,
                        isSelect = selectedFormat is SelectedItem.Suggested,
                        onSelect = { selectedFormat = SelectedItem.Suggested },
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                DownloadFormatSubTitle(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 4.dp,
                    ),
                    title = R.string.download_format_audio,
                )
            }

            itemsIndexed(audioFormats) { index, format ->
                DownloadFormatItem(
                    modifier = Modifier.fillMaxWidth(),
                    format = format,
                    isSelect = (selectedFormat as? SelectedItem.Audio)?.index == index,
                    onSelect = { selectedFormat = SelectedItem.Audio(index) },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                DownloadFormatSubTitle(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        bottom = 4.dp,
                    ),
                    title = R.string.download_format_video,
                )
            }

            itemsIndexed(videoFormats) { index, format ->
                DownloadFormatItem(
                    modifier = Modifier.fillMaxWidth(),
                    format = format,
                    isSelect = (selectedFormat as? SelectedItem.Video)?.index == index,
                    onSelect = { selectedFormat = SelectedItem.Video(index) },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                DownloadCautionItem(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                )
            }
        }
    }
}

private sealed interface SelectedItem {
    data object Suggested : SelectedItem
    data class Audio(val index: Int) : SelectedItem
    data class Video(val index: Int) : SelectedItem
}
