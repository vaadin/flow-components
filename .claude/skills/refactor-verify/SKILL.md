---
name: refactor-verify
description: Run the verification gates that must pass after a refactor slice lands but before the next one starts. Each gate is a separate command with a fixed pass/fail criterion. Use this between slices, or before opening a PR for a finished slice. Reports a single pass/fail line per gate plus deltas where relevant.
argument-hint: <module-path>
disable-model-invocation: true
allowed-tools: Read, Bash(mvn:*), Bash(npm:*), Bash(yarn:*), Bash(pnpm:*), Bash(go:*), Bash(pytest:*), Bash(git:*), Bash(grep:*), Bash(wc:*), Bash(find:*)
---

# Refactor verify

Run the verification gates after a refactor slice. Report a single pass/fail line per gate, plus deltas where relevant (test count, LOC, coverage if available). Don't continue to the next slice until every applicable gate is green.

## Inputs

- `$0` — module path the slice touched (e.g. `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow`, `packages/core`, `pkg/server`). Required for build-tool invocations.

## The principles this skill enforces

These are inlined here so this skill works standalone.

### Small steps, green between each

Every refactor slice ends with the test suite green. If a slice lands red, the safety net is broken — the next slice has no oracle. Stop and fix the red gate before starting another slice; don't accumulate failures.

### Each gate is structural, not stylistic

A gate fails on something machine-checkable (compile error, failing test, lint violation, missing assertion). Reviewer opinions are not gates — those live in `refactor-review`.

### Flaky gates get fixed, not loosened

If a gate fails intermittently, the failure mode is the problem, not the gate. Track the flake (capture seed / order / timing), reproduce it, fix the underlying race or order-dependency. Don't `@Ignore`, don't add retries, don't widen tolerances.

### A gate that doesn't apply is reported, not skipped silently

If the project has no mutation-testing setup, report "mutation: N/A — no setup" rather than dropping it from the report. Future setup may add it; transparency is the goal.

## The gates

Run these in order. Stop and report on the first hard failure (unless the operator asks for a full sweep).

### Gate 1 — Compile

```sh
# Java
mvn -pl $0 compile -B -q

# TypeScript
npx tsc --noEmit  # or `npm run build` if the project has it

# Go
go build ./...

# Python
python -m py_compile $(find . -name '*.py')
```

**Pass criterion:** exit code 0, no errors.
**Fail action:** show the first compile error verbatim, stop.

### Gate 2 — Format

```sh
# Java (Spotless)
mvn -pl $0 spotless:check -B -q

# TypeScript (Prettier)
npx prettier --check .

# Go
gofmt -l .  # empty output = pass

# Python (black/ruff)
ruff format --check .
```

**Pass criterion:** no files need reformatting.
**Fail action:** run the corresponding `apply` / `--write` command (`spotless:apply`, `prettier --write`, `gofmt -w`, `ruff format`), re-check, then proceed.

### Gate 3 — Lint

```sh
# Java
mvn -pl $0 checkstyle:check -B -q

# TypeScript
npx eslint .

# Go
golangci-lint run

# Python
ruff check .
```

**Pass criterion:** zero violations.
**Fail action:** show violations, stop. If the slice introduced them, fix in the same slice; if pre-existing, surface as separate cleanup.

### Gate 4 — Unit tests

```sh
# Java
mvn -pl $0 test -B

# TypeScript
npm test

# Go
go test ./...

# Python
pytest
```

**Pass criterion:** all pass, none erroring. Skipped tests OK as long as the count didn't change.
**Fail action:** show failing test name + assertion message verbatim, stop. **Don't continue if even one test went from green to red** — the safety net is gone.

Report: `Tests: {N} passed, {F} failed, {E} errored, {S} skipped` plus delta vs the count recorded at the last slice boundary.

**Verify the count matches expectation**, not just the exit code.
A test runner can report `0 passed, 0 failed` and exit 0 — that's
"nothing ran", not "everything passed". Symptoms:

- `wtr` reporting `8 test files | 0 passed, 0 failed`: the files
  matched the glob but couldn't be loaded (e.g. cross-module
  imports 404'd due to rootDir not spanning the source tree).
- `pytest` reporting `0 tests collected`: file pattern didn't
  match anything.
- Maven `Tests run: 0`: includes-pattern miss.

If the slice added N test cases and the runner reports <N total
afterward, the gate FAILS even though exit code is 0. The
canonical example: slices 118-121 added 107 tests but `wtr` ran
none of them because rootDir defaulted to the IT module (not the
parent dir containing the production module being tested). The
gap went undetected for four slices.

Always: count the `it(` / `@Test` declarations in the new files
manually, and verify the runner's total matches.

### Gate 5 — LOC delta

```sh
# For each file the slice touched
wc -l <god-class-file>
wc -l <new-helper-files>
```

**Pass criterion:** god-class LOC dropped by at least the slice plan's estimate (rough check — don't fail on small under-shoots).
**Fail action:** report the delta; if it's near zero, the slice may have moved code without decomposing it (Rule B violation). Surface for review.

### Gate 6 — Public API surface unchanged (for visibility-frozen classes)

```sh
# Java — count public and protected member declarations on the target class
grep -cE "^    (public|protected) " <god-class-file>

# Compare against a snapshot from before the slice (git show HEAD^:<path> | grep -cE ...)
```

