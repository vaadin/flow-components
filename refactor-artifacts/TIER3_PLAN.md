# Tier 3 — GWT client replacement: detailed examination

> Standalone strategic document. Sits alongside `MODERNIZATION_PLAN.md`
> as the deep dive on the largest open item.
> **Status:** initial draft, not yet reviewed by a human operator.
> Authored 2026-06-22 by slice 116 of the spreadsheet refactor.

## 1. Why this document exists

The GWT-compiled `vaadin-spreadsheet-flow-client` module (~22.6K LOC,
105 files, compiled to a 1.3 MB JS bundle) is the largest open
modernization item. Earlier sections of `MODERNIZATION_PLAN.md`
treated Tier 3 as "6–12 weeks for a team of 2–3 people" — accurate
as an order-of-magnitude, but not enough to act on. This document
maps the actual subsystems, evaluates concrete replacement paths,
and recommends one with a phased roadmap and risk register.

It also resolves a real tension surfaced in the operator review:
the `code-modernization` skill says "old artifacts must render
identically," which reads as "you can never rewrite a renderer."
That phrasing has been sharpened (slice 116's commit) to
distinguish **content + styling-intent preservation**
(non-negotiable) from **render-pixel-identity** (not promised
across UI tech changes; mitigated by golden-screenshot diffs with
tolerance thresholds). Tier 3 honors the former, accepts diffs in
the latter.

## 2. Current state

### 2.1 What's there

The client module compiles 105 Java files to `spreadsheet-export.js`
(1.3 MB). On top of it sits a thin Lit wrapper at
`vaadin-spreadsheet.js` (19 KB):

```
+--------------------------------------------+
| <vaadin-spreadsheet> (LitElement, 19 KB)   |  <-- public API surface
|   - properties: dirty, cellStyleToCSSStyle,|
|     rowH, colW, sheetIndex, sheetNames,    |
|     ... ~30 reactive Lit properties        |
|   - methods: updateBottomRightCellValues,  |
|     cellsUpdated, showSelectedCell, ...    |
|     ~25 server→client JS-callable methods  |
|   - events dispatched: spreadsheet-event   |
|     with {type, data} payload              |
+--------------------------------------------+
              | delegates to
              v
+--------------------------------------------+
| this.api: SpreadsheetApi (GWT, 1.3 MB)     |  <-- under the seam
|   the actual rendering / interaction code  |
+--------------------------------------------+
```

**The seam already exists.** Every server↔client message goes
through the Lit wrapper's properties + methods + events. The GWT
blob is "what's currently under the wrapper" — replacing the blob
doesn't require touching the seam.

### 2.2 Subsystem breakdown (from slice 116's GWT survey)

13 major subsystems, totaling ~22.6 K LOC:

| # | Subsystem | LOC | Difficulty (1-5) | Notes |
|---|---|-----|---|---|
| A | Virtual scrolling + viewport mgmt | 1,800 | **4** | Frozen panes (4 quadrants), DOM recycling, scroll sync |
| B | Cell rendering + DOM recycling | 2,000 | 2 | Straightforward DOM + style application |
| C | Selection (single / range / multi) | 2,600 | 3 | State machine + 8-div outline + merged-cell expansion |
| D | Formula bar + editing | 1,300 | 3 | Parser + 16-color reference highlighting + inline edit |
| E | Merged-cell handling | 280 | 2 | Pure-algorithm lookup + selection expansion |
| F | Sheet tabs + navigation | 568 | 1 | Simple Lit element |
| G | Row/column grouping | 327 | 2 | Server manages visibility; UI is straightforward |
| H | Popup buttons / filter dropdowns | 507 | 2 | Native popover API or Popper.js |
| I | Copy/paste | 263 | 2 | Standard clipboard API + TSV |
| J | Comments + annotations | 320 | 2 | Hover popup + overlay positioning |
| K | Custom cell editors | 500 | 3 | Vaadin Flow component lifecycle integration |
| L | Sheet overlays (images / charts / components) | 158 | 2 | Absolute positioning + scroll offset |
| M | GWT connector + state delegation | 617 | **4** | Hardest — replace GWT state machine + RPC infra |
| | **Total** | **~22,600** | | |

**The two 4/5 subsystems (A virtual scrolling + M connector) carry
the highest risk** and dominate the timeline. The 2/5 subsystems
collectively are mechanical work — fast given the seam.

### 2.3 Excel-quirk emulation

The GWT widget has accumulated ~15 years of Excel-specific
rendering fixes. Slice 116's survey identified 10 hard-emulation
areas, with the heaviest being:

1. **Frozen panes**: 4 separately-scrolled quadrants whose borders
   align pixel-precisely on the split lines.
2. **Selection outline**: 8-div pseudo-element that draws the
   blue border + corner + drag handles, with merged-cell-aware
   geometry.
3. **Virtual scrolling with dynamic row heights**: hidden rows
   (height 0), variable height, recycling DOM only for
   fully-visible rows.
4. **Cell overflow detection**: measure-then-display-### logic
   that takes merged-cell width into account.
5. **Formula reference highlighting**: real-time as user types,
   16-color cycle for multiple references, colored borders around
   referenced cells.

These cost real time in a rewrite. They're also load-bearing for
user perception — getting any one of them wrong shows up
immediately in a usability test.

## 3. Constraints

### 3.1 Non-negotiables (the back-compat Level-1 contract)

Per the sharpened `code-modernization` skill:

- **Old `.xlsx` files must load.** Server-side POI handles this;
  Tier 3 doesn't touch it.
- **Data must be preserved bit-equivalently.** Server-side
  serialization stays; the wire format is pinned by
  `DtoWireFormatTest` (slice 106). Tier 3 doesn't touch this.
- **Styling intent must be honored.** Bold stays bold, red stays
  red, borders stay where defined. The new renderer must apply
  every CSS rule the server emits.
- **Forward-write compatibility.** The server writes files via
  POI; client doesn't write. Tier 3 doesn't touch this either.

These are all server-side concerns; **Tier 3 is constrained by
none of them directly.** What Tier 3 must honor is the consumer
side: every CSS rule the server emits must produce the same
*visual styling intent*.

### 3.2 Not-promised (Level-2 — acceptable diffs)

- Pixel-exact font rendering.
- 1px-vs-0.5px border thickness at certain `devicePixelRatio`.
- Selection-highlight emulation of exact Excel pixel colors.
- Scrollbar appearance (browser native vs custom).

Safety net: golden-screenshot diffing with tolerance thresholds
(e.g., per-pixel diff < 5%, SSIM > 0.95 — values to be tuned).

### 3.3 Feature parity (must be preserved)

Every feature the GWT client implements today must be available
in the replacement. Slice 116's subsystem map IS the feature list:
13 subsystems, each enumerated. **No feature drops without
explicit user-facing announcement and a major-version bump.**

### 3.4 Vaadin Flow integration (must be preserved)

The custom-editor and overlay subsystems integrate Vaadin Flow
components into spreadsheet cells. This is a unique value
proposition (most spreadsheet libraries don't support hosting
arbitrary framework components in cells). The new client must
preserve this.

### 3.5 Performance (must not regress)

Current GWT widget handles ~100K-cell sheets acceptably. New
client must hit at least the same baseline. Canvas-based
rendering would dramatically exceed; DOM-based rendering must
match.

## 4. Options analysis

### 4.1 Option A — In-house TS/Lit rewrite (full)

Build a Lit-based spreadsheet from scratch, reusing only the
existing Lit wrapper as the integration surface.

**Pros:**
- Full control over every Excel quirk.
- Native Vaadin Flow integration (the wrapper element is already
  Lit; the new under-the-hood code is Lit too).
- No third-party licensing.
- Type-safe TypeScript across the codebase.
- Aligned with Vaadin's other-component-rewrite precedent (Grid,
  ComboBox migrated GWT → Lit using the same pattern).
