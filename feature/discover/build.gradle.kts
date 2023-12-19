plugins {
    id("kanade.library")
    id("kanade.library.compose")
    id("kanade.hilt")
    id("kanade.detekt")
}

android {
    namespace = "com.podcast.discover"
}

dependencies {

    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:repository"))
    implementation(project(":core:datastore"))
    implementation(project(":core:design"))
    implementation(project(":core:music"))
    implementation(project(":core:ui"))
    implementation(project(":core:usecase"))

    implementation(libs.bundles.ui.implementation)
    kapt(libs.bundles.ui.kapt)
    implementation(libs.rssparser)
}
