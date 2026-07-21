<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that a `ClickEvent` inside a `Dialog` bubbles to the `Dialog`'s parent `Button`, triggered by appending the dialog's element to a button and clicking inside the open dialog, observable as the button's click listener firing again (opening another dialog).
- **Regression?:** worked in 24.x / broke in 25.0 — nested/attached overlays are no longer teleported to `<body>`
- **Fixed by:** n/a (open; confirmed by maintainer as a known side effect of the Vaadin 25 overlay refactor)
- **Duplicate of:** none found (issue #24907 considered and dismissed by the reporter — different mechanism)
- **Branch:** `repro/24974` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ main (25.3.0-alpha5)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** ![Clicking inside Dialog 1 opened Dialog 2; counters at 2](https://raw.githubusercontent.com/vaadin/flow-components/da6c2c7bb2ff76d829fbeac69f17934354c66b6c/repro-24974.png) — embeds inline.

## Observed behavior

Clicking the "First Dialog" button once opens a modeless `Dialog` whose element is appended as a child of the button. A single click **inside** that open dialog (on the `Click me` span) re-fires the button's server-side click listener: the counters advance from `Button listener fired: 1` / `Dialogs opened: 1` to `2` / `2`, and a second dialog ("Dialog 2") opens.

DOM confirms the cause — the `<vaadin-dialog>` and its slotted content are a **direct child of `<vaadin-button>`**:

```
<vaadin-button id="open-button" ...>First Dialog
  <vaadin-dialog opened modeless ... aria-label="Dialog 1">
    <span id="dialog-body">Click me (Dialog 1)</span>
    ...
  </vaadin-dialog>
  <vaadin-dialog opened modeless ... aria-label="Dialog 2">...</vaadin-dialog>
</vaadin-button>
```

A click on `#dialog-body` bubbles up through `<vaadin-dialog>` to `<vaadin-button>`, so the button's click listener runs. Console is clean (only favicon 404 + Lit dev-mode warning).

## Expected behavior

Clicking inside a dialog should not trigger the click listener of the component the dialog element happens to be attached to. In Vaadin 24 the overlay was teleported to `<body>`, so the click never reached the button.

## Steps to reproduce

1. Open `http://localhost:8080/repro-24974`.
2. Click the **First Dialog** button — a modeless dialog opens (counters show `1` / `1`).
3. Click the **Click me** text inside the open dialog.
4. Observe: counters jump to `2` / `2` and a second dialog opens, although the button was never clicked directly a second time.

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-24974`
- **Scaffold:** `vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests/src/main/java/com/vaadin/flow/component/dialog/tests/Repro24974View.java`

```java
Button button = new Button("First Dialog", e -> {
    buttonClicks++;
    clickCount.setText("Button listener fired: " + buttonClicks);
    openDialog(e.getSource(), "Dialog " + buttonClicks, openCount);
});

private void openDialog(Component parent, String title, Span openCount) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle(title);
    dialog.setModality(ModalityMode.MODELESS);
    dialog.setDraggable(true);
    Span body = new Span("Click me (" + title + ")");
    body.setId("dialog-body");
    dialog.add(body);
    parent.getElement().appendChild(dialog.getElement()); // dialog becomes a child of the button
    dialog.addOpenedChangeListener(e -> {
        if (!e.isOpened()) {
            dialog.removeFromParent();
        }
    });
    dialog.open();
}
```

## Root cause (suspected)

This is the underlying **web component** behavior, not a defect in flow-components Java code. In Vaadin 25 the overlay is no longer teleported to `<body>`; `<vaadin-dialog>` renders its content in place, so when the dialog element is attached to another component (here a button) the dialog content becomes a DOM descendant of that component. Bubbling DOM events (`click`, `focusin`, keyboard/shortcut events) therefore propagate through the parent. Reproducing/fixing in `vaadin/web-components` is out of scope for this skill.

Related overlay stacking code the maintainer referenced:

https://github.com/vaadin/web-components/blob/767ecf18d678acc4cc7c813ed38478a703a91a5c/packages/overlay/src/vaadin-overlay-stack-mixin.js#L54

## Notes

- Confirmed by maintainer (@web-padawan): "Event bubbling is a known side effect of the overlay refactorization in Vaadin 25 since overlays are no longer teleported." The web component should not call `stopPropagation()`; `event.composedPath()` is the suggested way to identify the owning component.
- The issue also describes the same mechanism for `Shortcuts` (Ctrl+Enter closing a parent dialog while a nested dialog is focused) and for `focusin`/`focusout`. The scaffold reproduces the button/click case from the **last comment**, which is the most self-contained trigger.
- No pom edits were needed — the dialog IT module already depends on `vaadin-button-flow` and `vaadin-ordered-layout-flow`.
