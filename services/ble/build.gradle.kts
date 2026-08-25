import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val settingsProperties = Properties().apply {
    val file = rootProject.file("services/ble/src/main/resources/settings.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun resolveEnvOrProp(propValue: String?, envVarName: String): String {
    val envValue = System.getenv(envVarName)
    if (!envValue.isNullOrBlank()) return envValue
    if (propValue == null || propValue.startsWith("\${")) return ""
    return propValue
}

val tuyaAppKeyEnv: String = resolveEnvOrProp(settingsProperties.getProperty("tuya.app.key"), "TUYA_APP_KEY")
val tuyaAppSecretEnv: String = resolveEnvOrProp(settingsProperties.getProperty("tuya.app.secret"), "TUYA_APP_SECRET")

android {
    namespace = "co.japl.android.ev_ride_connect.ble"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "TUYA_APP_KEY", "\"${tuyaAppKeyEnv}\"")
        buildConfigField("String", "TUYA_APP_SECRET", "\"${tuyaAppSecretEnv}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":utils"))

    // Tuya Smart BLE SDK dependency:
    // To enable in online build environment with Maven access, uncomment the following line:
    // implementation(libs.tuya.smart.ble)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
