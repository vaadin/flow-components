<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a spurious **client-initiated** `value-change` carrying an empty value, triggered by `ui.push()` called before `setValue()` (followed by a second `ui.push()`) on a MultiSelectComboBox with push enabled, observable as the server value resetting to empty while the client still displays the selected chip.
- **Branch:** [`repro/9611`](https://github.com/vaadin/flow-components/tree/repro/9611) — pushed to `vaadin/flow-components`
- **Reproduced on:** `vaadin/flow-components` @ tag `25.1.8` (bundles web components `@vaadin/*` 25.1.4)
- **Present on main?:** yes (still broken) — also reproduced on `main` (25.3-SNAPSHOT, web components 25.2.0) with the same harness
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot:** the combo box shows chip "1" while the log shows the spurious empty client value-change:

![MultiSelectComboBox shows chip "1" while the log records a spurious empty client value-change](https://raw.githubusercontent.com/vaadin/flow-components/91e6f6d9a2361f857166a0aa36f98714f128819f/repro-9611.png)

## Observed behavior

After clicking **Run repro** (which runs the reporter's exact sequence in a live server round-trip), the value-change log shows two events:

```
value-change | old: [] | new: [1] | getValue(): [1]
value-change from client | old: [1] | new: [] | getValue(): []
```

The second event is **client-initiated** and carries an empty value. The server-side value (`getValue()`) becomes empty `[]`, yet the combo box still **visually displays the chip "1"** — so the server and client are out of sync. This matches the reporter's reported output exactly.

Instrumenting the page to capture the native web-component events during the same sequence shows the component itself emitting a transient empty selection (all programmatic, `isTrusted=false`) before settling on the value:

```
value-changed          selectedItems=[]
selected-items-changed selectedItems=[]   ← spurious empty, synced to the server
selected-items-changed selectedItems=[1]
```

So the spurious empty `value-change` originates in the **web component**, not in Flow — Flow only relays it.

## Expected behavior

No spurious value-change event should fire. After `ui.push()`, the combo box should keep its value (`"1"`) on both server and client.

## Steps to reproduce

1. Enable push on the UI (`@Push` / `AUTOMATIC`).
2. In a live server round-trip (here, a button click), create a `MultiSelectComboBox`, `setItems(List.of("1","2","3"))`, add it to the layout.
3. Call `ui.push()` **before** setting a value.
4. Call `setValue(Set.of("1"))`, then call `ui.push()` again.
5. Observe a spurious client-initiated `value-change` with an empty value firing after the second push; the server value is now empty while the client still shows the chip.

## Reproduction

How to run: from `flow-components`, start the integration-test server and open the route.

```bash
CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -DskipTests \
  -pl vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests
```

- **Route / page:** `http://localhost:8080/repro-9611` (then click **Run repro**)
- **Scaffold:** `vaadin-combo-box-flow-parent/vaadin-combo-box-flow-integration-tests/src/main/java/com/vaadin/flow/component/combobox/test/Repro9611View.java`

```java
@Route("repro-9611")
public class Repro9611View extends Div {
    public Repro9611View() {
        UI ui = UI.getCurrentOrThrow();
        ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

        Div log = new Div();
        log.setId("log");

        NativeButton run = new NativeButton("Run repro", e -> {
            MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
            comboBox.setId("combo");
            comboBox.addValueChangeListener(ev -> {
                Div entry = new Div();
                entry.setText("value-change"
                        + (ev.isFromClient() ? " from client" : "")
                        + " | old: [" + String.join(",", ev.getOldValue())
                        + "] | new: [" + String.join(",", ev.getValue())
                        + "] | getValue(): ["
                        + String.join(",", comboBox.getValue()) + "]");
                log.add(entry);
            });

            comboBox.setItems(List.of("1", "2", "3"));
            add(comboBox);

            ui.push();                     // first push (before setValue)
            comboBox.setValue(Set.of("1"));
            ui.push();                     // second push (after setValue)
        });
        run.setId("run");
        add(run, log);
    }
}
```

> Note on the harness: the reporter's literal snippet runs at "page load". A *direct* page load can't push during construction (no client yet), so the symptom only appears when the sequence runs against a **live** push channel. A button click reproduces it reliably. Push mode must be active (`AUTOMATIC`); the two `ui.push()` calls must produce two distinct client messages so the client first sees the empty selection, then the value.

## Root cause (suspected)

This is primarily a **web-component-side** defect (answering @yuriy-fix's question). When the server pushes `items` and `selectedItems` as two separate sync messages (the first push sends an empty selection, the second sends the value), the `multi-select-combo-box` transiently reassigns `selectedItems` back to an empty array while applying the update, then sets the real value. Because `selectedItems` is declared `notify: true, sync: true`, that empty reassignment both fires a `selected-items-changed` event and syncs the empty value to the server — Flow then treats it as a client-initiated value change:

https://github.com/vaadin/web-components/blob/5bb94117f8f8f1f23d57a3649b4f2454f75c7223/packages/multi-select-combo-box/src/vaadin-multi-select-combo-box-mixin.js#L118-L123

On the Flow side, the synced empty selection is applied to the server value here (this is the relay, not the origin):

https://github.com/vaadin/flow-components/blob/b7b1801915fba9ce2a24c0140c5d71f76383f215/vaadin-combo-box-flow-parent/vaadin-combo-box-flow/src/main/java/com/vaadin/flow/component/combobox/MultiSelectComboBox.java#L144-L149

> The exact line that reassigns `selectedItems` to `[]` during the two-phase sync was not isolated with a debugger; the captured native-event trace above confirms the empty `selected-items-changed` originates in the web component. Needs human verification.

## Notes

- **Version:** reproduced on the affected line `25.1.8` (web components 25.1.4) **and** on `main` / 25.3-SNAPSHOT (web components 25.2.0) — so it is not yet fixed. The trigger needs two separate push flushes against a live channel; a single coalesced push does not reproduce it.
- This addresses @yuriy-fix's question (component vs. Flow side) in the Root cause section: the spurious empty event originates in the web component.
