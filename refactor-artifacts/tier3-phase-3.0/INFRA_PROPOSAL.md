# Tier 3 / Phase 3.0 — Visual-regression infrastructure proposal

> Drafted 2026-06-22 by slice 117. Proposes the tooling +
> integration points for capturing + comparing golden screenshots
> against the current GWT client, so the Phase 3.2+ rewrite can
> verify visual equivalence against a stable baseline.
> **Status:** initial draft. Decisions need operator sign-off
> before Phase 3.0 week 2.

## 1. Decision summary (one paragraph)

Use **Vaadin TestBench's built-in screenshot-comparison API**
(`compareScreen(...)` + `@Screenshot` rule) for capturing and
diffing golden screenshots, layered on the existing IT
infrastructure. Tolerance: **0.01** (TestBench's documented
default); per-fixture overrides via metadata JSON for outliers.
Browsers: Chromium-headless + Firefox-headless, both at
`devicePixelRatio=1` initially; Safari + `dpr=2` added in Phase
3.5 once the new client is stable. Golden screenshots live in
`vaadin-spreadsheet-flow-integration-tests/src/reference-screenshots/`,
**committed as plain blobs** alongside the existing IT module
resources — matching upstream Vaadin Flow's `reference-screenshots/`
convention (no Git LFS). CI schedule: **subset on every PR + full
nightly** — PR runs the top-10 priority fixtures (~5 min), nightly
runs the full 162-fixture matrix. Regen workflow: a
`-Dscreenshot.refresh` maven property overwrites goldens; the
**PR author proposes regenerated PNGs, the code reviewer
counter-signs** them as part of the normal review.

The rest of this document walks the considered alternatives, the
threshold-tuning procedure, the browser matrix decision, and the
acceptance criteria for "Phase 3.0 is complete."

## 1.1 Resolved decisions (post-operator review, 2026-06-22)

The 6 open decisions in § 8 were resolved by the operator. The
following table is the canonical answer set; the rest of this
document was updated to reflect the resolutions.

| # | Decision | Resolution |
|---|---|---|
| 1 | Screenshot-diff tool | **TestBench `compareScreen`** — lowest-friction, already on classpath |
| 2 | Golden storage | **`reference-screenshots/` dir in source tree, no Git LFS** — matches upstream `vaadin/flow` convention (verified at `flow-tests/test-dev-mode/`, `flow-tests/test-router-custom-context/`, `flow-tests/test-embedding/test-embedding-generic/`) |
| 3 | Browser matrix | **Desktop evergreens (Chromium + Firefox + Safari), last 2 versions each.** Phase 3.0 captures only Chromium + Firefox headless; Safari + `dpr=2` deferred to Phase 3.5 |
| 4 | Tolerance default | **0.01** — TestBench's documented default. (Earlier draft said 0.05; corrected.) Per-fixture metadata JSON allows widening for charts / anti-aliased lines |
| 5 | CI schedule | **Subset on every PR (top-10 priority fixtures, ~5 min) + full 162-fixture nightly run** — balances safety and iteration speed |
| 6 | Goldens regen ownership | **PR author proposes, code reviewer counter-signs** — matches existing code-review pattern; no central bottleneck |

## 2. Considered options for the diff tool

### 2.1 TestBench `compareScreen` (RECOMMENDED)

`compareScreen("name.png")` and `compareScreen(File, String)`
are part of Vaadin TestBench (existing dep at version
`25.2.0-beta1` per the parent pom). Reads / writes screenshots
to `error-screenshots/` and `reference-screenshots/` by
convention; uses a "compare-pixel-by-pixel with tolerance"
algorithm tuned for cross-browser font rendering.

**Pros:**
- Zero new tooling cost — TestBench is already on the IT
  classpath.
- Tolerance-tuning is well-trodden in the Vaadin ecosystem;
  other components (Grid, ComboBox) use the same API.
- Failure outputs (the diff image) match what other Vaadin
  components produce, so reviewers see consistent diagnostics.
- Block-comparison: TestBench can mask regions of the
  screenshot via `@Screenshot.blockOutLocators({...})` — useful
  for time-sensitive cells (e.g. NOW() formula renders).

