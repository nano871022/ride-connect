## 🧪 Skill 2: Unit Testing (`unit test`)

Standardized rules for unit tests to ensure high isolation and reliability.

*   **Naming Conventions:**
    *   **Test Class Name:** Must follow the pattern `[ClassName]Test` (e.g., `PaymentServiceTest`).
    *   **Test Method Name:** Must use `camelCase` describing the exact context and expected outcome (e.g., `shouldCalculateTotalWhenDiscountIsApplied`).
*   **Initialization:**
    *   Use the `@Before` annotation over the `init()` method for pre-test setup, mock initialization, or context preparation.
*   **TestData Generation (PODAM):**
    *   Use **PODAM** by default for automated test object instantiation.
    *   ⚠️ **Circular Reference Mitigation:** To prevent infinite recursive calls during object filling, disable automatic PODAM filling for recursive/circular fields and **manually instantiate those objects**.
*   **Assertions & Test Framework:**
    *   **AssertJ:** Use fluent assertions via `assertThat(...)`.
    *   **JUnit:** Primary framework for test lifecycle execution.
*   **Conditional Branch Isolation Rule:**
    *   **Single Execution Path Per Test:** If a target method contains conditional logic (`if`, `else`, `switch`, or branch expressions), **each test method must test exactly ONE execution path**. Mixing multiple logical branches within a single test method is strictly prohibited.
