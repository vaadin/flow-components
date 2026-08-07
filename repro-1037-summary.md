<!-- Edit any field. This file is committed on the `repro/1037` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is the ComboBox dropdown visually flashing, triggered by `dataProvider.refreshAll()` while the overlay is open, observable as the open overlay briefly losing all items and showing the loading spinner on each refresh.
- **Regression?:** not a regression (long-standing behavior; reported on 18.0.5 in 2021, still present on main)
- **Fixed by:** n/a
- **Duplicate of:** none found (#4624 is related — same visible loading-flash symptom, but triggered by client-side filtering instead of `refreshAll()`)
- **Branch:** `repro/1037` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ main (25.3-SNAPSHOT, b28844221d)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** steady vs. mid-blink (same open dropdown, 1 second apart):
  ![Dropdown with 20 items in steady state](https://raw.githubusercontent.com/vaadin/flow-components/<commit-sha>/repro-1037-steady.png)
  ![Same dropdown empty with loading spinner during refreshAll](https://raw.githubusercontent.com/vaadin/flow-components/<commit-sha>/repro-1037-blink.png)
- **Demo video:** `repro-1037.webm` (on the branch; drag into the comment for inline playback)

## Observed behavior

With the dropdown open and the server calling `dataView.refreshAll()` once per second (UI polling stands in for the reporter's Kotlin coroutine flow):

- A `MutationObserver` on the `loading`/`opened` attributes recorded a `loading=true → loading=false` flash on **every** refresh cycle (5 out of 5 cycles), while `opened` stayed `true` throughout.
- With a 300 ms artificial fetch delay, sampling `comboBox.filteredItems` showed the open overlay's content flipping from **20 real items** to **200 empty placeholder rows** (`{}`) for the duration of each fetch, then back to 20 items. The 200 comes from the data communicator falling back to its initial size estimate (4 × page size) after the reset.
- Visually, the open dropdown goes completely blank with a loading spinner once per second — the "blinking" from the issue. With zero server latency the loading flash still occurs (~4 ms per cycle), matching the subtle flicker reported for fast responses.

## Expected behavior

The dropdown refreshes its items in place without flashing an empty/loading state — the same way Grid handles `refreshAll()` while visible.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1037?delay=300` (`delay` = artificial fetch latency in ms; `count` = item count, default 20).
2. Click **Start periodic refreshAll** (starts 1 s UI polling; each poll calls `dataView.refreshAll()`).
3. Open the ComboBox dropdown and leave it open.
4. The dropdown blanks out and shows the loading spinner once per second.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1037`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro1037View.java`

```java
ComboBox<String> comboBox = new ComboBox<>("Categories");
ComboBoxLazyDataView<String> dataView = comboBox.setItems(query -> {
    // filter + skip/limit over an in-memory list, optional Thread.sleep(delay)
});

// 1 s UI polling stands in for the reporter's coroutine-driven refresh
NativeButton start = new NativeButton("Start periodic refreshAll", e -> {
    var ui = e.getSource().getUI().orElseThrow();
    ui.setPollInterval(1000);
    pollRegistration = ui.addPollListener(pe -> dataView.refreshAll());
});
```

## Root cause (suspected)

`refreshAll()` fires a `DataChangeEvent`, and the ComboBox data controller reacts with a full reset instead of an in-place refresh:

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxDataController.java#L638-L646

`reset()` collapses the viewport range to `(0, 0)` and calls the client connector's `reset()`:

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxDataController.java#L245-L255

which drops the connector cache and calls `comboBox.clearCache()` on the web component:

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/resources/META-INF/frontend/comboBoxConnector.js#L186-L193

`clearCache()` in the web component replaces every loaded page with placeholders and turns the loading state on, so the open overlay renders empty placeholder rows with a spinner until the fresh page arrives:

https://github.com/vaadin/web-components/blob/ff6dd62222c0f02ee9ee8b778122dfdc6306d2ae/packages/combo-box/src/vaadin-combo-box-data-provider-mixin.js#L175-L180

Grid avoids this by keeping the previously rendered rows visible while refreshed data streams in; ComboBox throws its rendered state away first and refetches into an empty cache. A fix would need the reset path (or a dedicated refresh path) to keep the current items rendered until the replacement page arrives.

## Notes

- Related: #4624 — the same loading-flash symptom triggered by client-side filtering when item count < page size. Different trigger, same underlying "loading state blanks the open overlay" behavior; a fix that keeps stale items rendered during a refetch would likely address both.
- The reporter's Kotlin coroutine setup is incidental — any server-side `refreshAll()` while the dropdown is open triggers the blink. UI polling reproduces it with plain Java.
- No extra dependencies were added to the IT module; the console showed no bug-relevant errors.
