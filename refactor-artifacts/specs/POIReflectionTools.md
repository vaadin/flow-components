# Block: POIReflectionTools

> Phase 3 spec. Drafted from the source (`POIReflectionTools.java`)
> as of slice 103 (Tier 1 Step 6: POI 5+ audit) and the
> characterization tests (`POIReflectionToolsTest.java`, 16 tests,
> slice 79). **Status:** initial draft, not yet reviewed by a human
> operator.

## Purpose

A **named seam** for reaching into POI's hidden state via Java
reflection. Used by `ConditionalFormatter` (and indirectly by
`RuleBorderTracker`) to read XML-level conditional-formatting
metadata that POI's public Java API doesn't expose.

The class is positioned as a **swap point**: if a future POI
version exposes the needed value as a public method, this class is
the only place that needs to change. Slice 103 did exactly that —
dropped 2 of 4 reflection bridges in favor of POI 5+ public APIs
(`getPriority`, `getStopIfTrue`); the other 2 remain reflective
because POI's public API still doesn't cover the needed semantic.

## Inputs / methods

### `getFieldVal(Object owner, String fieldName)` — primitive

- Reflectively reads the field named `fieldName` from `owner`.
- Returns `null` (and logs at error level) on any reflection
  failure — including the case where the field doesn't exist on
  the running POI version (i.e., the seam outgrew the schema).
- Restores `setAccessible(false)` in `finally`.

### `getOrderedRuleList(ConditionalFormatting cf)` — public POI API as of slice 103

- Returns the XSSF rules of `cf` sorted by `priority` descending
  (higher priority first).
- Uses `XSSFConditionalFormattingRule.getPriority()` — public in
  POI 5+. NO reflection in this method as of slice 103.
- Implementation:
  `rules.sort(Comparator.comparingInt(XSSFConditionalFormattingRule
  ::getPriority).reversed())`.

### `stopHere(ConditionalFormattingRule rule)` — public POI API as of slice 103

- Returns `true` iff `rule` has its `stopIfTrue` flag set. Uses
  `XSSFConditionalFormattingRule.getStopIfTrue()` — public in POI
  5+. NO reflection in this method as of slice 103.
- Returns `false` for non-XSSF rules (HSSF doesn't expose the
  flag).
- Implementation:
  `rule instanceof XSSFConditionalFormattingRule xssfRule
  && xssfRule.getStopIfTrue()`.

### `isBorderSet(XSSFBorderFormatting borderFormatting, BorderSide b)` — STILL reflective

- Returns `true` iff the given border side has been **explicitly
  set** on the formatting (distinct from "is none"). The
  distinction matters because a conditional-formatting rule with
  explicit `BorderStyle.NONE` OVERRIDES the previous rule's
  border, while an unset side leaves the previous rule's border
  intact.
- Reflectively reads the `_border` field on the formatting,
  then dispatches on `BorderSide` to call `CTBorder.isSetXxx()`.
- **Why reflection stays**: POI's public
  `XSSFBorderFormatting.getBorderXxx()` returns a `BorderStyle`
  enum (with `NONE` as the default), which collapses
  "explicitly-set-NONE" and "never-declared" into the same value.
  The CF semantic needs them distinguished.

### `hasStrikeThrough(FontFormatting fontFormatting)` — STILL reflective

- Returns `true` iff ANY `<strike>` entry in
  `CTFont.getStrikeList()` has `val=true`. Falls through `false`
  for HSSF (XSSF-only XML detail).
- Reflectively reads the `_font` field, then walks the strike
  list.
- **Why reflection stays**: POI's public
  `XSSFFontFormatting.isStruckout()` inspects only the first entry
  in the strike list. The defensive-scan semantic of the existing
  code matches what the test suite has pinned (slice 79's
  `hasStrikeThrough_xmlElementCreatedDirectly_returnsTrue`).
  Switching to `isStruckout()` would be a behavior change, not
  modernization.

## Outputs

- `getFieldVal` returns `Object` (caller casts to expected type)
  OR `null` on reflection failure.
- The other three return `boolean` (predicate or
  list-not-empty-style) or `List<...>` for `getOrderedRuleList`.

## Side effects

- The reflective methods toggle `Field.setAccessible(true)` then
  `false` in a `try/finally`. Concurrent calls would race on the
  shared field-accessible flag — but POI Field objects aren't
  shared across the helper's calls (each call calls
  `getDeclaredField` fresh).
- Error logging on reflection failure. No exception propagation
  to callers — they get `null` and move on. This is the "graceful
  degradation when the POI schema changes" contract.

## Invariants

- **All methods are `static`. No instance state.** Pure utility.
- **Reflection failure logs at error, returns `null`.** Callers
  must null-check the result; `null` means "the POI version on
  the classpath doesn't have this field" or "security manager
  blocked us." Both are graceful-skip scenarios.
- **`isBorderSet` requires the CTBorder element to exist in the
  XML** — slice 79 found that `bf.setBorderLeft(BorderStyle.NONE)`
  via the Java API does NOT mark `isSetLeft()` true (POI's setter
  doesn't add the XML element, only the value). The XML-parsed
  path from a real `.xlsx` file DOES mark it. So:
  Java-API-constructed CF rules with NONE borders are NOT
  distinguishable from "unset" via this predicate; only
  file-parsed rules are. Tests `isBorderSet_javaApi_*` and
  `isBorderSet_xmlElementCreatedDirectly_*` pin this quirk.
- **`hasStrikeThrough` is "ANY", not "FIRST"**. The defensive-loop
  semantic is the load-bearing contract — slice 103 considered
  swapping to `isStruckout()` (first-only) and rejected it
  precisely because the test suite pinned ANY.
- **The "swap-when-POI-exposes" pattern paid off in slice 103.**
  The class's structural design — small focused helpers in a
  single file — meant the migration was a single-file change with
  no caller churn. Future migrations should preserve this shape.

## Open questions

- If POI ever exposes the `isSetXxx` distinction publicly,
  `isBorderSet` should be migrated. The current javadoc names
  this as the seam's purpose.
- The strike-through "ANY vs FIRST" semantic — is there a real
  file in the wild that produces multiple `<strike>` entries?
  Slice 79's investigation found the test had to construct the
  multi-entry case via reflection on `CTFont.addNewStrike()`
  because the public API doesn't. If real-world files NEVER have
  more than one entry, the defensive scan is unnecessary —
  but proving that would need a corpus audit, which is out of
  scope.
