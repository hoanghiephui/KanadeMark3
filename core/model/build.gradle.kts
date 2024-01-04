plugins {
    id("kanade.library")
    id("kanade.library.compose")
    id("kanade.detekt")
    id("kotlinx-serialization")
}

android {
    namespace = "caios.android.kanade.core.model"
}

dependencies {
    implementation(project(":core:common"))
    
    api(libs.bundles.infra.api)

    implementation(libs.androidx.media)
    implementation(libs.rssparser)
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
}
