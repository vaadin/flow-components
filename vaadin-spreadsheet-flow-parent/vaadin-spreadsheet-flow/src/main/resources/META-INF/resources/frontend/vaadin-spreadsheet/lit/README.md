# Vaadin Spreadsheet — TypeScript / Lit client (in-development)

> Phase 3.1+ of the Tier 3 GWT-replacement effort.
> See `refactor-artifacts/TIER3_PLAN.md` for the full project brief.
> **Status:** scaffolding — only pure-algorithm subsystems ported.

## What's here

```
lit/
├── README.md          (this file)
├── Spreadsheet.ts     (the class the wrapper will use as this.api — skeleton)
├── types/
│   ├── wire.ts                    (TS interfaces for the 6 wire DTOs)
│   ├── rpc-server-to-client.ts    (SpreadsheetClientApi interface — 50 methods)
│   └── rpc-client-to-server.ts    (SpreadsheetEvent discriminated union — 39 types)
├── algorithms/
│   ├── CellKeys.ts            ("col1 row1" wire codec + header-key classifier)
│   ├── CellRef.ts             (A1↔{col,row} parser, column-letter math)
│   ├── FormulaRefs.ts         (formula → cell-ref list, isCellRef regex)
│   ├── MergedRegion.ts        (MergedRegion shape + factory)
│   ├── MergedRegionUtil.ts    (findIncreasingSelection algorithm)
│   ├── Tsv.ts                 (clipboard tab-separated-values codec)
│   └── ViewportMath.ts        (point↔px conversion, row-height lookup, computeViewportBounds)
├── elements/
│   ├── SpreadsheetCellGrid.ts (the cell-grid LitElement — absolute-positioned cells)
│   └── types.ts               (CellGridRenderState + sibling input shapes)
├── state/
│   └── SelectionState.ts      (selection state + helpers — pure module)
└── wire/
    └── parser.ts      (JSON wire-format parser)
```

## Where this fits in the bigger picture

The existing **Lit wrapper element** at
`../vaadin-spreadsheet.js` is the public API surface of the
`<vaadin-spreadsheet>` web component. Today it delegates to
`this.api` — an instance of the **GWT-compiled `Spreadsheet`
class** in the 1.3 MB `../spreadsheet-export.js` blob.

The Tier 3 plan replaces that 1.3 MB blob with a new in-house
TS/Lit implementation. The Lit wrapper itself doesn't change
(much). What changes is what's UNDER it.

```
+---------------------------------------------+
| <vaadin-spreadsheet> (LitElement, ~600 LOC) |  PUBLIC API SURFACE
|   delegates to this.api                     |  (unchanged in Tier 3)
+---------------------------------------------+
              |
              v
+---------------------------------------------+
| TODAY: this.api = new Spreadsheet(...)      |  Will be REPLACED in
|        from spreadsheet-export.js (1.3 MB,  |  Phases 3.1-3.5.
|        GWT-compiled)                        |
+---------------------------------------------+

                  ⬇ replacement ⬇

+---------------------------------------------+
| AFTER: this.api = new Spreadsheet(...)      |  Same public API,
|        from this lit/ directory             |  TS/Lit underneath.
+---------------------------------------------+
```

The new code is being built incrementally. As of Phase 3.1
slice 118, ONLY the pure-algorithm subsystems are ported:

- ✓ MergedRegion + MergedRegionUtil (slice 118)
- ✓ Wire-format parser (slice 118)
- ✓ Cell-address parser + formula-ref extraction (slice 119)
- ✓ Wire-protocol RPC types + TSV codec (slice 120)
- ✓ Spreadsheet skeleton class + SelectionState (slice 121)
- ✓ ViewportMath: viewport bounds + unit conversion (slice 124)
- ✓ CellKeys: wire-format codec (toKey, parseColRow, header classifier) (slice 125)
- ✓ SpreadsheetCellGrid LitElement scaffolded (slice 132) — minimal
  positioning + cell-content render; styling / selection / merged
  cells / frozen panes follow in slices 135-137
