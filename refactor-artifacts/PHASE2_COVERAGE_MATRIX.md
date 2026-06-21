# Phase 2 — Test coverage matrix (v1)

Bootstrap output from the first Phase-2 inventory pass (2026-06-05). Rows are
the **functional blocks** that emerged from Phase 1 (slices 1-19): the 7
god-class survivors plus 50+ extracted helpers. Columns are the six test tiers
the pipeline tracks. Cells: `✓` = at least one passing test of that tier
covers the block; `–` = tier is N/A for this block; blank = gap.

This matrix is the Phase 2 backlog. Each blank cell is a candidate Phase 2
slice (unless it's a non-applicable tier marked `–`).

## Tier legend

| Tier | What it is | When it pins |
|---|---|---|
| `unit` | Direct unit tests, no fixture (pure-logic) | Pure-function clusters, algorithm correctness |
| `unit-fix` | Unit tests with in-memory POI (XSSFWorkbook) | POI-bound logic that needs a real fixture |
| `golden` | Golden-master / payload tests via `MockUIExtension` | Cross-class interactions, RPC payload preservation |
| `IT` | Maven failsafe integration tests | Multi-class workflows, framework dispatch |
| `E2E` | TestBench / Selenium browser drive | UI behaviors, DOM-dependent interactions |
| `char` | Explicit characterization tests | Legacy behavior pinning where the docstring lies |

## Matrix

### Slice 1 — History & Commands

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| CommandStack | – | – | – | ✓ | ✓ | – | Undo/redo via E2E; no direct helper tests |
| CommandReplayer | – | – | – | ✓ | ✓ | – | Same as above |
| SpreadsheetHistoryManager | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | Comprehensive — set the baseline for characterization |

### Slice 2 — Merged Regions

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| MergedRegionLookup | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 42** — 12 tests, supplier indirection makes mocking unnecessary |
| MergedRegionCascade | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 43** — 11 tests, mocked Spreadsheet + XSSFWorkbook fixture |
| MergedRegionOperations | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 45** — 8 tests, mocked sheet + stateful AtomicReference for cache list |
| MergedRegionSynchronizer | | ✓ | | ✓ | ✓ | ✓ | **Slice 48** — 7 tests, mocked sheet + operations |

### Slice 3 — PopupButton lifecycle

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| PopupButtonRegistry | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 49** — 10 tests, mocked PopupButton instances |
| PopupButtonBinding | | ✓ | | ✓ | ✓ | ✓ | **Slice 64** — 9 tests; attach/detach + virtual-child parent guard + detachAll |
| PopupButtonRowShifter | | ✓ | | ✓ | ✓ | ✓ | **Slice 65** — 9 tests; row classification branches + detach-first + replaceAll |
| PopupButtonRefresher | | ✓ | | ✓ | ✓ | ✓ | **Slice 63** — 6 tests; sheet-name guard + in/out viewport dispatch |

### Slice 4 — Sheet Overlays

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| SheetOverlayStorage | | ✓ | | ✓ | ✓ | ✓ | **Slice 51** — 12 tests; flagged write-only `overlayComponents` set as dead state |
| SheetOverlayVisibility | | ✓ | | ✓ | ✓ | ✓ | **Slice 53** — 10 tests; pins DONT_MOVE_AND_RESIZE + hidden-range + 4-quadrant logic |
| SheetOverlayInfoBuilder | | ✓ | | ✓ | ✓ | ✓ | **Slice 54** — 9 tests; covers XSSF DONT_MOVE_AND_RESIZE + hidden-skip path |
| SheetOverlayManager | | ✓ | | ✓ | ✓ | ✓ | **Slice 55** — 14 tests; all collaborators mocked, static factory branch skipped |

### Slice 5 — Custom Components

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| CustomComponentRangeScanner | | ✓ | | ✓ | ✓ | ✓ | **Slice 61** — 13 tests; first use of MockedStatic in this suite |
| CustomComponentRowSizer | | ✓ | | ✓ | ✓ | ✓ | **Slice 60** — 8 tests; grow + restore + default-height fallback |
| CustomComponentLoader | | ✓ | | ✓ | ✓ | ✓ | **Slice 62** — 14 tests; null-factory clear + reconciliation + unloadAll |

### Slice 6 — Cell Comments

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| CellCommentRangeScanner | | ✓ | | ✓ | ✓ | ✓ | **Slice 59** — 11 tests; merged-region skip-ahead + zero-height-row + invalid-formula |
| CellCommentLoader | | ✓ | | ✓ | ✓ | ✓ | **Slice 58** — 9 tests; topLeftLoaded latch + 4-quadrant dispatch |

### Slice 7 — Hyperlinks

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| HyperlinkRangeScanner | | ✓ | | ✓ | ✓ | ✓ | **Slice 57** — 11 tests; XSSF/HSSF + formula-handler paths |
| HyperlinkLoader | | ✓ | | ✓ | ✓ | ✓ | **Slice 56** — 6 tests; 4-quadrant scan dispatch |

### Slice 8 — Row/column sizing

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| RowSizeOperations | | ✓ | | ✓ | ✓ | ✓ | **Slice 71** — 13 tests; height-zero-equals-hidden entanglement, defensive-copy on hidden list |
| ColumnSizeOperations | | ✓ | | ✓ | ✓ | ✓ | **Slice 72** — 16 tests; XSSF downcast + dual-state sync + cache invalidation |
| ColumnAutofitter | | ✓ | | ✓ | ✓ | ✓ | **Slice 73** — 12 tests; POI NPE catch + filter padding + WeakHashMap refresh |

### Slice 9 — Tables

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| TableRegistry | | ✓ | | ✓ | ✓ | ✓ | **Slice 67** — 12 tests; HashSet semantics + per-sheet filter + load-latch |
| TableLoader | | ✓ | | ✓ | ✓ | ✓ | **Slice 82** — 8 tests; latch short-circuit + per-table sheet filter |
| TableLifecycle | | ✓ | | ✓ | ✓ | ✓ | **Slice 83** — 8 tests; inactive-early-return + filter-table clear path |

### Slice 10 — Sheet Navigation

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| ActiveSheetSelector | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 75** — 13 tests; index translation + activation validation + reload sequence |
| SheetScrollMemory | | ✓ | | ✓ | ✓ | ✓ | **Slice 66** — 4 tests; defensive-copy semantic on both arrays |
| SheetChangeDispatcher | | ✓ | | ✓ | ✓ | ✓ | **Slice 84** — 8 tests; stash-then-mutate ordering + abs() index normalization |

### Slice 11 — Sheet metadata + lifecycle

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| SheetLifecycleOperations | | ✓ | | ✓ | ✓ | ✓ | **Slice 85** — 14 tests; name validation + min-1-visible guard + active-shift |
| SheetMetadataOperations | | ✓ | | ✓ | ✓ | ✓ | **Slice 74** — 12 tests; rename validation + hidden-sheet-skips-mirror |
| SheetVisibilityOperations | | ✓ | | ✓ | ✓ | ✓ | **Slice 86** — 12 tests; min-1-visible + no-op short-circuit + active-shift |

### Slice 12 — Cell value shifting

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| SequenceValueExtractor | ✓ | ✓ | – | – | – | ✓ | Dedicated tests (14) |
| SequenceIncrement | ✓ | – | – | – | – | ✓ | Pure-logic tests (15) |
| CellValueShifter | ✓ | | | ✓ | ✓ | ✓ | **Slice 24** — 10 tests, Mockito + XSSFWorkbook (FORMULA branch deferred) |
| RangeSequenceDetector | | ✓ | | ✓ | ✓ | ✓ | **Slice 22** — 13 tests, XSSFWorkbook fixture |
| DirectionalShiftOperator | | ✓ | | ✓ | ✓ | ✓ | **Slice 76** — 14 tests; 4 directional methods + asymmetric clear ordering |

### Slice 13 — Cell rendering / client cache

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| CellNumericFormatter | | | ✓ | ✓ | ✓ | | FormulaFormatterTest, NumberFormatIT |
| CellFitCalculator | | ✓ | | ✓ | ✓ | ✓ | **Slice 68** — 13 tests; BigDecimal compare + 4px padding constant |
| CellDeletionGuard | | ✓ | | ✓ | ✓ | ✓ | **Slice 77** — 17 tests; 3 deletion shapes + isInRange + handler propagation |
| ClientCellCache | ✓ | | | ✓ | ✓ | ✓ | **Slice 21** — 13 tests, pure data structure |
| ClientFlushQueue | ✓ | | | ✓ | ✓ | ✓ | **Slice 21** — 9 tests, pure data structure |
| ClientCacheFlusher | | ✓ | | ✓ | ✓ | ✓ | **Slice 40** — 6 tests, Mockito + XSSFWorkbook, pays off slice-38 spec |

### Slice 14 — Viewport

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| ViewportBounds | ✓ | | | ✓ | ✓ | ✓ | **Slice 22** — 10 tests, pure data structure |
| ViewportBufferConfig | ✓ | | | ✓ | ✓ | ✓ | **Slice 30** — 7 tests, Mockito |
| FreezePaneState | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 33** — 9 dedicated tests, Mockito |

### Slice 15 — Sheet structure / state

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| SheetDimensions | | ✓ | | ✓ | ✓ | ✓ | **Slice 69** — 11 tests; property-mirror invariant per setter |
| SheetGrouping | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 87** — 10 tests; row-data write-only asymmetry pinned |
| SheetCssStyleMaps | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 88** — 10 tests; 6 setter/mirror pairs |

### Slice 16 — Style building

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| DefaultStyleBaseline | | ✓ | | ✓ | ✓ | ✓ | **Slice 36** — 6 tests, XSSFWorkbook + mocked parent |
| FontStyleComposer | | ✓ | | ✓ | ✓ | ✓ | **Slice 39** — 9 tests, real DefaultStyleBaseline + XSSFWorkbook |
| CellBorderStyleBuilder | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 81** — 22 tests; storage + spillover maps + 5-cache lifecycle |

### Slice 17 — Conditional formatting

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| POIReflectionTools | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 79** — 16 tests; flagged setBorderLeft(NONE)-via-Java-API asymmetry |
| RuleBorderTracker | | ✓ | | ✓ | ✓ | ✓ | **Slice 78** — 14 tests; HSSF skip + spillover maps + NONE-default-grey |
| RuleEvaluator | | ✓ | ✓ | ✓ | ✓ | ✓ | **Slice 80** — 18 tests; CELL_VALUE_IS dispatch + formula path + type-mismatch + soft-fail |

### God-class survivors

| Block | unit | unit-fix | golden | IT | E2E | char | Notes |
|---|---|---|---|---|---|---|---|
| Spreadsheet | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | Core; covered everywhere but at feature level |
| CellSelectionShifter | | | ✓ | ✓ | ✓ | | SelectionIT |
| CellValueManager | | | ✓ | ✓ | ✓ | | Across IT |
| SpreadsheetStyleFactory | | | ✓ | ✓ | ✓ | | StyleIT, StylingPartsIT |
| ConditionalFormatter | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ConditionalFormatterTest |
| CellSelectionManager | | | ✓ | ✓ | ✓ | | Selection management |
| SpreadsheetFactory | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | Dedicated tests |

## Per-tier summary

| Tier | Coverage | Notes |
|---|---|---|
| `unit` | **Sparse** (5/62) | Only the edge helpers and the most-touched god class; ~57 helpers have no isolated unit tests |
| `unit-fix` | **Thin** (5/62) | A handful use XSSFWorkbook fixture; biggest gap for POI-bound logic |
| `golden` | **Strong** (~20/62) | 34 MockUIExtension test files cover features (Spreadsheet, ConditionalFormatter, grouping, formulas, comments, custom components, charts) |
| `IT` | **Comprehensive** (~60/62) | 78 integration tests cover almost every helper indirectly via feature scenarios |
| `E2E` | **Comprehensive at feature level** | All ITs are browser-driven; helpers covered indirectly |
| `char` | **Minimal** (4/62) | Only SpreadsheetHistoryManager + Sequence pair + ConditionalFormatter |

## Top 5 Phase 2 gaps (priority order)

The agent's recommendations, ranked by LOC × complexity × refactoring risk:

1. **CellSelectionManager (843 LOC)** — state machine; only E2E coverage. Refactoring is gated on unit-tested state-transition pinning.
2. **CellValueManager (1,175 LOC)** — cache invalidation edge cases not pinned. ClientCellCache + ClientFlushQueue flush protocol especially.
3. **SpreadsheetFactory (1,189 LOC)** — load-ordering dependencies not characterized; column/row load + named-range activation.
4. **RuleEvaluator (289 LOC)** — pure logic; type-coercion edge cases not unit-tested. Easy win.
5. **Spreadsheet (4,983 LOC)** — tangled monolith; covered everywhere but only at feature level. Per-concern isolation tests missing.

## Tier-wide observations

- **`unit` is systematically empty.** This is the biggest structural gap. The codebase relies on feature-level integration testing; helpers were never designed for isolation.
- **`unit-fix` is the natural tier for the next 30 slices.** Most package-private helpers are POI-bound but pure-logic — they need a real `XSSFWorkbook`, not a mock, but they don't need a UI.
- **`char` should grow with every refactor slice going forward.** Every Phase 1 / Phase 4 slice should add or expand characterization tests for the blocks it touches.
- **The E2E / IT blur is real.** The 78 "ITs" are all TestBench browser-driven; true headless integration tests (POI + in-memory Spreadsheet + no UI) are nearly absent. Adding this tier is a Phase 2 structural opportunity.

## How to use this matrix

Each blank cell is a candidate Phase 2 slice. Slices are scoped at **one block × one tier**:

- `Phase 2 slice: ClientCellCache × unit-fix` → write `ClientCellCacheTest.java` with XSSFWorkbook fixture covering the prune-by-row-range, prune-by-column, and dropSent operations.
- `Phase 2 slice: RuleEvaluator × unit-fix` → write `RuleEvaluatorTest.java` covering the matches/matchesFormula/matchesValue dispatch with realistic POI rules.
- `Phase 2 slice: CellSelectionManager × char` → write characterization tests pinning the state-machine transitions (esp. merged-region cases) as a precondition for any future restructure.

Each Phase 2 slice should land green, update this matrix's cell to `✓`, and be journaled like a Phase 1 slice.

## Definition of done for Phase 2

Per pipeline §12.5: every block has at least one `✓` (the floor) **and** at
least one characterization or golden master `✓` if it has non-trivial logic
**and** at least one Playwright sanity `✓` if it participates in a UI path
(plus explicit `–` for non-applicable tiers).

Current state: ~62 blocks × 6 tiers = 372 cells. Of those, ~5% `✓` for
`unit` and `unit-fix`, ~30% `✓` for `golden`, ~95% `✓` for `IT`/`E2E`, ~6%
`✓` for `char`. Most concrete near-term win: a `unit-fix` slice per helper.
