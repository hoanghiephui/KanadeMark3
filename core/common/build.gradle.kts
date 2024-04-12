import com.android.build.api.variant.ResValue
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("kanade.library")
    id("kanade.detekt")
    id("kanade.hilt")
}

android {
    namespace = "caios.android.kanade.core.common"
    val localProperties = Properties().apply {
        load(project.rootDir.resolve("local.properties").inputStream())
    }
    androidComponents {
        onVariants {
            it.resValues.put(it.makeResValueKey("string", "APPLOVIN_SDK_KEY"),
                ResValue(localProperties.getProperty("KEY_APPLOVIN"), null)
            )
        }
    }
}

dependencies {
    api(libs.bundles.infra.api)
    implementation(libs.libraries.core)
    implementation(libs.moshi.kotlin)
    implementation(libs.androidx.lifecycle.compiler)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("org.apache.commons:commons-lang3:3.11")
}
