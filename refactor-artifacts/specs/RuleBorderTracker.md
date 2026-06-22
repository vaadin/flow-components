# Block: RuleBorderTracker

> Phase 3 spec. Drafted from the source (`RuleBorderTracker.java`)
> and the characterization tests (`RuleBorderTrackerTest.java`, 14
> tests, slice 78). **Status:** initial draft, not yet reviewed by
> a human operator.

## Purpose

Build the CSS for a conditional-formatting rule's border and
track which rules emit top- and left-border CSS that must spill
onto adjacent cells. The spillover indirection exists for the
same reason as
[`CellBorderStyleBuilder`](CellBorderStyleBuilder.md)'s
transferred-border maps — Excel renders top borders on the
cell-above's bottom edge to avoid double-rendering at shared
edges.

Owns two maps, both keyed by `ConditionalFormatting` so a later
cell match can look up whether the rule needs adjacent-cell
border styling:

- `topBorders` — CF → css-index whose CSS describes a
  `border-bottom` rule that should apply to the cell ABOVE a
  matched cell.
- `leftBorders` — analogous, for left borders rendered on the
  cell to the LEFT's right edge.

Sibling helpers:
[`POIReflectionTools`](POIReflectionTools.md) (for the
`isBorderSet` distinction),
[`CellBorderStyleBuilder`](CellBorderStyleBuilder.md) (the
non-CF border equivalent).

## Inputs

### `buildBorderCss(cf, rule, cssIndex, ...)`

- Emits the rule's border CSS into a `StringBuilder` (caller's).
- Per side (top / left / right / bottom): if the rule's border
  formatting has the side set (via
  [`POIReflectionTools.isBorderSet`](POIReflectionTools.md)),
  emit the CSS. For top / left: also register a spillover entry
  in `topBorders` / `leftBorders` so the matched-cell handling
  can know to also style the adjacent cell.
- The NONE-special-case: an explicitly-set `BorderStyle.NONE`
  emits a default `1pt solid #d6d6d6` (Excel's "none overrides
  previous rule" semantic).

### `clear()`

- Resets both spillover maps.

### `getTopBorderCssIndex(cf)` / `getLeftBorderCssIndex(cf)`

- Lookup for the adjacent-cell handler.

## Outputs

`buildBorderCss` writes CSS into the caller's StringBuilder.
Getters return `Integer` or `null`.

## Side effects

Mutation of `topBorders` / `leftBorders` maps. Read of
`POIReflectionTools.isBorderSet` for the explicitly-set
distinction.

## Invariants

- **NONE-special-case emits a default grey border**. The Excel
  semantic: an explicitly-set NONE in a CF rule OVERRIDES the
  previous rule's border. The default grey is what the override
  paints. A regression that omitted the NONE-emit would let
  the previous rule's border bleed through.
- **NONE-top writes into the rule's OWN CSS, not the spillover
  map**. Counter-intuitive: the NONE-top special case stays
  local (no adjacent-cell styling), while a non-NONE-top
  registers the spillover. Pinned by slice 78's "NONE-top
  doesn't populate the spillover map nor advance cssIndex"
  test.
- **cssIndex advances by 1 for each spillover entry**. Top and
  left each consume an index; right and bottom don't (they
  emit into the rule's own CSS).
- **Multi-CF keying**. Two CFs with the same rule shape get
  SEPARATE spillover entries because the key is the CF object.
  Same-shape rules from different CFs render correctly.

## Open questions

- The 4-way side asymmetry (top/left = spillover, right/bottom =
  local) is load-bearing but counterintuitive. A diagram in the
  source would help future readers; current shape relies on
  testers reading the slice 78 tests.
- `POIReflectionTools.isBorderSet` is the seam this class
  depends on. If it ever becomes possible to drop reflection
  (slice 103 considered it; rejected), this class's call sites
  would need to follow.
