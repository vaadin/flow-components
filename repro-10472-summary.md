> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that the popover's `opened=false` never reaches the server, triggered by hiding a server-side ancestor of the `Popover` with `setVisible(false)` while it is open, observable as `isOpened()` staying `true` and the first target click only closing the (invisible) popover.
- **Regression?:** unknown (the Flow client logic involved is long-standing, so likely not a regression)
- **Fixed by:** n/a
- **Duplicate of:** none found. Related: #6433 (same "opened out of sync" family, via modality/inert instead of visibility)
- **Branch:** `repro/10472` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.4-SNAPSHOT), reported on 25.3.0
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![After showing the container again and clicking the target once: no popover, log shows isOpened()=true then opened=false](https://raw.githubusercontent.com/vaadin/flow-components/b96554a6a1f9d2097898d32d98ab6d171f4b1f93/repro-10472.png)

## Observed behavior

"inside" variant (popover is a child of the hidden container, as in the issue), 6 of 6 runs:

| Step | client `popover.opened` | server `isOpened()` | overlay visible |
| --- | --- | --- | --- |
| Click target | `true` | `true` | yes |
| Hide container from server | `true` | `true` | no |
| Show container from server | `true` | `true` | no |
| 1st target click | `false` | `false` (event `fromClient=true`) | no |
| 2nd target click | `true` | `true` | yes |

"outside" control (only the target is in the hidden container, the popover component stays visible): hiding fires `opened=false` to the server, and the first click after showing opens the popover.

Second control: hiding the same container with client-only `style.display = 'none'` (no server visibility change) also closes and syncs correctly.

Tracing the `opened` setter during the server-side hide shows a ping-pong. The overlay sets `opened=false` (target rect is 0×0). Then Flow client `updateProperty` → `setJsProperty` resets it to `true`. This repeats a few times and ends with `true`. Console has no errors.

## Expected behavior

When the overlay closes because its target became invisible, `opened` becomes `false` on both sides and `opened-changed` reaches the server. A single click or hover opens the popover again after the target is shown.

## Steps to reproduce

1. Open `http://localhost:8080/repro-10472`.
2. In "Variant: inside", click **target inside**. The popover opens.
3. Click **hide (server)** inside the popover. The container is hidden and the overlay disappears.
4. Click **show container (server)**, then **server isOpened()**. The log shows `isOpened()=true`, but no popover is visible.
5. Click **target inside**. Nothing opens, and the log shows `opened=false`.
6. Click **target inside** again. The popover opens.

## Reproduction

How to run: start the server (`mvn package jetty:run -am -DskipTests -pl vaadin-popover-flow-parent/vaadin-popover-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-10472`
- **Scaffold:** `vaadin-popover-flow-parent/vaadin-popover-flow-integration-tests/src/main/java/com/vaadin/flow/component/popover/tests/Repro10472View.java`

```java
Div container = new Div();
NativeButton target = new NativeButton("target");
Popover popover = new Popover();
popover.setTarget(target);
popover.setOpenOnClick(true);
popover.setOpenOnHover(true);
popover.add(new NativeButton("hide (server)", e -> container.setVisible(false)));
container.add(target, popover); // "outside" control: add popover outside container
add(container, new NativeButton("show", e -> container.setVisible(true)));
```

## Root cause (suspected)

1. Hiding the container via `display: none` makes the target rect 0×0. On the next position update, the overlay closes itself, and the popover sets `opened=false`:

https://github.com/vaadin/web-components/blob/cb9b2fcdf8fcc8ba4ce40dc1dadcb2aaafc72cd5/packages/overlay/src/vaadin-overlay-position-mixin.js#L278-L283

2. The Flow client sees that the popover's state node is inactive, because an ancestor is invisible. It does not send the sync. Instead it fires a fake change event that writes the old value `true` back into the element:

https://github.com/vaadin/flow/blob/6c4e0e3cb363afc41d638318878643959a1fd82b/flow-client/src/main/frontend/internal/client/flow/nodefeature/MapProperty.ts#L259-L273

https://github.com/vaadin/flow/blob/6c4e0e3cb363afc41d638318878643959a1fd82b/flow-client/src/main/frontend/internal/client/flow/StateTree.ts#L389-L396

The reset is by design for inactive nodes. Flow does not allow a client-driven property change on an invisible component. The server would drop the RPC anyway (`AbstractRpcInvocationHandler`, `node.isInactive()`). So both sides keep `opened=true` while nothing is rendered. The next click runs `__onTargetClick` with `opened=true` and closes:

https://github.com/vaadin/web-components/blob/cb9b2fcdf8fcc8ba4ce40dc1dadcb2aaafc72cd5/packages/popover/src/vaadin-popover.js#L709-L720

A fix likely belongs in the Flow `Popover`. For example, it could close the popover on the server when it becomes inactive. The Flow client contract for invisible nodes is intentional.

## Notes

- No IT pom changes were needed. The View uses only `Div`, `Span` and `NativeButton`.
- Workaround from the issue: call `popover.setOpened(false)` when the container is shown again. The "outside" control suggests a second workaround: add the `Popover` outside the container that gets hidden.
- #6433 is related but has a different mechanism. Modal Dialog makes the popover inert, and the server drops the sync. Here the client refuses to send it.
