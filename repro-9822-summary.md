<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that a click which only dismisses an open field overlay inside the detail area also reaches the `MasterDetailLayout` backdrop, triggered by clicking the backdrop while a `MultiSelectComboBox` dropdown is open in overlay mode, observable as `backdrop-click` firing once (server listener runs, detail drawer closes) from that single click.
- **Regression?:** unknown — no working version is named in the report; reproduces both on the reported 25.2.5 line and on current `main`
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/9822` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT), `@vaadin/master-detail-layout` + `@vaadin/multi-select-combo-box` 25.3.0-alpha8
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![MultiSelectComboBox dropdown open over the detail drawer, then a single backdrop click closes the whole drawer](https://raw.githubusercontent.com/vaadin/flow-components/<commit-sha>/repro-9822-before.png) — embeds inline.
- **Demo video** (motion bug): n/a — two screenshots (`repro-9822-before.png`, `repro-9822-after.png`) on the branch show the before/after state.

## Observed behavior

With the layout in overlay mode and the detail drawer open:

1. The `MultiSelectComboBox` dropdown is open (`#mscb.opened === true`), the layout has the `overlay` attribute, `backdrop clicks: 0`, `detail: open`.
2. A single click on the backdrop (over the master column) both closes the dropdown **and** fires `backdrop-click`: `#mscb.opened === false`, `backdrop clicks: 1`, `detail: none`, and the layout loses its `has-detail` attribute.

The overlay's outside-click handling and the backdrop's click handler both act on the same click event, so the user loses the detail drawer (and any unsaved form state in it) with one click.

Not specific to `MultiSelectComboBox` — measured in the same view, one backdrop click with each field overlay open:

| Field overlay open when the backdrop is clicked | Overlay closed | `backdrop-click` fired |
| --- | --- | --- |
| none (baseline) | – | yes (correct) |
| `MultiSelectComboBox` | yes | **yes** |
| `ComboBox` | yes | **yes** |
| `DatePicker` | yes | **yes** |

Also reproduces with `OverlayContainment.PAGE`.

By contrast, the **Escape** path is already shielded: with the `MultiSelectComboBox` dropdown open, `Escape` closes only the dropdown and `detail-escape-press` does **not** fire. The backdrop-click path lacks the equivalent guard.

Console: clean (only the dev-server favicon 404 and the Lit dev-mode warning).

## Expected behavior

A click that dismisses an open field overlay inside the detail area should be consumed by that overlay: the dropdown closes, `backdrop-click` does not fire, and the detail drawer stays open — mirroring how `Escape` already behaves.

## Steps to reproduce

1. Open `http://localhost:8080/repro-9822` (the layout is sized so that master + detail do not fit, so it is always in overlay mode).
2. Click **Open detail**.
3. Click the `MultiSelectComboBox` toggle button to open its dropdown.
4. Click the backdrop over the master column (e.g. 40 px from the left edge, below the dropdown).
5. Observe `backdrop clicks: 1` and `detail: none` — the drawer closed from the same click that dismissed the dropdown.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-9822`
- **Scaffold:** `vaadin-master-detail-layout-flow-parent/vaadin-master-detail-layout-flow-integration-tests/src/main/java/com/vaadin/flow/component/masterdetaillayout/tests/Repro9822View.java`

```java
MasterDetailLayout layout = new MasterDetailLayout();
layout.setWidth("500px");
layout.setHeight("450px");
layout.setMasterSize("400px");   // master + detail don't fit -> overlay mode
layout.setDetailSize("400px");

layout.setMaster(new Div(new NativeButton("Open detail",
        e -> layout.setDetail(createDetail()))));

layout.addBackdropClickListener(e -> {
    backdropClicks++;
    backdropCount.setText("backdrop clicks: " + backdropClicks);
    layout.setDetail(null);      // reporter's listener: closes the drawer
});

// createDetail(): MultiSelectComboBox + ComboBox + DatePicker + TextField
```

The view also has buttons to switch `OverlayContainment` and to keep the drawer open on backdrop click, so the event can be counted without the drawer closing.

## Root cause (suspected)

This is a web-component level issue; the Flow API is only a pass-through for the `backdrop-click` DOM event.

The overlay of a field (combo box, multi-select combo box, date picker) closes from a document-level **capture-phase** click listener that does not stop propagation of the click:

https://github.com/vaadin/web-components/blob/3b9d869f4ade017a09dc4554ed69c6971aec1195/packages/overlay/src/vaadin-overlay-mixin.js#L538-L557

The click therefore continues to the `MasterDetailLayout` backdrop, whose handler dispatches `backdrop-click` unconditionally:

https://github.com/vaadin/web-components/blob/3b9d869f4ade017a09dc4554ed69c6971aec1195/packages/master-detail-layout/src/vaadin-master-detail-layout.js#L491-L494

Nested `vaadin-overlay`s protect each other through the overlay stack — `_shouldCloseOnOutsideClick()` returns `this._last`, so only the topmost overlay reacts to an outside click. The detail area in overlay mode is not a `vaadin-overlay` (it is a plain `<div id="backdrop">` plus a `<div id="detail">` in the layout's shadow root), so it is not part of that stack and gets no such protection:

https://github.com/vaadin/web-components/blob/3b9d869f4ade017a09dc4554ed69c6971aec1195/packages/master-detail-layout/src/vaadin-master-detail-layout.js#L242-L252

The keyboard path already has a guard of this kind (`__onDetailKeydown` ignores events with `defaultPrevented`), which is why `Escape` behaves correctly while the backdrop click does not.

Flow side, for reference — the event is simply forwarded:

https://github.com/vaadin/flow-components/blob/6dc316a50b4b1c49adc0ef0f211f1421cabdb78b/vaadin-master-detail-layout-flow-parent/vaadin-master-detail-layout-flow/src/main/java/com/vaadin/flow/component/masterdetaillayout/MasterDetailLayout.java#L734-L741

## Notes

- The reproduction module needed one extra dependency: `vaadin-combo-box-flow` added to `vaadin-master-detail-layout-flow-integration-tests/pom.xml` (committed on the branch).
- While the detail drawer is open with `OverlayContainment.LAYOUT`, the master column is `inert`, so the master-area control buttons in the repro view only work with the drawer closed.
- No duplicate found in `vaadin/flow-components`, `vaadin/web-components` or `vaadin/flow`.
