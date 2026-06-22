# Block: RuleEvaluator

> Phase 3 spec. Drafted from the source (`RuleEvaluator.java`), the
> characterization tests (`RuleEvaluatorTest.java`, slice 80), and the
> modernization pass that swept its instanceof sites in slice 100.
> **Status:** initial draft, not yet reviewed by a human operator.

## Purpose

Decide whether a given POI cell value matches a single
`ConditionalFormattingRule`. Used by `ConditionalFormatter` once per
rule per cell during a styling pass; pure function of the input cell
and the rule, with no mutation of cell or workbook state.

Two evaluation paths, dispatched on `rule.getConditionType()`:

- **`CELL_VALUE_IS`** — compare the cell's value to the rule's
  formula-as-value with a comparison operator (`EQUAL`, `LT`, `GT`,
  `BETWEEN`, etc.). Handles `STRING` / `BOOLEAN` / `NUMERIC` cell
  types AND formula cells whose cached result type is one of these.
- **Other types (formula-based)** — evaluate the rule's formula as a
  boolean / numeric expression with relative-reference deltas
  applied, and return whether it evaluates to `true` (or non-zero).

## Inputs

### `matches(Cell cell, ConditionalFormattingRule rule, int deltaColumn, int deltaRow)`

- `cell` — the POI cell being evaluated. Used for both its value and
  (for formula cells) re-evaluation via the spreadsheet's formula
  evaluator.
- `rule` — the conditional-formatting rule.
- `deltaColumn`, `deltaRow` — offsets applied to the rule's
  relative-reference cells before evaluation. The rule is defined
  against an anchor cell; for any other cell in the range, the
  references need to be translated by the cell-anchor delta.
- Catches `NotImplementedException` from POI's formula evaluator and
  returns `false` (rules using unsupported POI functions are
  silently skipped). The catch is at the public-method level so both
  `matchesValue` and `matchesFormula` paths share the same fallback.

### `matchesFormula(...)` (package-private)

- Same arg shape as `matches`. Early-returns `false` for non-XSSF
  rules (HSSF formula refs are read incorrectly by POI — a TODO
  comment names this). Empty / null `formula1` returns `false`.

### `matchesValue(...)` (package-private)

- Same arg shape. The complex path: cell-type and formula-cached-type
  predicates form a 3×2 type-coherence matrix. Returns the
  comparison result for the matching numeric/string/boolean branch,
  or `(operation == NOT_EQUAL)` when the types don't cohere (a
  String cell never equals a Numeric rule value — but it IS
  not-equal).

## Outputs

`boolean` — `true` iff the cell matches the rule.

## Side effects

- For formula cells, calls
  `formulaEvaluator.evaluateFormulaCell(cell)` to refresh the cached
  formula result before reading it. This is the only state change
  outside the SUT; it's mediated by the spreadsheet's evaluator and
  is observable only as a refresh of the cell's own cached result.
- Reads the spreadsheet's `WorkbookEvaluatorUtil` for formula parsing
  (no mutation there).

## Invariants

- **Type-coherence pre-check before equality**.
  `hasCoherentType(eval, cellType, isStringType, isBoolType, isNumericType)`
  must return `true` before any value-equality is computed. If it
  returns `false`, the rule matches iff the operator is `NOT_EQUAL`
  (every value is "not equal" to a value of a different type, by
  Excel's CF convention).
- **String comparison is case-insensitive**.
  `String.equalsIgnoreCase` on both `EQUAL` and `NOT_EQUAL` —
  matches Excel's behavior.
- **Boolean evaluation has no case for inequality operators**. Only
  `EQUAL` and `NOT_EQUAL` are handled; other operators silently fall
  through (returning `false` from the surrounding method). Excel
  doesn't expose `<`/`>` for boolean CF, so this matches the UI.
- **Numeric operations cover the full set**: `EQUAL`, `NOT_EQUAL`,
  `LT`, `LE`, `GT`, `GE`, `BETWEEN`, `NOT_BETWEEN`. The `BETWEEN`
  arm reads `rule.getFormula2()` as the upper bound; `NOT_BETWEEN`
  uses the same formula2 read but with inverted comparison.
- **Relative references shift with `deltaColumn` / `deltaRow`**. The
  `getValueEvalFromFormula` helper re-walks the parsed Ptg array and
  bumps any `RefPtgBase` whose row / column is marked relative. This
  is what makes a rule "B1>5" match cell `B7` against `B7`'s value
  rather than `B1`'s — POI's formula evaluator doesn't do that
  on its own.
- **Error-eval is logged at trace and falls through to `false`** —
  preserves the slice-100 pattern-matching idiom
  `if (eval instanceof ErrorEval err)`.

## Open questions

- The boolean branch's silent fall-through on non-EQ/NEQ operators
  is observable as "rule didn't match" rather than "rule
  failed" — is that the intended behavior, or should it surface as
  a debug log? Current tests don't cover the fall-through case.
- `BETWEEN` and `NOT_BETWEEN` both call
  `Double.valueOf(rule.getFormula2())` — `parseDouble` would be
  cheaper but the test suite doesn't pin which API is used.
- The HSSF early-return in `matchesFormula` has a TODO that's been
  in place for many years; if HSSF support ever lands, the test
  suite needs new branches.
