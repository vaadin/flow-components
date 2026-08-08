<!-- Edit any field. This file is committed on the `repro/2007` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** partially reproduced — the scroll position is lost when grids are swapped with `removeAll()` + `add()`, but it is **not** Firefox-only and one grid does **not** take over the other grid's position; the `setVisible()` variant reported as "worse" now works correctly
- **Hypothesis tested:** The bug is that swapping two grids in one container loses or mixes up their scroll positions, triggered by scrolling grid 1, switching to grid 2, scrolling it, and switching back, observable as grid 1 showing a different first row than before the switch.
- **Regression?:** not a regression for the `removeAll()`/`add()` case (client state has always been dropped when the element is detached). The `setVisible()` symptom (blank grid until scrolled) was fixed in 24.6.8.
- **Fixed by:** vaadin/web-components#8642 and vaadin/web-components#8896 (virtualizer restores `scrollTop` in `hostConnected` / on resize) — these fix the `setVisible()` case, not the `removeAll()` case
- **Duplicate of:** none found (vaadin/web-components#8630 is the already-fixed related issue)
- **Branch:** `repro/2007` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3.0-alpha, commit 18eefcbcbb)
- **Present on main?:** partially — scroll reset on `removeAll()`/`add()`: yes; Firefox-only difference and cross-grid interference: no
- **Theme / Browser:** Lumo / Firefox 152 **and** Chromium (identical results in both)
- **Screenshot:** ![Top: remove/add grid back at Row 1. Bottom: setVisible grid keeps Row 75](https://raw.githubusercontent.com/vaadin/flow-components/2cc1974da4587910dc192f473f0e17ca3933c777/repro-2007.png) — embeds inline.

## Observed behavior

Two cases in one view, each with two 999-row grids, driven identically in Firefox and Chromium.

**`removeAll()` + `add()` (the reporter's approach):**

| Step | Firefox | Chromium |
| --- | --- | --- |
| grid 1 scrolled | `scrollTop` 2666, first row `Row 73` | `scrollTop` 12000, first row `Row 333` |
| switch to grid 2, scroll it | `scrollTop` 4410, first row `Row 122` | `scrollTop` 20000, first row `Row 556` |
| switch back to grid 1 | `scrollTop` **0**, first row `Row 1` | `scrollTop` **0**, first row `Row 1` |

The position is reset to the top, not replaced by the other grid's position, and both browsers behave the same.

**`setVisible()` (the case reported in the comments as "worse"):**

| Step | Firefox | Chromium |
| --- | --- | --- |
| grid 1 scrolled, switch to grid 2 and back | `scrollTop` 2666, first row `Row 73`, all rows rendered | `scrollTop` 12000, first row `Row 333`, all rows rendered |

No blank rows, no reset — the behavior @liujf2k described is gone.

**Why the two cases differ:** an expando set on the grid element (`grid.__marker`) survives a `setVisible()` round trip but is `null` after a `removeAll()`/`add()` round trip. Flow creates a **new client-side element** when a component is detached and attached again, so every piece of client state — including the virtualizer's scroll position — starts from scratch.

Console shows no errors (Firefox adds an informational "scroll-linked positioning effect" warning, which is dev noise).

## Expected behavior

Ideally the grid keeps its scroll position when the component is removed and added again. At minimum, the behavior should not differ between browsers — which it no longer does.

## Steps to reproduce

1. Open `http://localhost:8080/repro-2007`.
2. In the first section, scroll grid 1 down, click "remove/add: show grid 2", scroll grid 2, then click "remove/add: show grid 1" — grid 1 is back at `Row 1`.
3. In the second section, do the same with the "visible: show grid …" buttons — the grid keeps its position.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-2007`
- **Scaffold:** `vaadin-grid-flow-parent/vaadin-grid-flow-integration-tests/src/main/java/com/vaadin/flow/component/grid/it/Repro2007View.java`

```java
Div container = new Div();
Grid<String> grid1 = createGrid("remove-add-grid-1");
Grid<String> grid2 = createGrid("remove-add-grid-2");
container.add(grid1);

NativeButton show1 = new NativeButton("show grid 1", e -> {
    container.removeAll();
    container.add(grid1);
});
NativeButton show2 = new NativeButton("show grid 2", e -> {
    container.removeAll();
    container.add(grid2);
});
```

## Root cause (suspected)

The virtualizer restores `scrollTop` when its host is connected again:

https://github.com/vaadin/web-components/blob/cb915ebde095ec5b94a87af93dd4530f51984c52/packages/component-base/src/virtualizer-iron-list-adapter.js#L197-L205

That restore only helps when the **same** element comes back. With `removeAll()` + `add()` Flow builds a fresh `<vaadin-grid>` element on the client (proved by the lost expando above), so there is no stored `_scrollPosition` to restore. Keeping the scroll position across a detach/attach would require the position to be remembered on the server side and re-applied after the new element is created — a Flow-side change, not something the web component can do on its own.

## Notes

- Firefox 152 and Chromium behave identically in both cases, so the "Firefox only" part of the original report no longer holds. The original report is against Vaadin 14.6.8 (2021).
- The cross-grid interference described in the title ("scroll on one grid will affect other") did not appear: the returning grid is always at the top, never at the other grid's position.
- `TabSheet` (suggested in the comments) avoids this entirely because it hides panels instead of removing them, which matches the working `setVisible()` case measured above.
- Programmatic and real wheel scrolling were both tried, with waits for the scroll to settle before switching.