- Single dependency surface; no API mismatch risk.

**Cons:**
- Largest engineering investment: 3-6 months for a team of 2-3.
- Every Excel quirk must be reimplemented (the 15-year accumulated
  fixes).
- Risk concentrated in subsystems A (virtual scrolling) and M
  (connector) — both 4/5 difficulty.
- Performance work (DOM recycling) is on us.

**Cost:** ~12 person-months. ~3-4 months wall-clock with a team of 3.

**Risk:** Medium-High. Mitigated by phased rollout + golden
screenshot tests.

### 4.2 Option B — Wrap an OSS spreadsheet library

Pick an open-source spreadsheet library and adapt our wire
protocol to its API. The Vaadin Spreadsheet becomes a thin
adapter on top.

**OSS candidates (as of mid-2026 — verify before committing):**

| Library | License | Stars | Feature set | Vaadin-fit |
|---|---|---|---|---|
| **Univer** | Apache 2.0 | 13K+ | Full Excel-like; formulas; collab | Adapter cost: high (their data model differs) |
| **Handsontable** | MIT (community) + commercial Pro | 19K+ | Mature; commercial cells/types are paywalled | License risk — community edition restrictions on commercial use |
| **Luckysheet / Fortune-Sheet** | MIT | 18K+ (Luckysheet) | Full Excel-like; active fork (Fortune-Sheet) maintained | Adapter cost: medium |
| **x-spreadsheet** | MIT | 13K+ | Lightweight; basic features | Feature gap — would need extension for formulas etc. |
| **Glide Data Grid** | MIT | 6K+ | Canvas-based; very fast; less Excel-shaped | Performance excellent; styling model differs |
| **AG Grid** | MIT (community) + commercial Enterprise | 13K+ | Powerful grid; "spreadsheet" features in Enterprise | License: Enterprise paywalled; cell-editing & formulas in paid tier |
| **Jspreadsheet (jExcel)** | LGPL (Community) + commercial Pro | 7K+ | Lighter Excel-like; Pro has features locked | License: similar pattern to Handsontable |

