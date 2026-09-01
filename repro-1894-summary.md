> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** partially reproduced — the original snippet is fixed, but a spurious `isFromClient() == true` event still occurs whenever the web component normalizes the value the server set
- **Hypothesis tested:** The bug is a value change event wrongly marked as client-originated, triggered by a server-side `setValue()` whose HTML the web component normalizes to different markup while the value change mode syncs `html-value-changed` directly (`EAGER`, `LAZY`, `TIMEOUT`), observable as a second value change event carrying the normalized value with `isFromClient() == true`.
- **Regression?:** not a regression (broken since the `htmlValue`-based implementation) — the *original* report (`asHtml().setValue("<p>Test</p>")`) was fixed before 24.3.9 and is covered by a regression test
- **Fixed by:** broad rework (no single PR) — the v24 move to `htmlValue` as the model value; `setValue`/`setAsHtmlValue` `isFromClient == false` is asserted in `RichTextEditorValueIT`
- **Duplicate of:** none found
- **Branch:** `repro/1894` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT), Flow 25.3-SNAPSHOT
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** ![Value change event logs per case](https://raw.githubusercontent.com/vaadin/flow-components/fc02502337f91ef4bb0ea87b494ba7b0658071e2/repro-1894.png) — embeds inline.
  In this run only the six mode-comparison cases were clicked, so the other cases show just their load-time event.
- **Demo video:** n/a (static failure)

## Observed behavior

Every case below is a separate `RichTextEditor` with a value change listener that appends
`#<n> value=<value> fromClient=<bool>` to a `Span`. The value is set once from a server-side
button click.

| Case | Value change mode | Server call | Event log observed in the browser |
| --- | --- | --- | --- |
| `plain-html` | `EAGER` | `setValue("<p>Test</p>")` | `#1 value=<p></p> fromClient=true` \| `#2 value=<p>Test</p> fromClient=false` |
| `bold-only` | `EAGER` | `setValue("<b>Test</b>")` | `#1 value=<p></p> fromClient=true` \| `#2 value=<b>Test</b> fromClient=false` \| **`#3 value=<p><strong>Test</strong></p> fromClient=true`** |
| `bare-text` | `EAGER` | `setValue("Test")` | `#1 value=<p></p> fromClient=true` \| `#2 value=Test fromClient=false` \| **`#3 value=<p>Test</p> fromClient=true`** |
| `ashtml-bare` | `EAGER` | `asHtml().setValue("Test")` | same as `bare-text` |
| `div-wrapped` | `EAGER` | `setValue("<div>Test</div>")` | `… #2 value=Test fromClient=false` \| **`#3 value=<p>Test</p> fromClient=true`** |
| `lazy-bold` | `LAZY` | `setValue("<b>Test</b>")` | same as `bold-only` — **reproduces** |
| `timeout-bold` | `TIMEOUT` | `setValue("<b>Test</b>")` | same as `bold-only` — **reproduces** |
| `onchange-bold` | `ON_CHANGE` (default) | `setValue("<b>Test</b>")` | `#1 value=<b>Test</b> fromClient=false` — no spurious event |
| `onblur-bold` | `ON_BLUR` | `setValue("<b>Test</b>")` | `#1 value=<b>Test</b> fromClient=false` — no spurious event |

Two distinct problems, both only in the modes that sync `html-value-changed` directly
(`EAGER`, `LAZY`, `TIMEOUT` — see `ValueChangeMode.eventForMode`):

1. **A spurious client-originated event on page load, before any user interaction:**
   `#1 value=<p></p> fromClient=true`. The editor starts empty, the server sets `""`, and the
   web component reports its normalized empty content `<p></p>` back as a client change.
2. **A spurious client-originated event after every server-side `setValue`** whose HTML the
   web component rewrites. `<b>Test</b>` is perfectly valid HTML, yet Quill normalizes it to
   `<p><strong>Test</strong></p>`, and that echo arrives as `fromClient=true`. This widens the
   scope of the 2024 comment on this issue, which assumed only malformed values ("you shouldn't
   be pushing broken values into the thing to begin with") were affected.

The practical impact of (2) is that a `Binder`-bound bean is written with the normalized HTML
and the write is indistinguishable from a real user edit — an application that ignores
server-originated changes (dirty tracking, autosave, undo stacks, change auditing) treats the
server's own value as user input.

Browser console during the whole run: 0 errors (only the usual Lit dev-mode warning).

## Expected behavior

A value change caused by a server-side `setValue()` should report `isFromClient() == false`,
including the follow-up event that carries the value the web component normalized it to. An
empty, untouched editor should not fire a client-originated value change event on page load.

## Steps to reproduce

1. Start the RTE integration-test server and open `http://localhost:8080/repro-1894`.
2. Read the `events:` log under the `bold-only` case — it already shows
   `#1 value=<p></p> fromClient=true` without any interaction.
3. Click the `setValue("<b>Test</b>")` button in that case.
4. Two more events appear: `#2 value=<b>Test</b> fromClient=false` (correct) followed by
   `#3 value=<p><strong>Test</strong></p> fromClient=true` (wrong — nothing was done on the client).
5. Compare with the `onchange-bold` case, which is identical except for the default
   `ON_CHANGE` mode: a single `fromClient=false` event.

## Reproduction

How to run: start the server and open the route below.

```sh
CI=true mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests \
  -pl vaadin-rich-text-editor-flow-parent/vaadin-rich-text-editor-flow-integration-tests
```

- **Route / page:** `http://localhost:8080/repro-1894`
- **Scaffold:** `vaadin-rich-text-editor-flow-parent/vaadin-rich-text-editor-flow-integration-tests/src/main/java/com/vaadin/flow/component/richtexteditor/tests/Repro1894View.java`

```java
// the minimal pair: the only difference is the value change mode
RichTextEditor failing = new RichTextEditor();
failing.setValueChangeMode(ValueChangeMode.EAGER);
failing.addValueChangeListener(e -> log(e.getValue(), e.isFromClient()));
add(failing, new NativeButton("set", e -> failing.setValue("<b>Test</b>")));
// -> #1 <p></p> true (on load), #2 <b>Test</b> false, #3 <p><strong>Test</strong></p> true

RichTextEditor control = new RichTextEditor(); // default ON_CHANGE
control.addValueChangeListener(e -> log(e.getValue(), e.isFromClient()));
add(control, new NativeButton("set", e -> control.setValue("<b>Test</b>")));
// -> #1 <b>Test</b> false
```

## Root cause (suspected)

`RichTextEditor` is an `AbstractSinglePropertyField` over the web component's read-only
`htmlValue` property, and `setValueChangeMode` registers `html-value-changed` as the
synchronizing DOM event for `EAGER`, `LAZY` and `TIMEOUT`:

https://github.com/vaadin/flow-components/blob/b706b06e761f9af5dfcd034714aa15d87a2c6bdb/vaadin-rich-text-editor-flow-parent/vaadin-rich-text-editor-flow/src/main/java/com/vaadin/flow/component/richtexteditor/RichTextEditor.java#L155-L160

Setting the value from the server writes the `htmlValue` property and then calls the web
component's `dangerouslySetHtmlValue` to actually apply it:

https://github.com/vaadin/flow-components/blob/b706b06e761f9af5dfcd034714aa15d87a2c6bdb/vaadin-rich-text-editor-flow-parent/vaadin-rich-text-editor-flow/src/main/java/com/vaadin/flow/component/richtexteditor/RichTextEditor.java#L208-L222

On the client, `dangerouslySetHtmlValue` converts the HTML through Quill's clipboard matchers —
which "may not produce the exactly input HTML" — and applies it with `SOURCE.API`:

https://github.com/vaadin/web-components/blob/88d6c5ffe5bfbadb0ae5a304cb67e8a0885f10fc/packages/rich-text-editor/src/vaadin-rich-text-editor-mixin.js#L782-L838

The resulting `text-change` updates the read-only `htmlValue` from Quill's own semantic HTML and
notifies `html-value-changed` — with no indication that the change originated from the server's
API call:

https://github.com/vaadin/web-components/blob/88d6c5ffe5bfbadb0ae5a304cb67e8a0885f10fc/packages/rich-text-editor/src/vaadin-rich-text-editor-mixin.js#L718-L731

Back on the server, that property sync is a client-to-server change, so
`AbstractSinglePropertyField` passes `event.isUserOriginated() == true` straight into
`setModelValue`, and the value differs from what the server set (it was normalized), so the
event is not suppressed as a no-op:

https://github.com/vaadin/flow/blob/58e607b937ca67a52ee27b7db6ab859d775f9511/flow-server/src/main/java/com/vaadin/flow/component/AbstractSinglePropertyField.java#L354-L358

The fix belongs in `RichTextEditor`: it already tracks a `pendingPresentationUpdate` flag around
the `dangerouslySetHtmlValue` call, which is the natural place to mark the echoed `htmlValue`
sync as server-originated rather than user-originated. Accepting the normalized value into the
server model is intentional (`RichTextEditorValueIT` documents "all values are only synced after
another roundtrip") — only the `fromClient` flag is wrong.

## Notes

- The original snippet in the issue (`asHtml().setValue("<p>Test</p>")`) now behaves correctly,
  matching the 2024 comment. It is covered by regression tests: `RichTextEditorValueIT`
  asserts `isFromClient == false` for `setValue`, `asHtml().setValue` and `asDelta().setValue`.
  Those tests all use HTML the web component normalizes to identical markup
  (`<h1>value</h1>`), which is why they do not catch this.
- No IT-module `pom.xml` change was needed — the scaffold only uses `RichTextEditor`,
  `Div`, `Span` and `NativeButton`.
- Value change modes were verified individually rather than assumed: `EAGER`, `LAZY` and
  `TIMEOUT` reproduce; `ON_CHANGE` (the component default) and `ON_BLUR` do not, because they
  sync on `change` / `blur` instead of `html-value-changed`.
- The load-time `<p></p>` `fromClient=true` event (problem 1) may be worth a separate issue —
  it needs no `setValue` call at all, just `EAGER`/`LAZY`/`TIMEOUT` mode.
