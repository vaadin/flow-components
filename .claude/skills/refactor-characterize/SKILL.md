---
name: refactor-characterize
description: Scaffold characterization tests for a method set before extracting it. Characterization tests pin what the code does today — bugs and quirks included — so the refactor's diff doesn't smuggle in behavior changes. Use this BEFORE moving a method body into a helper, especially when existing tests only cover the happy path.
argument-hint: <path-to-class> <comma-separated-method-names>
disable-model-invocation: true
allowed-tools: Read, Write, Bash(find:*), Bash(grep:*), Bash(mvn:*), Bash(npm:*), Bash(pytest:*), Bash(go:*)
---

# Refactor characterize

Produce a test class that pins the current behavior of a set of methods. The tests must:

- Call the real implementation (no mocks of the unit under test).
- Use a realistic fixture (in-memory database, in-memory document, real framework state).
- Assert on observed outputs, not on the LLM's model of what *should* happen.
- Capture edge cases: nulls, empties, off-by-ones, locale-specific paths, error branches.

These tests are the safety net. If the refactor changes behavior, they fail. If they pass before and after, the refactor is behavior-preserving with respect to *what the code actually does today*.

## Inputs

- `$0` — absolute path to the class containing the methods. Required.
- `$1` — comma-separated method names to characterize. Required.

## The principles this skill enforces

These are the invariants every characterization test must respect. Inlined here so this skill is self-contained.

### Observed behavior is ground truth

Test oracles come from **running the current code and recording its output**, not from:

- The LLM's model of intent.
- Javadocs or comments (treat as untrusted hypotheses — legacy comments lie).
- Specifications or design documents.
- What the code "should" do.

If you don't know what value an expression produces, write a temporary scaffold test that prints it, run it, copy the actual output into the assertion, delete the scaffold. The actual output is the oracle.

### Bugs are part of the pinned behavior

If the current code returns `null` for an empty list when the docstring says it returns an empty list, the test asserts `null`. If the current code throws on a leap-year boundary, the test asserts the throw. The refactor is not allowed to "fix" that — fixing it is a separate behavior-change commit with its own review.

The only exception: if a current behavior is a known bug that the user has explicitly chosen to fix as part of this refactor, the test still pins the *current* behavior and is updated in the same commit that fixes the bug. Don't ever land a refactor commit that quietly changes a pinned value.

### Choose the fixture closest to runtime

Mocks are a last resort. In priority order:

1. **Real fixture** — in-memory database (XSSFWorkbook, SQLite, fakeredis), in-memory document (JSON tree, in-memory file), real framework state. Use this when feasible.
2. **Recording proxy** — a stub that captures interactions and lets you assert on the captured call sequence. Use when the real fixture is too slow or hard to set up.
3. **Mock** — only when (1) and (2) are infeasible, and only for collaborators that are *not* the unit under test.

A characterization test that mocks the unit under test is not a characterization test — it's a behavior reinvention.

### Cover the four edge-case families

For each method, the test set should include at least one of each (where applicable):

- **Empty input** — empty list, empty string, zero-length array.
- **Null input** — null argument, null field, null in a collection.
- **Boundary** — first/last element, off-by-one, exactly-at-limit values.
- **Locale or environment dependence** — if the method uses `Locale`, `TimeZone`, `Charset`, `Random`, system time, etc., pin behavior under at least two settings.

A passing test set that doesn't cover these is a false safety net.

## Steps

### 1. Identify the fixture type

Read the class. Decide which fixture is closest to runtime:

| Domain                    | Fixture                                   |
|---------------------------|-------------------------------------------|
| Spreadsheet / POI         | `XSSFWorkbook` (in-memory)                |
| Vaadin Flow UI            | `MockUIExtension` + JUnit 5               |
| JDBC / SQL                | H2 in-memory or testcontainers           |
| HTTP server               | WireMock or built-in test server          |
| Filesystem                | `Files.createTempDirectory` per test      |
| Pure data / math          | No fixture — direct method calls          |
| Framework with reflection | The actual framework, never mocked        |

If the class uses several fixture types, the test class can mix them — but characterize one method per test, never more.

### 2. Find existing tests

Before writing new tests, check for existing ones:

```sh
# Java
find . -name "{ClassName}Test.java" -o -name "{ClassName}IT.java"
grep -rn "{method-name}" --include="*Test.java" --include="*IT.java"

# TypeScript / Python / Go — analogues with respective conventions
```

If a test class exists with happy-path coverage, add *characterization* tests as new methods on the same class. Don't duplicate the fixture setup.

### 3. Scaffold the test class

Output skeleton matches the project's existing test conventions (read a sibling test class to copy its imports, fixture annotations, and naming). For each method to characterize, write:

```
@Test
void {methodName}_{scenario}_{expectedShape}() {
    // Arrange: minimal real fixture
    ...
    // Act: call the real method
    ...
    // Assert: observed output (capture from a one-time run)
    assertThat(actual).isEqualTo(/* PIN: <recorded value> */);
}
```

Mark uncaptured values with `/* PIN: <recorded value> */` and a TODO. Run the tests once with `assertEquals(expected, actual)` deliberately failing, copy the actual into expected, then delete the TODO.

### 4. Cover the four edge-case families per method

For each method in `$1`, at minimum:

- Happy path — one realistic input.
- One edge case from each of the four families that applies (empty, null, boundary, environment).

If a family doesn't apply (e.g. method takes only primitives, no null path), say so in a one-line code comment and move on. Don't invent ceremonial tests for inputs that can't occur.

### 5. Run the tests

Run the test class against the *unchanged* implementation. They must all pass before any refactor touches the methods.

```sh
# Java/Maven
mvn -pl {module} test -Dtest={ClassName}Test

# TypeScript / Python / Go — analogues
```

If a test fails, the recorded value is wrong — re-record it from a passing run. Never adjust the code under test to make a characterization test pass.

### 6. Stop

Report:

- Path to the new test file.
- Number of tests added.
- Pass/fail count from the verification run.

The operator's next step is usually `refactor-slice-plan` (now that the safety net exists) or directly extracting the method bodies into a helper.

## When to push back instead of writing tests

- **The methods can't be tested in isolation** — they touch global state, depend on a static-initializer side effect, or call private methods that mutate a singleton. Report this; the refactor needs a seam-introduction step first.
- **The fixture cost is prohibitive** — e.g. the method only runs as part of a 30-second framework boot. Suggest a recording-proxy approach instead, or characterize at the public API level rather than the method level.
- **The "method" is really a 400-line dispatcher** — characterization tests at that granularity are too coarse to catch regressions. Split the method first (a separate refactor), then characterize the resulting smaller methods.
