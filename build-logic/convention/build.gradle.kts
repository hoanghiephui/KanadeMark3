import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.secret.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)
    implementation(libs.firebase.crashlytics)
    implementation(libs.gms.services)
    implementation(libs.gms.oss)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "kanade.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "kanade.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "kanade.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "kanade.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibraryGlance") {
            id = "kanade.library.glance"
            implementationClass = "AndroidLibraryGlanceConventionPlugin"
        }
        register("androidLibraryChaquopy") {
            id = "kanade.library.chaquopy"
            implementationClass = "AndroidLibraryChaquopyConventionPlugin"
        }
        register("androidHilt") {
            id = "kanade.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidDetekt") {
            id = "kanade.detekt"
            implementationClass = "AndroidDetektConventionPlugin"
        }
        register("androidRoom") {
            id = "com.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
    }
}
