package caios.android.kanade.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import caios.android.kanade.core.design.R
import caios.android.kanade.core.model.UserData
import caios.android.kanade.feature.setting.SettingTextItem
import java.util.Locale

@Composable
internal fun SettingTopPodcastSection(
    userData: UserData,
    onClickLang: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val countryCodeArray: List<String> = Locale.getISOCountries().asList()
    val (countryCodeNames, countryNameCodes) = countryCodeArray.associateWith { code ->
        val locale = Locale("", code)
        val countryName = locale.displayCountry
        countryName
    }.let { map -> map to map.entries.associateBy({ it.value }, { it.key }) }

    val countryNames = countryCodeNames[userData.countryCode]

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.navigation_podcast,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = R.string.select_country,
            description = R.string.select_country_content,
            onValueChanged = {
                countryNameCodes[it]?.let { it1 -> onClickLang.invoke(it1) }
            },
            value = countryNames ?: ""
        )
    }
}
