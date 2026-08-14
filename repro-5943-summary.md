<!-- Edit any field. This file is committed on the `repro/5943` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — duplicate of #1034 (same root cause in `vaadin-overlay`, different way of triggering the server roundtrip)
- **Hypothesis tested:** The bug is that a `Dialog` with `setCloseOnOutsideClick(true)` closes itself right after opening, triggered by committing a `TextField` value with a mouse click whose `mousedown` starts before the dialog exists and whose `mouseup` happens after it opened, observable as an `opened-change` event with `opened=false, fromClient=true` a few milliseconds after the server opened it.
- **Regression?:** not a regression — the same behavior was reported in #1034 in 2021 and still reproduces on `main`
- **Fixed by:** n/a — no fixing PR found
- **Duplicate of:** vaadin/flow-components#1034 (open)
- **Branch:** `repro/5943` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT, commit 17f7986ac0), `@vaadin/overlay` 25.3.0-alpha10
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshots:**

  ![Dialog is open while the mouse button is still held down on the second field](https://raw.githubusercontent.com/vaadin/flow-components/538887f480f5974b960c360a35df4d8d45caa6bf/repro-5943-1-while-held.png)

  ![After releasing the button the dialog is gone and the log shows opened=false fromClient=true](https://raw.githubusercontent.com/vaadin/flow-components/538887f480f5974b960c360a35df4d8d45caa6bf/repro-5943-2-after-release.png)

## Observed behavior

Typing into the first field and then clicking the second field with the mouse logs:

```
[1202ms] close-on-outside-click: value-change value=hello fromClient=true
[1202ms] close-on-outside-click: opened=true  fromClient=false   <- server opened the dialog
[1635ms] close-on-outside-click: opened=false fromClient=true    <- client closed it on mouse release
```

The dialog is visibly open while the mouse button is held and disappears the moment it is released. The close is caused by the overlay's outside-click handling: a listener added directly on `dialog.$.overlay` receives

```
vaadin-overlay-outside-click  sourceEvent.type=click  sourceEvent.target=body
_mouseDownInside=undefined    _mouseUpInside=false
```

`_mouseDownInside` is `undefined` because the overlay's `mousedown` listener did not exist yet when the button went down — the overlay only registers its global listeners when it opens.

The failure window is exactly the time between `mousedown` and `mouseup`:

| Case | Result |
| --- | --- |
| Click field 2, button held 0 ms (synthetic click) | dialog stays open |
| Click field 2, button held 50 / 100 / 200 / 400 ms | opens, then closes (`fromClient=true`) exactly on release |
| Click on empty page background, held 100 ms | opens, then closes |
| Commit the value with <kbd>Tab</kbd> | dialog stays open |
| `setCloseOnOutsideClick(false)`, held 100 ms | dialog stays open |
| Dialog opened from a button click, held 100 ms | dialog stays open |
| Server sleeps 300 ms before `open()`, button held 100 ms | dialog stays open (it opens after the release) |

The last two rows show the trigger is not "a click near an opening dialog" but specifically "a gesture whose `mousedown` happened while the dialog was still closed". A synthetic 0 ms click is faster than the roundtrip and does not reproduce it; a real click, which typically holds the button for 50–150 ms, does.

Console is clean apart from dev-server noise.

## Expected behavior

The dialog stays open. A mouse gesture that started before the dialog existed should not count as an outside click on it.

## Steps to reproduce

1. Open `http://localhost:8080/repro-5943`.
2. Type something into the first field of the `close-on-outside-click` row.
3. Click the second field with a normal mouse click (hold the button for about 100 ms).
4. The dialog appears while the button is down and disappears as soon as it is released — the log line ends with `opened=false fromClient=true`.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-5943` (optional `?delay=<ms>` sleeps on the server before opening the dialog)
- **Scaffold:** `vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests/src/main/java/com/vaadin/flow/component/dialog/tests/Repro5943View.java`

```java
TextField first = new TextField("Change value here");
TextField second = new TextField("Then click here");

first.addValueChangeListener(event -> {
    Dialog dialog = new Dialog(new Span("I'm a dialog"));
    dialog.setCloseOnOutsideClick(true);
    dialog.addOpenedChangeListener(e -> record(
            "opened=" + e.isOpened() + " fromClient=" + e.isFromClient()));
    dialog.open();
});

add(first, second);
```

## Root cause (suspected)

The overlay registers its `mousedown` / `mouseup` / capture-phase `click` listeners only when it opens:

https://github.com/vaadin/web-components/blob/1d2414c85ec60fbf1a270c6236a43f2dde368d49/packages/overlay/src/vaadin-overlay-mixin.js#L254-L267

`_outsideClickListener` then closes the overlay for any click outside it, guarded only by `_mouseDownInside` / `_mouseUpInside`:

https://github.com/vaadin/web-components/blob/1d2414c85ec60fbf1a270c6236a43f2dde368d49/packages/overlay/src/vaadin-overlay-mixin.js#L521-L546

When the dialog opens between `mousedown` and `mouseup`, the `mousedown` was never seen by the overlay, so `_mouseDownInside` is `undefined`, `_mouseUpInside` is `false`, and the `click` fired on release closes the dialog. A guard for "this gesture started before the overlay opened" — for example ignoring a click whose `mousedown` predates `_addGlobalListeners()` — would fix it.

The Flow side only forwards client state: `Dialog` opens on the value-change roundtrip as asked, and `opened=false fromClient=true` proves the close comes from the client. The fix belongs in `vaadin/web-components`, so it is out of scope for this repository.

## Duplicate

Same bug as **vaadin/flow-components#1034** (open): identical root cause and trigger — a server-side listener that fires on `mousedown` (focus there, value change here) opens the dialog before the mouse is released, and the resulting `click` is treated as an outside click. #1034 uses a focus listener on a `TextArea`, this one a value-change listener on a `TextField`; both need the mouse button to be held long enough for the roundtrip to finish, both are fixed by the same guard in `vaadin-overlay`, and both are avoided by `setCloseOnOutsideClick(false)`. Recommend closing this issue as a duplicate of #1034 and tracking the overlay fix there.

## Notes

- The workaround from the 2024 comment stopped working because `vaadin-dialog` now cancels the close by calling `preventDefault()` on `vaadin-overlay-outside-click` instead of reading `noCloseOnOutsideClick` at close time ([`_handleOutsideClick`](https://github.com/vaadin/web-components/blob/1d2414c85ec60fbf1a270c6236a43f2dde368d49/packages/dialog/src/vaadin-dialog-base-mixin.js#L193-L201)) — which matches the updated workaround posted by @mperktold.
- `vaadin-overlay-outside-click` is dispatched with `composed: false`, so it cannot be observed from a `document`-level listener; it has to be caught on `dialog.$.overlay`.
- IT pom edit on this branch: `vaadin-text-field-flow` added to the dialog integration-tests module.
- Not related to #2979 ("Dialog immediately closes when opened in view initialization"), which is caused by `vaadin-router` relocating the Flow root node.
