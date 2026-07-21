<!-- Edit any field. This file is committed on the `repro/12201` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a `ContextMenu` opening on a short tap, triggered by `autoselect=true` selecting the field text on touch focus (which makes Chromium fire a native `contextmenu` event), observable as the menu overlay opening without any long press.
- **Regression?:** unknown (reporter lists 25.2.3 and 24.9.7, both current lines; no older working version given)
- **Fixed by:** n/a
- **Duplicate of:** none found
- **Branch:** `repro/12201`
- **Reproduced on:** flow-components @ main (Vaadin 25.3.0-alpha5); underlying web components `@vaadin/context-menu` + `@vaadin/text-field`
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chrome (Pixel 7 mobile touch emulation, `hasTouch`+`isMobile`)
- **Screenshot:** ![short tap on the autoselect field opens the menu (Item1)](https://raw.githubusercontent.com/vaadin/flow-components/repro/12201/repro-12201.png)

## Observed behavior

Using a real mobile touch context (Pixel 7 emulation, Chrome), a **single short tap** on the `autoselect=true` field:

1. fires a native `contextmenu` event on the `<input>` (Chromium dispatches it because the touch focus selects the text), then
2. fires the `vaadin-contextmenu` gesture event, and
3. the `ContextMenu` reaches `opened=true` and its overlay ("Item1") shows.

The control field with `autoselect=false` receives the same short tap and fires **no** events — the menu stays closed. Instrumented event log:

```
AUTOSELECT short tap: events = ["native-contextmenu on input","vaadin-contextmenu gesture on input"]  -> autoselect-menu opened=true
PLAIN control short tap: events = []                                                                   -> plain-menu   opened=false
```

## Expected behavior

On touch devices the context menu should open only on a long press, not on a short tap that merely focuses the field.

## Steps to reproduce

1. Open the route on a touch device or with Chrome touch/mobile emulation (`hasTouch`).
2. Short-tap the `autoselect=true` field to focus it (no long press).
3. The context menu overlay opens.

## Reproduction

How to run: start the server and open the route below. To observe it, drive the page with a mobile touch context (see Notes) — a normal desktop mouse click does not trigger it.

- **Route / page:** `http://localhost:8080/repro-12201`
- **Scaffold:** `vaadin-text-field-flow-parent/vaadin-text-field-flow-integration-tests/src/main/java/com/vaadin/flow/component/textfield/tests/Repro12201View.java`

```java
// Failing case: autoselect = true
TextField autoselectField = new TextField();
autoselectField.setAutoselect(true);
autoselectField.setValue("AAA");
ContextMenu autoselectMenu = new ContextMenu();
autoselectMenu.setTarget(autoselectField);
autoselectMenu.addItem("Item1", e -> {});

// Control case: autoselect = false (default) -> menu does NOT open on a short tap
TextField plainField = new TextField();
plainField.setValue("BBB");
ContextMenu plainMenu = new ContextMenu();
plainMenu.setTarget(plainField);
plainMenu.addItem("Item1", e -> {});
```

## Root cause (suspected)

This is a web-component behavior, not a Flow-layer one — the Flow classes only wire `ContextMenu.setTarget(textField)`.

The `vaadin-contextmenu` gesture translates **every** native `contextmenu` event into an open request, gated only on `!e.shiftKey`. It does not distinguish a genuine long-press / right-click from a `contextmenu` that Chromium fires as a side effect of touch text-selection. Because `autoselect` selects the field text on touch focus, a short tap produces a native `contextmenu`, which this handler forwards straight to opening the menu:

https://github.com/vaadin/web-components/blob/ac126286bb7884ce91c370506ae95b61801e887a/packages/context-menu/src/vaadin-contextmenu-event.js#L94-L113

The touch long-press path in the same file (the 500 ms timer in `touchstart`) only self-fires on iOS; on Android/Windows it relies on the browser's native `contextmenu`, which is exactly what the autoselect-driven selection triggers early.

## Notes

- Added `vaadin-context-menu-flow` as a dependency to the text-field IT module `pom.xml` so the cross-component scaffold compiles — kept on the `repro/12201` branch.
- The bug needs a touch context. It was reproduced with Playwright launching Chrome under a `Pixel 7` device descriptor (`hasTouch: true`, `isMobile: true`); a desktop mouse click does not trigger it. Reproduction driver script kept in the session scratchpad.
- Issue is filed in `vaadin/web-components`, which matches the reproducing layer.
