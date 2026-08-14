## 🔗 Skill 3: Integration Testing (`integration test`)

Rules for validating inter-component behavior, state persistence, and end-to-end layer integration.

*   **Inherited Standards:** Must strictly comply with all rules defined in both `development` and `unit test` skills.
*   **Runtime Database Environment:**
    *   Use an **in-memory or runtime SQLite database** for database interaction tests.
*   **Database Scripts Execution:**
    *   Every integration test suite must execute DDL scripts (table/schema creation) and DML scripts (data seeding) prior to test execution.
*   **State Assertion:** Assert both return values and side-effects on the SQLite persistence layer to guarantee system integrity.
