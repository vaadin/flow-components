<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a modeless dialog failing to regain focus, triggered by clicking any drag region (header bar, title text) of a `draggable` dialog — the draggable mixin's `preventDefault()` on `mousedown` blocks the browser's native focus change — observable as `document.activeElement` staying on the background element and Esc no longer closing the dialog.
- **Regression?:** unknown — likely not a regression: the focus-blocking `preventDefault()` has been in the draggable mixin since before the 2021 monorepo move (not verified on 24.x)
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/9815` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (Flow 25.3-SNAPSHOT, web-components 25.3 snapshots)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** default / Chromium (playwright-cli)
- **Screenshot:** ![Draggable modeless dialog still open after header click + Esc, focus on background input](https://raw.githubusercontent.com/vaadin/flow-components/6d57cd0d0f89820992d18f8844f82584a85f6dbf/repro-9815.png)

## Observed behavior

Trial matrix (click background input first, then click the given target, then press Esc; dialog open/close observed via a server-side `openedChangeListener`):

| dialog | click target | focus after click | Esc closes |
| --- | --- | --- | --- |
| **draggable** | title text | stays on `input#bg-input` | **no** |
| **draggable** | empty header bar | stays on `input#bg-input` | **no** |
| draggable | content div | `vaadin-dialog` | yes |
| draggable | input in content | the input | yes |
| plain (non-draggable) | title text | `vaadin-dialog` | yes |
| plain (non-draggable) | empty header bar | `vaadin-dialog` | yes |
| plain (non-draggable) | content div | `vaadin-dialog` | yes |
| plain (non-draggable) | input in content | the input | yes |

`setDraggable(true)` is the trigger: with it, header clicks leave focus on the background element and Esc does nothing; without it, every click on the dialog (including the header) moves focus back to the `<vaadin-dialog>` host (which has `tabindex="0"`) and Esc closes it. Console shows no errors.

## Expected behavior

Clicking anywhere on a non-modal dialog — including its header — should bring the dialog back into focus and re-enable keyboard shortcuts like closing via Esc.

## Steps to reproduce

1. Open a modeless (`setModal(false)`), draggable (`setDraggable(true)`) dialog with a header title.
2. Click the background — focus moves out of the dialog.
3. Click the dialog's header (title text or empty header bar).
4. Press Esc — the dialog stays open; focus is still on the background element.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-9815`
- **Scaffold:** `vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests/src/main/java/com/vaadin/flow/component/dialog/tests/Repro9815View.java`

```java
private Dialog createDialog(boolean draggable, String key) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Title " + key);
    dialog.setDraggable(draggable); // true = broken, false = works
    dialog.setModal(false);
    dialog.setCloseOnEsc(true);
    dialog.setCloseOnOutsideClick(false);
    // content Div + input + footer close button ...
    return dialog;
}
```

## Root cause (suspected)

The bug is in **vaadin/web-components** (reproducing there is out of scope for this report). Chain:

1. `DialogDraggableMixin._startDrag` calls `e.preventDefault()` on `mousedown` over any drag region, which cancels the browser's native focus-on-mousedown, and nothing refocuses the dialog:

https://github.com/vaadin/web-components/blob/0b0c481b12917fb551cddf9cd90d770a55c1f90d/packages/dialog/src/vaadin-dialog-draggable-mixin.js#L86-L88

2. The whole header is a drag region: the header title renders as `<h2 class="draggable">` (an explicit drag handle), and the header bar itself has `pointer-events: none`, so clicks on it hit the resizer container (`isResizerContainer`):

https://github.com/vaadin/web-components/blob/0b0c481b12917fb551cddf9cd90d770a55c1f90d/packages/dialog/src/vaadin-dialog-overlay-mixin.js#L204-L206

https://github.com/vaadin/web-components/blob/0b0c481b12917fb551cddf9cd90d770a55c1f90d/packages/dialog/src/styles/vaadin-dialog-overlay-base-styles.js#L47-L57

3. With focus left outside the dialog, the overlay ignores Esc — a modeless overlay only closes on Esc when the keydown's composed path includes the dialog:

https://github.com/vaadin/web-components/blob/0b0c481b12917fb551cddf9cd90d770a55c1f90d/packages/overlay/src/vaadin-overlay-mixin.js#L568-L572

A fix would likely be for `_startDrag` to move focus to the dialog (e.g. focus the `<vaadin-dialog>` host, which has `tabindex="0"`) when it prevents the mousedown default.

## Notes

- Clicking the dialog's *content* works even when draggable, because slotted content has `pointer-events: auto` and is not a drag region — so the reporter's "clicking the main content area restores focus" observation matches: their content is filled with fields.
- The overlay's existing refocus fallback ([vaadin-overlay-mixin.js#L136-L142](https://github.com/vaadin/web-components/blob/0b0c481b12917fb551cddf9cd90d770a55c1f90d/packages/overlay/src/vaadin-overlay-mixin.js#L136-L142), added for flow-components#5507) cannot help here: it requires `document.activeElement === document.body` (focus is on the background element instead) and `tabindex="0"` on the overlay part (moved to the dialog host by vaadin/web-components#10024 in 25.1).
- Related but distinct: vaadin/web-components#7849 (header/footer `pointer-events` allow interaction under a modal overlay) — same header pointer-events design, different symptom.
- No extra dependencies were added to the IT module (repro uses `Div`/`Span`/`Input`/`NativeButton` only).