**Pass criterion:** count is equal or lower. New public/protected members on a class that started visibility-frozen is a Rule A violation.
**Fail action:** name the new members, stop.

### Gate 7 — Helpers are package-private

```sh
# Java — list any new top-level public class in the same package
git diff --name-only HEAD^ HEAD | xargs grep -lE "^public (final )?class"
```

**Pass criterion:** every new helper class is package-private (no `public` modifier on the class declaration).
**Fail action:** name the offenders, stop.

### Gate 8 — Differential payload check (only if the slice touched contract-bearing code)

When the slice touches code that emits server-to-client RPC, JSON, or any externally-observable payload, run the project's payload golden-master test. The payload is the frozen contract — it cannot change.

```sh
# Spreadsheet — payload tests are SpreadsheetHistoryManagerPayloadTest etc.
mvn -pl $0 test -Dtest='*PayloadTest' -B
```

**Pass criterion:** all payload tests pass.
**Fail action:** the slice changed an externally-observable contract. **This is not a refactor anymore — it's an API change.** Stop and surface.

### Gate 8b — Public surface inventory (only for client-side rewrites / flips)

When a slice touches a frontend component's DOM, default stylesheet, or any element-facing surface, verify that the **documented** public contract is preserved. The contract for Vaadin Flow components is specifically:

1. `::part(...)` attributes from `../docs/articles/components/<name>/styling.adoc`
2. `--vaadin-<name>-*` CSS custom properties documented there
3. `public static final String <NAME>_STYLE = "..."` constants in the main Java class

```sh
# 1. List documented parts.
grep -oE '::part\([a-z-]+\)' ../docs/articles/components/<name>/styling.adoc | sort -u

# 2. Confirm every documented part still appears in the new DOM
#    (the rendered Lit element must emit part="..." matching each).
grep -oE 'part=\"[a-z\\s-]+\"' <component-frontend-source> | sort -u

# 3. List documented custom properties.
grep -oE '--vaadin-[a-z-]+' ../docs/articles/components/<name>/styling.adoc | sort -u

# 4. List public Java constants of the form *_STYLE.
grep -nE 'public static final String [A-Z_]+_STYLE' <component-java-source>
```

**Pass criterion:** every documented part / property / Java constant still has a working implementation in the new DOM/CSS. Default-stylesheet internal selectors and TestBench element selectors are NOT part of this gate — they're internal implementation details that update alongside the component.

**Fail action:** missing documented surface = the slice has a contract gap. Add the missing `part="..."`, custom property fallback, or class-honoring behavior in the same slice. Don't flip the implementation until this gate is green.

**Why this gate exists:** the Spreadsheet GWT→Lit rewrite (slices 168-170 attempt 2026-06-26) was reverted after the IT suite went 5→350 failures. Root cause was a missed contract sweep: the legacy stylesheet + TestBench element queried internal class names that the audit (slice 172) later confirmed were NOT documented. Running this gate before the flip would have surfaced the gap up front.

### Gate 9 — Full integration suite (end of batch, optional per slice)

Run the project's IT / E2E suite. Slow but high-coverage.

```sh
# Java/Maven IT
mvn verify -am -pl <it-module> -DskipUnitTests -B
```

**Pass criterion:** every previously-passing test still passes. Pre-existing failures (recorded at the start of the batch) are allowed but reported.
**Fail action:** isolate which tests went green-to-red after the slice. Differential failures stop the batch; pre-existing ones are surfaced for separate cleanup.

### Gate 10 — Coverage delta (if a coverage profile exists)

```sh
# Java/Maven with JaCoCo
mvn -pl $0 -Pcoverage test
# Read jacoco.csv or jacoco.xml for line/branch coverage
```

**Pass criterion:** coverage on the god class and the new helpers did not drop.
**Fail action:** report the drop; if it's > 2 percentage points, the slice removed test coverage. Add tests in the same slice or split into a "add tests" precursor commit.

### Gate 11 — Mutation delta (if mutation testing is configured)

```sh
# Java/Maven with PITest
mvn -pl $0 org.pitest:pitest-maven:mutationCoverage -B
```

**Pass criterion:** mutation score on the god class and helpers held or improved.
**Fail action:** drop indicates weakened tests, not stronger code. Report and surface.

## Output format

After running the gates, emit one summary block:

```
Gate 1 (compile):           pass
Gate 2 (format):            pass
Gate 3 (lint):              pass
Gate 4 (unit tests):        pass — 306 / 0 / 0 / 4 (+0 vs previous slice)
Gate 5 (LOC delta):         pass — god class 890 → 243 (-647 LOC), 5 new helpers
Gate 6 (public API):        pass — 0 new public/protected members
Gate 7 (helpers private):   pass — 5/5 helpers package-private
Gate 8 (payload):           N/A — slice didn't touch contract code
Gate 9 (IT):                deferred to end-of-batch
Gate 10 (coverage):         N/A — no coverage profile in this build
Gate 11 (mutation):         N/A — no mutation setup
```

Each line is one of `pass`, `fail`, `N/A`, `deferred`. Anything else is wrong.

## When the operator should stop the loop

Two gates failing on the same slice is a sign the slice was wrong. Don't push through — surface to the operator. The fix is usually to revert the slice and re-plan, not to patch the symptoms.
