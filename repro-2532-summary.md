> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — the dropdown gets permanently stuck in the loading state with no items after committing a custom value while a fetch is in flight
- **Hypothesis tested:** The bug is that re-setting the ComboBox items (FetchCallback) inside a CustomValueSetListener leaves the overlay stuck loading, triggered by typing a value and committing it (Enter) then reopening before the fetch resolves, observable as `combo.loading === true` with zero `filteredItems`.
- **Regression?:** unknown (reported against ~Vaadin 23 in 2022, still present on main; no known-good version stated)
- **Flavor:** Flow (root cause in the flow-components combo-box connector)
- **Branch:** `repro/2532` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ main (25.3-SNAPSHOT, `@vaadin/combo-box` 25.3.0-alpha3)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![overlay stuck on the loading spinner with no items](https://raw.githubusercontent.com/vaadin/flow-components/repro-2532/repro-2532.png) — the reporter's own video shows the motion.

## Observed behavior

ComboBox with items set as a `FetchCallback` (`Stream.of("foo")`), re-set inside `addCustomValueSetListener`. Driven in Chromium:

- **Clean control** (fresh load, just open): `loading = false`, items = `["foo"]` — works.
- **Race gesture** (type a custom value → Enter → immediately reopen with ArrowDown): the overlay ends **`loading = true`, `opened = true`, `filteredItems = 0`** and **stays that way past 4 s** — permanently stuck, not merely slow. Hit on 5 of 6 rapid attempts (one won the race and recovered).

No console errors, no server exceptions — a silent stuck state.

## Expected behavior

After committing a custom value, reopening the dropdown should show the item ("foo"), not a perpetual loading spinner.

## Steps to reproduce

1. Open `http://localhost:8080/repro-2532`.
2. Type any text into the field and press Enter quickly (before the fetch resolves), then reopen the dropdown (ArrowDown / toggle).
3. The overlay stays on the loading spinner with no items; it does not recover on its own.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-2532`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro2532View.java`

## Root cause (suspected)

In the combo-box connector, the `dataProvider` callback debounces filter changes (`_filterDebouncer`, ~500 ms) and tracks `lastTypedFilter` / `lastRequestedFilter` / `needsDataCommunicatorReset`:

https://github.com/vaadin/flow-components/blob/2bb8f77a60b92f6289c9df10b9349acf9f71d7b1/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/resources/META-INF/frontend/comboBoxConnector.js#L36-L68

Committing the custom value re-sets the items, which resets the connector and clears `lastTypedFilter`:

https://github.com/vaadin/flow-components/blob/2bb8f77a60b92f6289c9df10b9349acf9f71d7b1/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/resources/META-INF/frontend/comboBoxConnector.js#L186-L200

When the reopen's (debounced) fetch races this reset, the filter-tracking state desyncs so no page request completes for the reopened overlay — `loading` is left set with an empty cache. The trigger site is the flow-components connector; confirm during the fix whether the web component's loading/filter state also needs a nudge.

## Notes

- Recovery in the field is manual: close and reopen again (per the issue) sometimes clears it; in this automated run the stuck state did not self-recover within 4 s.
- Timing-sensitive: reproduced 5/6 with an immediate reopen after Enter; a slower reopen (300 ms wait) did not reproduce.
