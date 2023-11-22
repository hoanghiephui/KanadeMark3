plugins {
    id("kanade.library")
    id("kanade.detekt")
    id("kanade.hilt")
}

android {
    namespace = "com.podcast.core.network"
}

dependencies {

    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))

    implementation(libs.bundles.ktor)

    implementation(libs.androidx.media)

    implementation(libs.jsoup)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.urlconnection)
}
