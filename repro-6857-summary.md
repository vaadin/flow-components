<!-- Edit any field. This file is committed on the `repro/6857` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** partially reproduced — the dialog's Escape shortcut still fires while a Notification is displayed on top; the *double* processing reported for 24.5 no longer happens on `main`
- **Hypothesis tested:** The bug is that an Escape shortcut registered inside a modal `Dialog` still fires when a `Notification` is stacked on top, triggered by pressing Escape while the notification is visible, observable as the dialog's click listener running (and, in 24.5, the notification's listener running too).
- **Regression?:** not a regression (broken since introduction) — but the *symptom* changed in 25.x, see Notes
- **Fixed by:** partly — `vaadin/flow`#24974 ("Shortcuts bubble through parent Dialogs in Vaadin 25", closed 2026-07-29) added a popover/modal scope guard that removed the double firing. The remaining half (wrong overlay handles Escape) is not fixed.
- **Duplicate of:** none found
- **Branch:** `repro/6857` — pushed to `vaadin/flow-components`
- **Reproduced on:** `vaadin/flow-components` @ `main` (25.3-SNAPSHOT, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes (partially — see Observed behavior)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![Escape pressed with notification on top: only the dialog's shortcut fired, notification stays open](https://raw.githubusercontent.com/vaadin/flow-components/29e9596092d295fa136bf48a3d3d74db30743537/repro-6857.png) — embeds inline.

## Observed behavior

On `main` (25.3-SNAPSHOT), with a modal `Dialog` open (`setCloseOnEsc(false)`, header button with `addClickShortcut(Key.ESCAPE)`) and a `Notification` (`setDuration(0)`) opened on top of it from inside the dialog, one Escape press produces:

```
1: dialog opened, modal=true
2: notification opened
3: DIALOG close button clicked (fromClient=false)
```

- The **dialog behind** handles Escape, exactly as reported.
- The **notification stays open** (`vaadin-notification-card` still visible, `offsetWidth > 0`) — its own Escape shortcut does not fire.
- No double invocation: the reporter's second log line (notification close button) does not appear on `main`.
- Console is clean (0 errors; only the usual dev-mode Lit/React-devtools notices).

Escape is routed by the **focus scope**, not by stacking order. Measured variants on the same view:

| Setup | Focus when Escape pressed | Dialog shortcut | Notification shortcut |
| --- | --- | --- | --- |
| Modal dialog + notification | inside dialog (default after clicking Save) | ✅ fires | ❌ |
| Modal dialog + notification | inside notification card | ❌ | ❌ (UI is inert due to modality) |
| Modeless dialog + notification | inside dialog | ✅ fires | ❌ |
| Modeless dialog + notification | inside notification card | ❌ | ✅ fires |
| Notification only, no dialog | outside notification | — | ❌ |
| Notification only, no dialog | inside notification card | — | ✅ fires |

So with a **modal** dialog behind it, the notification's Escape shortcut is unreachable: from the dialog it is out of scope, and from inside the notification the UI is inert.

## Expected behavior

Escape should be handled by the topmost overlay — the notification — and should not reach the dialog stacked behind it.

## Steps to reproduce

1. Open `http://localhost:8080/repro-6857`.
2. Click **Open modal dialog**.
3. In the dialog, click **Save (shows notification)** — an error-style notification appears on top of the dialog.
4. Press <kbd>Escape</kbd>.
5. The log shows `DIALOG close button clicked` and the notification is still open.

## Reproduction

How to run: start the server and open the route below.

```sh
CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests \
  -pl vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests
```

- **Route / page:** `http://localhost:8080/repro-6857`
- **Scaffold:** `vaadin-dialog-flow-parent/vaadin-dialog-flow-integration-tests/src/main/java/com/vaadin/flow/component/dialog/tests/Repro6857View.java`

```java
private void openDialog(boolean modal) {
    dialog = new Dialog();
    dialog.setModal(modal);
    dialog.setCloseOnEsc(false);
    dialog.setCloseOnOutsideClick(false);
    dialog.setHeaderTitle("Editor");

    Button dialogClose = new Button("Close dialog");
    dialogClose.addClickListener(e -> log("DIALOG close button clicked"
            + " (fromClient=" + e.isFromClient() + ")"));
    dialogClose.addClickShortcut(Key.ESCAPE);
    dialog.getHeader().add(dialogClose);

    dialog.add(new Button("Save (shows notification)", e -> openNotification()));
    dialog.open();
}

private void openNotification() {
    notification = new Notification();
    notification.setDuration(0);
    notification.setPosition(Notification.Position.MIDDLE);

    Button notificationClose = new Button("Close notification");
    notificationClose.addClickListener(e -> {
        log("NOTIFICATION close button clicked (fromClient=" + e.isFromClient() + ")");
        notification.close();
    });
    notificationClose.addClickShortcut(Key.ESCAPE);

    notification.add(new Div(new Paragraph("Person invalid")), notificationClose);
    notification.open();
}
```

## Root cause (suspected)

Flow scopes shortcuts by the nearest open popover/modal ancestor of the **event origin**, comparing it with the scope of the shortcut owner. There is no notion of a topmost overlay, so a keydown originating in the dialog matches the dialog's own shortcut and never reaches the notification:

https://github.com/vaadin/flow/blob/abe73fb837ee5d175442897b2ca58a1ce2747cc1/flow-server/src/main/resources/META-INF/frontend/FlowShortcut.js#L81-L93

The notification side of the asymmetry comes from `vaadin-notification-container` being a manual popover, which puts every shortcut registered on notification content into its own scope:

https://github.com/vaadin/web-components/blob/9722cd86105df7803af6be37ce098500257c6728/packages/notification/src/vaadin-notification-mixin.js#L41-L46

When a **modal** dialog is active, the notification's shortcut additionally cannot reach the server at all: its lifecycle owner is not a descendant of the active modal component, so the event source stays the (inert) `UI`:

https://github.com/vaadin/flow/blob/abe73fb837ee5d175442897b2ca58a1ce2747cc1/flow-server/src/main/java/com/vaadin/flow/component/ShortcutRegistration.java#L720-L742

## Notes

- The reporter's attached project (`shortcut-handling-flow-spring-24.zip`) was read and ported faithfully; the extra complexity there (Binder, editor dialog, reusable `ShortcutRegistration`) is not needed for the symptom.
- The reported **double** processing was addressed as a side effect of `vaadin/flow`#24974 (scope guard added on 25.x). On `main` only the dialog's handler runs.
- The scope guard also introduced a new rough edge visible in the table above: a shortcut registered on `Notification` content never fires unless focus is already inside the notification card — and never at all while a modal dialog is open.
- The issue text also asks for a general overlay/window manager with stacking-aware shortcut routing. That is an enhancement, not something the current shortcut model provides.
- IT-pom edit: `vaadin-notification-flow` added as a dependency of `vaadin-dialog-flow-integration-tests` so the cross-component view compiles.
