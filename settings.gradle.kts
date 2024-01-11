@file:Suppress("UnstableApiUsage")

include(":feature:episodes")


include(":core:analytic")


include(":feature:discover")


include(":core:usecase")


include(":core:network")


pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://www.jitpack.io")
        maven(url = "https://chaquo.com/maven-test")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")
    }
}

rootProject.name = "KanadeMark3"

include(":app")
include(":core")
include(":core:common")
include(":core:design")
include(":core:model")
include(":core:datastore")
include(":core:repository")
include(":core:ui")
include(":core:music")
include(":core:database")
include(":core:billing")
include(":feature:home")
include(":feature:playlist")
include(":feature:song")
include(":feature:artist")
include(":feature:album")
include(":feature:menu")
include(":feature:queue")
include(":feature:search")
include(":feature:sort")
include(":feature:lyrics")
include(":feature:information")
include(":feature:setting")
include(":feature:share")
include(":feature:tag")
include(":feature:download")
include(":feature:report")
include(":feature:equalizer")
include(":feature:billing")
include(":feature:widget")
include(":feature:welcome")
include(":in-app-update")
project(":in-app-update").projectDir = File("In-App-Update/inappupdatecompose")
