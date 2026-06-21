# Phase 2 Test Audit — 2026-06-21

Run via the `refactor-test-audit` skill against all 58 existing test
files for extracted helpers, plus verification of 3 potentially-orphaned
helpers. Six parallel `Explore` agents audited the suites against the
5-axis checklist (Coverage / Behavioral / Style / Assertion /
Regression).

## Result summary

- **Audited**: 58 test files + 3 orphan-checks = 61 helpers
- **Clean**: 49
- **Weak** (sharpening / matcher tightening): 11
- **Gap** (documented, accepted): 1 (CellBorderStyleBuilder XSSF XML
  path — requires CTBorder XML fixtures, deferred by design)
- **Missing-test entirely**: 3 — `CommandStack`, `CommandReplayer`,
  `CellNumericFormatter`

## Critical — missing-test (3)

These are slice-1 / slice-13a extracted helpers with no dedicated
test file. Their behavior is only touched indirectly via coordinator
tests (`SpreadsheetHistoryManagerTest`, `CellValueManager`-level paths)
and the indirect coverage is thin enough that an algorithmic refactor
of the helper could regress silently.

### `CommandStack` (slice 1)
- No dedicated test.
- Indirectly exercised by `SpreadsheetHistoryManagerTest`'s undo/redo
  flow, but the test asserts the *coordinator* outcome
  (`history.getHistoryIndex()` etc.), not the stack's `push() /
  canUndo() / nextForUndo() / setMaxSize() / clipAfter()` directly.
- **Fix**: Add `CommandStackTest.java` covering: push happy path,
  overflow at maxSize, canUndo/canRedo boundary conditions, nextForUndo
  /nextForRedo cursor mutations, setMaxSize + clipAfter, clear.

### `CommandReplayer` (slice 1)
- No dedicated test.
- Indirectly exercised but its three methods
  (`makeSureCorrectSheetActive`, `changeSelection`,
  `fireCellValueChangeEvent`) have branch logic the coordinator tests
  don't assert on.
- **Fix**: Add `CommandReplayerTest.java` covering: sheet-switch +
  initialSheetSelection seeding (painted-range / selected-cell /
  default-A1 cases); changeSelection dispatch (single cell, range,
  null); fireCellValueChangeEvent firing for ValueChangeCommand
  subtypes and no-op for others.

### `CellNumericFormatter` (slice 13a)
- No dedicated test. `FormulaFormatterTest` tests a different class
  (`FormulaFormatter`).
- 7 public methods entirely uncovered by direct tests.
- **Fix**: Add `CellNumericFormatterTest.java` covering: updateLocale
  rebuilds all four formatting fields; getOriginalCellValue per
  CellType (FORMULA, NUMERIC, STRING, BOOLEAN, BLANK, ERROR); date
  formatting detection; getCachedFormulaCellValue per cached-result
  type; valueContainsOnlyNumbers edge cases; isGeneralCell detection.

## Important — gaps in existing tests (4)

### `CellValueShifterTest` — FORMULA + ERROR branches uncovered
- The `shift(...)` switch dispatches on cell type; FORMULA and ERROR
  cases have no test.
- **Fix**: Add tests for both. FORMULA needs the rowShiftRegex /
  formula-reference re-write path pinned.

### `DirectionalShiftOperatorTest` — sequence increment + multi-row up
- The `sequenceIncrement` parameter to `cellValueShifter.shift` is
  always asserted via `any()`, so a regression that fails to pass the
  computed increment through (or computes the wrong one) wouldn't be
  caught.
- `shiftRowsUp` lacks a multi-row duplication test (its
  shift-down sibling has one).
- **Fix**: Add a test that wires real source cells with known content
  and verifies `cellValueShifter.shift` receives the increment from
  `RangeSequenceDetector`. Add multi-row shiftRowsUp test.

### `SheetOverlayManagerTest` — OverlayChangeListener callback path
- `loadOrUpdate` attaches an `OverlayChangeListener` to each visible
  overlay (lines 119-124 of the SUT). No test exercises the listener
  callback or verifies it triggers re-load.
- **Fix**: Capture the listener with `ArgumentCaptor`, invoke
  `overlayChanged()` manually, verify `loadOrUpdate` is re-invoked.

### `FontStyleComposerTest` — FontFamily enum cases + assertion sharpness
- `styleFontFamily`'s switch handles 6 `FontFamily` cases (ROMAN,
  SWISS, MODERN, SCRIPT, DECORATIVE, NOT_APPLICABLE); only ROMAN is
  named in a test.
- Assertions use `assertTrue(css.contains(...))` which would pass on
  wrong-ordered substrings.
- **Fix**: Add per-FontFamily case tests. Tighten assertions to check
  ordering or exact substrings where the contract demands it.

## Minor — sharpening (7)

### `SequenceValueExtractorTest`
- One edge-case test asserts three separate `assertNull` instead of
  `assertArrayEquals(new String[]{null,null,null}, actual)` — tiny
  consistency.

### `MergedRegionOperationsTest`
- `addRegion_discardsExistingThatLiesInsideCandidate` verifies
  `verify(cascade).afterUnmerge(any())` / `afterMerge(any())` without
  capturing the region instance. A regression that swapped the two
  calls wouldn't be caught.
- **Fix**: Capture both with `ArgumentCaptor`, assert on region
  coordinates.

### `SheetOverlayStorageTest`
- `overlayComponents_isWriteOnly` test has NO assertion — calls
  `addComponent` / `removeComponent` in a loop but verifies nothing.
- **Fix**: Either remove the test (and convert to a comment in the
  test class header) or add a concrete observation (e.g. that the
  calls don't throw, via assertDoesNotThrow).

### `HyperlinkLoaderTest`
- `load_noFreezePane_onlyArea4Scanned` has both a specific
  `scan(1,1,10,10)` verify AND a loose `times(1).scan(anyInt,anyInt,
  anyInt,anyInt)` verify. The loose form duplicates without adding
  rigor.
- **Fix**: Remove the loose verify; the specific one is sufficient.

### `ColumnSizeOperationsTest`
- `setWidth_positive_setsColW_andCallsSheetSetColumnWidth` uses
  `anyInt()` for the second arg of `sheet.setColumnWidth(col, width)`.
  A regression in `pixel2WidthUnits` wouldn't be caught.
- **Fix**: Use `argThat(wu -> wu > 0)` or a specific computed value.

### `TableRegistryTest`
- `getAll_returnsLiveReference` mutates the returned `HashSet` and
  expects the registry to see the mutation. This pins implementation
  detail (HashSet identity). Acceptable as a documented contract (the
  source comment says it's intentional for performance) but the test
  should make that explicit.
- **Fix**: Add a comment that ties the test to the documented
  performance contract.

### `SheetGroupingTest`
- `setRowGroupingData_writeOnly_noFieldStored` asserts
  `assertNull(grouping.getColGroupingData())` to document the
  asymmetry between col-data (cached) and row-data (mirrored only).
  The assertion isn't wrong, but the connection to the asymmetry is
  implicit.
- **Fix**: Improve the comment to make the intent explicit.

## Accepted gap (1)

### `CellBorderStyleBuilderTest` — XSSF theme-border XML path
- Lines 129-180 of the SUT (XSSFCellStyle with
  `getApplyBorder() == false`) require constructing CTBorder XML
  fixtures to exercise. Documented in the test class header as
  deferred by design. Not a fix-pass target; recorded here so the
  decision is auditable.

## Fix-pass plan

Execute in three batches:

**Batch A** — Critical missing tests:
1. `CommandStackTest.java`
2. `CommandReplayerTest.java`
3. `CellNumericFormatterTest.java`

**Batch B** — Important gaps:
4. `CellValueShifterTest` — FORMULA + ERROR cases
5. `DirectionalShiftOperatorTest` — sequence increment + multi-up
6. `SheetOverlayManagerTest` — listener callback
7. `FontStyleComposerTest` — FontFamily cases + ordering

**Batch C** — Minor sharpening (all 7 weak findings above).

After each batch: spotless + checkstyle + test (full suite).

## Open question — resolved

The agent's "minimal coverage" flag on
`SpreadsheetHistoryManagerPayloadTest` was reread during the fix pass.
The two @Test methods are golden-snapshot payload assertions — they
capture the full server-to-client element-property delta plus pending
JS invocations and compare against a literal byte-exact golden string.
That's intentionally strong, not weak coverage — any future refactor
that perturbs the wire shape would fail one of these. Resolution:
keep the test as-is. The "low test count" was misleading because each
test asserts on the entire payload, not a single property.

## Fix-pass execution log

All three batches landed green. Full suite 963 tests, 0 failures, 4
pre-existing skips. Numbers (delta vs prior 879-test baseline):

- **Batch A**: +66 tests across 3 new test files
  (CommandStackTest 22, CommandReplayerTest 13, CellNumericFormatter 31).
- **Batch B**: +17 tests across 4 existing files
  (CellValueShifter +5 FORMULA/ERROR, DirectionalShiftOperator +5
  dispatch+multi-up, SheetOverlayManager +1 listener,
  FontStyleComposer +6 FontFamily exhaustive).
- **Batch C**: 0 net new; 7 tests sharpened in place.

## Quirk discovered during fix-pass

`CellNumericFormatter.valueContainsOnlyNumbers` builds its regex by
concatenating `localeDecimalSymbols.getDecimalSeparator()` into the
pattern without `Pattern.quote(...)`. Under US locale the separator
is `.`, which is a regex metacharacter meaning "any single
character" — so "1,5", "1X5", "1.5" all match. Pinned as a quirk in
`CellNumericFormatterTest.valueContainsOnlyNumbers_commaUnderUSLocale_pinsRegexMetacharQuirk`.
A future patch can quote the separator; the test name and comment
explicitly tell the next reader to update the test when that
happens. Not blocking the current refactor; documented here so the
fix is not lost.

## Meta-audit on the fix-pass tests (2026-06-21)

After Batches A/B/C landed, six parallel `Explore` agents reapplied
the 5-axis checklist to the 84 new and modified tests. The audit
asked, for each test, "name a 1-line regression that would break it"
— intentionally hunting for false positives where the test passes
trivially. Findings:

- **1 HIGH** — `CellValueShifterTest.shift_formulaCell_invalidFormulaTriggersCatchBranch_copiesOriginal`
  was a false positive: name promised catch-branch coverage but the
  test used "2+3" (no cell references), so the matcher loop never
  ran. A real regression in the catch fallback would not have been
  detected. Resolved by (a) renaming the original test to
  `shift_formulaCell_noCellReferences_passesThroughUnchanged` (which
  honestly states what it pins) and (b) adding a NEW test
  `shift_formulaCell_postShiftRefIsInvalid_catchBranchRestoresOriginal`
  that genuinely triggers the catch via a row-shift that produces
  "A0" (POI rejects row 0 in formula syntax → catch fires →
  fallback restores the source's "A1" on dst).

- **1 MEDIUM** — `MergedRegionOperationsTest.addRegion_discardsExistingThatLiesInsideCandidate`
  used ArgumentCaptor to pin coordinates but plain `verify(cascade)
  .afterUnmerge(...)` / `verify(cascade).afterMerge(...)`. A
  regression that swapped the two cascade calls would still pass
  because the captured MergedRegion args are determined by local
  variables, not call order. Resolved by replacing both verifies
  with `InOrder cascadeOrder = Mockito.inOrder(cascade)` and
  ordered `cascadeOrder.verify(...)` calls.

- **1 MEDIUM** — `CommandReplayerTest.fireCellValueChangeEvent_valueChangeCommand_emptyChangedCells_stillFires`
  used `any(CellValueChangeEvent.class)` to verify the event fired.
  A regression that fired an event with a placeholder non-empty
  cells set would slip past. Resolved by adding ArgumentCaptor +
  explicit `assertTrue(event.getChangedCells().isEmpty())` mirroring
  the non-empty test in the same file.

- **1 LOW** — `CellNumericFormatterTest.getOriginalCellValue_blankCell_returnsEmptyString`
  was vacuous: BLANK and default both return "", so removing the
  explicit BLANK case would not break the test. Resolved by
  strengthening to assert no value-extraction getters (getCellFormula,
  getStringCellValue, getBooleanCellValue, getNumericCellValue,
  getErrorCellValue, getDateCellValue) are consulted — a regression
  that fell BLANK through to a different cell-type branch would now
  fire one of those and fail the test.

- **1 LOW** — `CellNumericFormatterTest.getOriginalCellValue_numericDateFormatted_formatsViaSimpleDateFormat`
  had a misleading comment claiming "independent of the host's
  default locale". Both test and SUT actually use the JVM default
  via no-arg SimpleDateFormat — consistent across hosts, but NOT
  locale-independent. Comment corrected.

- **1 LOW** — `CellNumericFormatterTest.isGeneralCell_stringEvenWithGeneralFormat_false`
  relied on NPE-as-failure mode if the AND order flipped. Resolved
  by stubbing `style.getDataFormatString()` to return "General" so
  the assertion holds regardless of AND order (the order is not
  part of the contract, just an implementation detail).

- **1 LOW** — `SheetOverlayStorageTest.overlayComponents_isWriteOnly`
  used `assertDoesNotThrow` over a loop, but the contract is already
  pinned by the sibling `overlayComponents_addAndRemove_donotThrow`
  test. Removed the redundant test; folded its commentary about the
  write-only nature into the sibling's comment block.

**After meta-audit**: 7/7 findings addressed. Suite still 963 / 0 /
0 / 4 green; spotless + checkstyle clean. The catch-branch trigger
test in particular goes from "no real regression coverage" to "POI
rejects A0 → catch's setCellFormula fallback fires → dst formula
restored to source's" — exactly the contract the SUT documents.
