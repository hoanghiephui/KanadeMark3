package caios.android.kanade.core.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import caios.android.kanade.core.design.component.AdType
import caios.android.kanade.core.design.component.AdViewState
import caios.android.kanade.core.design.component.MaxTemplateNativeAdViewComposable
import caios.android.kanade.core.design.theme.center

@Composable
fun EmptyView(
    title: Int,
    content: Int,
    adViewState: AdViewState? = null,
    openBilling: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth(),
            painter = painterResource(R.drawable.vec_empty_music),
            contentDescription = "empty music",
        )

        Text(
            modifier = Modifier
                .padding(
                    top = 32.dp,
                    start = 24.dp,
                    end = 24.dp,
                )
                .fillMaxWidth(),
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium.center(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    start = 24.dp,
                    end = 24.dp,
                )
                .fillMaxWidth(),
            text = stringResource(content),
            style = MaterialTheme.typography.bodyMedium.center(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (adViewState != null) {
            MaxTemplateNativeAdViewComposable(
                adViewState = adViewState,
                adType = AdType.SMALL,
                showBilling = openBilling,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

    }
}
