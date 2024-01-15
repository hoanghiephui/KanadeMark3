package caios.android.kanade.core.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import caios.android.kanade.core.design.R
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCountry(
    currentCountryCode: String,
    selectCountry: (String) -> Unit,
    dismissDialog: () -> Unit
) {
    val countryCodeArray: List<String> = Locale.getISOCountries().asList()
    val (countryCodeNames, countryNameCodes) = countryCodeArray.associateWith { code ->
        val locale = Locale("", code)
        val countryName = locale.displayCountry
        countryName
    }.let { map -> map to map.entries.associateBy({ it.value }, { it.key }) }

    val countryNamesSort: List<String> = countryCodeNames.values.sorted()
    val state = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.select_country),
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        dismissDialog.invoke()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                }
            })
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it),
            state = state,
            content = {
                items(countryNamesSort) {
                    val backgroundColor = if (currentCountryCode == countryNameCodes[it])
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    else Color.Transparent
                    Box(
                        modifier = Modifier
                            .background(
                                backgroundColor,
                            )
                            .fillMaxWidth()
                            .clickable {
                                val countryName = it
                                if (countryNameCodes.containsKey(countryName)) {
                                    val countryCode =
                                        countryNameCodes[countryName] ?: return@clickable
                                    selectCountry.invoke(countryCode)
                                }

                            }
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                )
                                .fillMaxWidth()
                        )
                    }
                }
            }
        )
    }
    LaunchedEffect(key1 = Unit, block = {
        state.animateScrollToItem(
            countryCodeNames.keys.sorted().indexOf(currentCountryCode),
            scrollOffset = 50000
        )
    })
}
