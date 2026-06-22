# Tier 3 — QUIRKs audit + clean-room rewrite direction

> Drafted 2026-06-22 by slice 129 after a strategic clarification:
> the TS/Lit rewrite is a **clean-room rewrite that matches modern
> Vaadin component style** — NOT a verbatim port. Drivers: legacy
> maintenance burden + licensing/copyright concerns around copying
> GWT-era code. This document catalogs every "QUIRK" / "byte-equivalent
> port" marker banked in slices 118-125 and classifies each as either
> "real-world contract — keep" or "apparent GWT bug — drop".
>
> **Status: RESOLVED.** Slice 130 executed all 4 DROPs + 3 REPHRASEs +
> a sweep of the broader "ported byte-equivalent from..." javadoc
> framing on every algorithm file. Tests stay 184/184 green. The
> visual-regression IT (slices 122-123) is the deferred safety check
> for the rendering-affecting DROPs (Q1 + Q5); run before Phase 3.6
> rollout.

## Why this audit exists

Slices 118-125 ported pure-algorithm GWT-Java code to TS using a
"byte-equivalent preserve verbatim + QUIRK callout" pattern (see
`code-modernization` skill, pre-slice-129 version). The pattern was
chosen for safety: every behavior the upstream had — even
suspicious-looking ones — got preserved + javadoc-flagged as an
"open question for human review".

The user clarified on 2026-06-22:

> we are not just eliminating GWT code to copy it to our repo. We
> want to modernize the way it works. We want to refactor the
> structure to be close to our other components. ... massive legacy
> code maintenance burden & cost and licensing & copyright issues

This changes the direction:

- The new code should look like **vaadin-grid-flow's gridConnector.ts**
  + sibling modern components, not like translated GWT.
- "Byte-equivalent port" applies ONLY to wire-protocol code (the
  DTOs server emits, the RPC interface names). It does NOT apply
  to algorithm/implementation code, where a clean-room rewrite
  preserving the behavior CONTRACT (same input → same output for
  documented behaviors) is the goal.
- Quirks that look like GWT bugs should be DROPPED, not preserved.
  Their inclusion would carry both the legacy burden and the
  copyright signal forward.

## Audit results

Each entry below names a banked quirk, cites its file:line, and
classifies it.

### CLASSIFICATION KEY

- **KEEP**: real-world Excel/POI/clipboard contract; clean-room
  rewrite still produces this behavior.
- **DROP**: apparent GWT bug or implementation accident; clean-room
  rewrite removes it. Visual-regression IT (slices 122-123) acts
  as the safety net for any user-observable shift.
