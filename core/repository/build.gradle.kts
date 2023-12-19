plugins {
    id("kanade.library")
    id("kanade.detekt")
    id("kanade.hilt")
}

android {
    namespace = "caios.android.kanade.core.repository"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))

    implementation(libs.bundles.ktor)

    implementation(libs.androidx.media)
    implementation(libs.rssparser)
    implementation(libs.jsoup)
}
