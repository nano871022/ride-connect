# Upgrade Project to Support External `about` Module

The `about` module (`japl-android-about-module`) is targeting a modern environment with **AGP 9.0+** and **Kotlin 2.4.10**. To integrate it without modifying its own build scripts, we must upgrade the main project (`EVRideConnect`) to match these versions and the new AGP 9.0 architecture.

## User Review Required

> [!IMPORTANT]
> This plan involves a major upgrade to the project's build system:
> - **AGP 8.3.2 → 9.3.1**: This introduces "Built-in Kotlin" support.
> - **Kotlin 1.9.23 → 2.4.10**: Includes the new K2 compiler and the Compose Compiler Gradle plugin.
> - **Hilt 2.51.1 → 2.60.1**: Aligning with the version used in the `about` module.
> - **Removal of `kotlin-android` plugin**: This is deprecated/replaced in AGP 9.0+ in favor of built-in Kotlin support.

> [!WARNING]
> Since the `about` module has a hardcoded dependency on `id("com.android.legacy-kapt")`, we must provide this plugin in the host project. This plugin is specifically for projects using AGP 9.0+ built-in Kotlin that still require `kapt`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Alejo/git/ride-connect/gradle/libs.versions.toml)
- Upgrade `agp` to `9.3.1`.
- Upgrade `kotlin` to `2.4.10`.
- Upgrade `hilt` to `2.60.1`.
- Upgrade `ksp` to `2.3.11`.
- Add `kotlin-compose` plugin to the `[plugins]` section.
- Add `android-legacy-kapt` plugin to the `[plugins]` section.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/build.gradle.kts)
- Update plugins to use the new IDs and versions from the catalog.
- Add the `android-legacy-kapt` and `kotlin-compose` plugins as `apply false`.

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/settings.gradle.kts)
- Ensure repositories are set up correctly for the new versions.

### Modules

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/app/build.gradle.kts)
#### [MODIFY] [ui/build.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/ui/build.gradle.kts)
#### [MODIFY] [services/ble/build.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/services/ble/build.gradle.kts)
#### [MODIFY] [services/database/build.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/services/database/build.gradle.kts)
#### [MODIFY] [track/build.gradle.kts](file:///C:/Users/Alejo/git/ride-connect/track/build.gradle.kts)
- Remove `id("org.jetbrains.kotlin.android")` (replaced by built-in Kotlin).
- Apply the new Compose compiler plugin: `id("org.jetbrains.kotlin.plugin.compose")`.
- Remove `composeOptions { kotlinCompilerExtensionVersion = ... }`.
- Ensure Hilt and KSP versions are aligned.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build.
- Run `./gradlew :about:assembleDebug` to verify the external module compiles within the host project.
- Run unit tests: `./gradlew test`.

### Manual Verification
- Verify that the IDE syncs without errors.
- Verify that the `about` screen (if accessible) renders correctly with Compose.
