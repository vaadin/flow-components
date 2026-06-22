# Tier 3 / Phase 3.0 — Golden-screenshot capture workflow

> Drafted 2026-06-22 by slice 122. Operational runbook for
> bootstrapping the first set of golden-screenshot baselines used by
> Phase 3.2+ to verify the new TS/Lit client renders fixtures
> visually equivalent to the GWT client.
> **Status:** 9 priority baselines captured + diff IT live as of
> slice 123 (2026-06-22). Bulk capture of the remaining ~150
> fixtures deferred to a later slice.

## Why this exists

`INFRA_PROPOSAL.md` § 1.1 locked in **TestBench `compareScreen`** as
the diff tool and `src/reference-screenshots/` as the storage path.
That answers *what* infrastructure to use but not *how* the first
set of baselines gets written — TestBench by default does NOT
auto-generate references; it expects them to already exist and
fails when they don't. This document is the bootstrap procedure.

## The capture vs. diff split

Phase 3.0 ships **two** ITs that share the same fixture list:

| IT class | What it does | When it runs |
| --- | --- | --- |
| `Phase30BaselineCaptureIT` | Loads each fixture, screenshots the full viewport, writes the PNG into `src/reference-screenshots/<slug>/1280x720-dpr1-chrome.png`. Does NOT call `compareScreen`. | One-off, manually triggered via the `-Dphase3.baseline.capture=true` system property. Skipped by default. |
| `Phase30BaselineDiffIT` | Loads each fixture, reads the corresponding baseline PNG via `ImageIO`, calls `getCommandExecutor().compareScreen(BufferedImage, errorName)`. Fails the test with a fixture list if any diff exceeds the 0.01 per-pixel tolerance. | Every CI run by default. |

Slice 122 shipped `Phase30BaselineCaptureIT` + this workflow.
Slice 123 shipped `Phase30BaselineDiffIT`, the
`pom.xml` testbench parameter config (tolerance / reference dir /
error dir / retry), and the first 9 baselines.

## How to run the capture

The IT is gated behind a system property so it never accidentally
overwrites committed baselines during normal `mvn verify` runs.

```sh
# From repo root:
mvn -am -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests \
    -Dit.test='Phase30BaselineCaptureIT' \
    -Dphase3.baseline.capture=true \
    verify
```

The IT loads each of the 9 priority fixtures in turn (the
inventory's top-10 minus 3 that aren't reachable via the demo
combo, plus 2 substitutes — see "Fixture inventory caveat" below),
waits 2s for the GWT client to settle, then writes a PNG to:

```
vaadin-spreadsheet-flow-integration-tests/src/reference-screenshots/<fixture-slug>/1280x720-dpr1-chrome.png
```

where `<fixture-slug>` is the filename lowercased, with the
extension dropped and non-alphanumerics collapsed to `-` (e.g.
`Simple Invoice.xlsx` → `simple-invoice`,
`100_000_rows.xlsx` → `100-000-rows`).

A successful run prints one `[baseline] wrote ...` line per fixture
to stdout. Observed wall-clock on the slice 123 run (macOS,
Chromium): ~38s total for the 9-fixture set. Expect proportional
time for larger sets.

## Fixture inventory caveat

