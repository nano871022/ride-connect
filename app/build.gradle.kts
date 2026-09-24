import java.io.FileInputStream
import java.util.Properties

abstract class CopyGoogleServicesTask : DefaultTask() {
    @get:Input
    @get:Optional
    abstract val sourceFilePath: Property<File>

    @get:OutputFile
    abstract val targetFile: RegularFileProperty

    @TaskAction
    fun copy() {
        val srcPath = sourceFilePath.orNull
        val dest = targetFile.get().asFile

        if (!dest.exists() && srcPath != null && srcPath.exists()) {
            srcPath.copyTo(dest, overwrite = true)
            logger.lifecycle("--> [Build Local] google-services.json copiado exitosamente.")
        } else {
            logger.lifecycle("--> [Build Local] google-services.json No fue encontrado.")
        }
    }
}

val copyGoogleServicesJson =
    tasks.register<CopyGoogleServicesTask>("copyGoogleServicesJson") {
        description = "Copia el archivo google-services.json si no existe localmente."
        // Cambia la ruta según la ubicación de tu repositorio externo
        var externalFile = layout.projectDirectory.file("../../japl-properties/ride-connect/google-services.json").asFile
        var target = layout.projectDirectory.file("google-services.json")
        sourceFilePath.set(externalFile)
        targetFile.set(target)

        onlyIf {
            !target.asFile.exists()
        }
    }

tasks.configureEach {
    if ((name.startsWith("process") && name.endsWith("GoogleServices")) || name == "preBuild") {
        dependsOn(copyGoogleServicesJson)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
}

android {
    namespace = "co.japl.android.ev_ride_connect"
    compileSdk = 36
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "co.japl.android.ev_ride_connect"
        minSdk = 26
        targetSdk = 36
        versionCode = 1_00_010
        versionName = "1.00.010 Added voltage input value"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val localFile = File(project.rootDir,"local.properties")
        val properties = Properties();
        properties.load(localFile.inputStream())
        manifestPlaceholders["API_KEY_MAP"] = properties.getProperty("api.key.map")?:"NO-API"
    }

    signingConfigs {
        create("release") {
            val keystoreFilePath = System.getenv("KEYSTORE_FILE") ?: "ride-connect-sha.jks"
            val keystoreFile = rootProject.file(keystoreFilePath).takeIf { it.exists() } ?: file(keystoreFilePath)
            if (keystoreFile.exists() && keystoreFile.length() > 0) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS") ?: "ride-connect"
                keyPassword = System.getenv("KEY_PASSWORD") ?: System.getenv("KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val keystoreFilePath = System.getenv("KEYSTORE_FILE") ?: "ride-connect-sha.jks"
            val keystoreFile = rootProject.file(keystoreFilePath).takeIf { it.exists() } ?: file(keystoreFilePath)
            signingConfig = if (keystoreFile.exists() && keystoreFile.length() > 0 && System.getenv("KEYSTORE_PASSWORD") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    //noinspection WrongGradleMethod
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INDEX/AL2.0"
            excludes += "/META-INDEX/LGPL2.1"
            pickFirsts += "settings.properties"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.coil.compose)
    implementation(project(":core"))
    implementation(project(":services:ble"))
    implementation(project(":services:database"))
    implementation(project(":services:llm"))
    implementation(project(":track"))
    implementation(project(":utils"))
    if (findProject(":about") != null) {
        implementation(project(":about"))
    }
    implementation(project(":ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.org.json)

    testImplementation(project(":core"))
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.podam)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.androidx.ui.test.manifest)
}
