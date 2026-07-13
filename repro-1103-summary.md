<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is the ComboBox overlay not opening, triggered by calling `setOpened(true)` while the component is attached (in the constructor) on a direct page load / refresh, observable as the overlay staying closed with no items.
- **Regression?:** unknown (reported on 17.0.2, still present per a 2025 comment on 24, confirmed here on 25.3-SNAPSHOT; no known-good version)
- **Flavor:** Flow
- **Branch:** `repro/1103` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium
- **Screenshot:** `![combo-box stays closed on direct load](https://raw.githubusercontent.com/vaadin/flow-components/6a9fd892ede9e9be5da4d3c4d42d97d74464a59b/repro-1103.png)`

## Observed behavior

A minimal pair on the same build, driven in the browser:

| Case | `combo.opened` | items rendered |
| --- | --- | --- |
| Direct load of `/repro-1103` | **false** | 0 |
| Refresh of `/repro-1103` | **false** | 0 |
| Client-side (SPA) navigation to `/repro-1103` | **true** | 2 ("Option one", "Option two") |

On a direct load or refresh the overlay never opens; via client-side navigation the same view opens correctly. This matches the 2025 comment ("opening when navigating to the page, but not when opening the page directly or refreshing").

## Expected behavior

`setOpened(true)` called during attach opens the overlay, regardless of whether the view was reached by direct load, refresh, or client-side navigation.

## Steps to reproduce

1. Open `/repro-1103` directly in the browser (or refresh it).
2. Observe the ComboBox stays closed.
3. For contrast, reach the same view via client-side navigation (`/repro-1103-nav` → click the link) — it opens.

## Reproduction

How to run: start the server (`mvn … jetty:run` on the combo-box IT module) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1103` (control: `http://localhost:8080/repro-1103-nav`)
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro1103View.java`

```java
ComboBox<String> combo = new ComboBox<>();
combo.setItems("Option one", "Option two");
combo.setOpened(true);
add(combo);
```

## Root cause (suspected)

`setOpened(true)` only sets the `opened` element property; the overlay open is then handled on the client. On attach the connector and lazy data provider are wired in `onAttach` (`initConnector()` + `dataController.onAttach()`). On a direct render the `opened=true` sync is processed around the same time the data provider is being connected, so the overlay opens before any items/size are available and is dismissed; on client-side navigation the ordering differs and the open survives.

https://github.com/vaadin/flow-components/blob/6a9fd892ede9e9be5da4d3c4d42d97d74464a59b/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxBase.java#L607-L612

## Notes

- No known duplicate. Related in spirit to other "state applied during attach not re-run on the client" issues, but here it is specific to the initial-render vs. navigation ordering of the `opened` property against data-provider initialization.
- Reproduced deterministically without any timing hacks: the overlay is simply closed after a direct load.