**For each, the adapter work is**:
1. Convert our wire-JSON DTOs (CellData, MergedRegion, etc.) into
   the library's expected format. ~1 week per major DTO.
2. Map library events back to our DOM events on
   `<vaadin-spreadsheet>`. ~1 week.
3. Integrate Vaadin Flow custom-editor components into the
   library's cell-editor extension point — IF the library
   supports it. Most don't natively.
4. Bridge sheet-overlay rendering (images, charts, embedded
   components). Library-specific.
5. Translate server-side CSS rules into the library's styling
   API. Could be the biggest mismatch — our server emits per-cell-
   style CSS classes; libraries typically have their own
   cell-style model.

**Pros:**
- Massive head-start on Excel quirks.
- Community maintenance for the rendering engine.
- Faster initial milestone.

**Cons:**
- API mismatch is the silent cost — every "we want X, library
  does Y" is custom work.
- Custom-editor integration is the biggest gap; most libraries
  don't host arbitrary framework components in cells.
- Licensing complexity: most "free" libraries paywall the features
  we'd need (cell-types in Handsontable Pro, range-selection
  formulas in AG Grid Enterprise, etc.).
- Long-term lock-in to library's evolution direction.
- Vaadin's brand-consistency: an inconsistent look-and-feel vs
  other Vaadin Flow components.

**Cost:** ~6 person-months IF the library's editor / styling API
fits our needs. Up to ~12 if extensive adapter work is needed.

**Risk:** Medium for the rendering. **High for custom-editor
integration** — this is where the integration costs blow up.

### 4.3 Option C — Hybrid: in-house Lit shell + OSS rendering core

Build a Lit shell (overlay, custom-editor integration, formula bar,
sheet tabs, popup buttons) but use an OSS library purely for the
cell grid rendering + scrolling + selection.

The library handles subsystem A (virtual scrolling) and parts of
B (cell rendering) + C (selection). We handle subsystems D, E,
F, G, H, I, J, K, L, M.

**Pros:**
- Offload the hardest 4/5 subsystem (virtual scrolling).
- Keep control over custom-editor / overlay integration (the
  Vaadin-unique value).
- Smaller in-house surface.

**Cons:**
- Two-system integration is harder than one — the OSS library's
  grid coordinates must align with our overlay-positioning logic,
  selection-state, etc.
