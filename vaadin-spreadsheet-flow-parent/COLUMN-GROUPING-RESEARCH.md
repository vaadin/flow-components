# Dynamic column grouping for Spreadsheet: research notes

Research and prototype for [vaadin/sponsored-development#58](https://github.com/vaadin/sponsored-development/issues/58),
done 2026-09-28 to 2026-10-01. Estimate posted in
[this comment](https://github.com/vaadin/sponsored-development/issues/58#issuecomment-5932943904).

This branch (`proto/spreadsheet-column-grouping-api`) is a prototype, not for
merging. This file collects everything learned, so the real implementation can
start from it.

## Contents

1. [The request](#1-the-request)
2. [Summary](#2-summary)
3. [Estimate](#3-estimate)
4. [Proposed API](#4-proposed-api)
5. [Limitations](#5-limitations)
6. [How grouping works today](#6-how-grouping-works-today)
7. [Apache POI findings](#7-apache-poi-findings)
8. [Excel behaviour](#8-excel-behaviour)
9. [Known bugs](#9-known-bugs)
10. [Fixes that can land before the feature](#10-fixes-that-can-land-before-the-feature)
11. [Refactors that can land before the feature](#11-refactors-that-can-land-before-the-feature)
12. [The prototype](#12-the-prototype)
13. [Prototype gaps](#13-prototype-gaps)
14. [Risks](#14-risks)
15. [Open questions](#15-open-questions)
16. [Test plan](#16-test-plan)
17. [Tooling and lessons](#17-tooling-and-lessons)

## 1. The request

EPICOR (Vaadin 25.2.3, XLSX workbooks) wants Excel-style column groups that are
fully managed from documented server-side APIs:

- create, update, remove and clear column groups;
- nested groups, and groups ending at the final column;
- collapse and expand from the server;
- an event when the user collapses or expands a group;
- read and restore collapsed states, including nested ones;
- refresh grouping after the first render, keeping in sync: group definitions,
  nested visibility, collapsed state and hidden columns, column widths and
  autofit widths, cells shown after expanding, frozen and scrollable panes,
  control positions, and the viewport limits.

Today they use protected and package-private methods, edit POI outline data
directly, and touch client internals. They report stale or misaligned
controls, restored cached widths, missing cells after expanding,
server/client mismatches, and controls outside the component, over frozen
panes, or visible under a collapsed parent.

Questions they asked, with the answers given:

| Question | Answer |
|---|---|
| Is dynamic column grouping officially supported? | No. Only groups loaded from the workbook are supported (there is a docs demo). Outline changes made at runtime through POI are not synchronized. |
| Supported API for defining and clearing groups? | None today. Proposed in [section 4](#4-proposed-api). |
| Supported API for collapse/expand and events? | None today (only `protected` methods, no event). |
| Supported way to restore nested collapsed states? | None today. The prototype's `setColumnGroupCollapsed` can restore them in any order. |
| Fix or public API planned for Vaadin 25? | The new API can't go into a 25.2 maintenance release; it would go into the next 25.x minor. The bug fixes in [section 10](#10-fixes-that-can-land-before-the-feature) can be backported to 25.2. |

## 2. Summary

- **Feasible, with low architectural risk.** Spreadsheet already renders
  groups from the workbook. What's missing is a public API, an event, and a
  refresh that works after the first render. None of it needs a redesign.
- **The hard part is correctness.** The current column code calls private POI
  methods by reflection and has nested-group bugs. Two public POI methods
  corrupt the `<cols>` element in some states. The prototype replaces the
  column path with a small model that uses only public POI API.
- **Most of the risk is in the GWT client:** scrolling and frozen panes, an
  area with a history of bugs.
- **XLSX sets the limits.** Spreadsheet exports through POI to XLSX (and XLS),
  so it follows what Excel can store and does
  ([section 8](#8-excel-behaviour)).

## 3. Estimate

**21 days (range 18–25.5) for columns**, including tests, docs and review.
Backporting the bug fixes to 25.2 isn't included (about 0.75 day). Rows with
the same API add **6–8 days**. One
developer who knows Flow but not Spreadsheet's internals; focused working
days, not calendar time. All numbers are judgment and are not calibrated
against past Vaadin work.

| # | Work item | Days | Reasoning |
|---|---|---|---|
| 1 | API spec and review | 1.5 | The shape exists in the prototype. Time goes to the decisions in [section 15](#15-open-questions) and one Vaadin API review round. |
| 2 | Harden the outline model | 3.5 | About five cases at half a day each (adjacent-group merge, groups sharing an end column, an update-range method, validation, inverted outlines), plus 1 day buffer for more POI surprises: two came up in the prototype. |
| 2b | Hidden-state handling | 1 | Follow Excel for columns the app hid itself; derive each group's state from the `hidden` flags; make client and server read the same thing. |
| 2c | `<cols>` compaction and benchmark | 1 | Merge identical neighbouring `<col>` elements after changes; benchmark a few thousand columns. |
| 3 | Replace the legacy column code | 2 | Move `loadGrouping` onto the model and drop the column reflection. Mechanical, but the old index and sorting logic is fragile. |
| 4 | Sync gaps | 2 | About six small gaps ([section 13](#13-prototype-gaps)). `calculateSheetSizes` is used by many features, so each change needs regression checks. |
| 5 | Client (GWT) fixes | 2.5 | Five fixes ([section 10](#10-fixes-that-can-land-before-the-feature)). With SDM, rebuilds take about 3 s, so time goes into understanding the layout code. Least certain item. |
| 6 | Tests | 3.5 | About seven TestBench scenarios at half a day each, plus running and fixing the existing grouping ITs. |
| 7 | Round-trip in Excel and LibreOffice | 0.5 | Open saved files and check for repair prompts. |
| 8 | Docs | 1.5 | vaadin/docs article and demo with its own review, plus Javadoc. |
| 9 | Review rounds | 1.25 | Review feedback on the bug-fix PRs and the feature PR. Backporting the bug fixes to 25.2 isn't counted: about 0.75 day, mostly GWT conflicts across 25.x branches. |
| 10 | Ramp-up and build friction | 0.25 | SDM is documented on `main`; the traps are written down in [section 17](#17-tooling-and-lessons). |
| | **Total** | **20.5** | |

- **18 days** if the client fixes stay local, the spec decisions take the
  simple options, and POI has no more surprises.
- **25.5 days** if the client fixes uncover new scroll or frozen-pane layout
  problems and the round trip forces a change to how columns are stored.
  Items 5 and 2 carry most of this spread.

Part of this can be done earlier as prep work, using the bug fixes in
[section 10](#10-fixes-that-can-land-before-the-feature) and the refactors in
[section 11](#11-refactors-that-can-land-before-the-feature). That moves about
2–2.5 days out of the feature and adds about 0.5 day of review overhead.

### Bug fixes and feature

The work items above mix bug fixes with feature work. Sorted by bug instead,
the estimate splits into three parts:

| Part | Days | Range |
|---|---|---|
| A. Bugs the customer reported | 4.75 | 4–6.5 |
| B. Bugs found in this research, not reported | 3.25 | 3–4 |
| C. The feature | 13 | 11–15 |
| **Total** | **21** | **18–25.5** |

Both bug groups also affect groups loaded from an XLSX file, with no new API.
Most of them come from code reading, so each must be reproduced before it's
fixed; a bug that doesn't reproduce drops out with its time. The bugs are
described in [section 9](#9-known-bugs).

**A. Bugs the customer reported**

| Bug | Confirmed? | Days | From item |
|---|---|---|---|
| Cells missing after expanding a nested group | Code reading | 0.25 | 4 |
| The control flips before the server answers, so browser and server disagree on visible columns | Code reading | 0.5 | 5 |
| The control of a group ending at the last column goes outside the sheet | Reproduced, fixed in prototype | 0.25 | none (new) |
| Controls slide over the row headers while scrolling (group pane not clipped) | Code reading | 1 | 5 |
| Controls over frozen panes (freeze-pane clone condition) | Code reading | 0.5 | 5 |
| Stub controls under a collapsed parent group (probably also #3912) | Code reading | 0.5 | 3 |
| Cached widths come back: a local column resize can skip the next server redraw | Code reading | 0.5 | 5 |
| Tests, mostly TestBench ITs for scrolling and frozen panes | | 1 | 6 |
| Review | | 0.25 | 9 |
| **Total** | | **4.75** | |

**B. Bugs found in this research**

| Bug | Confirmed? | Days | From item |
|---|---|---|---|
| Expanding a parent group keeps the columns of nested groups hidden | Reproduced (2 disabled tests in `GroupingTest`) | 0.5 | 3 |
| Collapsing or expanding resets `setMaxColumns` / `setMaxRows` | Reproduced | 0.5 | 4 |
| Level buttons: the bar isn't rebuilt, and a click reloads the whole component | Code reading | 0.5 | 3, 5 |
| Small server bugs: `hidden="0"` reads as collapsed, NPE in `expandColumn`, crash on a saved sheet without `<cols>`, collapse marker past the last column | Marker reproduced in POI, others code reading | 0.5 | 3 |
| Autofit width for filter buttons lost; groups and hidden columns past `cols` not sent | Code reading | 0.5 | 4 |
| Tests, mostly unit tests | | 0.5 | 6 |
| Review | | 0.25 | 9 |
| **Total** | | **3.25** | |

Every bug in group B breaks something the customer asked for, so it makes
sense to keep them in the task:

- Nested expand and `hidden="0"` read as collapsed: nested groups and restoring
  collapsed states.
- Limits reset and columns past `cols`: the viewport limits.
- Autofit width lost: column widths and autofit widths.
- Collapse marker past the last column: groups ending at the final column.
- NPE and the crash on a sheet without `<cols>`: the new API calls themselves,
  for example `addColumnGroup` on a saved sheet.
- Level-button bar and full reload: the reload drops component state, and the
  toggle event and refresh must also work for level buttons.

**C. The feature** is what's left of the work items: 1 (1.5), 2 (3.5),
2b (1), 2c (1), 3 (0.25: move `loadGrouping` onto the model), 4 (0.75: sheet
switching and overlays), 6 (2), 7 (0.5), 8 (1.5), 9 (0.75) and 10 (0.25).
That's 13 days. It includes refreshing the controls when groups change at
runtime, which is where the customer's "stale or misaligned controls" and most
of the "cached widths" problem come from. Those aren't bugs: changing groups
after the first render isn't supported today.

Backporting isn't counted in any part. It would add about 0.5 day for group A
and 0.25 day for group B.

The total is 0.5 day above the 20.5 from the work items, because sizing each
bug separately came out a little higher: the last-column fix wasn't in any
item, and the client fixes add up to 0.25 day over item 5.

The row bugs #10207 and #10226 aren't in A or B. They belong to the row work
(+6–8 days).

**Rows (+6–8 days)** is not a copy of the column work: rows have their own
reflection path in `GroupingUtil`, rows may not exist yet and must be created,
the collapsed flag is stored on the row, and the two open Major bugs in this
area are both about rows (#10207, #10226).

How the estimate changed:

| Version | Columns | Rows | Why |
|---|---|---|---|
| First draft | 18 (15–24) | +4–5 | Built from the prototype findings. |
| After independent review | 22 (18–28) | +6–8 | Prototype gaps added as work items; row bugs added to scope. |
| After SDM was set up | 21 (17.5–26) | +5.5–7.5 | Client loop down from about 45 s to about 5 s; mostly lowers the upper end. |
| After the Excel checks | 21 (17.5–25) | +5.5–7.5 | Hidden-column behaviour follows Excel, so no server-side state is needed. |
| Posted | 21 (18–25) | +6–8 | Rounded. |
| Split by bug, backport excluded | 21 (18–25.5) | +6–8 | Per-bug sizing adds 0.5 day; the backport of the bug fixes (0.75 day) is no longer counted. |

## 4. Proposed API

On `Spreadsheet`, for the active sheet, XLSX only. This is what the prototype
has, plus the open points from [section 15](#15-open-questions).

```java
void addColumnGroup(int firstColumn, int lastColumn)
void removeColumnGroup(int firstColumn, int lastColumn)
void clearColumnGroups()
List<ColumnGroup> getColumnGroups()
void setColumnGroupCollapsed(ColumnGroup group, boolean collapsed)
Registration addColumnGroupToggleListener(ComponentEventListener<ColumnGroupToggleEvent> listener)
void refreshGrouping()

record ColumnGroup(int firstColumn, int lastColumn, int level, boolean collapsed)
```

- `addColumnGroup`: a range inside an existing group becomes a nested group.
  At most 7 levels (Excel's limit; POI doesn't enforce it, so the API must).
- `removeColumnGroup`: removes one level from the range, like "Ungroup" in
  Excel.
- `clearColumnGroups`: removes all groups and shows the columns that collapsed
  groups had hidden.
- `getColumnGroups`: all groups, including nested ones and groups inside a
  collapsed parent. Sorted by first column, then level.
- `setColumnGroupCollapsed`: nested groups keep their own state, so a list from
  `getColumnGroups()` can be restored in any order. The group is matched by
  range and level; its `collapsed` value is ignored.
- `ColumnGroupToggleEvent`: `getGroup()` returns the group with its new state,
  `isCollapsed()` is a shortcut for `getGroup().collapsed()`, and
  `isFromClient()` tells a user click from a server call.
- `ColumnGroup.contains(int column)` is a small helper.
- `refreshGrouping`: resends the grouping state to the browser. Only needed
  after changing outlines directly through POI. All API methods call it.

Columns are 0-based and `lastColumn` is inclusive, like the rest of the
Spreadsheet API.

## 5. Limitations

These match how Excel behaves with the same XLSX file
([section 8](#8-excel-behaviour)):

- **Nested groups that end on the same column collapse and expand together.**
  They share one summary column, and XLSX has one `collapsed` flag per column.
  After collapsing and expanding the outer group, the inner group is expanded
  too. A file where only the inner group is collapsed still opens with that
  state. Google Sheets keeps the two apart, but XLSX can't save the inner
  group's state while the outer group is collapsed.
- **Expanding a group shows all of its columns,** including columns the app hid
  itself. XLSX can't tell "hidden by a group" from "hidden by the app".
- **A group ending at the last sheet column (XFD)** has no column after it to
  hold the `collapsed` flag. Its state is derived from all of its columns being
  hidden. Excel doesn't write a flag there either.
- **XLSX only.** `.xls` (HSSF) has no equivalent API in POI; the prototype
  throws for writes and returns an empty list for reads (make this
  consistent, see [section 15](#15-open-questions)).

## 6. How grouping works today

As on `main` at `c1eb3a2194`, before the prototype's changes.

### Server

- `SpreadsheetFactory.loadGrouping(Spreadsheet)` (package-private) reads the
  first `<cols>` element and the row outline levels into `GroupingData` lists,
  and sends them as element properties. It runs on sheet load and after each
  collapse/expand.
  - It assumes `<col>` elements are sorted.
  - It skips children of collapsed groups, but only compares with the last
    group added.
  - It drops a group that ends (or, when inverted, starts) on the same column
    as another group, so that group gets no control. Its comment says Excel
    does the same; not checked.
  - It reads the collapsed state with `isSetHidden()` on the group's first
    column.
- `SpreadsheetFactory.calculateSheetSizes` rebuilds `rows`, `cols`, `rowH`,
  `colW` and the hidden indexes from POI. It resets `cols` and `rows` to the
  sheet's data size, which undoes `setMaxColumns` and `setMaxRows`.
- `Spreadsheet.setGroupingCollapsed(boolean isCols, int index, boolean collapsed)`
  is `protected`. `index` is the group's `uniqueIndex`, not any column in it
  (its Javadoc says otherwise, which is outdated).
  It calls `GroupingUtil`, then `calculateSheetSizes`, `loadGrouping`,
  `reloadActiveSheetStyles`, overlays and `updateMarkedCells`.
- `Spreadsheet.levelHeaderClicked(boolean isCols, int level)` is `protected`.
  For columns it only toggles `hidden` (no `collapsed` flags) and then does a
  full `reloadSpreadsheetComponent`, which rebuilds custom components and
  tables.
- `GroupingUtil` (package-private) has copies of POI's outline logic and calls
  private `XSSFSheet` methods by reflection ([section 7](#7-apache-poi-findings)).
- `GroupingData` (shared DTO): `startIndex`, `endIndex` (0-based, inclusive),
  `level`, `uniqueIndex` (0-based column that identifies the group for
  collapse/expand), `collapsed`.
- The only public way to show a group added through POI after the first render
  is `reload()`, which rebuilds the whole component.

### Client (GWT)

- Element properties: `colGroupingData`, `colGroupingMax`,
  `colGroupingInversed`, the row equivalents, `hiddenColumnIndexes`, `colW`,
  `cols`, `rows`. `vaadin-spreadsheet.js` forwards changed properties to
  `SpreadsheetJsApi`, which stores them on the widget, then calls
  `SpreadsheetConnector.onStateChanged`.
- **`onStateChanged` re-lays out the sheet only when hidden indexes, `colW`,
  `rowH`, `rows`, `cols` or a split position changed.** Grouping properties
  weren't in that list, so a grouping-only change never rebuilt the controls.
  This is the main cause of the stale controls (fixed in the prototype).
- `SheetWidget.updateGrouping` fully rebuilds all markers each time. Positions
  are absolute (header size + padding + sum of column widths). Scrolling is
  done by setting a negative `marginLeft` on the group pane. For frozen
  columns, markers are cloned into `colGroupFreezePane`.
- A marker's length ends at the middle of the next column, where the +/-
  button sits. For a group ending at the last column, this ran past the sheet
  (fixed in the prototype).
- `updateColGrouping` skips rebuilding the level buttons when their count is
  unchanged.
- `GroupingWidget` flips its own +/- state on click before the server answers.

### User click flow

`GroupingWidget.onBrowserEvent` → `SpreadsheetWidget.setGroupingCollapsed` →
`SpreadsheetServerRpcImpl` → DOM event `groupingCollapsed` with
`[isCols, uniqueIndex, collapsed]` → `SpreadsheetEventListener` →
`SpreadsheetHandlerImpl.setGroupingCollapsed` → `Spreadsheet.setGroupingCollapsed`.

Level buttons send `levelHeaderClicked` with `[isCols, level]`, level 1-based.

## 7. Apache POI findings

POI version 5.5.1 (`poi.version` in the root `pom.xml`).

- **Private methods called by reflection** from `GroupingUtil`, all still
  present with the same signatures: `findStartOfColumnOutlineGroup(int)`,
  `findEndOfColumnOutlineGroup(int)`, `setGroupHidden(int,int,boolean)`,
  `setColumn(int,Integer,Integer,Boolean,Boolean)`, `findColInfoIdx(int,int)`,
  `isAdjacentBefore(CTCol,CTCol)`. `callSheetMethod` matches by name, swallows
  all exceptions and returns `null`, so a POI change turns into an NPE far from
  the cause.
- **Public API:** `groupColumn`, `ungroupColumn`, `setColumnGroupCollapsed`,
  `isColumnHidden`, `getColumnOutlineLevel`. `ColumnHelper.setColHidden(long, boolean)`
  is public and splits a `<col>` range; `getOrCreateColumn1Based` is
  protected.
- **`groupColumn` writes overlapping ranges** (`min=2 max=5`, `min=2 max=6`, …)
  once the columns are already split into single `<col>` elements, for
  example after a clear and regroup. Reproduced. The prototype raises the
  level per column instead.
- **`ungroupColumn` skips columns** once the range has been split. Reproduced.
- **Splitting appends `<col>` elements out of order.** `loadGrouping` assumes
  sorted order, so the client got a wrong `uniqueIndex` and clicks did nothing.
  The prototype sorts with `ColumnHelper.sortColumns` after each change.
- **Collapsing a group that ends at the last column** writes the `collapsed`
  marker on a `<col min="16385">`, past XFD. Both POI's
  `setColumnGroupCollapsed` and `GroupingUtil.collapseColumn` do this.
- **No outline-level limit:** nine nested `groupColumn` calls give level 9.
  Excel allows 7.
- **`XSSFSheet.write` removes an empty `<cols>`,** so code using
  `getColsArray(0)` throws `IndexOutOfBoundsException` on a sheet saved
  without column definitions.
- `setColumnHidden(i, false)` writes `hidden="0"`. Code that checks
  `isSetHidden()` then reads the column as hidden.

## 8. Excel behaviour

Checked 2026-10-01 with Microsoft Excel for Mac, through AppleScript (`show
detail` on the summary column, reading `hidden` back). The files were generated
with POI. Outer group B:G, inner group E:G, summary column H.

| Step | Hidden columns |
|---|---|
| Open a file with only the inner group collapsed | E:G (Excel keeps that state) |
| Collapse at H | B:G |
| Expand at H | none, so the inner group's state is lost |

Columns hidden by the app, group B:G:

| Step | Hidden columns |
|---|---|
| App hides C | C |
| Collapse the group | B:G |
| Expand the group | none, C is shown again |

Not checked: whether Excel draws a separate button for the inner group of a
shared-end pair, and what a mouse click does compared with `show detail`.

Google Sheets stores `collapsed` per group (`DimensionGroup.collapsed` in the
Sheets API), so nested groups ending on the same column work independently
there. That state can't be saved to XLSX.

How XLSX encodes outlines:

- Each column has `outlineLevel` and `hidden`.
- A collapsed group has all its columns hidden, and the column right after it
  has `collapsed="1"`. With `summaryRight="0"` (inverted), the flag is on the
  column before the group.
- Groups ending on the same column share that flag column.

## 9. Known bugs

Issues in flow-components:

- [#3912](https://github.com/vaadin/flow-components/issues/3912) (open, 2022):
  hidden columns in a child group cause rendering issues.
- [#10207](https://github.com/vaadin/flow-components/issues/10207) (open, Major):
  collapsing a row group while scrolled down leaves the viewport empty.
- [#10226](https://github.com/vaadin/flow-components/issues/10226) (open):
  after expanding a row group at the bottom, the collapse control is out of
  view.
- [#10164](https://github.com/vaadin/flow-components/issues/10164) (fixed by
  #10183 in 25.2.9): row group ending on the last row couldn't be collapsed.
- [#6295](https://github.com/vaadin/flow-components/issues/6295) (closed):
  grouping inside a `SplitLayout` hid rows.

Legacy `vaadin/spreadsheet` add-on (EOL): #249 (frozen pane + grouping), #341
(client exception on collapse), #398 (nested grouping fails on reopen).

Days to fix each bug, and which ones the customer reported, are in
[section 3](#bug-fixes-and-feature).

Bugs found during this research. "Reproduced" means seen in a test or the
browser; "code reading" means not yet reproduced.

| Bug | Where | Status |
|---|---|---|
| Grouping-only changes don't redraw the controls | `SpreadsheetConnector.onStateChanged` | Reproduced, fixed in prototype |
| Marker of a group ending at the last column runs past the sheet | `SheetWidget.updateGrouping` | Reproduced, fixed in prototype |
| Expanding a parent keeps child columns hidden (2 `@Disabled` tests in `GroupingTest`) | `GroupingUtil.expandColumn` | Reproduced, fixed in prototype |
| Grouping refresh resets `setMaxColumns` / `setMaxRows` | `SpreadsheetFactory.calculateSheetSizes` | Reproduced (rows still reset in prototype) |
| `hidden="0"` reads as collapsed | `GroupingUtil.isColumnGroupCollapsed`, `checkHidden` | Code reading |
| NPE in `expandColumn` when the column has no `<col>` | `GroupingUtil.expandColumn` | Code reading |
| `IndexOutOfBoundsException` on `getColsArray(0)` after saving a sheet with no columns | `loadGrouping`, `GroupingUtil` | Code reading (POI behaviour confirmed in bytecode) |
| Collapse marker written past XFD | `GroupingUtil.collapseColumn` | Reproduced in POI |
| Level buttons do a full component reload and write no `collapsed` flags | `Spreadsheet.levelHeaderClicked` | Code reading; replaced for columns in prototype |
| Level-button bar not rebuilt when the count is unchanged | `SheetWidget.updateColGrouping` | Code reading |
| Control flips before the server answers; stays wrong if the server does nothing | `GroupingWidget.onBrowserEvent` | Code reading |
| Group pane isn't clipped while scrolling, so markers slide over row headers and the corner | `.col-group-pane` styles, `SheetWidget` scroll margin | Code reading |
| Freeze-pane clone condition misses inverted groups starting right after the split | `SheetWidget.updateGrouping` | Code reading |
| Children of collapsed groups drawn as stubs (server filter only checks the last group, mixes 0- and 1-based indexes) | `loadGrouping`, `SheetWidget.updateGrouping` | Code reading |
| A local column resize can swallow the next server relayout | `SpreadsheetWidget` (`cancelNextSheetRelayout`) | Code reading |
| Autofit's extra width for filter buttons lost after a grouping refresh | `calculateSheetSizes` | Code reading |
| Groups and hidden columns past `cols` never sent to the client (`colW` has `cols` entries) | `calculateSheetSizes` | Code reading |
| Cells not refreshed after expanding when `expandColumn` returns -1 | `Spreadsheet.updateExpandedRegion` | Code reading |

## 10. Fixes that can land before the feature

These don't add public API, so they can go in as bug fixes and be backported to
25.2. They also lower the feature's risk, because they fix the shared client
code first. Suggested order:

1. **Redraw grouping on grouping-only changes.** Add the six grouping
   properties to the relayout check in `SpreadsheetConnector.onStateChanged`.
   8 lines, in the prototype. Fixes stale controls for all users, including
   workbook-loaded groups.
2. **Keep the last-column marker inside the sheet.** In
   `SheetWidget.updateGrouping`, don't add half of the next column's width
   when there is no next column. In the prototype.
3. **Expanding a parent keeps child groups collapsed.** Enables the two
   `@Disabled` tests in `GroupingTest`. In the prototype this comes with the
   new model; as a standalone fix it means correcting `GroupingUtil.expandColumn`.
4. **Keep `setMaxColumns` / `setMaxRows` across grouping refreshes.** Store the
   limit the app set, and have `calculateSheetSizes` respect it. The
   prototype's stopgap (restore the old `cols`) breaks when data grows; don't
   ship it.
5. **Small server fixes:** use `getHidden()` instead of `isSetHidden()`, null
   check in `expandColumn`, guard `getColsArray(0)`, don't write the collapse
   marker past XFD.
6. **Client fixes:** don't flip the control before the server answers (or
   always echo the state back), rebuild the level-button bar every time, clip
   the group pane while scrolling, fix the freeze-pane clone condition.
7. **Level buttons without a full reload** (columns), and with `collapsed`
   flags written.

1, 2 and 3 are verified in the prototype. The rest come from code reading and
need a reproduction first.

## 11. Refactors that can land before the feature

These keep behaviour the same, so each can be a small PR of its own. They're
not usually backported, but the bug fixes from
[section 10](#10-fixes-that-can-land-before-the-feature) will be written on top
of them, so port them together with the fixes to keep the backports
conflict-free. 25.2 and 24.10 use the same POI version (5.5.1), and their
`GroupingUtil` and `SpreadsheetFactory` differ from `main` by only a few lines.

Suggested order: 1 → 2 → 3 → 4 → 5, then the bug fixes on top.

1. **Delete the commented-out code in `GroupingUtil`.** About 87 comment lines,
   mostly old code kept "for reference" and `/** start */` / `/** end */`
   markers around copied POI code. Trivial, and it makes the diff of 3
   readable.
2. **Build test sheets in code.** `GroupingTest` depends on
   `Groupingtest.xlsx` and magic sheet and column indexes
   (`SHEET6_COLUMN_PARENT_GROUP = 2`). A small builder for grouped sheets
   (outline levels, hidden and collapsed columns, inverted outlines) lets every
   later fix and the feature write readable tests. Test-only, so it ports
   without risk.
3. **Remove the reflection.** `GroupingUtil` makes 12 reflective calls, all in
   the column code, to 6 private `XSSFSheet` methods
   ([section 7](#7-apache-poi-findings)); row grouping uses none. Copy those
   methods into `GroupingUtil`, working on `CTCols` directly, so the logic
   stays exactly the same. `GroupingUtil` already contains copied POI code
   (Apache 2.0), so this fits its pattern. This is not the prototype's
   `ColumnOutline`: that model changes behaviour (it fixes nested expand) and
   belongs with the feature or a bug fix. About 0.5–1 day.
4. **Split `loadGrouping` into pure functions.** It's 253 lines that read
   column groups, read row groups, and set element properties. Extract a column
   reader and a row reader that return `List<GroupingData>`, so the read path
   can be unit-tested without a client. The feature rewrites exactly this path
   (estimate item 3), and three known bugs live in it: the sorted-columns
   assumption, the filter for children of collapsed groups, and the mixed 0-
   and 1-based indexes.
5. **Extract the grouping refresh.** The end of `setGroupingCollapsed`
   (`calculateSheetSizes`, `loadGrouping`, styles, overlays,
   `updateMarkedCells`) becomes one private method. Tiny; the feature's public
   `refreshGrouping()` and the level-button fix reuse it.
6. **Optional, client: pull the marker layout math out of
   `SheetWidget.updateGrouping`.** The method is 153 lines handling columns,
   rows, inverted groups and the frozen-pane copy. With the position and length
   math in a helper without GWT dependencies, the last-column and frozen-pane
   fixes become unit-testable. Not checked: whether the client module's tests
   can run such a helper on a plain JVM.

Not pure refactors, so they belong with the fixes in section 10: keeping
`setMaxColumns` / `setMaxRows`, and level buttons without a full reload.

Effect on the estimate: refactors 3–5 move about 2–2.5 days out of estimate
items 3 and 6 into prep work, and separate PRs add about 0.5 day of review.
Net about +0.5 day overall; the feature itself drops to about 19 days once the
prep and fixes are done.

## 12. The prototype

Branch `proto/spreadsheet-column-grouping-api`, one commit on top of
`c1eb3a2194`.

| File | What |
|---|---|
| `vaadin-spreadsheet-flow/.../spreadsheet/ColumnGroup.java` | Public record. |
| `vaadin-spreadsheet-flow/.../spreadsheet/ColumnOutline.java` | Package-private outline model on public POI API: read groups, group, ungroup, clear, collapse, expand, level buttons, inverted outlines, last column, max level, sorting. |
| `vaadin-spreadsheet-flow/.../spreadsheet/Spreadsheet.java` | Public API, `ColumnGroupToggleEvent`, `refreshGrouping()`. Column clicks and level buttons go through `ColumnOutline`; rows unchanged. |
| `vaadin-spreadsheet-flow-client/.../client/SpreadsheetConnector.java` | Relayout on grouping properties. |
| `vaadin-spreadsheet-flow-client/.../client/SheetWidget.java` | Last-column marker clamp. |
| `vaadin-spreadsheet-flow/src/test/.../tests/ColumnGroupApiTest.java` | 11 unit tests: nested add, collapse/expand nesting, restore in any order, last column, clear, ungroup, max level, event, save/reload round trip, clear-regroup-restore, keep max columns. |
| `vaadin-spreadsheet-flow/src/test/.../tests/GroupingTest.java` | The two nested-column tests re-enabled. |
| `vaadin-spreadsheet-flow-integration-tests/.../tests/ColumnGroupingApiView.java` | Manual test view at `/spreadsheet-column-grouping-api`, with buttons for every API call, a freeze pane and autofit. |

How the model works:

- A group is a maximal run of columns with `outlineLevel >= L`, for each level
  L.
- Collapsed state is read from the `collapsed` flag on the column after the
  group (before it when inverted), or from "all columns hidden" for a group
  ending at XFD.
- After every change, each column's `hidden` is recomputed as "some group
  containing it is collapsed". This makes restoring nested states
  order-independent, but it's also why columns the app hid get shown again.
- Ranges are split per column with `ColumnHelper.setColHidden`, then sorted.

Verified:

- All spreadsheet unit tests pass (34 classes).
- In the browser:
  - runtime add;
  - nested collapse and expand by clicking;
  - save state, clear, regroup and restore;
  - last-column group;
  - autofit widths kept;
  - frozen-pane clone;
  - level buttons 1 and 2.

Not verified:

- No TestBench ITs were written, and the existing grouping ITs weren't run.
- Saved files weren't opened in Excel or LibreOffice (only POI-generated files
  were used in [section 8](#8-excel-behaviour)).
- Horizontal scrolling, `.xls`, and sheets with thousands of columns.

## 13. Prototype gaps

Not a full list of production work, but everything known to be wrong or
missing in the prototype:

- **Client and server read collapsed state differently.** `loadGrouping` still
  uses the legacy read (`isSetHidden()` on the first column), the model uses
  the flag column. With nested groups sharing an end column, the client reports
  the outer group as collapsed even when B:D are visible.
- **Nested groups sharing an end column:** toggling the inner one toggles both,
  and the event only reports the inner one.
- **Adding a group next to a collapsed one** merges them: the merged group
  reads as expanded, the old columns stay hidden, and a stale flag is left.
- **`refreshGrouping` restores the old column count,** which shrinks it when
  data grows. Rows aren't restored at all.
- **No compaction:** one `<col>` per grouped column, and `getGroups` is
  O(columns × `<col>` count), called several times per click.
- **Ungrouping inside a collapsed group expands it,** because markers of all
  overlapping groups are cleared.
- No update-range method.
- Level buttons and cascaded changes fire no event.
- `ColumnGroup` and the event have no sheet reference.
- `.xls`: reads return an empty list, writes throw.
- `getColsArray(0)` on a saved sheet with no columns isn't guarded.
- The legacy `GroupingUtil` column code is still there (unused for clicks).

## 14. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Client layout issues with scrolling and frozen panes (bug history: #10207, #10226, #3912, legacy #249) | Upper end of the range | Land the client fixes from section 10 first; use SDM for debugging; ITs for scroll and frozen combinations. |
| More POI surprises in `<cols>` handling | Model rework | Use only per-column operations and sort; unit-test against POI-generated and Excel-saved files. |
| Excel repair prompts on saved files | Customer files rejected | Round-trip test in Excel and LibreOffice (item 7). |
| Performance on wide sheets | Slow clicks | Compaction and benchmark (item 2c). |
| Behaviour change of `protected setGroupingCollapsed` / `levelHeaderClicked` | Breaks subclasses, like the customer's | Keep them working on top of the model; note it in the release notes. |
| `calculateSheetSizes` is used by many features | Regressions outside grouping | Regression tests around the max-rows/cols change. |
| Customer expects Google Sheets behaviour for shared-end groups | Disappointment | Stated as a limitation in the posted reply. |
| Customer needs it in 25.2 | Can't ship new API there | Bug fixes can be backported; the API needs the next minor. |

## 15. Open questions

- Rows: needed now, or columns only? (+6–8 days.)
- `.xls`: throw everywhere, or return empty/no-op everywhere?
- Groups sharing an end column: reject in `addColumnGroup`, or allow and toggle
  both (Excel)? `getColumnGroups()` must report them consistently either way.
- Update-range method: `updateColumnGroup(ColumnGroup, first, last)`, or
  remove + add that keeps the collapsed state?
- Do level buttons fire one event per changed group, or a separate event?
- Should `ColumnGroup` or the event carry the sheet (sheet switching)?
- Should `refreshGrouping()` be public, or only an internal step of the API
  methods? It's needed only for direct POI edits.
- Target version for the API.

## 16. Test plan

Unit tests (most exist in `ColumnGroupApiTest`):

- nesting, restore in any order, last column, max level, clear, ungroup;
- adjacent groups, shared end column, inverted outlines;
- columns hidden by the app inside a group;
- save and reload, including a sheet with no `<cols>`;
- `.xls` behaviour;
- a wide-sheet benchmark.

TestBench ITs:

- add at runtime; nested collapse and expand by clicking; restore;
- last-column group;
- frozen columns plus grouping;
- horizontal scroll (controls stay clipped and aligned);
- autofit kept after toggling;
- `setMaxColumns` / `setMaxRows` kept;
- level buttons;
- event `isFromClient()`.

Also run the existing `GroupingIT`, `UngroupingIT` and `ClosedGroupNumbersIT`,
and open saved files in Excel and LibreOffice.

## 17. Tooling and lessons

- **SuperDevMode** for the GWT client: the patch and steps are in
  `vaadin-spreadsheet-flow-client/README.md` on `main` (#10267). This branch is
  older than that PR, so get the patch from `main`:
  `git show origin/main:vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-client/patches/gwt-sdm-debugging.patch | git apply`.
  Recompile plus reload takes about 5 s; a Maven rebuild plus Jetty restart
  about 45 s.
- **Rebuilding the client without SDM** needs `mvn clean install` on the client
  and flow modules. A plain `package` can skip the GWT compile and keep the
  old bundle.
- **`clean` fails while Jetty runs,** because Jetty holds `target/`. Stop it
  with `mvn jetty:stop`, not by killing the Maven task.
- **`-q` hides Jetty startup errors:** the server answered 503 with no output.
  Run without `-q` when the server doesn't come up.
- **Unit tests calling the client event path** need a viewport first:
  `TestHelper.fireClientEvent(spreadsheet, "onSheetScroll", "[1, 1, 1, 1]")`,
  or `loadHyperLinks` gets column -1 and throws.
- **Testing Excel behaviour on a Mac:** generate files with POI and drive Excel
  through AppleScript (`show detail of entire column of range "H1"`, and read
  `hidden of entire column`). This reads Excel's real state without
  screenshots.
