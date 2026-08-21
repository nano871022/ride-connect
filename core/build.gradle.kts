plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.org.json)
    testImplementation(libs.junit)
    testImplementation(libs.assertj.core)
    testImplementation(libs.podam)
    testImplementation(libs.kotlinx.coroutines.test)
}
