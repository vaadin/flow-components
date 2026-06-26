<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a TabSheet (or any wide content) inside a `Dialog` overflowing the viewport, triggered by opening the dialog on a narrow/mobile viewport, observable as the dialog content bleeding past the right screen edge instead of being clipped/scrolled inside the dialog.
- **Branch:** `repro/9615` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `25.3-SNAPSHOT` (pulls published `@vaadin/dialog` 25.2.x web components)
- **Present on main?:** yes (still broken on the 25.3 line)
- **Theme / Browser:** Lumo / Chromium (Playwright), viewport 390×844 (mobile)
- **Screenshot** (static bug): __IMAGE__

## Observed behavior

Opening a `Dialog` containing a `TabSheet` with 10 tabs on a 390px-wide viewport: the dialog's visible overlay box is correctly capped at the screen width, but its **content** and the **TabSheet** inside grow to their natural width and bleed ~300px past the right edge of the screen. Measured live in the running browser:

| Element | Width | Right edge |
| --- | --- | --- |
| Viewport | 390px | 390px |
| `[part='overlay']` (dialog box) | 358px | 374px ✅ within screen |
| `.resizer-container` | **676px** | 692px ❌ off-screen |
| `[part='content']` | **676px** | 692px ❌ off-screen |
| `vaadin-tabsheet` | **628px** | 668px ❌ off-screen |

So the overlay box itself respects the screen, but its content escapes it. The tab strip never falls back to its scroller and the tab content takes the same too-wide size. Console is clean (no bug-related errors).

## Expected behavior

A `TabSheet` (and any other content) inside a `Dialog` is limited by the dialog width, which is limited by the screen width. Overflowing tabs should scroll inside the tab strip; overflowing content should scroll inside the dialog content area — nothing should bleed past the screen edge.

## Steps to reproduce

1. Open `repro-9615` on a narrow (mobile) viewport, e.g. 390px wide.
2. Click the **TabSheet issue** button to open the dialog.
3. Observe the tab strip and content extending past the right edge of the screen.

## Reproduction

How to run: start the Flow IT server and open the route below.

- **Route / page:** `http://localhost:8080/repro-9615`
- **Scaffold:** `vaadin-tabs-flow-parent/vaadin-tabs-flow-integration-tests/src/main/java/com/vaadin/flow/component/tabs/tests/Repro9615View.java` (plus `vaadin-button-flow` / `vaadin-dialog-flow` deps added to the IT `pom.xml`)

```java
Button button = new Button("TabSheet issue");
button.addClickListener(e -> {
    Dialog dialog = new Dialog();
    TabSheet tabSheet = new TabSheet();
    for (int i = 0; i < 10; i++) {
        tabSheet.add(new Tab("Tab" + i),
            new Div("Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum "
                  + "Lorem Ipsum Lorem Ipsum Lorem Ipsum Lorem Ipsum"));
    }
    dialog.add(tabSheet);
    dialog.open();
});
add(button);
```

## Root cause (suspected)

The bug is in the web component dialog overlay styles (`@vaadin/dialog`), not in Flow. The resizable dialog overlay sets `[part='overlay'] { overflow: visible; }` and lays out an inner flex column (`.resizer-container` → `[part='content']`). The overlay box is correctly capped at `max-width: 100%`, but the inner flex items keep their default `min-width: auto`, so they refuse to shrink below their content's min-content width and grow to `max-content` (676px), overflowing the 358px overlay. Because the overlay has `overflow: visible`, the excess is not clipped or scrolled — it bleeds off-screen. Adding `min-width: 0` to `.resizer-container` and `[part='content']` snaps both back to 358px (verified live in the browser).

`[part='content']` already has `min-height: 0` but is missing the matching `min-width: 0`:

https://github.com/vaadin/web-components/blob/2ce57738e7d11eb4ec801f774533f4032c6c5f7d/packages/dialog/src/styles/vaadin-dialog-overlay-base-styles.js#L170-L200

## Notes

- This is a `flow-components` issue but the root cause lives in the shared `@vaadin/dialog` web component; a plain `vaadin-dialog` + `vaadin-tabsheet` page would reproduce it too.
- IT `pom.xml` gained `vaadin-button-flow` and `vaadin-dialog-flow` dependencies (committed on the branch so it is runnable).
- The reporter on the original issue was asked for a screenshot; the screenshot above is from this reproduction.
