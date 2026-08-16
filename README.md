# VSETT C7 Plus Scooter Controller App (ride-connect)

[![Android CI](https://github.com/nano871022/ride-connect/actions/workflows/test.yml/badge.svg)](https://github.com/nano871022/ride-connect/actions/workflows/test.yml)
[![Bundle Release](https://github.com/nano871022/ride-connect/actions/workflows/compile.yml/badge.svg)](https://github.com/nano871022/ride-connect/actions/workflows/compile.yml)

An Android Native application designed to connect to electric vehicles (specifically the VSETT C7 Plus Scooter) over Bluetooth Low Energy (BLE) using the Tuya Protocol. The app enables real-time telemetry display, accurate battery percentage calculation, trip tracking with GPS foreground service, local SQLite storage, Google Drive App Space cloud backup, and customizable UI components.

---

## 🏗️ Architecture Overview

The project adheres strictly to **Hexagonal Architecture (Ports and Adapters)** and a modular design principles:
- **Clean Architecture & Decoupling:** UI and ViewModels in `:app` never directly access database implementations or low-level service adapters. They interact strictly with domain entities, Use Cases, and Output Ports provided by `:core`.
- **UI & Controller Separation:** Jetpack Compose layout composables reside in `ui` packages, while ViewModels and state management logic reside in `controller` packages.
- **SOLID & TDD:** Modules are designed around Single Responsibility, Open/Closed, and Dependency Inversion principles with comprehensive unit test coverage using AssertJ and PODAM test object generation.

```
                  +-----------------------------------+
                  |           :app (UI/UX)            |
                  |  DashboardScreen | BackupScreen   |
                  +-----------------+-----------------+
                                    |
                                    v
                  +-----------------------------------+
                  |        :core (Domain Core)        |
                  | ScooterState | BackupConfig       |
                  |  Ports: BleScooterPort, etc.      |
                  +-----------------+-----------------+
                                    |
         +--------------------------+--------------------------+
         |                          |                          |
         v                          v                          v
+------------------+     +--------------------+     +-------------------+
|  :services:ble   |     | :services:database |     |      :track       |
|  TuyaBleAdapter  |     |  RoomTripAdapter   |     | ForegroundService |
+------------------+     +--------------------+     +-------------------+
```

---

## 📦 Project Modules

| Module | Description |
|---|---|
| `:app` | Application entry point, Jetpack Compose UI screens (`DashboardScreen`, `BackupScreen`), ViewModels (`DashboardViewModel`, `BackupViewModel`), and Hilt Dependency Injection setup. |
| `:core` | Domain models (`ScooterState`, `BackupConfig`, `BackupStatus`) and Output Ports (`BleScooterPort`, `TripDatabasePort`, `GoogleDriveBackupPort`). Completely framework-agnostic business logic. |
| `:services:ble` | Tuya BLE protocol adapter mapping incoming Bluetooth Data Points (DPs) to domain `ScooterState`. |
| `:services:database` | Room SQLite database persistence for trip history (`TripEntity`, `TripDao`) and Google Drive App Space backup helper (`GoogleDriveBackupHelper`). |
| `:track` | Android Foreground Service (`ScooterTrackingService`) for background GPS route tracking and continuous BLE connection persistence. |
| `:ui` | Reusable Jetpack Compose UI components (`MetricCard`, `SegmentedButtonGroup`, `SettingSwitchRow`, `StatusCard`) and custom theme (`MaterialThemeComposeUI`). |
| `:utils` | Shared utility logic including `BatteryCalculator` (13S Li-ion voltage percentage formula) and `DateUtils`. |
| `:about` | External module reference (`japl-android-about-module`) for app about screen integration. |

---

## 🛰️ BLE Tuya Protocol Mapping

The BLE adapter maps Tuya Data Points (DPs) received over Bluetooth to domain models:

| DP ID | Identifier | Type | Description |
|---|---|---|---|
| **DP1** | `lock_status` | `Boolean` | Lock / Unlock status of the scooter |
| **DP2** | `speed_mode` | `Int` | Current speed mode (e.g. Eco, Normal, Sport) |
| **DP4** | `light_switch` | `Boolean` | Headlight / Taillight status toggle |
| **DP5** | `current_speed` | `Int` | Real-time vehicle speed (km/h) |
| **DP6** | `total_odometer` | `Int` | Cumulative distance driven (km) |
| **DP7** | `realtime_voltage` | `Int` | Live battery voltage (tenths of a Volt, e.g. `546` = 54.6V) |

### 🔋 Battery Percentage Calculation (`:utils`)
Standard DP3 battery indicators on Tuya scooters are often inaccurate. Ride-Connect calculates battery percentage dynamically using 13S Li-ion battery discharge curves:
- **Max Voltage (100%):** 54.6V (`546`)
- **Cut-off Voltage (0%):** 39.0V (`390`)
- **Formula:** $\text{Percentage} = \frac{\text{Voltage} - 390}{546 - 390} \times 100$ (clamped between 0% and 100%).

---

## 🚀 Key Features

1. **Real-time Telemetry Dashboard:** Displays real-time speed, accurate 13S Li-ion battery percentage, odometer readings, voltage, and light/lock controls.
2. **Foreground Trip Tracking:** Persistent Android service records location and ride telemetry even when the app is in the background or screen is off.
3. **Trip History & SQLite Persistence:** Saves trip distance, duration, and battery consumed using Room database.
4. **Google Drive App Space Backup:** Automatic and manual backup configuration for app data and images directly to Google Drive App Space.
5. **Internationalization (i18n):** Complete string localization in English (`values/strings.xml`) and Spanish (`values-es/strings.xml`).
6. **Reusable Custom UI Design System:** Standardized Jetpack Compose components (`MetricCard`, `StatusCard`, `SegmentedButtonGroup`, `SettingSwitchRow`).

---

## 🛠️ Tech Stack & Dependencies

- **Language:** Kotlin 1.9+
- **Minimum SDK:** Android 8.0 (API 26) / Target SDK 34 (Android 14)
- **UI Framework:** Jetpack Compose with Material Design 3
- **Dependency Injection:** Hilt / Koin
- **Database:** Room / SQLite
- **Asynchronous / Reactive:** Kotlin Coroutines & Flow
- **Testing:** JUnit 5, AssertJ, PODAM (`PodamFactoryImpl`), MockK
- **CI/CD:** GitHub Actions

---

## 💻 Building and Running

### Prerequisites
- JDK 17
- Android SDK 34 (`/opt/android-sdk` or configured `ANDROID_HOME`)
- Gradle 8.8 (via Gradle Wrapper)

### Compilation & Tests
```bash
# Run unit tests across all modules
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Generate release Android App Bundle (AAB)
./gradlew bundleRelease
```

---

## ⚙️ CI/CD Workflows

The repository includes automated GitHub Actions workflows under `.github/workflows/`:
- **`test.yml`:** Runs `./gradlew test lint` on pull requests targeting `master` and uploads unit test results and lint code analysis reports as workflow artifacts.
- **`compile.yml`:** Builds release AAB bundle (`./gradlew bundleRelease`) on direct pushes to `master`.

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
