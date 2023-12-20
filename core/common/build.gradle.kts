plugins {
    id("kanade.library")
    id("kanade.detekt")
    id("kanade.hilt")
}

android {
    namespace = "caios.android.kanade.core.common"
}

dependencies {
    api(libs.bundles.infra.api)
    implementation(libs.libraries.core)
    implementation(libs.moshi.kotlin)
    implementation("org.apache.commons:commons-lang3:3.11")
}
