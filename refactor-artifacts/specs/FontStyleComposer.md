# Block: FontStyleComposer

> Phase 3 spec. Drafted from the source (`FontStyleComposer.java`)
> and the characterization tests (`FontStyleComposerTest.java`,
> slice 39). **Status:** initial draft, not yet reviewed by a
> human operator.

## Purpose

Compose the font-related portion of a cell's CSS, emitting only
the properties that differ from the
[`DefaultStyleBaseline`](DefaultStyleBaseline.md). The "diff-only
emission" is the optimization that keeps cell-style CSS small:
the default style's rules are emitted once for cell-style index
0, and every other cell-style references only its overrides.

Holds no per-cell state — it's an instance only because the
comparison baseline travels with the parent factory.

## Inputs

### `appendFontStyles(CellStyle cellStyle, StringBuilder sb)`

- Walks: font-family, font-size, weight (bold), style (italic),
  underline, strikethrough, color. For each, compares to the
  baseline and emits CSS only if different.

### `styleFontFamily(Font font)` — `static`

- Returns the CSS font-family value for the font. Intentionally
  static — has no field touches AND
  [`DefaultStyleBaseline`](DefaultStyleBaseline.md) calls it
  during baseline capture (before any composer instance
  exists for the workbook).
- Pinned 6 FontFamily enum cases in slice 39: ROMAN → "roman",
  SWISS → "swiss", MODERN → "modern", SCRIPT → "script",
  DECORATIVE → "decorative", NOT_APPLICABLE → no family suffix.
  XSSF font-family lookup uses pattern-matching idiom
  `font instanceof XSSFFont xssfFont` (slice 100 sweep).

## Outputs

Appends CSS to the supplied StringBuilder. No return value.

## Side effects

Reads from `DefaultStyleBaseline` (no mutation).

## Invariants

- **Emit-only-if-different is load-bearing for CSS size.** A
  regression that emitted every property unconditionally would
  bloat the per-cell style by ~6-7 rules per cell. The diff
  pattern is what makes the CSS-per-style approach tractable.
- **`styleFontFamily` is static** so the baseline capture
  itself can use it. Pinned by slice 39's
  `DefaultStyleBaseline` capture test (which calls
  `FontStyleComposer.styleFontFamily(...)` directly without
  a composer instance).
- **The FontFamily enum coverage is exhaustive** — all 6
  enum cases have a test in slice 39 with exact-string
  expectations. NOT_APPLICABLE emits no suffix.

## Open questions

- The "emit only if different" pattern could be generalized
  across all cell-style aspects (alignment, fill, border).
  Currently only font uses it; other aspects always emit. A
  future refactor could extract a generic
  `CssDiffEmitter<T>` if the pattern recurs.