- Tightly coupled to one library; switching later is expensive.
- Performance debugging across the boundary is hard.

**Cost:** ~8 person-months. Could be best-case (5 months) if the
library fits or worst-case (15 months) if integration friction
blows up.

**Risk:** Medium. The hybrid surface is hard to reason about; many
production bugs originate at integration boundaries.

### 4.4 Option D — Canvas-based renderer (in-house, novel)

Render the cell grid to an HTML5 canvas (like Google Sheets) and
overlay DOM elements only for editors, popup buttons, custom
components.

**Pros:**
- 10-100x performance for large sheets (no per-cell DOM).
- No accumulated DOM-quirk fixes needed — canvas drawing is
  pixel-precise.
- Frozen panes and scrolling become trivial (just translate
  drawing offsets).
- Selection rendering becomes trivial (draw the outline once per
  frame, not 8 div elements).

**Cons:**
- Accessibility: canvas content is invisible to screen readers
  unless we emit a parallel ARIA shadow DOM. Major work.
- Custom-editor integration: DOM editors over a canvas grid is
  doable but the coordinate-sync work is non-trivial.
- Text-selection / copy/paste from cells: requires us to
  implement what the browser does for free in DOM-based grids.
- Internationalization (RTL, complex scripts): canvas text drawing
  is per-glyph, much harder than CSS.
- No precedent in the Vaadin Flow components — would be a unique
  approach.

**Cost:** ~8-15 person-months. High variance — canvas is faster to
implement basic rendering, slower to do accessibility right.

**Risk:** **High**. Accessibility alone could be a 2-3 month
project on its own. And a canvas-rendered spreadsheet inside a
Vaadin Flow ecosystem of DOM-rendered components is a
consistency anomaly.

### 4.5 Option E — j2cl / GWT 3 / Kotlin/JS

Stay close to the Java toolchain but exit GWT.

