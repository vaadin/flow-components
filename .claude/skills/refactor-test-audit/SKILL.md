---
name: refactor-test-audit
description: Audit existing characterization tests for an extracted helper class against a 5-axis checklist (Coverage, Behavioral, Style, Assertion, Regression). Produces a structured findings report identifying gaps + low-quality tests, so a fix pass can pin behavior properly before refactoring.
---

# Refactor — Test Audit

Used after Phase 1 (decomposition) and Phase 2 (test-writing) — before
Phase 4 (architectural refactor) or any dead-code removal. The audit
catches gaps and low-quality tests that would let a refactor silently
break behavior the existing tests appeared to cover.

Run this skill once per extracted helper. Produces a written findings
report. A separate fix pass then closes the gaps.

## When to invoke

- Before any Phase 4 architectural refactor of a helper.
- Before deleting code flagged as "dead" or "unreachable" — the audit
  may show the deletion would silently regress an untested path.
- After bulk Phase 2 test-writing, as a quality gate before declaring
  coverage complete.
- When test quality complaints surface from the team ("we have tests
  but the refactor still broke things").

## The 5-axis checklist

Apply every axis to every test file in scope. For each finding, record:
file path, line number, severity (gap / weak / wrong / leak), one-line
description, recommended fix.

### Axis 1 — Coverage (C)

**C1.** Every public / package-private method on the SUT is exercised
by at least one test. Read the SUT's method list; cross-reference
against the test class's `@Test` methods.

**C2.** Every branch within those methods is exercised:
- if/else, switch cases (every arm)
- try/catch (both the happy path and the catch)
- ternaries
- loop bodies (zero-iteration AND multi-iteration cases)
- early returns

**C3.** Edge cases for each method:
- Null inputs (where the contract accepts / handles null)
- Empty collections (List, Map, Set inputs of size 0)
- Single-element collections
- Boundary values: 0, 1, -1, max, max-1
- Multi-call sequences (state transitions on stateful SUTs)
- Error paths (exceptions the contract throws — IllegalArgumentException,
  NPE on null where contract forbids, etc.)

### Axis 2 — Behavioral / observable-only (B)

**B1.** Tests assert on PUBLIC observable outcomes only:
- Public return values
- Verified method calls on collaborators (these ARE the contract for
  orchestrator helpers)
- State queryable via PUBLIC getters
- Thrown exceptions

**B2.** No reflection on the SUT to inspect private state. Reflection
to WIRE a mock (because the parent has a `final` package-private field
that's the only injection point) is acceptable — that's plumbing, not
assertion.

**B3.** No assertions on data-structure shape that isn't guaranteed by
the contract:
- HashSet iteration order
- HashMap entry order
- Whether a defensive copy was made — UNLESS the contract documents
  it (then yes, pin it via "original unchanged" assertion)

**B4.** No assertions on intermediate computed values that aren't part
of the contract.

### Axis 3 — Style consistency (S)

**S1.** Naming convention: `methodName_condition_expectedOutcome` or
close variant. Each test name tells the reader exactly what's pinned.

**S2.** JUnit 5 + Mockito 5+ idioms:
- `@Mock` with `MockitoAnnotations.openMocks(this)` in `@BeforeEach`
- `@AfterEach` close for any opened `MockedStatic`
- `lenient().when(...)` ONLY where strict stubbing would flag the stub
  as unused
- `verify(mock).method(args)` with explicit arg matchers when needed

**S3.** One concept per test. Multiple assertions per test are OK if
they're all the same concept (e.g. "all 5 cells emitted the same CSS").
If a test name uses "and" between two unrelated checks, split it.

**S4.** Helpers (`private` factory methods, etc.) are confined to the
test class; reused across tests via `@BeforeEach` setup.

### Axis 4 — Assertion correctness (A)

**A1.** Test name matches the assertion. A test named
`xxx_returnsNullForEmptyMap` had better assert `assertNull(...)` and
seed an empty map.

**A2.** Assertions are SHARP. Mental simulation: "if I introduced
regression Y in the production code, would this test fail?" If no —
the assertion is weak.

**A3.** No vacuous assertions:
- `assertEquals(true, true)` — never useful
- `assertNotNull(new Object())` — never useful
- `verify(mock, times(0)).method()` when the test setup makes the
  method unreachable — passes trivially
- Tests with NO assertion (only setup + invoke)

**A4.** `assertSame` vs `assertEquals` chosen correctly:
- Identity → `assertSame`
- Value equality → `assertEquals`
- Mockito-mock equality → `assertSame` (mocks override neither equals
  nor identity)

**A5.** Argument matchers in `verify(mock).method(matchers)` are
specific enough. `verify(mock).method(any(), any())` is fine when the
test is asserting "the call happened" but not the values; if the
values matter, use specific matchers or `argThat(...)`.

### Axis 5 — Regression detection (R)

**R1.** For every test in the file: if you mentally introduce a
plausible regression in the production code (off-by-one, wrong branch,
swapped arguments, missing call), would the test fail?

**R2.** No blanket-loose matchers covering the actual contract. A test
named `xxx_passesArgsXYZ` that only verifies `verify(mock).method(any(),
any(), any())` would pass on any arg set — defeats the test name.

**R3.** No tests that pass trivially because the setup never reaches
the SUT's code path being tested. (E.g. a test for "the loop body
runs" that passes an empty collection — the loop never runs but the
test passes anyway.)

## Workflow

For each test file in scope:

1. **Read the SUT** (`src/main/java/.../<Helper>.java`). List the
   public + package-private methods and branches.

2. **Read the test** (`src/test/java/.../<Helper>Test.java`). For
   each `@Test` method:
   - Identify what the name claims to pin
   - Identify what the assertion actually checks
   - Mentally simulate a regression and ask "would this fail?"

3. **Apply the 5 axes**. Record findings in this structure:
   ```
   ## <HelperName>Test.java
   - **Coverage** (C1/C2/C3):
     - Methods uncovered: [list]
     - Branches uncovered: [list]
     - Edge cases missing: [list with severity]
   - **Behavioral** (B1-B4):
     - Implementation-detail leaks: [list]
   - **Style** (S1-S4):
     - Naming/consistency violations: [list]
   - **Assertions** (A1-A5):
     - Name-vs-assertion mismatches: [list]
     - Vacuous assertions: [list]
     - Weak (any/any matchers where specific would be sharp): [list]
   - **Regression** (R1-R3):
     - Tests that wouldn't fail on plausible regressions: [list]
   - **Severity**: gap / weak / wrong / leak / clean
   - **Recommended fix**: [terse]
   ```

4. **Triage** the findings:
   - `gap` — missing test for a real behavior. ADD a test.
   - `weak` — test exists but assertion isn't sharp. STRENGTHEN it.
   - `wrong` — test name lies about what it asserts. FIX the name or
     the assertion.
   - `leak` — test inspects internal state. REWRITE to assert through
     the public API.
   - `clean` — file is solid; skip.

5. **Fix in batches**. Group findings by module / family. After each
   family is fixed, run the full gates (`mvn spotless:apply checkstyle:check test`).

## Recommended dispatch shape

For a large helper count (≥20), dispatch parallel `Explore` agents.
Each agent audits a batch of 5-8 helpers using this checklist and
returns the structured findings format above. Synthesize the agents'
reports into a single triage list before fixing.

For smaller batches, audit inline.

## Pitfalls

- **The "looks-like-tested" trap.** A coordinator class's tests can
  indirectly exercise extracted helpers, but the indirect coverage is
  rarely tight. The audit must check whether the EXTRACTED helper has
  its own focused tests, not just whether its behavior is touched
  somewhere.

- **Mocking the SUT.** If the test mocks the very class it's testing,
  that's a structural smell. Helpers should be instantiated as real
  objects with their collaborators mocked.

- **Over-specifying the mock.** A test that verifies every mock call
  in order (`InOrder` for unrelated calls) couples to implementation
  detail. Use `InOrder` only when ordering is part of the contract.

- **"Should never happen" branches.** Code can have defensive branches
  that the spec considers unreachable. Either: (a) document in the
  spec they're unreachable and accept they're untested, or (b)
  manufacture the unreachable input to pin the defensive behavior.
  Don't silently leave them uncovered.

- **Test-as-documentation tension.** Sometimes a test exists less to
  catch regressions than to document a non-obvious quirk. That's
  fine — pin it explicitly with a comment ("Pin: ...") so a future
  reader knows it's intentional.

## When the audit is done

Every helper file has either:
- A "clean" finding (no gaps, sharp assertions, no leaks), OR
- A fix-pass commit that brought it to clean

The journal entry for each fix-pass records the findings + the fix,
so future contributors can see what was tightened and why.
