# Reproduction: ComboBox — Filtering issues (#6035)

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Issue:** https://github.com/vaadin/flow-components/issues/6035
- **Verdict:** reproduced (bug #2 — the loading loop). Bug #1 (fast tab-out race) not reproduced — see Notes.
- **Hypothesis tested:** With a lazy (server-backed) ComboBox and `autoOpen=false`, typing a filter then clearing it *before* opening, then opening, leaves the overlay stuck loading — observable as `comboBox.loading === true` permanently with the spinner shown over an empty dropdown.
- **Branch:** `repro/6035` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `24.10` (`24.10-SNAPSHOT`) — matches the reporter's "V24"
- **Present on main?:** unverified. The frontend build for `main` (25.3-SNAPSHOT) could not be produced in this environment (its nightly `@vaadin/*` npm packages trip pnpm's `minimum-release-age` supply-chain guard). The connector was heavily refactored between 24.10 and main (`comboBoxConnector.js`: +145/−102), so it **may be fixed on main** — needs a working main build to confirm.
- **Theme / Browser:** base / Chromium (playwright-cli)

## Observed behavior

After the sequence below, `comboBox.loading` stays `true` indefinitely (still `true` after 10s), the overlay carries the `loading` attribute, and the dropdown renders empty with a spinner in the top-right — exactly the reporter's "blank page with a spinning wheel". Measured live:

```
after type→delete→open:  { opened: true, loading: true, overlayLoading: true }   (stuck at 3s / 6s / 10s)
CONTROL (open directly):  { opened: true, loading: false, overlayLoading: false, renderedRows: 17 }   ✓ works
```

The control (opening the dropdown without the type→delete sequence) loads correctly, which isolates the type-then-clear-before-open sequence as the precise trigger.

## Expected behavior

Opening the dropdown shows the items and clears the loading state, regardless of a filter having been typed and cleared beforehand.

## Steps to reproduce

1. Open `/repro-6035` (a lazy, server-side-filtered `ComboBox` with `setAutoOpen(false)`, 60 items).
2. Focus the input and type a filter (e.g. `asd`) **without** opening the dropdown.
3. Delete the typed text (filter back to empty), still without opening.
4. Open the dropdown (toggle button).
5. The overlay opens but stays stuck on a loading spinner over a blank list; `loading` never clears.

## Reproduction

Route `repro-6035` · `…/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro6035View.java`

```java
@Route("repro-6035")
public class Repro6035View extends Div {
    public Repro6035View() {
        List<String> items = IntStream.rangeClosed(1, 60)
                .mapToObj(i -> String.format("Item %02d", i)).collect(Collectors.toList());
        ComboBox<String> comboBox = new ComboBox<>("Item");
        comboBox.setId("combo");
        comboBox.setAutoOpen(false);
        comboBox.setItems(query -> {                    // lazy: each keystroke goes to the server
            String f = query.getFilter().orElse("").toLowerCase();
            return items.stream().filter(i -> i.toLowerCase().contains(f))
                    .skip(query.getOffset()).limit(query.getLimit());
        }, query -> {
            String f = query.getFilter().orElse("").toLowerCase();
            return (int) items.stream().filter(i -> i.toLowerCase().contains(f)).count();
        });
        add(comboBox);
    }
}
```

## Root cause (suspected)

`vaadin-combo-box-flow/src/main/resources/META-INF/resources/frontend/comboBoxConnector.js:88-118` — the `dataProvider` tracks `lastFilter` and has dedicated handling for "filter changes then changes back within the debounce window" (`needsDataCommunicatorReset`, lines 93-99). With `autoOpen=false`, the filter is typed and cleared **while the overlay is closed** (so no page request completes); on open, `params.filter` equals `lastFilter`, `filterChanged` is `false`, no data is requested, and the web component's `loading` flag — set when the overlay opens — is never cleared by a server response. The fix area is this filter/debounce/reset bookkeeping in the connector together with the server `setViewportRange`/`DataCommunicator` state.

## Notes

- **Bug #1** ("write a value and tab out too quickly → may not register"): a timing race; the maintainer (yuriy-fix) also couldn't reproduce it. Not reproduced here either — it is non-deterministic and the deterministic bug #2 was the focus. Worth a separate, timing-focused attempt.
- **Build environment:** flow-components must run jetty with `CI=true` (no-TTY pnpm), and the combo-box IT module needed `mvn clean` to clear a stale generated `package.json` (`$@vaadin/router` override) before a fresh build succeeded on 24.10.
