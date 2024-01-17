import com.android.build.api.variant.BuildConfigField
import org.jetbrains.kotlin.konan.properties.Properties
import java.io.Serializable

plugins {
    id("kanade.library")
    id("kanade.library.compose")
    id("kanade.hilt")
    id("kanade.detekt")
}

android {
    namespace = "caios.android.kanade.core.ui"
    val localProperties = Properties().apply {
        load(project.rootDir.resolve("local.properties").inputStream())
    }
    androidComponents {
        onVariants {
            it.buildConfigFields.apply {
                putBuildConfig(localProperties, "HOME_NATIVE")
                putBuildConfig(localProperties, "FEED_PODCAST_NATIVE")
                putBuildConfig(localProperties, "PLAYER_NATIVE")
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:repository"))
    implementation(project(":core:datastore"))
    implementation(project(":core:design"))
    implementation(project(":core:music"))
    api(project(":core:analytic"))

    implementation(libs.bundles.ui.implementation)
    kapt(libs.bundles.ui.kapt)
    implementation(libs.androidx.palette)
    implementation(libs.reorderble.compose)
}

fun MapProperty<String, BuildConfigField<out Serializable>>.putBuildConfig(
    localProperties: Properties,
    key: String,
    value: String? = null,
    type: String = "String",
    comment: String? = null
) {
    put(key, BuildConfigField(type, value ?: localProperties.getProperty(key) ?: System.getenv(key) ?: "\"\"", comment))
}
