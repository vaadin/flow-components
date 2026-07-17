<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a server/client state desync where `AccordionPanel.setOpened(true)` leaves the panel collapsed, triggered by making the panel invisible in one request and then visible + opened (together with `Accordion.open(panel)`) in a later request, observable as the target panel staying collapsed while the server reports `opened == true`.
- **Regression?:** unknown (reported on 24.9.0, still present on `main` / 25.3-SNAPSHOT; no known "worked in" version — the panel-vs-accordion state conflict looks present since the accordion single-open design)
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/8100` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ `main` (25.3-SNAPSHOT), `@vaadin/accordion` 25.3.0-alpha4
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** ![panel2 collapsed after str2 despite open=true](https://raw.githubusercontent.com/vaadin/flow-components/5d02b18845b133ed41cf4dbb538f29ede4ce0d73/repro-8100.png)

## Observed behavior

Reproducing the reporter's two-branch flow (str1 then str2) with buttons:

- **str1** (panel2 → invisible, panel1 → visible + `setOpened(true)` + `accordion.open(panel1)`): panel1 opens correctly (DOM `opened=true`, expanded).
- **str2** (panel1 → invisible, panel2 → visible + `setOpened(true)` + `accordion.open(panel2)`): panel2 stays **collapsed**. The DOM shows `panel2.opened=false` while the server reports `panel2 open=true`. Inspecting the accordion:
  - `accordion.opened` (client index) = **0**, not 1
  - `items[0]` = panel1: `opened=true`, `hidden=true`, `display:none`
  - `items[1]` = panel2: `opened=false`

So the previously-opened panel1, now hidden, keeps `opened=true` and the accordion's index derivation locks onto it (index 0), leaving panel2 (index 1) closed. Forcing `accordion.opened=1` or `panel1.opened=false` from the browser console does not stick — the server state (both panels `opened=true`, index synced back to 0) is re-applied.

An **isolated single panel** (visibility toggled false→true then `setOpened(true)`, without `Accordion.open`) opens correctly — so the trigger is the combination of the accordion single-open index control with the hidden, still-`opened` previous panel.

## Expected behavior

`setOpened(true)` should open the panel regardless of whether its visibility was toggled beforehand.

## Steps to reproduce

1. Accordion with two panels, both initially visible, accordion closed.
2. Request 1 ("str1"): hide panel2, show panel1, `panel1.setOpened(true)`, `accordion.open(panel1)` → panel1 opens.
3. Request 2 ("str2"): hide panel1, show panel2, `panel2.setOpened(true)`, `accordion.open(panel2)` → **panel2 stays collapsed**.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-8100`
- **Scaffold:** `vaadin-accordion-flow-parent/vaadin-accordion-flow-integration-tests/src/main/java/com/vaadin/flow/component/accordion/tests/Repro8100View.java`

```java
// Request 2 branch ("str2") — panel2 was made invisible in the previous request:
accordion.close();
panel1.setVisible(false);
panel2.setVisible(true);
panel2.setOpened(true);
accordion.open(panel2);
// Result: panel2 collapsed; accordion.opened == 0 (panel1, hidden) instead of 1.
```

## Root cause (suspected)

Two independent state channels conflict. `AccordionPanel.setOpened(true)` sets the panel's own `Details.opened` property; `Accordion.open(index)` / `close()` only set the accordion's index property and never reset the individual panels' `opened` state:

https://github.com/vaadin/flow-components/blob/daaeb73a2e93d875b04c79d011a679fcdbeac1c2/vaadin-accordion-flow-parent/vaadin-accordion-flow/src/main/java/com/vaadin/flow/component/accordion/Accordion.java#L146-L164

On the client, the accordion derives its `opened` index from the panels' `opened-changed` events; when the previously opened panel is hidden but still `opened=true`, it wins the index and the observer never closes it (the index does not change to a value that would close it):

https://github.com/vaadin/web-components/blob/62ae5674daf0d16b006dfd59ae5d5b7de53ee7b8/packages/accordion/src/vaadin-accordion-mixin.js#L137-L184

The interaction with Flow's invisible-element sync (a hidden panel whose `opened` state is not reconciled) is what leaves the server holding both panels `opened=true` and the accordion index at the hidden panel.

## Notes

- The reporter's original example used `ComboBox` value changes to drive the two branches; the repro replaces those with two `NativeButton`s (`#str1`, `#str2`) and `Div` content to keep the scaffold within the accordion IT module's dependencies. The server-side state is echoed in a `#status` span.
- A single-panel control (`#iso-hide` → `#iso-show-open`) confirms that visibility-toggle + `setOpened(true)` alone works; the accordion index control is required to trigger the bug.
- Possible fix direction: have `Accordion.open(...)`/`close()` also reconcile the panels' `opened` state, and/or reset a panel's `opened` when it is opened/closed via the accordion index, so the panel-level and index-level state cannot diverge.
