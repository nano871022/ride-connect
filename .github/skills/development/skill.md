## 🛠️ Skill 1: Development (`development`)

The primary objective is to write clean, decoupled, maintainable, and simple code from the first iteration.

*   **Test-Driven Development (TDD):**
    1.  **Red:** Write the failing unit test first based on requirements.
    2.  **Green:** Write the minimal implementation code necessary to pass the test.
    3.  **Refactor:** Clean up, optimize, and organize the code while ensuring all tests remain green.
*   **SOLID Principles:**
    *   **Single Responsibility Principle (SRP):** A class or module should have one, and only one, reason to change.
    *   **Open/Closed Principle (OCP):** Software entities should be open for extension, but closed for modification.
    *   **Liskov Substitution Principle (LSP):** Subtypes must be substitutable for their base types without altering program correctness.
    *   **Interface Segregation Principle (ISP):** Clients should not be forced to depend on interfaces they do not use.
    *   **Dependency Inversion Principle (DIP):** High-level modules should depend on abstractions, not concrete implementations.
*   **Clean Code Rules:**
    *   Use meaningful, domain-specific, and intention-revealing names for variables, functions, and classes.
    *   Keep functions small, focused, and operating at a single level of abstraction.
    *   Implement explicit error handling and avoid silent exception swallowing.
*   **KISS Principle (Keep It Simple, Stupid):**
    *   Avoid premature optimization, over-engineering, or unnecessary abstraction layers. Choose the simplest design that satisfies the current requirements.
*   **Internationalization (i18n):**
    *   Never hardcode user-visible messages or text strings directly in source code.
    *   Always define strings in resource files (`strings.xml`).
    *   Provide string resources for both English (`values/strings.xml`) and Spanish (`values-es/strings.xml`).
*   **Constants & Settings Management:**
    *   Do not hardcode class or action constants in companion objects directly inside service or component classes.
    *   Place constants in a dedicated settings/constants object or file.
    *   When referencing class names in actions, keys, or contexts, use dynamic class references (e.g., `Class::class.java.name` or `Class::class.qualifiedName`) rather than hardcoding string class paths.
*   **Package & Architecture Organization (UI vs Controller):**
    *   Strictly separate presentation components into dedicated packages:
        *   Place Jetpack Compose composable UI functions and screens exclusively inside the `ui` package/folder.
        *   Place ViewModels, state holders, and controller logic exclusively inside the `controller` package/folder.
*   **External Module Guidelines (`:about` module):**
    *   Do NOT create local source code, layout files, or folders for the `:about` module inside this repository.
    *   The `:about` module is an external repository reference configured in `settings.gradle.kts` via `project(":about").projectDir = file("../japl-android-about-module")`.
