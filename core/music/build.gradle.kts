plugins {
    id("kanade.library")
    id("kanade.library.compose")
    id("kanade.library.chaquopy")
    id("kanade.hilt")
    id("kanade.detekt")
}

android {
    namespace = "caios.android.kanade.core.music"
    buildFeatures {
        buildConfig = true
    }
}

chaquopy {
    defaultConfig {
        version = "3.8"

        pip {
            install("ytmusicapi")
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:design"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:repository"))

    implementation(libs.bundles.youtubedl)

    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.media)

    api(libs.google.exoplayer)
    implementation(libs.google.exoplayer.dash)
    //implementation(libs.google.exoplayer.hls)
    implementation(libs.google.exoplayer.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.ffmpeg)
}
