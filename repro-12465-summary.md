> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced (click-through confirmed; narrower than reported — see Notes)
- **Hypothesis tested:** The bug is that opening a modal overlay (Select) inside a modeless Dialog disables pointer events on *every* other overlay including the dialog that hosts it, triggered by two overlapping modeless dialogs where the front one opens its Select overlay, observable as a click on the front dialog activating a control of the dialog behind it.
- **Regression?:** not a regression — identical behavior on 25.1.11 (reported), 25.2 and main (25.3-SNAPSHOT)
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/12465` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT), `25.2`, and `25.1` (platform 25.1.11 — the reported version)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** ![Select 2 overlay open, "Footer 1" of the dialog behind hidden under Dialog 2](https://raw.githubusercontent.com/vaadin/flow-components/6fe0fa0072c498650844174eb16474297d7d000a/repro-12465-before.png)
- **Screenshot:** ![After clicking that same spot: the hidden "Footer 1" button fired and Dialog 1 was raised](https://raw.githubusercontent.com/vaadin/flow-components/6fe0fa0072c498650844174eb16474297d7d000a/repro-12465.png)

## Observed behavior

Two overlapping modeless dialogs, the front one (`Dialog 2`) partly covering the back one (`Dialog 1`).
While the front dialog's `Select` overlay is open, a click at a point that lies **inside the front dialog** and over a
hidden control of the dialog behind activates that hidden control:

- `document.elementFromPoint(407, 284)` returns `BUTTON#footer-button-1` — a button of the **hidden** dialog behind — even though the point is inside `Dialog 2`'s box (`x 391–751, y 268–494`).
- The click produces the server-side log entry `footer-button-1 clicked`.
- The same click **before** opening the Select overlay does nothing (the front dialog blocks it) — control case in the same run.
- As a side effect, the hidden dialog is brought to the front by the click that was never meant for it.

Measured pointer-events state (main), same run, before → after opening the Select overlay:

| Element | Select overlay closed | Select overlay open |
| --- | --- | --- |
| `document.body` | `` | `none` |
| `Dialog 1` `[part="overlay"]` | `auto` | `none` (inline) |
| `Dialog 2` `[part="overlay"]` (frontmost) | `auto` | `none` (inline) |
| `Dialog 1` title `<h2 slot="title">` | `auto` | `auto` |
| `Dialog 1` footer button | `auto` | `auto` |
| `Dialog 1` content (`Select`, buttons) | `auto` | `none` |

So the frontmost dialog stops blocking pointer events, while the title / header-content / footer of the dialog
behind stay hit-testable and receive the click.

Console: clean (only the dev-mode Lit warning and a favicon 404).

## Expected behavior

Clicks on the frontmost dialog must not fall through to a dialog behind it, whether or not an overlay of the
frontmost dialog is open.

## Steps to reproduce

1. Open `http://localhost:8080/repro-12465` and press **Open dialog**, then **Open nested dialog** inside it.
2. Drag the front dialog (`Dialog 2`) by its header so that it covers the **Footer 1** button of `Dialog 1` — that button must be hidden behind the front dialog.
3. Click that spot once: nothing happens (correct — the front dialog blocks the click).
4. Open the **Select 2** overlay in the front dialog.
5. Click the same spot again, outside the Select overlay but inside the front dialog.
6. The log at the top of the page shows `footer-button-1 clicked` — the hidden button of the dialog behind was pressed, and `Dialog 1` is raised above `Dialog 2`.

## Reproduction

How to run: `mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests`, then open the route below.

- **Route / page:** `http://localhost:8080/repro-12465`
- **Scaffold:** `vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests/src/main/java/com/vaadin/flow/component/dialog/tests/Repro12465View.java`

```java
private void openDialog(Dialog parent) {
    int index = ++dialogCount;

    Dialog dialog = new Dialog();
    dialog.setId("dialog-" + index);
    dialog.setHeaderTitle("Dialog " + index);
    dialog.setDraggable(true);
    dialog.setModality(ModalityMode.MODELESS);
    dialog.setWidth("360px");
    dialog.getElement().getStyle().set("--vaadin-dialog-padding", "48px");
    dialog.setTop((60 + (index - 1) * 260) + "px");
    dialog.setLeft((80 + (index - 1) * 40) + "px");

    Select<String> select = new Select<>();
    select.setId("select-" + index);
    select.setLabel("Select " + index);
    select.setItems("1", "2", "3");

    NativeButton openNested = new NativeButton("Open nested dialog",
            e -> openDialog(dialog));
    // ... "Button N" and "Close" ...

    NativeButton footerButton = new NativeButton("Footer " + index,
            e -> logEvent("footer-button-" + index + " clicked"));
    footerButton.setId("footer-button-" + index);
    dialog.getFooter().add(footerButton);

    dialog.add(new Div(select), new Div(openNested, marker, close));
    if (parent != null) {
        parent.add(dialog); // as in the reported example
    }
    dialog.open();
}
```

## Root cause (suspected)

This is a web-component level issue, not a Flow one — the Flow `Dialog` only sets `modeless` and renders the content.

When the `Select` overlay opens it is modal, so `OverlayStackMixin._enterModalState()` sets
`pointer-events: none` on `document.body` **and on the `[part="overlay"]` of every other attached overlay** — including
the dialog that contains the Select:

https://github.com/vaadin/web-components/blob/8c979ffaea81a928ad12a7598141ab4fb9a91877/packages/overlay/src/vaadin-overlay-stack-mixin.js#L101-L115

Elements slotted into a dialog overlay are opted back in by the dialog styles, so they keep receiving pointer events
while the dialog box around them no longer blocks anything:

https://github.com/vaadin/web-components/blob/8c979ffaea81a928ad12a7598141ab4fb9a91877/packages/dialog/src/styles/vaadin-dialog-overlay-base-styles.js#L59-L61

The combination is what produces the fall-through: the frontmost dialog becomes transparent to the pointer, and the
title / header-content / footer of the dialog underneath are still `pointer-events: auto`, so they take the click.

## Notes

- **Scope difference from the report:** what reproduces is a click-through onto elements slotted **directly** into the dialog behind — its title (which is also the drag handle, so the hidden dialog can be dragged), header content, and footer. Content added with `dialog.add(...)` goes through `<vaadin-dialog-content>` and inherits `pointer-events: none` in this state, so a `Select` or a button placed in the dialog **body** did not react in this reproduction on any of 25.1.11, 25.2 or main. The reported video shows a hidden `Select` in the dialog body reacting; that specific part could not be reproduced — the same click was inert in all attempted geometries.
- Verified with a control in the same run: with the Select overlay closed, the identical click is swallowed by the front dialog.
- Dragging the hidden dialog through the front one also works: `mousedown` on the back dialog's title (covered by the front dialog) followed by a mouse move moves the back dialog, while the front dialog stays put and the Select overlay stays open.
- The IT module `pom.xml` gained a `vaadin-select-flow` dependency so the reproduction view can use `Select`.
