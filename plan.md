
## SDD Plan: VSETT C7 Plus Scooter Controller App

**Architecture Overview:** Hexagonal (Ports and Adapters), Multi-module.
**UI Framework:** Jetpack Compose (Material UI 3).
**Core Technology:** Android Native (Kotlin, Gradle), BLE (Tuya Protocol), SQLite.

---

### Task 1 (Sequential)
- **Objective:** Initialize the Gradle multi-module project structure and core dependencies.
- **Files to Modify/Create:** `settings.gradle.kts`, `build.gradle.kts` (root), and module `build.gradle.kts` files.
- **Details & Signatures:** 
  - Create the following modules: `:app`, `:core`, `:services:ble`, `:services:database`, `:track`, `:utils`, `:about`, `:ui`.
    -  about: its a references to japl-android-about-module over github.com/nano871022/, external module
    -  ui: its a module specializate over components reusables.
  - Configure Hilt/Koin for Dependency Injection across all modules.
- **Acceptance Criteria:** Project syncs successfully without Gradle errors. All modules are correctly linked in `settings.gradle.kts`.

### Task 2 (Parallel to Task 1)
- **Objective:** Implement the `:utils` module for 13S Li-ion battery calculations.
- **Files to Modify/Create:** `utils/src/main/java/co/japl/android/ev_ride_connect/utils/BatteryCalculator.kt` | `utils/src/test/java/co/japl/android/ev_ride_connect/utils/BatteryCalculatorTest.kt`
- **Details & Signatures:** 
  - `fun calculate13SPercentage(voltage: Int): Int`
  - Max voltage (100%): 54.6V (input `546`).
  - Cut-off voltage (0%): 39.0V (input `390`).
- **Acceptance Criteria:** Unit test passing using TDD, `camelCase` method names (e.g., `shouldReturnHundredWhenVoltageIs546`), and AssertJ assertions.

### Task 3 (Sequential)
- **Objective:** Define the Domain Entities and Use Cases in the `:core` module.
- **Files to Modify/Create:** `core/src/main/java/co/japl/android/ev_ride_connect/core/domain/ScooterState.kt` | `core/src/test/java/co/japl/android/ev_ride_connect/core/domain/ScooterStateTest.kt`
- **Details & Signatures:** 
  - `data class ScooterState(val isLocked: Boolean, val speedMode: Int, val currentSpeed: Int, val realtimeVoltage: Int, val batteryPercentage: Int, val totalOdometer: Int, val isLightOn: Boolean)`
- **Acceptance Criteria:** Unit test passing verifying PODAM object instantiation (excluding circular dependencies if any) and AssertJ assertions.

### Task 4 (Parallel to Task 3)
- **Objective:** Define Output Ports (Interfaces) for BLE and Database in the `:core` module.
- **Files to Modify/Create:** `core/src/main/java/co/japl/android/ev_ride_connect/core/ports/BleScooterPort.kt` | `core/src/main/java/co/japl/android/ev_ride_connect/core/ports/TripDatabasePort.kt`
- **Details & Signatures:** 
  - `interface BleScooterPort { fun observeScooterState(): Flow<ScooterState>; fun sendCommand(dpId: Int, value: Any) }`
  - `interface TripDatabasePort { suspend fun saveTripData(distance: Int, batteryConsumed: Int) }`
- **Acceptance Criteria:** Interfaces compile successfully and enforce Dependency Inversion Principle (DIP).

### Task 5 (Sequential)
- **Objective:** Implement the local SQLite database adapter in `:services:database`.
- **Files to Modify/Create:** `services/database/src/main/java/co/japl/android/ev_ride_connect/database/RoomTripAdapter.kt` | `services/database/src/test/java/co/japl/android/ev_ride_connect/database/RoomTripAdapterIntegrationTest.kt`
- **Details & Signatures:** 
  - Implement `TripDatabasePort` using Room/SQLite.
  - Set up entities for storing trip history (duration, battery consumed, distance).
  - Prepare Google Drive App Space backup helpers (stubs for phase 2).
- **Acceptance Criteria:** Integration test passing using an in-memory SQLite database. Must execute DDL scripts and test data persistence with AssertJ.