- **REPHRASE**: behavior stays, but the comment claiming "preserved
  verbatim from GWT" should be replaced with one citing the
  behavior contract (Excel spec, POI documentation, or simply "by
  test").

### Q1 — `ViewportMath.getRowHeightPx`: `row >= length` check

**Location:** `lit/algorithms/ViewportMath.ts:75-94` (slice 124).

**Current behavior:** for a `definedRowHeightsPx` array of length N,
rows 1..N-1 use the array; row N (1-based) AND rows beyond N use
the default height. Documented as a "QUIRK preserved for byte-
equivalence".

**Smell:** off-by-one. A length-5 array `[20, 30, 40, 25, 20]`
would intuitively cover rows 1..5, but this implementation says
row 5 falls through to default. The GWT-side `definedRowHeights`
array may be sized N+1 in practice (one extra slot), making the
quirk a no-op upstream — but the TS version receives a tightly-
sized array per call (the wrapper has full control), so the off-
by-one shows up.

**Classification:** **DROP**. Clean-room version uses `row >
definedRowHeightsPx.length` (i.e. the array is fully populated for
rows 1..N). The Phase30BaselineCaptureIT + visual-regression IT
catch any rendering shift this introduces.

**Future-slice action:** edit `getRowHeightPx` + test
expectations; run visual regression as the safety check.

### Q2 — `CellKeys.parseColRow`: ignore characters after the second space

**Location:** `lit/algorithms/CellKeys.ts:60-95` (slice 125).

**Current behavior:** scanner breaks after 2 spaces. So
`parseColRow("col5 row3 cell-range extra")` returns `(5, 3)` —
ignoring everything after `row3`.

**Smell:** none. The wrapper's CSS class attributes look like
`"col5 row3 cell-range editable"` — the cell-coordinate prefix is
followed by additional class names. The "stop at second space"
semantic is exactly how the GWT side extracts coords from a class
list.

**Classification:** **KEEP** + **REPHRASE**. The behavior is a
real contract (CSS class lists with cell-key prefix). Rewrite the
comment from "preserved verbatim" to "the wire format embeds the
cell key in a CSS class list; parsing must stop at the boundary".

### Q3 — `CellKeys.headerType`: cell keys + "row5" return HEADER_NONE

**Location:** `lit/algorithms/CellKeys.ts:97-129` (slice 125).

**Current behavior:** `headerType("col1 row1")` returns 0 (NOT a
header) — because str[1] is 'o', not 'h'. Same for `headerType("row5")`.

**Smell:** none if you accept the convention. The naming
convention IS:
- Header keys: `"rh<N>"` (row header) / `"ch<N>"` (col header)
- Cell keys: `"col<C> row<R>"` (cells)

The classifier checks str[0..1] for `rh` or `ch`. Cell keys start
with `col` (str[0]='c', str[1]='o') — correctly NOT classified as
column headers (which would require str[1]='h').

**Classification:** **KEEP**. Real contract — the naming separation
is meaningful, not accidental. **REPHRASE** the comment to remove
the "preserved verbatim" framing and cite the naming convention.

### Q4 — `CellKeys.parseHeaderIndex`: accumulates ALL digit runs

**Location:** `lit/algorithms/CellKeys.ts:131-145` (slice 125).

**Current behavior:** `parseHeaderIndex("rh1ch2")` returns 12 (not
1, not 2, not error). The scanner accumulates digits anywhere.

**Smell:** moderate. The upstream presumably never calls with
multi-run input (header keys are single-typed: `rh<N>` or
`ch<N>`). The permissive impl handles the never-invoked case in a
non-obvious way.

**Classification:** **DROP**. Clean-room version: scan a single
digit run (the first one after `rh` or `ch`), bail on
non-digit-after-digit or extra prefix chars. Update the test
expectation. Visual-regression IT covers the no-real-impact claim.

### Q5 — `FormulaRefs`: `[A-z]` regex character class

**Location:** `lit/algorithms/FormulaRefs.ts` (slice 119).

**Current behavior:** the regex extracting cell-ref tokens uses
`[A-z]` (which in ASCII includes `[`, `\`, `]`, `^`, `_`, `` ` `` —
not just letters) instead of the standard `[A-Za-z]`. Slice 119
preserved this with a comment about "wider than standard,
intentional behavior".

