# Reproduction: ComboBox — Filtering issues (#6035)

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Issue:** https://github.com/vaadin/flow-components/issues/6035
- **Verdict:** reproduced — **both** reported symptoms, both requiring `setAutoOpen(false)`.
- **Branch:** `repro/6035` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `24.10` (`24.10-SNAPSHOT`, matches the reporter's "V24") **and `25.1`** (`25.1-SNAPSHOT`) — identical behavior on both.
- **Present on main?:** very likely, not directly verified. The connector was heavily refactored between 24.10 and main (`comboBoxConnector.js` +145/−102), yet the bug still reproduces on `25.1` (one step below main), so the refactor did **not** fix it. A direct `main` (25.3-SNAPSHOT) run could not be built here (nightly `@vaadin/*` deps trip pnpm's `minimum-release-age` guard); confirm on `main` when buildable.
- **Theme / Browser:** base / Chromium (playwright-cli)

## Observed behavior

Both symptoms trace to one defect: **with `setAutoOpen(false)`, a server-side filter typed while the overlay is closed never completes — `loading` stays `true` forever and `filteredItems` stays empty.** Measured live on 25.1:

```
Scenario 1 — type "finland", quick Tab:   { value: "",  inputText: "finland", label: "Value: null", loading: true }
Scenario 1 — type "finland", WAIT, Tab:    { value: "",  inputText: "finland" }            ← still no commit
Diagnostic — type "finland", overlay CLOSED, wait 2s:  { loading: true, opened: false, filteredItems: [] }   ← request never resolves
Scenario 2 — type "asd", delete, open:     { opened: true, loading: true }                 ← stuck spinner (10s+)
CONTROL — open directly (no typing):       { opened: true, loading: false, items: 60 }      ✓ works
```

- **Symptom #1 (value not committed):** because the closed-overlay filter request is stuck loading, `"Finland"` never arrives in `filteredItems`, so Tab cannot match/commit it — the value stays `null` and the input keeps the raw `"finland"`. This is **not** a flaky race (it reproduces whether Tab is quick or after a wait); it is the deterministic stuck-loading state. The artificial 300 ms server latency in the maintainer's repro makes it reliable to observe.
- **Symptom #2 (blank spinner):** the same stuck-loading state, surfaced visually when the dropdown is opened — overlay shows a spinner over an empty list.
- The control (opening directly, no closed-overlay typing) loads all items and clears `loading`, isolating "type a server filter while closed under `autoOpen=false`" as the trigger.

## Expected behavior

A filter typed while the overlay is closed should complete normally: items load, `loading` clears, a typed value that matches an item (`"finland"` → `"Finland"`) commits on Tab, and opening the dropdown shows the list.

## Steps to reproduce

Both scenarios use a lazy, server-filtered `ComboBox` with `setAutoOpen(false)` (and a small simulated server latency).

- **Scenario 1:** focus the field, type `finland`, press Tab. The value is not committed; the input keeps `finland` instead of `Finland`.
- **Scenario 2:** focus the field, type `asd`, delete it, then open the dropdown via the toggle button. The overlay opens to a blank list with a stuck loading spinner.

## Reproduction

Route `repro-6035` · `…/vaadin-combo-box-flow-integration-tests/.../test/Repro6035View.java` (adapted from the maintainer @vursen's reproduction in the issue — two `setAutoOpen(false)` combos, 100 items incl. "Finland", `Thread.sleep(300)` server latency, a value-change label). Run with `CI=true mvn package jetty:run … -pl …combo-box…-integration-tests`. Full source on the branch.

## Root cause (suspected)

`vaadin-combo-box-flow/src/main/resources/META-INF/resources/frontend/comboBoxConnector.js` (the `dataProvider` filter/debounce/`needsDataCommunicatorReset` bookkeeping, ~lines 88-118) together with the server `setViewportRange`/`DataCommunicator` state. When `autoOpen=false`, a filter request issued while the overlay is closed never completes, leaving the web component's `loading` flag set with no server response to clear it and `filteredItems` empty. Both reported symptoms follow from this single unresolved-request state.

## Notes

- **Improvement over the first attempt:** the maintainer @vursen's repro ([comment](https://github.com/vaadin/flow-components/issues/6035#issuecomment-4621375844)) adds `Thread.sleep(300)` latency to the data callbacks. The earlier scaffold used an instant in-memory provider, which masked symptom #1; the latency makes the closed-overlay stuck-loading reliably observable and let us tie #1 and #2 to the same cause.
- **Bug #1 framing corrected:** the report and maintainer describe it as a "sometimes" tab-out race. Here it reproduced deterministically (quick Tab and waited Tab both failed to commit), so it presents as the stuck-loading defect rather than a pure timing race. Worth confirming with the team whether any timing-dependent variant also exists.
- **Build environment:** flow-components needs `CI=true` (no-TTY pnpm purge), and the combo-box IT module needed `mvn clean` to clear a stale generated `package.json` (`$@vaadin/router` override) before a fresh build succeeded.
