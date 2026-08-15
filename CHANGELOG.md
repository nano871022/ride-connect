# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-15

### Added
- **Gradle Multi-Module Architecture:** Initialized modular setup including `:app`, `:core`, `:services:ble`, `:services:database`, `:track`, `:utils`, `:ui`, and `:about` external module integration.
- **Battery Percentage Utility (`:utils`):** Implemented `BatteryCalculator` for accurate 13S Li-ion battery percentage calculation (54.6V max, 39.0V cut-off) with TDD unit tests.
- **Domain & Output Ports (`:core`):** Defined core domain models (`ScooterState`, `BackupConfig`, `BackupStatus`) and Output Ports (`BleScooterPort`, `TripDatabasePort`, `GoogleDriveBackupPort`) adhering to Hexagonal Architecture (DIP).
- **SQLite & Room Persistence (`:services:database`):** Implemented `RoomTripAdapter` and DAOs for storing ride history (distance, battery consumed, duration).
- **Tuya BLE Communication (`:services:ble`):** Implemented `TuyaBleAdapter` mapping Tuya Data Points (DP1 to DP7) to `ScooterState` domain objects.
- **Foreground Tracking Service (`:track`):** Implemented `ScooterTrackingService` for persistent background location tracking and continuous BLE state monitoring.
- **External Module Reference (`:about`):** Integrated external `japl-android-about-module` repository reference in `settings.gradle.kts`.
- **Reusable Jetpack Compose UI System (`:ui`):** Built reusable custom Jetpack Compose components (`MetricCard`, `StatusCard`, `SegmentedButtonGroup`, `SettingSwitchRow`) and `MaterialThemeComposeUI` executor theme.
- **Dashboard & Cloud Backup Screens (`:app`):** Developed `DashboardScreen`, `BackupScreen`, `DashboardViewModel`, and `BackupViewModel` using Compose and Material UI 3.
- **Google Drive App Space Backup (`:services:database`, `:app`):** Implemented manual and automatic recurring background cloud backup support for local SQLite database and media files to Google Drive App Space.
- **GitHub Actions Workflows:** Configured CI/CD workflows for automated unit testing (`test.yml`) on pull requests and Android App Bundle release compilation (`compile.yml`) on `master` branch.
- **Internationalization (i18n):** Provided full string resource translations in English (`values/strings.xml`) and Spanish (`values-es/strings.xml`).
