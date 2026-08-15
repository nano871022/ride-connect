## [1.0.1] - 2026-08-15

### Summary of Changes
b419edc ci: add manual github action for versioning and tagging (#14)
c9e6dc9 docs: update README.md and add CHANGELOG.md for v1.0.0 (#13)
45228c7 ci: add github actions workflows for testing and release bundle compile (#12)
edb2295 feat(ui): develop reusable Jetpack Compose components in :ui module (#11)
00ffbf7 Implement Google Drive Backup in App Space (Task 10) (#10)
1d5c0e6 Implement Task 9: Jetpack Compose Dashboard UI and ViewModel (#9)
f6efb5c Task 8: Implement About module and UI theme executor (#8)
b2af38b Implement Foreground Service for Scooter Background Tracking (#7)
92b682f Update plan.md
4e9a4ed Merge pull request #6 from nano871022/feature/task-6-tuya-ble-adapter-5399813724700960045
32cb30c feat(ble): implement TuyaBleAdapter for BleScooterPort
d526959 Update plan.md
14e0ad0 Merge pull request #5 from nano871022/task-5-room-database-adapter-15356543695409039701
89a6df1 feat(database): implement Room database adapter for Task 5
573af7c Merge pull request #4 from nano871022/jules-task-4-output-ports-17812521914729355961
a586c04 feat(core): define BleScooterPort and TripDatabasePort output interfaces and unit tests
7560cf3 Update plan.md
4371513 Merge pull request #3 from nano871022/feat/task-3-scooter-state-13145916535441180749
bd8fcfc feat(core): implement ScooterState domain entity and unit tests
1c87a45 Merge pull request #2 from nano871022/task-2-battery-calculator-10690833824337463753


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
