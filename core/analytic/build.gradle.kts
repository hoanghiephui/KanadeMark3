plugins {
    id("kanade.library")
    id("kanade.hilt")
    id("kanade.library.compose")
    id("kanade.detekt")
}

android {
    namespace = "com.podcast.analytic"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.core)
    implementation(libs.firebase.analytics)
    implementation(libs.kotlinx.coroutines.android)
}
