plugins {
    id("kanade.library")
    id("kanade.detekt")
    id("kanade.hilt")
    id("com.android.room")
}

android {
    namespace = "com.android.podcast.core.database"
}

dependencies {
    api(libs.bundles.infra.api)

    implementation(libs.androidx.room)
    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.room.compiler)
}
