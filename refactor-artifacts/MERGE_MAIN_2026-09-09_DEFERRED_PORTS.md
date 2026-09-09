# Merge of `main` into `proto-spreadsheet-refactor` — deferred ports

**Date:** 2026-09-09. Merge base was `bccb70fdef` (2026-06-03); `main` had moved
330 commits ahead, 36 of them touching `vaadin-spreadsheet-flow-parent/`.

This file records every change from `main` that the merge **did not carry into
the branch**, and why. Nothing here is lost accidentally — each item is a
deliberate decision with a follow-up.

## How the merge resolved

44 conflicts:

| Class | Count | Resolution |
| --- | --- | --- |
| GWT client files main modified, branch deleted | 22 | Stay deleted (module is gone) |
| `shared/SpreadsheetState.java` | 1 | Stay deleted — main's change was javadoc-only, and it is referenced only from the deleted GWT module |
| main added `@since` above `@SuppressWarnings("serial")` | 10 | Kept main's `@since`; kept the branch's removal of the annotation (slice 101) |
| main added `@since` to a javadoc the branch rewrote | 5 | Kept both — branch prose plus main's `@since` |
| branch extracted code, main only fixed comment typos | 3 | Kept the extraction; carried the typo fixes into the new homes (`SpreadsheetFactory`, `CellBorderStyleBuilder`) |
| `vaadin-spreadsheet.js` | 4 hunks | See below |
| `Spreadsheet.java` | 13 hunks | See below — the custom-editor deferral |

Also handled, and **not** signalled by any conflict: `main` moved the frontend
directory from `META-INF/resources/frontend/` to `META-INF/frontend/`
(#9613). Git relocated the two wrapper files but left the branch's whole
`lit/` tree at the old path, where the wrapper's `./lit/Spreadsheet.js` import
would not resolve. The tree was moved and all 18 wtr test imports updated.

## Deferred — must be ported

### 1. Custom-editor reuse and callback scoping (the big one)

**Main commits:** `f83fdf7cfb` (#9814, 532 lines), `0becc08f5c` (#9493, 381
lines), `9d32e25d53` (#9825, cleanups).

**What main does:** caches the editor instance per cell key (`cellKeyToEditor`)
so re-renders that keep the same cells visible — scroll, selection, resize —
reuse the editor instead of asking the factory for a new one, which would
discard uncommitted editor state (e.g. a half-typed ComboBox value). Tracks
`lastEditorCallbackKey` / `lastEditorCallbackComponent` so
`onCustomEditorDisplayed` fires only on a real selection change. Adds
`loadCustomComponents(boolean recreateEditors)`,
`updateMarkedCellsRecreatingEditors()`, `resetEditorCallbackTracker()`,
`setCellKeyToEditorMap()`.

**Why deferred:** the branch extracted this exact code into
`CustomComponentLoader`, which has none of these features. Porting is a
feature-integration slice, not a merge resolution — and it cannot be validated
right now because the ITs that prove it (`CustomEditorReloadIT`,
`CustomEditorSelectionIT`) are among the ~301 currently broken.

**What was taken instead:** the branch's version of all 13 hunks. Main's call
sites that landed cleanly outside the conflicts were rewritten to the branch's
idiom: `loadCustomComponents(true)` → `loadCustomComponents()`,
`updateMarkedCellsRecreatingEditors()` → `updateMarkedCells()`,
`setCellKeyToEditorMap(null)` → `setCellKeysToEditorIdMap(null)`, and
`loadCustomEditorOnSelectedCell()` restored to the branch's body.

**API impact: none.** Everything main added here is private/package-private.
`public void reloadVisibleCellContents()` predates main's work and is present
on both sides. `SpreadsheetComponentFactory`'s javadoc (main's only change to
that public interface) still resolves.

**Behaviour impact:** editor state is not preserved across re-renders, and
`onCustomEditorDisplayed` may fire on re-renders that keep the same selection.
Port into `CustomComponentLoader` once the IT suite can validate it.

### 2. Wrapper: generic property dispatch

**Main commit:** `4916f31fe4` (#9451) — replaced the 116-line `if/else` property
chain with a generic `set<Name>` convention (12 lines).

**Deferred because:** the branch's explicit chain is the known-working mapping
against the Lit connector, whose method set differs from the GWT client's. The
generic convention requires every property to have an exactly-matching
`setXxx`. This is a refactor, not a fix — safe to defer, worth doing later.

### 3. Wrapper: `performUpdate()` before connector calls

**Main commit:** `04e1758d1c` (#9381) — flushes pending Lit updates before each
call into the client, so `selectCellRange` and friends do not run against stale
layout.

**Deferred because:** untested against the Lit path. **Worth trying early** —
the branch's largest open problem is render timing, and this is exactly that
class of fix.

### 4. GWT-client fixes with no Lit equivalent yet

These lived only in the deleted module. Each needs a Lit-side equivalent:

- `4ae47f87d0` (#9303) — overlays as native popover, per instance.
- `2ee6739fd0` (#9295) — `!important` on hidden-column style.
- `04e1758d1c` (#9381) — the GWT half of the layout-timing fix.

### 5. Overlay stylesheet

`vaadin-spreadsheet-styles.js` kept the branch's empty `spreadsheetOverlayStyles`
export (slice 176). Main's GWT-era `#spreadsheet-overlays .v-tooltip` rules
target markup the Lit DOM no longer emits. Revisit when the Lit path grows a
context menu — currently `showActions()` is a no-op.

## Carried in, worth knowing

- Overlay container is now **per-instance and slotted** into the element rather
  than appended to `document.body` (#9303 / #9713). The branch had regressed
  this; the merge restored main's version, which the surrounding already-merged
  code (`this._overlays.getRootNode()`) requires.
- Version is now `25.4-SNAPSHOT`.
- `main` added a new IT — `SpreadsheetInShadowRootIT` — that this branch has
  never run.

## Verified after the merge

| Tier | Result |
| --- | --- |
| Java unit | 1067 pass / 0 fail / 4 skip (was 1058; main added 9) |
| Frontend wtr | 497 pass / 0 fail, 18 files |
| IT | re-baseline still owed — the pre-merge figure was 301 of 377 broken |