- **j2cl** (Google's GWT successor): compiles Java to JS, targets
  modern web platform. ALPHA / experimental. No clear Vaadin Flow
  precedent.
- **GWT 2.x → 3.x**: GWT 3 has been planned for years; current
  status is unclear. Not a credible bet for new work.
- **Kotlin/JS**: compiles Kotlin to JS, with multiplatform support.
  Type-safe, integrates with web platform. But the Java code
  doesn't transfer 1:1 — would still be a rewrite.

**Pros:**
- Reuses Java skill set on the team.
- Type safety end-to-end.

**Cons:**
- Toolchain maturity: j2cl is alpha, GWT 3 is vapor.
- Ecosystem: Kotlin/JS is fine but no precedent in flow-components.
- Doesn't actually save much over Lit/TS — we're still rewriting
  ~22K LOC.

**Cost:** Similar to Option A (~12 person-months) plus
toolchain-risk overhead.

**Risk:** High. Tool-chain risk dominates; production stability
question marks.

### 4.6 Option F — Defer / status-quo + maintenance mode

Keep the GWT client alive. Patch bugs as they appear, but don't
invest in modernization.

**Pros:**
- Zero engineering cost.
- Zero rendering regression risk.

**Cons:**
- GWT 2.10 is on borrowed time. Maintenance has slowed; security
  patches not guaranteed.
- Build infrastructure is the heaviest in the flow-components
  monorepo (custom Maven plugin, JSNI, deferred binding).
- Onboarding cost: new engineers must learn GWT to maintain.
- 1.3 MB JS bundle is significantly larger than modern alternatives.
- The Lit wrapper hides the GWT-ness from CONSUMERS, but every
  bug-fix is a Java-compiles-to-JS round-trip.

**Cost:** ~$0 initial; ~6 person-months/year ongoing maintenance.

**Risk:** Low short-term, **high long-term**. The GWT ecosystem
is in maintenance mode; this is technical debt accruing interest.

## 5. Recommendation

### 5.1 Path: **Option A — in-house TS/Lit rewrite, phased**

Reasons:

1. **Aligned with Vaadin Flow's component-rewrite precedent.**
   Grid, ComboBox, etc. were all migrated GWT → Lit/TS via the
   same pattern. The toolchain is mature (Vite + Lit + TypeScript
   is the flow-components standard). Re-using the established
   path keeps the Spreadsheet consistent with the rest of the
   ecosystem.

2. **Seam already exists.** The `<vaadin-spreadsheet>` Lit
   element is the integration surface; the GWT blob sits below
   it. The rewrite swaps what's below — no API-surface impact
   from the server's perspective.

3. **Custom-editor + Vaadin-component-in-cell integration is the
   unique value.** OSS libraries (Options B/C) all have gaps
   here; building this ourselves preserves the Vaadin-unique
   feature.

4. **Server-side stays unchanged.** Tier 1+2 work (slices 99-115)
   already left the server in a clean state. Tier 3 doesn't
   require any more server work — the wire format is pinned by
   `DtoWireFormatTest`.

5. **Long-term ownership.** Every other Vaadin Flow component is
   owned end-to-end. A Spreadsheet that depends on an external
   OSS library introduces a foreign maintenance contract.

6. **OSS-library options were considered and have specific
   blockers**: Handsontable Pro licensing, AG Grid Enterprise
   paywall, custom-editor integration gaps in lightweight
   libraries. None match Vaadin's commercial and feature needs
   without significant adapter work.

### 5.2 What we explicitly reject

- **Option B (full OSS wrap)**: the custom-editor integration gap
  is the killer. Vaadin's value is putting Vaadin components in
  cells; OSS spreadsheet libraries are designed to be standalone.
- **Option C (hybrid)**: two-system integration is harder than
  one; we'd save little vs. Option A and gain a long-term coupling
  liability.
- **Option D (canvas)**: accessibility cost is prohibitive; no
  precedent in the Vaadin ecosystem.
- **Option E (j2cl)**: toolchain risk; we already have a mature
  Lit/TS path used by every other Vaadin Flow component.
- **Option F (defer)**: GWT 2.10's maintenance window is
  shrinking; the debt-cost is real.

### 5.3 Borrow ideas from the considered alternatives

Where Option A's rewrite can reuse OSS work without taking on
their full integration:

- **Hyperformula** (Apache 2.0): a standalone formula engine.
  Server-side POI handles formula evaluation today, so we don't
  need a client-side engine. Skip.
- **Glide Data Grid's row-virtualization approach**: study their
  open-source virtualization for ideas; don't import the library.
- **Native browser virtualization** (`content-visibility: auto`,
  CSS containment): can dramatically reduce virtual-scrolling
  complexity. Use.
- **Lit Virtualizer** (`@lit-labs/virtualizer`): an official Lit
  virtual-scrolling primitive. Use for subsystem A's
  vertical-only virtualization; bidirectional virtualization
  may still need custom code.

These reduce the in-house surface by ~20-30% of subsystem A's
LOC without taking on full third-party dependencies.

## 6. Phased roadmap

### Phase 3.0 — Pre-work (~2-3 weeks)

**Goal: safety net before any rewriting.**

1. **Golden-screenshot corpus**: 30-50 representative `.xlsx`
   files exercising every visible feature (merged cells, frozen
   panes, conditional formatting, custom editors, images,
   charts). Capture pixel-accurate screenshots of the CURRENT
   GWT client rendering at multiple devicePixelRatios. These
   become the visual regression test.

2. **Pixelmatch / Resemble.js threshold tuning**: configure the
   diff tool's tolerance (per-pixel-diff%, SSIM). The thresholds
   must allow font-rendering differences without allowing
   styling-intent regressions.

3. **Feature inventory**: enumerate every Excel feature the GWT
   client supports, with one example `.xlsx` file per feature.
   This is the parity checklist.

4. **Browser-coverage matrix**: which browsers + versions does the
   spreadsheet officially support? Each must pass the new
   client's tests.

### Phase 3.1 — Foundation (~4 weeks)

**Goal: build the inert skeleton.**

1. Set up `vaadin-spreadsheet-flow-client-lit` module (new
   sibling to the existing GWT module). Vite + TS + Lit, copying
   the conventions from `vaadin-grid-flow`.

2. Port pure-algorithm subsystems first — no DOM, easy to test:
   - Subsystem E (merged-cell handling, 280 LOC) → pure TS module.
   - Subsystem D's `Parser.java` (185 LOC formula reference
     extraction) → pure TS module.
   - SequenceIncrement / SequenceValueExtractor utility code.

