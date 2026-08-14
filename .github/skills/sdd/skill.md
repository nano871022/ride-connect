## 📋 Skill 4: SDD Plan Creation (`creation plan SDD`)

Spec-Driven Design workflow for decomposing specifications into granular, unambiguous tasks prior to coding.

*   **Atomic Task Decomposition:**
    *   Break down features into tiny, low-effort atomic tasks that can be completed quickly and integrated continuously without causing merge conflicts or regressions.
*   **Dependency & Execution Matrix:**
    *   Explicitly categorize each task as either **Sequential** (blocking/dependent on a prior task) or **Parallel** (independent; can be executed concurrently).
*   **High Granularity & Pre-Investigation:**
    *   Every task must be thoroughly researched before execution.
    *   Include exact file paths, class/interface signatures, concrete inputs/outputs, and edge-case handling guidelines.
    *   The specification must be detailed enough that any instruction passed to the agent can be executed deterministically with zero ambiguity.

---

## 📝 SDD Execution Template

When creating or processing an SDD plan, use the following structure:

```markdown
## SDD Plan: [Feature / Module Name]

### Task 1 (Sequential)
- **Objective:** [Exact concise technical goal]
- **Files to Modify/Create:** `src/main/...` | `src/test/...`
- **Details & Signatures:** [Signatures, exceptions, logic specifics]
- **Acceptance Criteria:** Unit test passing using TDD, `camelCase` method name, and AssertJ assertions.

### Task 2 (Parallel to Task 1)
- **Objective:** [Exact concise technical goal]
- **Files to Modify/Create:** `src/main/...`
- **Details & Signatures:** [Details]
- **Acceptance Criteria:** [Criteria]
```