- ◯ Cell rendering (Phase 3.2 cont.)
- ◯ Virtual scrolling — `computeViewportBounds` shipped (slice 124),
  cell recycling + DOM wiring still pending
- ◯ Selection (Phase 3.2)
- ◯ Formula bar + editing (Phase 3.3)
- ◯ Sheet tabs, grouping, popup buttons, etc. (Phase 3.5)
- ◯ Custom editors + Vaadin integration (Phase 3.4)

Until Phase 3.6 (rollout), this code is NOT loaded by the
Lit wrapper. It exists as a parallel codebase under
development. Once the new `Spreadsheet` class is feature-
complete, the wrapper's import switches from
`./spreadsheet-export.js` to `./lit/Spreadsheet.ts` (or its
bundled equivalent) behind a feature flag.

## Conventions

The new code follows TypeScript strict mode and the
conventions established by `vaadin-grid-flow`'s
`gridConnector.ts` — modern ES modules, no GWT residue.

- **No `@ts-nocheck`.** Every file is type-checked.
- **Pure functions where possible.** Algorithms take inputs +
  return outputs; side effects are in named functions.
- **Wire DTOs are interfaces, not classes.** They're the
  JSON shape; behavior lives elsewhere.
- **Test-coverage per module.** Each ported subsystem has a
  `.test.ts` file in the IT module's `test/` directory.
- **Clean-room rewrite, NOT verbatim port** (clarified 2026-06-22,
  slice 129). The GWT-side code is a CROSS-REFERENCE, not an
  implementation template:
  - **Mode A (wire / contract code)** — DTOs, RPC interface names,
    event-type discriminators, CSS class names — preserve
    byte-equivalence to the server's emitted shape. The server
    + the testbench module + the customer-facing API depend on it.
  - **Mode B (algorithm / implementation code)** — pixel math,
    parsers, formatters, state helpers — derive from the BEHAVIOR
    contract (Excel spec, POI semantics, tests). Use the GWT-side
    file only to cross-check correctness; close it before writing
    the TS version. Drop apparent bugs (`row >= length`
    off-by-one, `[A-z]` regex character class that includes 6
    non-letter chars). Comments cite the behavior contract, not
    the source-file location. See `refactor-artifacts/QUIRKS_AUDIT.md`
    for the per-quirk classification of the slices 118-125 banked
    quirks + cleanup direction.

The legacy maintenance burden + licensing/copyright concerns are
the explicit drivers. See `refactor-artifacts/GWT_ELIMINATION_ROADMAP.md`
for the full elimination plan.

## Testing

Tests run via Web Test Runner (the existing flow-components
convention, established in `vaadin-grid-flow-integration-tests`).
Test files live in
`../../../../../../../vaadin-spreadsheet-flow-integration-tests/test/`
and import production code via relative paths.

```sh
# One-time: install top-level dependencies (web-test-runner, chai, etc.):
(cd ../../../../../../../../.. && npm install)

# From the integration-tests module, run all Lit tests:
cd ../../../../../../../vaadin-spreadsheet-flow-integration-tests
../../node_modules/.bin/wtr

# Run a specific test file:
../../node_modules/.bin/wtr test/lit-merged-region-util.test.ts
```

The Web Test Runner config is at
`../../../../../../../vaadin-spreadsheet-flow-integration-tests/web-test-runner.config.mjs`.
It sets `rootDir: '..'` so the runner can serve the lit/ source from
the sibling module via relative `../../vaadin-spreadsheet-flow/...`
imports.

## How the team continues

See `refactor-artifacts/TIER3_PLAN.md` § 6 for the phased
roadmap. The next chunks of work:

1. **Phase 3.1 cont.** — port remaining pure-algorithm
   helpers (Parser for formula references, sequence-detection
   utilities, etc.).
2. **Phase 3.2** — cell grid + virtual scrolling (the
   critical path; ~6 weeks).
3. **Phase 3.3** — editing + formula bar.
4. **Phase 3.4** — Vaadin Flow integration (custom editors,
   overlays).
5. **Phase 3.5** — polish + parity.
6. **Phase 3.6** — rollout via feature flag.
