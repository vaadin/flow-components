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
infrastructure. Tolerance thresholds: `0.05` per-pixel diff
allowed (TestBench's default-ish), `0` for layout-shift
detection. Browsers: Chromium-headless + Firefox-headless, both
at `devicePixelRatio=1` initially; add `dpr=2` once the
tolerance baseline is established. Golden screenshots live under
`vaadin-spreadsheet-flow-integration-tests/src/test/resources/golden-screenshots/<fixture-name>/<viewport-key>.png`,
checked into git via Git LFS. Regen workflow: a `-Dscreenshot.refresh`
maven property overwrites goldens; default diff-asserts.

The rest of this document walks the considered alternatives, the
threshold-tuning procedure, the browser matrix decision, and the
acceptance criteria for "Phase 3.0 is complete."

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

```
vaadin-spreadsheet-flow-integration-tests/
├── src/
│   ├── main/
│   │   └── resources/
│   │       ├── testsheets/                    # existing
│   │       └── test_sheets/                   # existing (157 .xlsx)
│   ├── test/
│   │   ├── java/.../test/                     # existing 77 ITs
│   │   └── resources/
│   │       └── golden-screenshots/            # NEW (Phase 3.0)
│   │           ├── Simple Invoice.xlsx/
│   │           │   ├── 1024x768-dpr1-chrome.png       # baseline
│   │           │   ├── 1024x768-dpr1-chrome.metadata.json
│   │           │   ├── 1024x768-dpr1-firefox.png
│   │           │   └── ... per fixture × browser × viewport
│   │           ├── Tagetik 5.xlsx/
│   │           │   └── ...
│   │           └── ...
│   │           reference-screenshots/         # symlink to TestBench dir
└── pom.xml
```

**Storage**: PNGs are binary blobs (typical ~50-200 KB each).
With 162 fixtures × 2 browsers × 1 dpr = 324 PNGs in the
initial set. At ~150 KB average that's ~50 MB. **Use Git LFS**
to keep the main repo small.

Git LFS pattern in `.gitattributes`:

```
vaadin-spreadsheet-flow-parent/vaadin-spreadsheet-flow-integration-tests/src/test/resources/golden-screenshots/**/*.png filter=lfs diff=lfs merge=lfs -text
```

## 4. Threshold defaults + the tuning procedure

### 4.1 Initial defaults

```java
// per TestBench's compareScreen API
parameters.setScreenshotErrorTolerance(0.05); // 5% per-pixel diff
parameters.setMaxScreenshotRetries(2);
parameters.setScreenshotComparisonCursorDetection(true);
```

The `0.05` (5%) tolerance is TestBench's documented default
for cross-browser font rendering. Adjust during tuning if false
positives dominate.

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

The IT module already runs on CI. Adding screenshot tests:

- **Per-PR**: a "visual-regression" job that captures + compares
  goldens. Fails the PR if any diff exceeds threshold.
- **Nightly**: a "noise-floor" job that captures fresh goldens
  on the unchanged GWT client and compares against the
  committed goldens. Detects environment drift.

When the rewrite (Phase 3.2+) begins, the visual-regression
job runs against BOTH clients (GWT + new). Both must pass.
Once the new client is the default (Phase 3.6), the GWT-side
of the matrix can be dropped.

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
            <com.vaadin.testbench.Parameters.screenshotErrorTolerance>0.05</com.vaadin.testbench.Parameters.screenshotErrorTolerance>
            <com.vaadin.testbench.Parameters.screenshotComparisonCursorDetection>true</com.vaadin.testbench.Parameters.screenshotComparisonCursorDetection>
            <com.vaadin.testbench.Parameters.screenshotReferenceDirectory>${project.basedir}/src/test/resources/golden-screenshots</com.vaadin.testbench.Parameters.screenshotReferenceDirectory>
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

## 8. Open decisions for the operator

Before Phase 3.0 week 2 begins:

1. **Approve TestBench-based screenshot tests** (recommended)
   vs Playwright migration (rejected here, but operator can
   override).
2. **Approve Git LFS** for golden storage (alternative: a
   separate sibling repo, like Vaadin's `flow-components-screenshots`
   pattern if one exists).
3. **Approve the browser matrix cut** (drop IE11, pre-Chromium
   Edge; keep last-2-versions of evergreen browsers + mobile
   Safari + Chrome Android).
4. **Approve the 0.05 default tolerance** as the starting point
   (final value determined by tuning).
5. **CI worker capacity** — adding visual-regression tests adds
   ~30 minutes per CI run (162 fixtures × 2 browsers × ~5
   seconds per capture). Verify the CI budget can absorb this.
6. **Owner for goldens** — when a real UI change lands and
   goldens shift, who approves the regenerated PNGs? Suggested:
   the engineer who authored the change reviews the diff;
   merge-base reviewer counter-signs.

## 9. Risks specific to Phase 3.0

| # | Risk | Probability | Impact | Mitigation |
|---|------|-------------|--------|------------|
| 1 | TestBench tolerance can't be tuned to pass without false negatives | Medium | High | Fallback: Playwright `toHaveScreenshot` (week-2 swap if needed) |
| 2 | Git LFS storage costs become significant | Low | Medium | Goldens compressed; periodically purge superseded captures |
| 3 | CI run-time blows up | Medium | Medium | Parallelize across CI workers; cap initial set at top-50 fixtures |
| 4 | Goldens drift between OS / CPU architectures of CI workers | Medium | Medium | Pin browser versions; document the CI worker spec |
| 5 | Fixture content has confidentiality issues we missed | Low | High | Audit fixture origins; redact / replace any customer-named content |
| 6 | The 6 new fixtures don't reflect real-world use | Medium | Low | Get user input on representative scenarios before authoring |
