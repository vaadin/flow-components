# Observing the bug in the browser

Core discipline: **never claim a bug reproduced until you have seen the signal in a running browser.** A git diff, an issue comment, or a listening port is not evidence.

## Drive the browser

```bash
playwright-cli open http://localhost:8080/repro-<issue>
playwright-cli snapshot
# ... click / type / press to follow the repro steps ...
playwright-cli console          # check for JS errors
playwright-cli screenshot --filename=/tmp/repro-<issue>.png   # for visual bugs
playwright-cli close
```

Prefer locators (`page.locator('#id')`) over coordinate clicks — locators pierce open shadow DOM, auto-wait, and auto-scroll; `page.mouse.click(x, y)` into nested shadow roots or overlays lands on the wrong element and yields garbage.

## run-code, eval, snapshots

- **`run-code` takes a function** `async (page) => {…}`; do DOM work inside `page.evaluate(() => {…})`. The outer scope has `page` only — no `document`/`window`/`setTimeout`; wait with `page.waitForTimeout(ms)`.
- The script is echoed back and `console.log` is **not** captured — keep it terse and `return` a JSON string of the measurements. `--raw eval "() => …"` is for one-liners only: nested quotes inside it can fail silently (empty output) — move anything non-trivial into a file and run `run-code --filename`.
- **`open <url>` once per session before any `run-code`** — a fresh CLI session errors *"browser is not open"*. `open` auto-prints a snapshot; otherwise navigate with `page.goto(...)` inside `run-code` and read values with `eval`. Snapshot only to discover structure/refs.

## Filter dev-server noise

Ignore dev-environment noise — a favicon 404 and dev-mode warnings always appear and belong to no bug. Don't cite them; if nothing else appears, say the console is clean.

## Inspect the real DOM before asserting

Components render into shadow DOM and slot into light DOM; "hidden" is rarely a `hidden` attribute — it may be a slot, a `part`, `display: none`, or a property. Don't guess a selector; dump the structure once, then build the check from what you see:

```bash
playwright-cli --raw eval "() => document.querySelector('vaadin-<component>').shadowRoot.innerHTML.replace(/\s+/g,' ').slice(0,800)"
```

Prefer the component's own state (a public/observed property, computed style, `offsetWidth > 0`) over the bare `hidden` attribute.

## General gotchas

- Prefer server-side assertion targets — a `Span`/`Div` with an `id` updated from listeners (value-change with `isFromClient`, click counters, data-provider query logs) — over sniffing client component state.
- After setting a property/attribute on a Lit-based component, wait a frame before asserting computed styles — a same-tick `getComputedStyle` read shows the stale style.
- `@PreserveOnRefresh` views: component state (e.g. an open dialog) persists across full navigations — `page.goto` is not a clean slate; reset with `Escape` or a fresh browser context before each trial.
- A client `TypeError` from `VAADIN/generated/jar-resources/*Connector.*` is a stale dev bundle, never a product bug — regenerate per [reproduce.md](reproduce.md).