3. Wire-protocol adapter layer. The new client's TS entry point
   has the same surface as the existing Lit wrapper's `api`. The
   wrapper switches under a feature flag.

4. Stand up the test harness: every subsystem has its own
   `.test.ts` file (Vitest), plus an integration suite that runs
   the new client against the existing IT suite.

**Milestone**: the new module builds, basic harness runs, no UI yet.

### Phase 3.2 — Cell grid + scrolling (~6 weeks)

**Goal: the heaviest subsystems work standalone.**

1. Subsystem A (virtual scrolling): start with `@lit-labs/virtualizer`
   for vertical; custom code for horizontal + frozen-pane
   coordination. Heavy test investment here — golden-screenshot
   diffs at every viewport boundary.

2. Subsystem B (cell rendering): Lit element per visible cell
   (or per-row, depending on perf measurements). Apply server-
   emitted CSS classes; text-overflow → ### display.

3. Subsystem C (selection): 8-div outline; range-expansion logic
   from subsystem E.

**Milestone**: the new client can render a static spreadsheet
correctly. No editing yet. Performance baseline matches GWT.

### Phase 3.3 — Editing + formula bar (~4 weeks)

1. Subsystem D (formula bar): inline + full editing, reference
   highlighting with the 16-color cycle, parser integration.

2. Cell-input flow: enter / tab / esc / arrow-key behavior must
   match the GWT widget byte-for-byte.

**Milestone**: a user can type, edit, navigate. Feature parity
on basic-edit scenarios.

### Phase 3.4 — Vaadin integration (~4 weeks)

1. Subsystem K (custom cell editors): Vaadin Flow component
   instances mounted into cells. This is the highest-risk
   integration work — preserve the lifecycle behavior pinned in
   slices 60-62.

2. Subsystem L (overlays — images, charts, embedded components):
   absolute-positioned shells inside the new scrolling viewport.

3. Subsystem M (connector / state delegation): translate the
   existing Lit wrapper's properties into the new module's state.

**Milestone**: custom editors and overlays work. The new client is
feature-complete enough to put behind a feature flag for
real users.

### Phase 3.5 — Polish + parity (~3 weeks)

1. Remaining subsystems F, G, H, I, J (tabs, grouping, popup,
   copy/paste, comments) — straightforward Lit work.

2. Browser-matrix testing.

3. Golden-screenshot diff thresholds tuned; any unexpected diffs
   investigated and resolved (or accepted with documentation).

4. Performance profiling vs GWT baseline. Optimize until match.

### Phase 3.6 — Rollout (~3 weeks)

1. Feature flag enables the new client for opt-in users (default
   off).

2. Beta release; user feedback over 2-4 weeks.

3. Flag flip: new client default; GWT available as fallback flag
   for one major version.

4. Next major version: drop GWT module entirely; archive its code.

### Phase 3.7 — GWT elimination (concrete steps)

See `refactor-artifacts/GWT_ELIMINATION_ROADMAP.md` (added in slice
127) for the inventory + ordered removal steps. Slice 127 verified
the surface is contained (zero Java cross-imports server→client;
JS coupling is a single `import` line in vaadin-spreadsheet.js;
the 1.2 MB compiled blob is the only build artifact). Once Phase
3.6 ships and verifies in production, Phase 3.7 is a multi-commit
cleanup pass: delete the GWT module + the blob, remove the
maven dependency, unwind the gwt profile + version property,
delete the spreadsheetWidget escape hatch from the TS skeleton.

### Total wall-clock estimate

~5-6 months for a team of 2-3 engineers. Per-phase has buffer.

### Critical path

Phase 3.2 (virtual scrolling) is the critical path. If it ships
on time, the rest follows. If it slips, the whole plan slips.

## 7. Risk register

