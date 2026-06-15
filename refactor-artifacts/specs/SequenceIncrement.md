# Block: SequenceIncrement

> First Phase 3 spec. Dogfoods the template from `LEGACY_REFACTOR_PIPELINE.md` §13.1.
> Drafted from the source (`SequenceIncrement.java`), the existing unit tests
> (`SequenceIncrementTest.java`, 15 tests), and the git history of the slice
> that introduced it (slice 12a, journal entry 2026-06-05).
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Compute the constant arithmetic increment of a sequence of values. Used by the
Excel autofill / drag-fill semantics in the Vaadin Spreadsheet — when the user
drags a fill handle out of a selection, this block decides whether the
selection is a regular arithmetic progression and what its step size is. If
the values aren't a clean progression, the autofill falls back to literal
repetition; if they are, the autofill extends them.

Two overloads — one for pure-numeric values, one for strings with trailing
digits. Both return a `Double` step, or `null` when the inputs don't form a
progression.

## Inputs

### `compute(Double[] values)`

- `values` — array of boxed doubles. Must be non-null. May contain `null`
  entries (which abort the computation by returning `null`). Empty arrays
  are accepted and return `null`. Single-element arrays return `null` (the
  numeric overload cannot infer an increment from one value).

### `compute(String[] values)`

- `values` — array of strings. Must be non-null and have at least one
  element (`values[0]` is dereferenced unconditionally; a zero-length array
  throws `ArrayIndexOutOfBoundsException`). Element entries may be `null`,
  which aborts the computation by returning `null`.

## Outputs

Both overloads: `Double` — the common arithmetic difference between
consecutive values, or `null` if the input doesn't form an arithmetic
progression.

### `compute(Double[])` outputs

- Returns the common difference if all consecutive pairs in `values` share
  the same numeric delta (e.g. `{2, 4, 6}` → `2.0`).
- Returns `Double.valueOf(0)` for a constant sequence (e.g. `{5, 5, 5}`).
- Returns a negative double for a decreasing sequence (e.g. `{10, 7, 4}` →
  `-3.0`).
- Returns a fractional double for fractional increments (e.g. `{1, 1.5, 2}`
  → `0.5`).
- Returns `null` when any consecutive pair differs from the others
  (e.g. `{1, 2, 4}`).
- Returns `null` when any entry is `null` (e.g. `{1, null, 3}`).
- Returns `null` for arrays of length 0 or 1.

### `compute(String[])` outputs

- For strings with a trailing-digits run and a common prefix:
  returns the common difference of the digit suffixes
  (e.g. `{"A1", "A2", "A3"}` → `1.0`, `{"Item 2", "Item 4", "Item 6"}` →
  `2.0`).
- Returns `Double.valueOf(1)` as a special case for a single-element string
  array whose value ends in digits (Excel autofill default — one cell of
  `"A1"` autofills as `"A1, A2, A3, ..."`).
- Returns `null` when the strings have no trailing digits
  (e.g. `{"foo", "bar"}`).
- Returns `null` when the prefix is not uniform across the array
  (e.g. `{"A1", "B2", "A3"}`).
- Returns `null` when the digit suffixes don't form an arithmetic
  progression (e.g. `{"A1", "A2", "A4"}`).
- Returns `null` when any entry past the first is `null`
  (e.g. `{"A1", null, "A3"}`).
- Throws `ArrayIndexOutOfBoundsException` for a zero-length array — the
  function dereferences `values[0]` unconditionally.

## Side effects

None — both overloads are pure functions on their inputs.

## Invariants

- The result is mathematically well-defined: if non-null, `result *
  (values.length - 1)` exactly accounts for the difference between
  `values[values.length-1]` and `values[0]`.
- The string overload's invariants additionally pin the prefix:
  `values[i].substring(0, matcher.start())` equals
  `values[0].substring(0, matcher.start())` for all valid (non-null) `i`.
- Both overloads' iteration order is left-to-right, so the prefix /
  reference value is locked to `values[0]`.

## Edge cases

- **Empty numeric array** → `null` (early exit because the loop body never
  runs).
- **Single-value numeric array** → `null` (the numeric overload diverges
  from the string overload here — the string overload returns `1.0`).
- **Single-value string array with trailing digits** → `1.0` (Excel-autofill
  convention).
- **Single-value string array without trailing digits** → `null`.
- **Constant numeric sequence** → `0.0` (a degenerate but valid arithmetic
  progression).
