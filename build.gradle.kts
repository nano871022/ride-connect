// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.legacy.kapt) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
}

subprojects {
    afterEvaluate {
        plugins.withId("com.android.library") {
            configure<com.android.build.api.dsl.LibraryExtension> {
                compileSdk = 37
            }
        }
        plugins.withId("com.android.application") {
            configure<com.android.build.api.dsl.ApplicationExtension> {
                compileSdk = 37
            }
        }
    }
}