| # | Risk | Probability | Impact | Mitigation |
|---|------|-------------|--------|------------|
| 1 | Subsystem A (virtual scrolling) takes longer than estimated | High | High | Lit Virtualizer for vertical; budget +2 weeks; consider canvas for the grid if DOM hits a perf wall |
| 2 | Custom-editor integration silently regresses (subsystem K) | Medium | High | Slice 60-62 characterization tests are the spec; expand to integration tests against real Vaadin components |
| 3 | Excel-quirk emulation gaps surface only in user feedback | High | Medium | Golden-screenshot corpus is the safety net; feature flag allows graceful rollback |
| 4 | Pixel-rendering changes are user-visible | High | Low (per the sharpened back-compat rule) | Document in release notes; tolerance thresholds + side-by-side migration |
| 5 | New client bundle is larger than GWT's 1.3 MB | Low | Low | Lit + TS typically produces smaller bundles; verify early in Phase 3.1 |
| 6 | Vaadin Flow's lifecycle changes mid-project | Low | High | Track Flow's roadmap; coordinate with upstream Flow team if needed |
| 7 | Wire-format drift between server + new client | Low | High | `DtoWireFormatTest` + the new client's adapter layer enforce identity |
| 8 | Team unavailability mid-project | Medium | High | Document every subsystem's design BEFORE coding it; reduce bus-factor |
| 9 | OSS library we DIDN'T choose later proves superior | Low | Medium | Reevaluate at Phase 3.5; rollback to GWT is still possible until Phase 3.6 |
| 10 | Performance regression on large sheets | Medium | High | Continuous perf benchmarks vs GWT; budget for the optimization tail |

## 8. Open questions for the operator

Before Phase 3.0 starts, decisions needed:

1. **Team allocation**: 2-3 engineers full-time, OR slower with
   partial allocation. The roadmap assumes ~6 months at 2-3 FTE.

2. **Beta timing**: how long should the new client live behind a
   feature flag before becoming default? 2 weeks of internal
   dogfooding + 4 weeks of opt-in beta is a starting suggestion.

3. **Major-version bump for GWT removal**: dropping the GWT
   module is a breaking change for any consumer that depends on
   the client-module artifact directly (unlikely but possible).
   Coordinate with the Vaadin Flow release schedule.

4. **Browser-coverage cut**: GWT's compile output today supports
   browsers Vaadin Flow no longer officially supports. The
   rewrite is an opportunity to drop them. Decide which
   browsers + minimum versions.

5. **Golden-screenshot acceptable diff thresholds**: these need
   real numbers (per-pixel diff %, SSIM threshold). Tune in
   Phase 3.0.

6. **Should the new client support a non-Vaadin embedding**?
   The current client is tightly coupled to Vaadin Flow's
   component model. The rewrite could optionally expose a
   "standalone" mode where the spreadsheet runs without Flow
   underneath. Out of scope by default; flag-able for
   discussion.

## 9. Acceptance criteria

The new client ships when:

- Every Excel feature in the parity checklist (Phase 3.0 output)
  has at least one passing test in the new module.
- Golden-screenshot diffs are within the agreed tolerance for
  every fixture file.
- Browser-matrix tests pass on every supported (browser × OS) cell.
- The 1050+ unit tests on the SERVER side still pass (no
  server-side changes were needed; this is the smoke test).
- A beta user can open a representative production spreadsheet
  and complete their normal workflow without regressions.
- The bundle size is at most the same as GWT's (~1.3 MB).

When all of those hold, the feature flag flips to default-on.
GWT module deprecation follows in the next major version.

## 10. References

- `MODERNIZATION_PLAN.md` — sibling document; Tier 3 was originally
  framed there in 1 paragraph; this document is the deep version.
- `code-modernization` skill — sharpened back-compat rule
  (slice 116) is the conceptual basis.
- Slice 116 GWT subsystem map — the per-file inventory this plan
  builds on.
- `DtoWireFormatTest` — the wire-format snapshot the new client
  must consume identically.
- Upstream Vaadin Flow component rewrites (Grid, ComboBox,
  TextField) — the in-ecosystem precedent for the chosen approach.
