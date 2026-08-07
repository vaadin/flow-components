<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a server-side NPE in `Popover.setTarget`, triggered by `setTarget` re-entering (e.g. from a Popover subclass's `onAttach` calling `setTarget`, as a UI-scoped component does when re-applying its target) while the first call is synchronously auto-adding the popover because the target is already attached, observable as `NullPointerException: Cannot invoke "Registration.remove()" because "this.targetAttachRegistration" is null` in the server log.
- **Regression?:** not a regression — the unguarded `targetAttachRegistration.remove()` has been in `setTarget` since the initial Popover implementation (836f59b092, first shipped in Vaadin 24.5)
- **Fixed by:** n/a (still present on main)
- **Duplicate of:** none found
- **Branch:** `repro/7758` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components `main` @ b28844221d (Vaadin 25.3-SNAPSHOT); reported against 24.7.11 / 24.8.4 — same code on all lines
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot** (static bug): ![Repro view: re-entrant trigger leaves status stuck, control completes](https://raw.githubusercontent.com/vaadin/flow-components/f71d0b7f1ca453c01d0363d8021fe43283be1e56/repro-7758.png)

## Observed behavior

Clicking "Set reentrant target" makes the round-trip fail server-side — the status span never updates — and the server log shows exactly the trace from the issue report:

```
java.lang.NullPointerException: Cannot invoke "com.vaadin.flow.shared.Registration.remove()" because "this.targetAttachRegistration" is null
	at com.vaadin.flow.component.popover.Popover.setTarget(Popover.java:735)
	at com.vaadin.flow.component.popover.tests.Repro7758View$ReentrantPopover.onAttach(Repro7758View.java:74)
	at com.vaadin.flow.component.ComponentUtil.onComponentAttach(ComponentUtil.java:313)
	...
	at com.vaadin.flow.internal.StateNode.fireAttachListeners(StateNode.java:968)
```

The control ("Set plain target") — a plain `Popover` going through the identical sequence with an already-attached target — completes normally (`status` = "plain setTarget completed"). The trigger is precisely the re-entrant `setTarget` call, not the auto-add itself.

## Expected behavior

`setTarget` should not throw. When the previous `setTarget` call has not yet assigned the attach/detach registrations, the re-entrant call must not blindly call `.remove()` on them.

## Steps to reproduce

1. Open `http://localhost:8080/repro-7758`.
2. Click "Set reentrant target" (id `trigger-reentrant`).
3. Server log shows the `NullPointerException` at `Popover.setTarget`; the status span (id `status`) stays unchanged because the request failed.
4. Control: click "Set plain target" (id `trigger-plain`) — completes and updates the status span.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -pl vaadin-popover-flow-parent/vaadin-popover-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-7758`
- **Scaffold:** `vaadin-popover-flow-parent/vaadin-popover-flow-integration-tests/src/main/java/com/vaadin/flow/component/popover/tests/Repro7758View.java`

```java
NativeButton reentrantTarget = new NativeButton("Reentrant target");
add(reentrantTarget); // target is attached before setTarget is called

ReentrantPopover popover = new ReentrantPopover();
popover.setTarget(reentrantTarget); // NPE

// Mimics a UI-scoped component that re-applies its target when attached
public static class ReentrantPopover extends Popover {
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setTarget(getTarget());
    }
}
```

## Root cause (suspected)

`setTarget` removes the old registrations unconditionally whenever `this.target != null`:

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-popover-flow-parent/vaadin-popover-flow/src/main/java/com/vaadin/flow/component/popover/Popover.java#L734-L737

but the registrations are assigned only *after* `onTargetAttach` has already run synchronously for an attached target:

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-popover-flow-parent/vaadin-popover-flow/src/main/java/com/vaadin/flow/component/popover/Popover.java#L750-L753

`onTargetAttach` auto-adds the popover to the UI, which fires the popover's attach listeners synchronously — so any `setTarget` call made from `onAttach` (the UI-scoped scenario from the report) re-enters while `this.target` is set but both registrations are still null. Assigning the registrations before invoking `onTargetAttach` (or null-guarding the removes) would close the window.

## Notes

- The stack trace matches the issue's screenshot (line 697 in 24.8.4 = line 735 on current main; the code is unchanged).
- Reproduced deterministically with a button click; the original report's "rare cases of a reload" is the same window hit non-deterministically by a UI-scoped popover being re-attached.
- No pom edits needed; the view uses only `NativeButton`/`Span` from `flow-html-components`.
