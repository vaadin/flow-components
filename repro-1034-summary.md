<!-- Edit any field. This file is committed on the `repro/1034` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced (still present; the underlying cause is in the `vaadin-overlay` outside-click logic, not in `CustomField`)
- **Hypothesis tested:** The bug is that a modal `Dialog` opened from a focus listener closes itself again, triggered by pressing and holding the mouse on the field so the server roundtrip opens the dialog before the mouse is released, observable as an `opened-change` event with `opened=false, fromClient=true` immediately after the server opened it.
- **Regression?:** not a regression (same behavior since the issue was filed in 2021)
- **Fixed by:** n/a — no fixing PR found
- **Duplicate of:** none found
- **Branch:** `repro/1034` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3.0-alpha, commit 18eefcbcbb), `@vaadin/overlay` 25.3.0-alpha9
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** ![Open/close/open sequence logged by the opened-change listener](https://raw.githubusercontent.com/vaadin/flow-components/c8622716670ae5e3dd6e564358a3a84bff20ce7d/repro-1034.png) — embeds inline.

## Observed behavior

Pressing and holding the mouse on the `TextArea` (about 900 ms, long enough for the focus roundtrip to complete) and then releasing it logs:

```
[custom-field opened=true  fromClient=false]   <- server opened the dialog on focus
[custom-field opened=false fromClient=true]    <- client closed it on the click that followed
[custom-field opened=true  fromClient=false]   <- focus returns to the field, listener opens it again
```

So the dialog visibly flashes: it opens, closes without any user intent, and then reopens because closing it returns focus to the field, which fires the focus listener again.

The same loop makes the dialog impossible to dismiss while the field keeps focus: pressing <kbd>Esc</kbd> logs `opened=false fromClient=true` followed immediately by `opened=true fromClient=false`.

Controls in the same view:

| Case | Result |
| --- | --- |
| `TextArea` inside a `CustomField` (reporter's example), press-and-hold | opens → closes (`fromClient=true`) → reopens |
| Plain `TextArea`, press-and-hold | identical — the `CustomField` wrapper is not involved |
| Plain `TextArea`, quick click | dialog opens and stays open (click happens before the roundtrip) |
| `setCloseOnOutsideClick(false)`, press-and-hold | dialog opens and stays open |
| Keyboard focus with <kbd>Tab</kbd> | dialog opens and stays open |

Console clean (only the Lit dev-mode warning).

## Expected behavior

A dialog opened from a focus listener stays open. A mouse gesture that started before the dialog existed should not count as an "outside click" that closes it.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1034`.
2. Press and hold the mouse button on the first `TextArea` for about a second, then release.
3. The dialog appears while the button is held, disappears on release, and appears again — see the `custom-field log` line.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1034`
- **Scaffold:** `vaadin-custom-field-flow-parent/vaadin-custom-field-flow-integration-tests/src/main/java/com/vaadin/flow/component/customfield/tests/Repro1034View.java`

```java
Dialog dialog = new Dialog();
dialog.add("Dialog opened from plain TextArea focus");
dialog.addOpenedChangeListener(e -> log.setText(log.getText() + " [opened="
        + e.isOpened() + " fromClient=" + e.isFromClient() + "]"));

TextArea textArea = new TextArea();
textArea.addFocusListener(e -> dialog.open());

add(textArea, log);
```

## Root cause (suspected)

The overlay registers its outside-click listeners only when it opens, and the listener closes the overlay for any `click` whose path is outside it — including a click whose `mousedown` happened before the overlay existed:

https://github.com/vaadin/web-components/blob/cb915ebde095ec5b94a87af93dd4530f51984c52/packages/overlay/src/vaadin-overlay-mixin.js#L258-L271

https://github.com/vaadin/web-components/blob/cb915ebde095ec5b94a87af93dd4530f51984c52/packages/overlay/src/vaadin-overlay-mixin.js#L529-L549

`_mouseDownInside` / `_mouseUpInside` only protect gestures that start or end inside the overlay. With a press-and-hold on the field, the `mousedown` is dispatched while the overlay is still closed (no listener attached yet), so on release the `click` reaches `_outsideClickListener` and closes the dialog. A guard for "the gesture started before this overlay opened" would fix it — this lives in `vaadin/web-components`, so a fix there is out of scope for this repo.

The Flow side only forwards the client state; `Dialog` opens on the focus roundtrip as asked, and the `opened=false fromClient=true` event proves the close comes from the client.

## Notes

- Reported originally on Firefox 85 / Chromium 87; still reproduces in current Chromium.
- The timing dependency described by @yuriy-fix in 2021 is confirmed: a quick click is faster than the focus roundtrip, so the dialog survives; a slower press reliably triggers the close.
- Workaround confirmed: `dialog.setCloseOnOutsideClick(false)` keeps the dialog open. Note that with the focus listener still attached, closing the dialog re-focuses the field and reopens it, so applications also need to guard against the reopen loop.
- IT pom edit on this branch: `vaadin-dialog-flow` added to the custom-field integration-tests module.
