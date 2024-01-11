plugins {
    id("kanade.library")
    id("kanade.library.compose")
    id("kanade.detekt")
}

android {
    namespace = "caios.android.kanade.core.design"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.bundles.ui.implementation)
    kapt(libs.bundles.ui.kapt)
    api(libs.lottie)
    api(libs.lottie.compose)
    implementation(libs.coil.compose)
}