- **Negative numeric increment** → returned as a signed double (caller
  applies it with the correct sign).
- **Fractional increment** → returned as an exact double; no rounding.
- **String prefix that contains digits then non-digits then trailing
  digits** (e.g. `"V2C100"`) → the trailing-digit run is the part after the
  last non-digit, per the `\d+$` regex. So `"V2C100"` matches prefix
  `"V2C"` and digit suffix `100`.
- **Mixed-case string prefixes** (e.g. `{"a1", "A2"}`) → treated as
  different prefixes; returns `null`.

## Error modes

- **`null` array argument** (i.e. `compute((Double[]) null)`) →
  `NullPointerException` from the `for` loop's `values.length` access. Not
  swallowed.
- **`null` value entry in numeric overload** (any position) → returns
  `null` (the loop's null-check abort). No exception.
- **`null` value entry in string overload past the first position** →
  returns `null` (explicit null-check abort). The first position is never
  null-checked because `values[0]` is the prefix-detection seed; passing
  `{null, ...}` would NPE on `values[0].matcher(...)`.
- **Zero-length string array** → `ArrayIndexOutOfBoundsException` on
  `values[0]`. Not swallowed.
- **String entries with non-parseable digits** (theoretical — the regex
  matches digits only) → cannot occur given the regex.
- **String digit suffix outside `int` range** → `Double.parseDouble`
  accepts arbitrarily large numbers; the result is the floating-point
  difference. No overflow check.

## Non-obvious quirks

- **The numeric vs string single-value asymmetry is intentional.** A single
  numeric value can't yield an increment (no second value to subtract); a
  single string-with-trailing-digits value is treated as the start of a
  unit progression because that's Excel autofill's behavior. The
  characterization test `numeric_singleValue_returnsNull` documents this
  explicitly.
- **The `TRAILING_DIGITS_PATTERN` is shared.** Both `SequenceIncrement`'s
  string overload and `CellValueShifter#shiftString` need to detect the
  same trailing-digit pattern; the constant is package-private on
  `SequenceIncrement` and `CellValueShifter` references it directly. This
  prevents the two regex copies from drifting silently.
- **Iteration order matters for tie-breaking.** For an array like
  `{"A1", "B2", "C3"}`, the first iteration captures prefix `"A"`, the
  second sees prefix mismatch with `"B"`, returns `null`. If the iteration
  reversed, the prefix would have been `"C"` first and we'd still return
  `null`, but the rejected element would be `"A"`. Behaviour observable
  via short-circuit timing; not behavior observable via return value.

## Source links

- Source: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/main/java/com/vaadin/flow/component/spreadsheet/SequenceIncrement.java`
- Tests: `vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow/src/test/java/com/vaadin/flow/component/spreadsheet/SequenceIncrementTest.java` (15 tests pin every behavior in this spec)
- Slice that introduced it: journal entry 2026-06-05, slice 12a (`SequenceIncrement + SequenceValueExtractor`)
- Companion block: `SequenceValueExtractor` (the data-feeding side); `RangeSequenceDetector` (the dispatch side); `CellValueShifter#shiftString` (the application side)

## Contract status

Every invariant in this spec maps to at least one passing test in
`SequenceIncrementTest.java`. The spec is the contract; the tests are the
enforcement. Any change to the contract (e.g. deciding `compute(new
Double[]{42d})` should return `1.0` for symmetry with the string overload) is
a behavior change, not a refactor — separate commit, separate review.

## What the spec doesn't cover

- Performance characteristics. The block is O(n) in array length; trivially
  fast at any practical input size. Not relevant for the autofill use case.
- Thread safety. The block has no state; safe to call concurrently.
- Memory allocation. The string overload allocates one `Matcher` per
  iteration; not a hot path.

## Human review

**Status: not yet reviewed.** Per pipeline §13.2, this draft needs a domain
expert to verify:

1. Does every invariant correspond to an actual assertion in
   `SequenceIncrementTest.java`? (Tentative yes — 15 tests, each tagged.)
2. Is the "intentional asymmetry" in the single-value behavior actually
   intentional? (Likely yes — matches Excel autofill — but should be
   confirmed against the Excel reference or by the original author per
   `refactor-history` skill output.)
3. Are any contract points missing? Walk the source after reading this
   spec; do all branches map to a paragraph here?
