pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "EVRideConnect"
include(":app")
include(":core")
include(":services:ble")
include(":services:database")
include(":services:llm")
include(":track")
include(":utils")
if (file("../japl-android-about-module").exists()) {
    include(":about")
    project(":about").projectDir = file("../japl-android-about-module")
}
include(":ui")