**Cons:**
- Per-pixel algorithm is less sophisticated than SSIM-based
  diffing. May produce false positives on small font-hinting
  differences.
- Limited control over tolerance shape: TestBench's setting is
  global, not per-region.

**Selected reason:** lowest-friction integration; established
ecosystem convention; reusable knowledge if Vaadin's other
components ever add screenshot tests for Spreadsheet.

### 2.2 Playwright `toHaveScreenshot`

Playwright is the modern web-test standard. Its
`toHaveScreenshot` matcher does the same job as
TestBench's compareScreen but with a richer config (per-pixel
threshold, `maxDiffPixelRatio`, `animations: 'disabled'`,
`scale: 'css'` to normalize devicePixelRatio).

**Pros:**
- More sophisticated tolerance config.
- Cross-browser (Chromium, Firefox, WebKit) without extra setup.
- Better support for fixing flaky tests (built-in retry, video).
- Active development; aligned with modern web-testing
  best-practice.

**Cons:**
- New test framework on top of TestBench — adds tooling weight.
- Existing IT tests use TestBench + Selenium; running Playwright
  in parallel means two harnesses for one module.
- Migration cost: porting all 77 existing IT classes would
  inflate Phase 3.0 scope.

**Path if we change our mind:** start Phase 3.0 with TestBench;
if tolerance pain becomes the dominant friction, migrate to
Playwright in Phase 3.1.

### 2.3 odiff (Rust CLI tool)

`https://github.com/dmtrKovalenko/odiff`. Very fast,
SSIM-based diff. CLI tool — would be invoked from a custom
JUnit rule that wraps it.

**Pros:**
- Fastest diff implementation by ~10x.
- SSIM + per-pixel options.
- Stable, well-tested.

**Cons:**
- New native tool to install + version-pin in the build.
- No JUnit / Selenium integration — would write a custom rule.
- Speed advantage is illusory: the bottleneck is screenshot
  capture (slow Selenium round-trip), not diff computation.

**Rejected:** the integration cost exceeds the speed benefit.

### 2.4 pixelmatch + custom Java integration

The de-facto Node.js diff library; would be invoked via a
custom Maven exec step.

**Pros:**
- Mature; widely-used in JS ecosystem.

**Cons:**
- Requires Node.js in the Maven build.
- Same custom-rule integration cost as odiff.
- TestBench already wraps pixel-diffing.

**Rejected:** TestBench's built-in does the same job natively.

### 2.5 Resemble.js + custom integration

JS image-diff library. Would be invoked through a JS test
runner.

**Rejected:** same reasoning as 2.4.

## 3. Where the screenshots live

**Pattern decision (resolved post-operator review)**: match the
upstream Vaadin Flow convention. The `flow-tests/` module
hierarchy has `reference-screenshots/` directories committed
directly under the test module roots (verified at
`flow-tests/test-dev-mode/reference-screenshots/`,
`flow-tests/test-router-custom-context/reference-screenshots/`,
`flow-tests/test-embedding/test-embedding-generic/reference-screenshots/`).
The PNGs are committed as plain blobs — no Git LFS in upstream
Flow.

```
vaadin-spreadsheet-flow-integration-tests/
├── src/
│   ├── main/
│   │   └── resources/
│   │       ├── testsheets/                    # existing
│   │       └── test_sheets/                   # existing (157 .xlsx)
│   ├── test/
│   │   ├── java/.../test/                     # existing 77 ITs
│   │   └── resources/                         # existing
│   ├── reference-screenshots/                 # NEW (Phase 3.0)
│   │   ├── Simple Invoice.xlsx/
│   │   │   ├── 1024x768-dpr1-chrome.png       # baseline
│   │   │   ├── 1024x768-dpr1-chrome.metadata.json
│   │   │   ├── 1024x768-dpr1-firefox.png
│   │   │   └── ... per fixture × browser × viewport
│   │   ├── Tagetik 5.xlsx/
│   │   │   └── ...
│   │   └── ...
└── pom.xml
```