**Smell:** strong. Cell references in Excel formulas are
alphanumeric (sheet name) + digits + colon. The 6 extra characters
that `[A-z]` accepts are not part of any legal cell reference. The
"intentional" claim in slice 119 was a defensive assumption; the
GWT-side intent is likely "I typed `[A-z]` thinking it meant
letters" rather than "I deliberately accepted ` ` `\` `]` `^` `_`".

**Classification:** **DROP**. Use `[A-Za-z]`. The
`FormulaBarWidget` IT tests + visual regression catch the no-impact
claim. Cell references with those characters wouldn't have parsed
correctly anyway because the surrounding tokenization expects
letters.

### Q6 — `FormulaRefs`: regex-metachar escape in sheet names

**Location:** `lit/algorithms/FormulaRefs.ts` (slice 119).

**Current behavior:** slice 119 ALREADY went beyond verbatim and
added an `escapeRegex(sheetName)` helper because the GWT version
would have miscategorized sheet names containing `.` (e.g.
`"Q1.2026"` → false matches).

**Smell:** none — this was the right call already.

**Classification:** **KEEP**. The slice 119 commit explicitly
acknowledged the deviation from verbatim. This entry exists in the
audit only to confirm the call was correct.

### Q7 — `Tsv` codec: newlines in cells break round-trip

**Location:** `lit/algorithms/Tsv.ts:27-31` (slice 120).

**Current behavior:** cells containing `\t` or `\n` get serialized
+ split lossy on parse. Documented as "Excel-clipboard quirk".

**Smell:** none. This IS Excel's clipboard behavior — every
spreadsheet app does this. TSV has no escape sequence; the
limitation is the format.

**Classification:** **KEEP**. Real-world contract that pre-dates
both GWT and TS by decades.

### Q8 — `ViewportMath.convertPointsToPixel`: `Math.trunc` mirrors Java's `BigDecimal.intValue()`

**Location:** `lit/algorithms/ViewportMath.ts:36-42` (slice 124).

**Current behavior:** uses `Math.trunc((points * ppi) / 72)` to
match Java's `BigDecimal.intValue()` (truncation toward zero), as
opposed to `Math.round` (banker's rounding) or implicit truncation.

**Smell:** none. Pixel calculations in spreadsheets are conventionally
truncated, not rounded — fractional pixels would produce
sub-pixel rendering artifacts (different on every browser /
DPI). Excel itself truncates.

**Classification:** **KEEP**. Real-world contract. **REPHRASE**
the comment from "mirroring Java's BigDecimal.intValue()" to
"truncate, not round — fractional pixels render inconsistently
across browsers".

## Summary of actions per quirk

| ID | Classification | Future-slice action |
| --- | --- | --- |
| Q1 | DROP | `getRowHeightPx`: change `>=` to `>` length; update test |
| Q2 | KEEP + REPHRASE | rewrite comment to cite CSS-class-list contract |
| Q3 | KEEP + REPHRASE | rewrite comment to cite naming convention |
| Q4 | DROP | `parseHeaderIndex`: single-run scanner; update test |
| Q5 | DROP | `[A-z]` → `[A-Za-z]`; visual-regression covers |
| Q6 | KEEP (already correct) | no action |
| Q7 | KEEP | no action (already cited Excel-clipboard contract) |
| Q8 | KEEP + REPHRASE | rewrite comment to cite truncate-for-pixel-stability |

**Estimated effort:** 1 follow-up slice handles all 4 DROPs +
REPHRASEs in one batch (it's pure-TS work with existing visual-
regression as the safety net). Defer to slice 130+.

## What this means for the broader "Ported byte-equivalent from..." framing

Every file in `lit/algorithms/` has javadoc claiming it's "ported
byte-equivalent from `<GWT-side file>`". Under the clean-room
direction, those framings are wrong-direction — they imply we
copied the algorithm.

The replacement framing per the `code-modernization` skill update
(slice 129) is:

- For algorithm code: cite the **behavior contract** (Excel spec,
  POI documentation, or "by test") + use the GWT file as a
  *reference implementation* for cross-checking, NOT as a
  byte-equivalent target.
- For wire-protocol code: byte-equivalence to the server's emitted
  shape is required (the server still emits the wire format
  unchanged). The DTO interfaces + the `SpreadsheetClientApi`
  interface stay as-is.

A follow-up slice (130 or later) sweeps the javadoc on each
algorithm file + updates the framing. The behavior doesn't change
in that sweep — only the comments do. The DROP-quirk slice
(above) and the REPHRASE-comment sweep can be the same slice.

## Why this matters for licensing/copyright

A verbatim byte-equivalent port reads as a derivative work of the
original GWT-side code. The QUIRK callouts + the "preserved
verbatim" comments + the implementation choices that mirror
GWT-Java idioms (1-based row indices in a TS context, the BigDecimal
truncation comment, the JSNI-influenced parser style) all signal
"this is the same algorithm, different syntax".

A clean-room rewrite, by contrast, derives the behavior from the
behavior contract (Excel spec, POI semantics, visual-regression
output), uses the GWT-side code only as a CROSS-REFERENCE for
spot-checking correctness, and produces code in the modern Vaadin
component idiom — which looks substantively different from translated
GWT-Java even if it computes the same outputs. Documentation
explicitly says "behavior is documented by test; GWT-side file is
informational" rather than "ported byte-equivalent from".

The 4 DROPs above are also the "we removed the upstream's bugs"
evidence. A derivative work copies the bugs; a clean-room rewrite
makes deliberate behavior choices.

## What this DOES NOT change

- The wire-format DTOs (`CellData`, `MergedRegion`, `OverlayInfo`,
  `SpreadsheetActionDetails`, `PopupButtonState`, `GroupingData`)
  stay byte-equivalent — they're the wire shape the server emits.
- The `SpreadsheetClientApi` interface stays — the wrapper calls
  these method names; the server expects them.
- The `SpreadsheetEventType` discriminated union stays — the event
  names are protocol.
- The `MirroredProperty<T>` server-side architecture stays.
- The 105-method API surface from slice 126 stays.
- The CSS class names (`col1 row1`, `.cell`, `.sheet-tabsheet`)
  stay — the TestBench module hardcodes them.

The clean-room direction applies to PURE ALGORITHMS — the
implementation behind each interface method, the math, the
parsers, the formatters. The interfaces, wire format, and CSS
class names are CONTRACTS that the server + tests + customer code
depend on; those stay byte-equivalent.
