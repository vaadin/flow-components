# Tier 3 / Phase 3.0 — safety net before any rewriting

> Drafted 2026-06-22 by slice 117.
> Phase 3.0 is the pre-work for the Tier 3 GWT-client rewrite — see
> [`../TIER3_PLAN.md`](../TIER3_PLAN.md) for the full project brief.
> **Status:** scaffolding complete; the team-driven capture work
> (goldens, fixtures, threshold tuning) is the next step.

## Goal of Phase 3.0

Build the **visual safety net** before any code is rewritten. Once
in place, the Phase 3.2+ team can iterate on a new TS/Lit client
and have an automated "does it still look right?" check per fixture.

## The three deliverables

### [`FEATURE_INVENTORY.md`](FEATURE_INVENTORY.md)

The parity bar. Every Excel feature the GWT client implements,
organized by the 13 subsystems from slice 116's GWT survey.

Each feature has:
- The Phase 3 spec(s) covering its server side.
- The existing IT class(es) that exercise it.
- Example `.xlsx` fixtures.
- A concrete acceptance criterion.

**The new client must support every entry.**

### [`FIXTURE_INVENTORY.md`](FIXTURE_INVENTORY.md)

The 162 existing `.xlsx` fixtures categorized into 16 themes.
Identifies 6 gaps (custom-editor fixtures, sheet-visibility,
performance baseline, etc.) that Phase 3.0 should fill.

Includes a recommended **capture order** for the goldens — top 10
fixtures cover all 13 subsystems at least once, so the early
captures get the most parity-bar leverage.

### [`INFRA_PROPOSAL.md`](INFRA_PROPOSAL.md)

The screenshot-diff tooling proposal:

- **Tool**: Vaadin TestBench's `compareScreen` API (already on
  the IT classpath; lowest-friction).
- **Storage**: `src/test/resources/golden-screenshots/<fixture>/<viewport>.png`,
  committed via Git LFS.
- **Browsers**: Chromium + Firefox (headless), `dpr=1` initially.
- **Tolerance**: 0.05 per-pixel diff default, tunable per fixture.
- **CI**: a new `visual-regression` Maven profile + PR-blocking
  CI job.

Considered + rejected alternatives: Playwright (too disruptive
in this phase; available as a fallback), odiff / pixelmatch /
resemble.js (integration cost exceeds benefit).

## What's NOT in Phase 3.0

The team-driven work this slice intentionally does not do:

- **Capture the actual goldens.** Requires the IT server running
  + a stable CI worker baseline. The Maven profile + harness is
  ready; running it is the next step.
- **Tune the tolerance.** Needs the baseline captures first.
- **Add the 6 new fixtures.** Author them after the inventory is
  reviewed; their shape may depend on operator input on
  representative scenarios.
- **Build the new TS/Lit client.** That's Phases 3.1-3.5.

## How to start the team-driven phase 3.0 work

1. **Operator review + sign-off on the three docs.** Confirm
   feature inventory is complete, fixture categorization is
   right, infra proposal is acceptable.
2. **Decide the 6 open questions in INFRA_PROPOSAL § 8.**
   Specifically: TestBench-vs-Playwright (recommended TestBench),
   Git LFS approval, browser matrix cut, tolerance default, CI
   budget, owner for golden regen.
3. **Capture the top-10 goldens** (per FIXTURE_INVENTORY § "Recommended
   Phase 3.0 capture order"). This exercises the harness +
   surfaces tooling bugs early.
4. **Tune the tolerance** (per INFRA_PROPOSAL § 4.3).
5. **Capture remaining 152 fixtures**.
6. **Author the 6 gap fixtures** (per FIXTURE_INVENTORY § G.1-G.4).
7. **Capture goldens for those too**.
8. **Verify Phase 3.0 acceptance criteria** (per INFRA_PROPOSAL § 7).

Total team-driven effort: ~2-3 weeks per the TIER3_PLAN.md estimate.

## After Phase 3.0

Phase 3.1 (foundation) starts — see TIER3_PLAN.md § 6 for the
six-phase roadmap.
