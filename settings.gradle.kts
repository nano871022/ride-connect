pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven { url = java.net.URI("https://maven-central.storage-download.googleapis.com/maven2/") }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = java.net.URI("https://maven-central.storage-download.googleapis.com/maven2/") }
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
        maven { url = java.net.URI("https://maven-other.tuya.com/repository/maven-releases/") }
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
include(":about")
if (file("../japl-android-about-module").exists()) {
    project(":about").projectDir = file("../japl-android-about-module")
} else {
    val dummyAbout = file("build/tmp/about")
    dummyAbout.mkdirs()
    val buildFile = file("build/tmp/about/build.gradle.kts")
    if (!buildFile.exists()) {
        buildFile.writeText("""
            plugins {
                id("com.android.library")
                id("org.jetbrains.kotlin.plugin.compose")
            }
            android {
                namespace = "co.com.japl.homeconnect.about"
                compileSdk = 36
                defaultConfig { minSdk = 26 }
                buildFeatures { compose = true }
            }
            dependencies {
                implementation(platform(libs.androidx.compose.bom))
                implementation(libs.androidx.material3)
            }
        """.trimIndent())
        val srcDir = file("build/tmp/about/src/main/java/co/com/japl/homeconnect/about/ui")
        srcDir.mkdirs()
        file("build/tmp/about/src/main/java/co/com/japl/homeconnect/about/ui/About.kt").writeText("""
            package co.com.japl.homeconnect.about.ui
            import androidx.compose.material3.Text
            import androidx.compose.runtime.Composable
            @Composable
            fun About(versionDetail: String, applicationId: String) {
                Text(text = "About " + versionDetail)
            }
        """.trimIndent())
    }
    project(":about").projectDir = dummyAbout
}
include(":ui")
