<!-- Edit any field. This file is committed on the `repro/1732` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a client TypeError (`Cannot read properties of null (reading 'x')`), triggered by long-press opening a context menu and then dragging without lifting the finger, observable as an uncaught console error from the context-menu gesture code's `touchmove` handler.
- **Regression?:** not a regression (broken since the touch gesture support; reported 2019, the failing logic is unchanged through two repo migrations)
- **Fixed by:** none found
- **Duplicate of:** none found
- **Branch:** `repro/1732` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ main (25.3-SNAPSHOT, b28844221d)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli, synthetic touch events replaying the Android Chrome long-press sequence)
- **Screenshot:** ![Context menu opened by long-press, before the drag that throws](https://raw.githubusercontent.com/vaadin/flow-components/9dcb256b8f0fff2fb7e0314d87e2ce3e18bce53d/repro-1732-menu-open.png)
- **Demo video:** `repro-1732.webm` (on the branch; drag into the comment for inline playback)

## Observed behavior

Replaying the Android long-press sequence (`touchstart` → hold 600 ms → browser-style `contextmenu` while the finger is still down → `touchmove` drag) on a `ContextMenu` target:

- The context menu opens after the `contextmenu` event (screenshot; `vaadin-context-menu[opened]` in DOM). Zero errors up to this point.
- The first `touchmove` after the menu opened throws an uncaught error — the exact error from the issue:
  ```
  TypeError: Cannot read properties of null (reading 'x')
      at Object.touchmove (@vaadin/context-menu/src/vaadin-contextmenu-event.js)
      at HTMLDivElement._handleNative (@vaadin/component-base/src/gestures.js)
  ```

## Expected behavior

Dragging after a long-press-opened context menu should not throw; the recognizer should either ignore the move or guard against the cleared gesture state.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1732` on a touch device (or emulate the event sequence below).
2. Long-press the target until the context menu opens.
3. Without lifting the finger, drag.
4. The console shows the uncaught TypeError.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -pl vaadin-context-menu-flow-parent/vaadin-context-menu-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1732`
- **Scaffold:** `vaadin-context-menu-flow-parent/vaadin-context-menu-flow-integration-tests/src/main/java/com/vaadin/flow/component/contextmenu/it/Repro1732View.java`

```java
Div target = new Div("Long-press here, then drag");
ContextMenu contextMenu = new ContextMenu(target);
contextMenu.addItem("Item 1");
contextMenu.addItem("Item 2");
```

The issue reports this with `GridContextMenu` (`grid.addContextMenu()`), but the failing code is the target-agnostic `vaadin-contextmenu` gesture recognizer, so a plain `ContextMenu` on a `Div` hits the same line. Touch was emulated with synthetic `TouchEvent`s replaying exactly what Android Chrome produces on long-press (`contextmenu` fires while the finger is down).

## Root cause (suspected)

This is a bug in the **web component layer** (`vaadin/web-components`), not in Flow. The gesture framework resets a recognizer whenever any of its `flow.start` events fires:

https://github.com/vaadin/web-components/blob/ff6dd62222c0f02ee9ee8b778122dfdc6306d2ae/packages/component-base/src/gestures.js#L308-L309

For the `vaadin-contextmenu` recognizer, `flow.start` includes `contextmenu` itself, so the `contextmenu` fired mid-long-press runs `reset()`, which nulls `touchStartCoords`:

https://github.com/vaadin/web-components/blob/ff6dd62222c0f02ee9ee8b778122dfdc6306d2ae/packages/context-menu/src/vaadin-contextmenu-event.js#L33-L38

The finger is still down, so the next `touchmove` dereferences the nulled coords without a guard:

https://github.com/vaadin/web-components/blob/ff6dd62222c0f02ee9ee8b778122dfdc6306d2ae/packages/context-menu/src/vaadin-contextmenu-event.js#L87-L96

A null guard in `touchmove` (bail out when `touchStartCoords` is null) would fix it. Reproducing/fixing in `vaadin/web-components` is out of scope here; the issue may be worth transferring there.

## Notes

- The error is uncaught but appears mostly cosmetic: the menu stays open and usable afterwards. Matches the issue's Severity: Minor label.
- The original report (2019) pointed at the Polymer-era `vaadin-context-menu` repo, line 71 of `vaadin-contextmenu-event.html`; the same unguarded dereference survived the migrations to the monorepo (`vaadin-contextmenu-event.js`).
- No extra dependencies were needed; console was clean apart from the reproduced error.