`FIXTURE_INVENTORY.md` § "Recommended Phase 3.0 capture order"
names 10 priority fixtures. Three of them
(`Simple Invoice.xlsx`, `Grouping.xlsx`, `Formulas.xlsx`) live
under `src/main/resources/testsheets/` (a directory created by the
original IT init in vaadin/spreadsheet#770) which `SpreadsheetPage`
does NOT expose — its combo loads only from
`src/main/resources/test_sheets/` via
`ClassLoader.getResource("test_sheets/")` (see
`SpreadsheetPage.java:71-82`).

For slice 123, those 3 were resolved as:

- `Formulas.xlsx` → substituted with `FormulaValues.xlsx` from `test_sheets/`.
- `Grouping.xlsx` → substituted with `Groupingtest.xlsx` from `test_sheets/`.
- `Simple Invoice.xlsx` → dropped; no clean substitute in `test_sheets/`.

The captured set is therefore 9, not 10. To restore `Simple Invoice.xlsx`
or any other `testsheets/`-only fixture as a baseline, either:

- Add a second combo to `SpreadsheetPage` that exposes
  `testsheets/`; or
- Add a separate IT capture path that loads the fixture
  programmatically via `Spreadsheet.read(InputStream)` (skipping
  the demo combo entirely).

Either way is its own slice. `FIXTURE_INVENTORY.md` should also be
updated to flag the `testsheets/` vs `test_sheets/` split.

## How to regenerate a single fixture's baseline

If a baseline needs refreshing (e.g. a deliberate intentional
visual change ships):

1. Edit `Phase30BaselineCaptureIT.TOP_PRIORITY_FIXTURES` (or copy
   the class to a new one) to list only the fixtures you need.
2. Run the command above.
3. `git diff` the new PNG; visually confirm in the GitHub PR review
   that the changed pixels match the intentional behavior change.
4. Commit + push.

Per `INFRA_PROPOSAL.md` § 1.1: PR author proposes regeneration,
code reviewer counter-signs.

## Scaling to the remaining 152 fixtures

The top-10 list is just the first batch. The full inventory has 162
fixtures (per `FIXTURE_INVENTORY.md`). When ready to capture the
rest:

1. Generalize `TOP_PRIORITY_FIXTURES` to read the full
   `FIXTURE_INVENTORY.md` machine-readable section (or hard-code a
   second list — `RemainingFixtures`).
2. Run the IT against that list. Allow ~80 minutes wall-clock
   (162 × 30s).
3. Review each new baseline PNG before committing — a 162-PNG diff
   is too noisy to eyeball; use a per-fixture commit and per-fixture
   PR if any look wrong.

## Cross-platform / multi-browser strategy

Phase 3.0 ships **Chromium only** for baseline capture. Firefox
parity is deferred (per `INFRA_PROPOSAL.md` § 1.1 — Phase 3.0
ships Chromium+Firefox, but Firefox-only diff verification is fine;
each baseline is browser-stamped via filename, so the diff IT will
look up the right file per browser).

The filename convention reserves space for cross-axis variants:

```
<fixture-slug>/<viewport>-<dpr>-<browser>.png

simple-invoice/1280x720-dpr1-chrome.png       <-- baseline today
simple-invoice/1280x720-dpr1-firefox.png      <-- Firefox add-on, Phase 3.0+
simple-invoice/1280x720-dpr2-chrome.png       <-- retina add-on, Phase 3.6+
```

## Known limitations / open questions

- **Font hinting**: the GWT client's rendering depends on the host
  OS's font rasterizer. Baselines captured on macOS may diff against
  Linux CI. Mitigation: capture on the SAME OS that runs the diff IT
  in CI. (Open: which OS?)

- **Scrollbar visibility**: macOS hides scrollbars by default; Linux
  shows them. Both are part of the baseline. Mitigation: force one
  via CSS / browser flags at capture time. (Deferred — first
  baseline pass will reveal whether this matters.)

- **Animation jitter**: the GWT client may have transient transition
  states (hover effects, popover opens). The 2s settle delay catches
  most. Per-fixture longer waits can be wired via a config map.

## Provenance

The capture infrastructure was bootstrapped by slice 122 (Phase 3.0
execution). The original plan was to capture via Playwright MCP
against a running Jetty; the user's "Playwright is enough" guidance
unblocked the choice between manual capture vs. requiring human
operators. After investigating, the TestBench-IT path was selected
because:

1. It uses the same toolchain as the future diff IT — no
   inconsistency between bootstrap captures and runtime diffs.
2. It runs deterministically in CI — Playwright MCP requires a
   logged-in Claude session.
3. The existing `AbstractSpreadsheetIT.loadFile()` helper already
   knows how to drive the demo page's fixture combo.

Playwright MCP remains useful for ad-hoc inspection (server logs,
DOM probing, one-off screenshots) but is not the production capture
pipeline.
