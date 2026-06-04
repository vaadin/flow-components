# Reproduction: MenuBar — checkable MenuItem doesn't toggle via addClickShortcut() (#8085)

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Issue:** https://github.com/vaadin/flow-components/issues/8085
- **Verdict:** reproduced.
- **Hypothesis tested:** invoking the click-shortcut on a checkable `MenuItem` fires the click listener but leaves `isChecked()` unchanged (no toggle), whereas a real mouse click toggles **then** fires.
- **Branch:** `repro/8085` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `24.10` (`24.10-SNAPSHOT`) and `main` (`25.3-SNAPSHOT`); the report names 24.8.
- **Present on main?:** yes — confirmed on `main` (`25.3-SNAPSHOT`): a mouse click on the item toggles (`checked = true`), while the shortcut fires the listener without toggling (`checked` stays the same). The root-cause wiring in `MenuItemBase` (toggle bound to a DOM `click` listener that `addClickShortcut` does not dispatch) is unchanged between `24.10` and `main`.
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Demo video (on this branch):** `repro-8085-shortcut-no-toggle.webm`. Drag into the comment for inline playback.

## Observed behavior

A checkable sub-menu item whose click listener reports `e.getSource().isChecked()`. Measured live on 24.10:

```
shortcut Ctrl+Shift+Alt+T (item starts unchecked):  "checked = false"  → repeat → "checked = false"   (never toggles)
mouse: open Menu → click "Toggle":                  "checked = true"                                   (toggles + fires)
shortcut again (item now checked):                  "checked = true"   → repeat → "checked = true"     (reports current, no toggle)
```

The shortcut always fires the click listener but the checked state never flips — it just reports the current value. A real mouse click toggles the checkmark and the state. Exactly the report.

## Expected behavior

The click-shortcut should behave like a real click: toggle the checked state, **then** invoke the click listener (so `isChecked()` reflects the new state).

## Steps to reproduce

1. `MenuBar` with a **sub-menu** checkable item (root items can't be checkable), a click listener that reads `isChecked()`, and `addClickShortcut(Key.KEY_T, CONTROL, SHIFT, ALT)`.
2. Press the shortcut → the listener fires but `isChecked()` does not change (no toggle).
3. Open the menu and click the item with the mouse → it toggles and fires.
4. Press the shortcut again → still no toggle; it only reports the current state.

## Reproduction

Route `repro-8085` · `vaadin-menu-bar-flow-parent/vaadin-menu-bar-flow-integration-tests/src/main/java/com/vaadin/flow/component/menubar/tests/Repro8085View.java`

```java
@Route("repro-8085")
public class Repro8085View extends Div {
    public Repro8085View() {
        Div label = new Div("checked = (initial)");
        label.setId("label");
        MenuBar menuBar = new MenuBar();
        MenuItem root = menuBar.addItem("Menu");
        MenuItem checkable = root.getSubMenu().addItem("Toggle",
                e -> label.setText("checked = " + e.getSource().isChecked()));
        checkable.setCheckable(true);
        checkable.addClickShortcut(Key.KEY_T, KeyModifier.CONTROL, KeyModifier.SHIFT, KeyModifier.ALT);
        add(menuBar, label);
    }
}
```

(The issue's literal snippet makes a **root** item checkable, which throws `UnsupportedOperationException: A root level item in a MenuBar can not be checkable`; checkable items must live in a sub-menu, so the reproduction uses a sub-menu item.)

## Root cause (suspected)

`vaadin-context-menu-flow/src/main/java/com/vaadin/flow/component/contextmenu/MenuItemBase.java` (constructor, ~lines 72-77):

```java
getElement().addEventListener("click", e -> {
    if (checkable) {
        setChecked(!isChecked());
    }
});
```

The checked-toggle is bound to a server-side **DOM `click`** event listener. A mouse click dispatches that DOM event, so the toggle runs before the user's click listener. `addClickShortcut(...)` triggers the component's click through a path that does **not** dispatch this DOM `click` event, so the toggle is skipped while the user's click listener still fires — hence `isChecked()` never changes via the shortcut. A fix would route the shortcut through the same toggle (or toggle in the click-event path the shortcut uses). This wiring is unchanged on `main`.

## Notes

- Build needed `CI=true` (no-TTY pnpm) and `mvn clean` on the IT module before a fresh 24.10 build.
- Not a duplicate — issue search for the same symptom returned nothing.