### Task 6 (Sequential)
- **Objective:** Implement the BLE Adapter in `:services:ble` mapping Tuya Data Points (DPs).
- **Files to Modify/Create:** `services/ble/src/main/java/co/japl/android/ev_ride_connect/ble/TuyaBleAdapter.kt` | `services/ble/src/test/java/co/japl/android/ev_ride_connect/ble/TuyaBleAdapterTest.kt`
- **Details & Signatures:** 
  - Implement `BleScooterPort`.
  - Map incoming byte arrays / SDK callbacks to `ScooterState`.
  - DP1: `lock_status` (Boolean), DP2: `speed_mode` (Int), DP4: `light_switch` (Boolean), DP5: `current_speed` (Int), DP6: `total_odometer` (Int), DP7: `realtime_voltage` (Int).
  - Use `BatteryCalculator.calculate13SPercentage` (from `:utils`) using DP7 data instead of the inaccurate DP3.
- **Acceptance Criteria:** Unit test passing testing exactly one conditional flow per test method. Mock the BLE payload and assert the correct mapping to `ScooterState`.

### Task 7 (Sequential)
- **Objective:** Implement the Foreground Service for background tracking in the `:track` module.
- **Files to Modify/Create:** `track/src/main/java/co/japl/android/ev_ride_connect/track/ScooterTrackingService.kt`
- **Details & Signatures:** 
  - Create an Android `Service` running in the foreground with a persistent notification.
  - Combine Android Location Services (GPS) and `BleScooterPort` flow.
  - Ensure the BLE connection stays alive while the screen is off (handling OS background constraints).
- **Acceptance Criteria:** Service implementation adheres to Android 14+ background location and Bluetooth permissions.

### Task 8 (Parallel to Task 7)
- **Objective:** Implement the `:about` reusable module.
- **Details & Signatures:** this project reference to https://github.com/nano871022/japl-android-about-module, make a reference to this project,it will be alocated in same folder of ride-connect folder example git/ride-connect, git/japl-android-about-module. file: src/main/java/co/com/japl/homeconnect/about/AboutActivity.kt, UI file: src/main/java/co/com/japl/homeconnect/about/ui/About.kt, in UI module you need to create "co.com.japl.ui.theme.MaterialThemeComposeUI" MaterialThemeComposeUI its a executor theme

### Task 9 (Sequential)
- **Objective:** Develop the main Jetpack Compose Dashboard in the `:app` module.
- **Files to Modify/Create:** `app/src/main/java/co/japl/android/ev_ride_connect/app/ui/DashboardScreen.kt`
- **Details & Signatures:** 
  - Inject `BleScooterPort` and `TripDatabasePort` via Use Cases.
  - Render real-time speed, accurate battery percentage, odometer, and lock/unlock toggle using Material UI 3.
- **Acceptance Criteria:** Unit tests passing for the ViewModel logic, ensuring SRP (Single Responsibility Principle) and KISS principles are maintained.

### Tastk 10 (sequantial)
- **Objective:** implement connection with drive
- **description:** create a implementation for make bakup over google drive in app space to make backups, add interface to setting time recurrent for buckups automaitcally, manual backup, backups is about images and DB sqlite local of app.

### Tast 11 (sequantial)
- **Objective:** Develop `UI` module jetpack compose
- **Details:** this module contain componentes custom create for some interfaces, when a components its used in multiple times and it has a common setting that is complex so create custom comentent reusable that simplify use of it.

### Task 12 (sequential)
- **Objective:** Develop a test, and compile process with github actions
- **Details:** add 2 actions for github one of them to run test each time try to integrate in master the other branch if test file it does not integrate, second:when code was integrate in master in run a compile process of application in formate to upload in android console.
  
### Task 13 (sequential)
- **Objective:** update readme.md file and change log
- **Detail:** update readme.md file with information about the project with all detail about it, and create changelog.md file with information about each version its create and deploy

### Task 14 (sequential)
- **Objective:** create a github action to update version of project
- **Detail:** update version in build.gradle, add in changelog, create a detail checkik las merge made in master branch, create a recap about changes, and create a tag with version, its a running manual.
