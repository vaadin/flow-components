<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is _an initially hidden lazy ComboBox never receives its item data_, triggered by _making it visible and opening it in the same server round-trip_, observable as _an open dropdown stuck on the loading spinner with empty (placeholder) items_.
- **Branch:** [`repro/9622`](https://github.com/vaadin/flow-components/tree/repro/9622) — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (`flow.version` 25.3-SNAPSHOT, web components resolve to `@vaadin/*@25.2.0` — the affected version)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** ![Open ComboBox stuck on loading spinner with no items](https://raw.githubusercontent.com/vaadin/flow-components/c38cad53da069e477f2438bc453695d93b38ba9c/repro-9622.png)

## Observed behavior

Clicking **Add item** makes the ComboBox visible, sets its value to `null`, and opens it. The dropdown opens but shows only a loading spinner — the items `A`, `B`, `C` never appear.

Inspecting the web component right after opening:

```
size: 3, opened: true, loading: true, filteredItems: [{}, {}, {}]
```

The server delivered the **size** (3 → 3 placeholder rows render), but the actual item **data** for the first page never arrives, so `loading` stays `true` forever and every row stays empty. Typing a filter (`A`) does not recover it either — the combo box stays `loading: true` with `filteredItems: []`, so no data page is ever delivered for this component.

Console is clean (only the dev-server favicon 404 and the Lit dev-mode warning).

## Expected behavior

When the ComboBox is made visible and opened, its items (`A`, `B`, `C`) load and are shown in the dropdown. This worked in 25.1.x.

## Steps to reproduce

1. Open `http://localhost:8080/repro-9622`.
2. Click **Add item** (the ComboBox starts hidden and is made visible + opened by the click listener).
3. The dropdown opens but is stuck on the loading spinner; the items never load.

## Reproduction

How to run: from `flow-components`, start the IT server and open the route below.

```bash
CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -DskipTests \
  -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests
```

- **Route / page:** `http://localhost:8080/repro-9622`
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro9622View.java`

```java
ComboBox<String> addTokenComboBox = new ComboBox<>();
addTokenComboBox.setVisible(false);
addTokenComboBox.setItems(List.of("A", "B", "C"));

Button addTokenButton = new Button("Add item");
addTokenButton.addClickListener(event -> {
    addTokenButton.setVisible(false);
    addTokenComboBox.setVisible(true);
    addTokenComboBox.setValue(null);
    addTokenComboBox.setOpened(true);
    addTokenComboBox.focus();
});

add(addTokenComboBox, addTokenButton);
```

(This is the reporter's minimal example verbatim; the value-change and `opened` property listeners from the issue are kept in the scaffold but are not needed to trigger the bug.)

## Root cause (suspected)

The failure is that the **client never receives the first page of item data** after the ComboBox transitions from invisible to visible+opened in one round-trip — the server sends the size (placeholders render) but no item data, so the dropdown is stuck `loading`. The flow-components ComboBox data bridge (`ComboBoxDataController`, which drives Flow's `DataCommunicator` and the client connector) is the code that governs this delivery:

https://github.com/vaadin/flow-components/blob/c38cad53da069e477f2438bc453695d93b38ba9c/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/ComboBoxDataController.java#L245-L288

This Java code is **unchanged across the regression window** (last touched 2026-03, well before 25.2.0), and only the bundled `@vaadin/combo-box` web component was bumped to 25.2.0. So the regression most likely sits in the 25.2.0 platform bump — either the combo-box web component (the 25.2.0 scroller/initialization refactor in `vaadin-combo-box-scroller-mixin.js` / data-provider flow) or Flow core's `DataCommunicator` handling of a component that was invisible during its initial flush and is not re-flushed when made visible. This needs a human to bisect between web-components 25.1.x → 25.2.0 and the Flow core version.

## Notes

- No IT-module pom change was needed — `vaadin-button-flow` and `vaadin-notification-flow` are already dependencies of the ComboBox integration-tests module.
- Reproduced with the platform's released web components (`@vaadin/*@25.2.0`) on top of flow-components `main`; no maintenance-branch switch was required since the current line is past 25.2.0 and still broken.
