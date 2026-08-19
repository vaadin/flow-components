> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that a menu item which is hidden at the moment the client-side items array is first generated permanently loses its sub menu, triggered by `setVisible(false)` before the first render at any nesting level followed by `setVisible(true)`, observable as the item rendering with `aria-haspopup="false"` and no `children` in the items array, so its sub menu never opens.
- **Regression?:** not a regression (broken since introduction) — `MenuItemsArrayGenerator` and `menubarConnector.js` are logically identical on `24.0`, `24.4`, `24.10` and `main`, and `_containerNodeId` dates back to the repository's initial commit.
- **Fixed by:** n/a — partially addressed by #5539, which restored the *button* for an initially hidden root item but not its sub menu
- **Duplicate of:** none found. #4984 (closed, fixed by #5539) is the root-item *button* half of the same root cause; its reproduction used items without sub menus, which is why the sub-menu half survived.
- **Branch:** `repro/1042` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ `main` (25.3-SNAPSHOT, `4f185a0690`)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** ![Sub 1 opens its sub-sub menu, Sub 2 does not](https://raw.githubusercontent.com/vaadin/flow-components/fcfc159a5449797f1263e66e6e95cf57030f6304/repro-1042.png)

## Observed behavior

Running the issue's own example: `Sub 2` and `Sub sub 2` start hidden, then "toggle 2" makes both visible again. `Sub 2` reappears in the menu, but without its sub menu — hovering it opens nothing.

Reading the generated items array and the rendered items after "toggle 2":

```
Root submenu contents:
  Sub 1  hidden=false  aria-haspopup="true"   _containerNodeId=51  item.children=1   <- control, opens "Sub sub 1"
  Sub 2  hidden=false  aria-haspopup="false"  _containerNodeId=54  item.children=NONE <- bug, opens nothing
```

`_containerNodeId` **is** present on `Sub 2` — the server did send it once the item became visible — but `item.children` was never rebuilt from it, so the web component sees a leaf item.

The same failure occurs one level up, for a root item that has a sub menu and starts hidden:

```
before setVisible(true)          : no button rendered
after  setVisible(true)          : aria-haspopup=null   item.children=NONE  _containerNodeId=38  submenu never opens
after a forced resetContent()    : aria-haspopup="true" item.children=2     _containerNodeId=62  submenu opens ["Sub 1","Sub 2"]
```

A control in the same view isolates the trigger. The identical root item, **visible** when the items array is first generated, keeps its sub menu across an arbitrary number of hide/show cycles:

```
initial          : aria-haspopup="true" item.children=2 submenu opens ["Sub 1","Sub 2"]
after hide + show: aria-haspopup="true" item.children=2 submenu opens ["Sub 1","Sub 2"]
```

So "hidden at first render", not "visibility toggled", is the trigger. Console was clean apart from dev-server noise (favicon 404, Lit dev-mode warning).

## Expected behavior

Making a previously hidden menu item visible should restore it with its sub menu intact, exactly as if it had been visible from the start.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1042`.
2. Click **toggle 2** — this makes `Sub 2` and `Sub sub 2` visible.
3. Click the **Root** button, then hover **Sub 1** — its sub menu opens with `Sub sub 1`.
4. Hover **Sub 2** — no sub menu opens, and the item has no submenu chevron.
5. Second variant: in the "Root item hidden initially" bar, click **toggle** and then the **Root hidden** button — the button appears but its sub menu never opens. Clicking **force reset** (which triggers `MenuBar.resetContent()`) restores it.

## Reproduction

How to run:

```sh
CI=true mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests \
  -pl vaadin-menu-bar-flow-parent/vaadin-menu-bar-flow-integration-tests
```

- **Route / page:** `http://localhost:8080/repro-1042`
- **Scaffold:** `vaadin-menu-bar-flow-parent/vaadin-menu-bar-flow-integration-tests/src/main/java/com/vaadin/flow/component/menubar/tests/Repro1042View.java`

```java
// The issue's own example
MenuBar menuBar = new MenuBar();
menuBar.addItem("Dummy");
MenuItem rootItem = menuBar.addItem("Root");

var subMenu = rootItem.getSubMenu();
MenuItem sub1 = subMenu.addItem("Sub 1");
MenuItem subSub1 = sub1.getSubMenu().addItem("Sub sub 1");

MenuItem sub2 = subMenu.addItem("Sub 2");
MenuItem subSub2 = sub2.getSubMenu().addItem("Sub sub 2");
sub2.setVisible(false);
subSub2.setVisible(false);

// clicking this restores Sub 2, but without its sub menu
NativeButton toggle2 = new NativeButton("toggle 2", event -> {
    subSub2.setVisible(!subSub2.isVisible());
    sub2.setVisible(subSub2.isVisible());
    rootItem.setVisible(sub1.isVisible() || sub2.isVisible());
});

// Root-level variant: same failure, one level up
MenuBar bar2 = new MenuBar();
MenuItem hiddenRoot = bar2.addItem("Root hidden");
hiddenRoot.getSubMenu().addItem("Sub 1");
hiddenRoot.getSubMenu().addItem("Sub 2");
hiddenRoot.setVisible(false);   // <- remove this line and everything works
```

## Root cause (suspected)

`MenuItemsArrayGenerator` publishes each parent item's sub-menu container as a **server-side element property** on the item, and the connector reads that property off the client element to attach `item.children`:

https://github.com/vaadin/flow-components/blob/4f185a0690a24cc72a17d0356d9468c8b2939a9d/vaadin-context-menu-flow-parent/vaadin-context-menu-flow/src/main/java/com/vaadin/flow/component/contextmenu/MenuItemsArrayGenerator.java#L73-L84

An invisible Flow node is only *partially* bound on the client — `ELEMENT_PROPERTIES` is not bound at all while the node is invisible:

https://github.com/vaadin/flow/blob/58e607b937ca67a52ee27b7db6ab859d775f9511/flow-client/src/main/java/com/vaadin/client/flow/binding/SimpleElementBindingStrategy.java#L230-L264

So when `$connector.generateItems(nodeId)` walks the tree, a hidden item has no `_containerNodeId` yet and lands in `__generatedItems` without `children`.

When the item later becomes visible, Flow rebinds and the property does arrive (confirmed: `_containerNodeId=54` / `38` above), but the only thing that reacts is the connector's `MutationObserver` on `hidden`/`disabled`, which calls `generateItems()` **without** a node id — a path that deliberately reuses the stale `__generatedItems` tree and never re-reads `_containerNodeId`:

https://github.com/vaadin/flow-components/blob/4f185a0690a24cc72a17d0356d9468c8b2939a9d/vaadin-menu-bar-flow-parent/vaadin-menu-bar-flow/src/main/resources/META-INF/frontend/menubarConnector.js#L29-L67

The missing `children` is therefore permanent until something else triggers a full `MenuBar.resetContent()` — which is exactly why the reporter's "add and remove a dummy item" workaround works, and why the forced reset in the reproduction restores the sub menu.

For nested items (this issue's own example) there is no client-side event at all: the `MutationObserver` only watches root items, so a sub-level visibility change reaches the client purely as Flow toggling the `hidden` attribute on the item element.

## Notes

- **The "visible → hidden → visible" toggle is not affected.** Verified as a control in the same view: an item that was visible when the items array was first generated keeps its `children` across hide/show cycles, because the tree object holding them is never rebuilt. Only items hidden at first render are broken. Reports phrased as "toggling visibility breaks the sub menu" should be checked for an initial `setVisible(false)`.
- **`ContextMenu` is affected the same way** — it shares `MenuItemsArrayGenerator`, and the nested case above is effectively the context-menu code path.
- A fix hook exists on the server: `MenuItemBase` already holds the `contentReset` callback, so `setVisible` could trigger a regeneration. Doing it unconditionally re-renders the whole bar on every toggle, which is the flicker the reporter complained about — narrowing it to "a parent item becoming visible whose children were never generated" would avoid that. A client-only fix is not sufficient, because nested items produce no connector-observable event.
- No dependencies were added to the integration-tests pom; the view uses only `Div`, `H3`, `NativeButton`, `MenuBar` and `MenuItem`.
