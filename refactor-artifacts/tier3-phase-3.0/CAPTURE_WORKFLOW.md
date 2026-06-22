# Tier 3 / Phase 3.0 — Golden-screenshot capture workflow

> Drafted 2026-06-22 by slice 122. Operational runbook for
> bootstrapping the first set of golden-screenshot baselines used by
> Phase 3.2+ to verify the new TS/Lit client renders fixtures
> visually equivalent to the GWT client.
> **Status:** infra ready; bulk-run deferred to a follow-up slice.

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
| `Phase30BaselineDiffIT` (deferred) | Loads each fixture, calls `compareScreen(referenceId)` against the baselines this captured. | Every CI run after baselines are committed. |

This slice (122) ships `Phase30BaselineCaptureIT` + this workflow.
The diff IT lands in a follow-up.

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

The IT loads each of the 10 top-priority fixtures in turn (see
`FIXTURE_INVENTORY.md` § "Recommended Phase 3.0 capture order"),
waits 2s for the GWT client to settle, then writes a PNG to:

```
vaadin-spreadsheet-flow-integration-tests/src/reference-screenshots/<fixture-slug>/1280x720-dpr1-chrome.png
```

where `<fixture-slug>` is the filename lowercased, with the
extension dropped and non-alphanumerics collapsed to `-` (e.g.
`Simple Invoice.xlsx` → `simple-invoice`,
`100_000_rows.xlsx` → `100-000-rows`).

A successful run prints one `[baseline] wrote ...` line per fixture
to stdout. Expect ~30s/fixture with the 2s settle + Maven IT
overhead, so ~5 min for the top-10 set.

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
