plugins {
    id("kanade.library")
    id("kanade.detekt")
    id("kanade.hilt")
}

android {
    namespace = "com.podcast.core.usecase"
}

dependencies {

    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:repository"))
}