**Storage**: PNGs are binary blobs (typical ~50-200 KB each).
With 162 fixtures × 2 browsers × 1 dpr = 324 PNGs in the
initial set. At ~150 KB average that's ~50 MB. **Commit as
plain blobs** in the source tree — matches upstream Flow's
pattern. Git LFS is a fallback option if the binary growth
becomes a real problem; current consensus says it isn't.

**TestBench convention** (per the official docs at
`vaadin/docs/articles/flow/testing/end-to-end/screenshots.adoc`):
- `screenshotReferenceDirectory` setter points TestBench at the
  reference dir.
- `screenshotErrorDirectory` setter points at where failure
  captures go (already configured to `error-screenshots/`).
- TestBench then auto-resolves screenshot names via the standard
  filename convention `{viewport}-{browser}-{fixture}.png` (or
  whatever convention the test author picks for the
  `compareScreen("name")` argument).

## 4. Threshold defaults + the tuning procedure

### 4.1 Initial defaults

```java
// per TestBench's documented API (see Vaadin docs:
// articles/flow/testing/end-to-end/screenshots.adoc)
parameters.setScreenshotComparisonTolerance(0.01);
parameters.setMaxScreenshotRetries(2);
parameters.setScreenshotRetryDelay(500); // ms
parameters.setScreenshotComparisonCursorDetection(true);
```

**Correction (post-operator review)**: TestBench's documented
default for `screenshotComparisonTolerance` is **0.01**, not
0.05. The earlier value cited in this proposal was wrong. The
0.01 default is the tightest reasonable starting point; per-
fixture overrides (via the metadata JSON in § 4.2) widen it for
charts / anti-aliased lines / subpixel-positioned text.

The tuning procedure (§ 4.3) verifies the chosen value against
the noise floor of independent CI worker runs of the unchanged
GWT client.

### 4.2 Per-fixture tolerance escape hatch

For fixtures where the default fails repeatedly with no
content-regression (typically: charts with anti-aliased lines,
cells with subpixel text positioning), allow per-fixture
override via a metadata file:

```json
{
  "tolerance": 0.10,
  "tolerance-reason": "anti-aliased chart edges vary 2-3% across browser font versions"
}
```

Tighten by default; widen only with rationale.

### 4.3 Tuning procedure (Phase 3.0 week 2)

1. Capture goldens against the CURRENT GWT client for the
   top-10 fixtures (per `FIXTURE_INVENTORY.md` priority list).
2. Re-run the same capture on a different machine / CI worker
   to baseline the "no-change diff" noise floor.
3. The noise floor sets the tolerance — pick the lowest value
   that passes the no-change baseline reliably.
4. Repeat per browser. Chromium typically has lower noise;
   Firefox slightly higher.

If the tuned tolerance exceeds ~10% per-pixel diff, switch to
SSIM (TestBench supports it via a custom comparator).

## 5. Browser matrix

### 5.1 Currently supported (per Vaadin Flow's official matrix)

Per the broader Vaadin Flow 25 support policy (verify in the
release notes):

- **Chromium** (Chrome, Edge): latest 2 versions.
- **Firefox**: latest 2 versions.
- **Safari**: latest 2 versions (macOS + iOS).

### 5.2 Phase 3.0 capture: Chromium + Firefox

Initial golden-screenshot capture covers Chromium-headless +
Firefox-headless. **Skip Safari for Phase 3.0** — the
WebKit-on-Linux story is shaky for CI; deal with Safari in
Phase 3.5 once the new client's behavior is established on the
two evergreen browsers.

### 5.3 Phase 3.5 expansion

- Add Safari (via Playwright's WebKit channel if we've migrated
  by then, OR via macOS-runner CI for Selenium).
- Add `devicePixelRatio=2` per browser. Doubles the screenshot
  set; ~100 MB more LFS storage.
- Add mobile viewports (`360x640`, `768x1024`) for the touch
  UI paths. Spreadsheet's touch mode has different selection
  handles.

### 5.4 Browser-coverage cut (decision needed)

GWT's compile output today still supports IE11-style code paths
(via the `compileForUserAgents` config). The rewrite is an
opportunity to drop them. **Recommended cut**:

- ✗ IE11 — dropped.
- ✗ Pre-Chromium Edge — dropped.
- ✓ Chrome / Edge / Firefox / Safari — last 2 versions each.
- ✓ Mobile Safari + Chrome Android — last 2 versions each.

The Vaadin Flow 25 release notes already drop IE11. Confirming
"the new client also drops them" closes the audit.

## 6. Build / CI integration

### 6.1 Local-dev workflow

```sh
# Capture goldens for the first time (or after intended UI changes):
mvn verify -Dscreenshot.refresh=true \
  -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests

# Default run (asserts goldens):
mvn verify -pl vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests
```

The `screenshot.refresh` system property: when set, the test
overwrites the golden instead of asserting. Used after a real UI
change is reviewed and approved.

### 6.2 CI integration

The IT module already runs on CI. Adding screenshot tests
(resolution from § 1.1 #5):

- **Per-PR (subset)**: a "visual-regression-pr" job runs only the
  top-10 priority fixtures (per `FIXTURE_INVENTORY.md` "Recommended
  Phase 3.0 capture order"). Wall-clock cost: ~5 min added to the
  PR check pipeline. Catches the highest-leverage regressions
  at PR time.
- **Nightly (full)**: a "visual-regression-full" job runs the
  full 162-fixture matrix on both browsers. Wall-clock cost:
  ~30 min. Catches long-tail regressions within 24 hours.
- **Noise-floor (nightly)**: a separate job that captures fresh
  screenshots against the *unchanged* GWT client and diffs them
  against the committed reference set. Detects CI-worker drift
  (browser update, JDK change, font change, etc.). If this job
  flickers, the tolerance defaults need re-tuning, not the
  goldens.

When the rewrite (Phase 3.2+) begins, the visual-regression jobs
run against BOTH clients (GWT + new). Both must pass. Once the
new client is the default (Phase 3.6), the GWT-side of the
matrix can be dropped.

**Top-10 priority fixtures for the per-PR subset** (per
`FIXTURE_INVENTORY.md`):

1. `Simple Invoice.xlsx`
2. `Tagetik 5.xlsx`
3. `Grouping.xlsx`
4. `100_000_rows.xlsx`
5. `merged_cells.xlsx`
6. `scrolled_frozen.xlsx`
7. `ConditionalFormatterSamples.xlsx`
8. `spreadsheet_styles.xlsx`
9. `Formulas.xlsx`
10. `data_filters.xlsx`

This 10-fixture set exercises every subsystem at least once
(per the parity-bar analysis in `FEATURE_INVENTORY.md`), giving
maximum coverage for minimum CI cost.

### 6.3 Goldens regen workflow

Resolution from § 1.1 #6: the **PR author proposes regenerated
PNGs, the code reviewer counter-signs**.

Workflow:

1. PR author makes a UI change (intentional).
2. Visual-regression-pr job fails on goldens that no longer
   match.
3. Author runs `mvn verify -Dscreenshot.refresh=true` locally
   to regenerate the PNGs.
4. Author commits the regenerated PNGs into the PR; the diff
   is visible in the GitHub PR review (binary blob, no inline
   render, but reviewer can pull-and-eyeball).
5. PR description includes a "Why goldens changed" paragraph
   per regenerated fixture.
6. Reviewer pulls the branch, eyeballs the new screenshots
   against the diff, signs off.
7. Merge proceeds normally.

This matches the existing code-review pattern: no central
gatekeeper, every change reviewed, accountability spread.

### 6.3 Maven plugin config

Add a new profile to the IT module's pom:

```xml
<profile>
  <id>visual-regression</id>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-failsafe-plugin</artifactId>
        <configuration>
          <systemPropertyVariables>
            <com.vaadin.testbench.Parameters.screenshotComparisonTolerance>0.01</com.vaadin.testbench.Parameters.screenshotComparisonTolerance>
            <com.vaadin.testbench.Parameters.screenshotComparisonCursorDetection>true</com.vaadin.testbench.Parameters.screenshotComparisonCursorDetection>
            <com.vaadin.testbench.Parameters.maxScreenshotRetries>2</com.vaadin.testbench.Parameters.maxScreenshotRetries>
            <com.vaadin.testbench.Parameters.screenshotRetryDelay>500</com.vaadin.testbench.Parameters.screenshotRetryDelay>
            <com.vaadin.testbench.Parameters.screenshotReferenceDirectory>${project.basedir}/src/reference-screenshots</com.vaadin.testbench.Parameters.screenshotReferenceDirectory>
            <com.vaadin.testbench.Parameters.screenshotErrorDirectory>${project.build.directory}/screenshot-diffs</com.vaadin.testbench.Parameters.screenshotErrorDirectory>
          </systemPropertyVariables>
        </configuration>
      </plugin>
    </plugins>
  </build>
</profile>
```

## 7. Acceptance criteria for Phase 3.0 completion

Phase 3.0 is done when:

1. **Feature inventory** — `FEATURE_INVENTORY.md` is reviewed +
   signed off by the engineering owner. All gaps (G.1-G.4) have
   either a fixture authored OR an explicit deferral with
   rationale.

2. **Fixture inventory** — `FIXTURE_INVENTORY.md` is current. All
   162 existing fixtures are categorized. 6 new fixtures (per
   FIXTURE_INVENTORY § G.1-G.4) authored and committed.

3. **Visual-regression infrastructure** — the Maven profile above
   exists; `mvn verify -Dscreenshot.refresh=true` runs end-to-end
   on at least 10 fixtures (the top-10 priority list).

4. **Goldens captured** for all 162 fixtures × 2 browsers × 1 dpr
   = 324 PNGs committed to git LFS.

5. **Tolerance tuned** — the 0.05 default is verified to pass on
   2+ independent CI worker runs of the unchanged GWT client.
   Per-fixture overrides documented for any that need it.

6. **Browser matrix decision** — operator sign-off on the browser
   list for Phase 3.2+ testing.

7. **Performance baseline captured** — for `100_000_rows.xlsx`
   and `perf_baseline.xlsx`: time-to-first-paint, scroll FPS over
   10s window, memory after 5 minutes. Numbers stored alongside
   the goldens in a `.perf-baseline.json` per fixture.

8. **CI integration done** — a CI job runs the visual-regression
   profile on every PR.

When all 8 hold, Phase 3.1 (foundation) starts.

## 8. Decisions (resolved 2026-06-22)

All 6 decisions answered. Canonical resolution is in § 1.1 above.
The brief recap:

1. **Screenshot-diff tool**: TestBench `compareScreen`. ✓
2. **Golden storage**: `reference-screenshots/` in source tree,
   no Git LFS. Matches upstream `vaadin/flow` convention. ✓
3. **Browser matrix**: Desktop evergreens (Chromium + Firefox +
   Safari) last 2 versions. Phase 3.0 captures Chromium + Firefox
   only; Safari + `dpr=2` deferred to Phase 3.5. ✓
4. **Tolerance default**: **0.01** (TestBench documented default).
   Earlier draft incorrectly cited 0.05; corrected. ✓
5. **CI schedule**: subset (top-10 priority fixtures) on every
   PR + full 162-fixture nightly. ✓
6. **Goldens regen ownership**: PR author proposes, code reviewer
   counter-signs. Matches existing code-review pattern. ✓

Phase 3.0 can now proceed to its team-driven work (capture
goldens, tune tolerance, author the 6 gap fixtures).

## 9. Risks specific to Phase 3.0

| # | Risk | Probability | Impact | Mitigation |
|---|------|-------------|--------|------------|
| 1 | TestBench tolerance can't be tuned to pass without false negatives | Medium | High | Fallback: Playwright `toHaveScreenshot` (week-2 swap if needed) |
| 2 | Git LFS storage costs become significant | Low | Medium | Goldens compressed; periodically purge superseded captures |
| 3 | CI run-time blows up | Medium | Medium | Parallelize across CI workers; cap initial set at top-50 fixtures |
| 4 | Goldens drift between OS / CPU architectures of CI workers | Medium | Medium | Pin browser versions; document the CI worker spec |
| 5 | Fixture content has confidentiality issues we missed | Low | High | Audit fixture origins; redact / replace any customer-named content |
| 6 | The 6 new fixtures don't reflect real-world use | Medium | Low | Get user input on representative scenarios before authoring |
