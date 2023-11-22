plugins {
    id("kanade.library")
    id("kanade.library.glance")
    id("kanade.hilt")
    id("kanade.detekt")
}

android {
    namespace = "caios.android.kanade.feature.widget"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:repository"))
    implementation(project(":core:datastore"))
    implementation(project(":core:design"))
    implementation(project(":core:music"))
    implementation(project(":core:ui"))
    implementation(project(":core:billing"))

    implementation(libs.bundles.ui.implementation)
    kapt(libs.bundles.ui.kapt)
}