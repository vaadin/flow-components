<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is a spurious validation error, triggered by CRUD clearing the editor fields on cancel/save/delete, observable as a `withValidationStatusHandler` firing with `isError()` after a valid cancel of a non-empty-validated field.
- **Regression?:** not a regression (broken since introduction — the field-clearing has been in `BinderCrudEditor.clear()` since the binder-based editor was first added)
- **Branch:** `repro/1165` — pushed to `vaadin/flow-components`
- **Reproduced on:** vaadin/flow-components @ `main` (bdd9b85, Flow 25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot:** `![CRUD cancel triggers a spurious validation error](https://raw.githubusercontent.com/vaadin/flow-components/b85ebddf9e2b610ab58ce7bdca6a39af713b748f/repro-1165.png)`

## Observed behavior

A first-name field bound with `withValidator(non-empty)` + `withValidationStatusHandler`, editing a **valid** existing record ("Sayo"):

1. Open the editor on the valid record → error counter stays `0` (the record is valid).
2. Reset the counter, then click **Cancel** on the unmodified, valid editor.
3. The counter becomes **`errors: 1`** — the validation status handler fired with `isError() == true`, even though the user only cancelled and never entered an invalid value. The `cancel` event fired and the editor closed.

The status handler runs because `Crud`'s cancel path calls `getEditor().clear()`, which empties every bound field (`binder.getFields().forEach(HasValue::clear)`); emptying the first-name field re-runs its non-empty validator and reports the error to the handler. The same clearing runs after save and delete, so any use case that reacts to validation status (e.g. the reporter's error dialog) sees a false error on every valid operation. Console clean (0 errors).

## Expected behavior

Cancelling, saving, or deleting a valid record should not fire a validation error. Clearing the editor should not surface validation errors through `withValidationStatusHandler`.

## Steps to reproduce

1. Open `http://localhost:8080/repro-1165`.
2. Click the edit pencil on the first row (a valid record, First name "Sayo").
3. Click **Reset counter**, then click **Cancel** in the editor.
4. Observe `errors: 1` — a spurious validation error fired on a valid cancel.

## Reproduction

How to run: start the server (`CI=true mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-crud-flow-parent/vaadin-crud-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1165`
- **Scaffold:** `vaadin-crud-flow-parent/vaadin-crud-flow-integration-tests/src/main/java/com/vaadin/flow/component/crud/tests/Repro1165View.java`

```java
binder.forField(firstName)
        .withValidator(name -> name != null && !name.isEmpty(), "name cannot be empty")
        .withValidationStatusHandler(result -> {
            if (result.isError()) {
                errorCount++;              // fires on cancel/save/delete of a VALID record
                status.setText("errors: " + errorCount);
            }
        }).bind(Person::getFirstName, Person::setFirstName);
// open a valid record, then Cancel -> errorCount becomes 1
```

## Root cause (suspected)

`BinderCrudEditor.clear()` frees the item and then clears every bound field individually. Clearing a field to empty fires a value-change that re-runs that binding's validator and invokes its `withValidationStatusHandler` with an error — even though the operation (cancel/save/delete) was valid. A fix would suppress validation-status reporting while clearing (e.g. clear without triggering per-field validation, or reset the binder's status after clearing).

https://github.com/vaadin/flow-components/blob/bdd9b8573360fa2df82db83822806f4f8d8de021/vaadin-crud-flow-parent/vaadin-crud-flow/src/main/java/com/vaadin/flow/component/crud/BinderCrudEditor.java#L82-L87

The clear is invoked from `Crud`'s cancel (and save) handlers:

https://github.com/vaadin/flow-components/blob/bdd9b8573360fa2df82db83822806f4f8d8de021/vaadin-crud-flow-parent/vaadin-crud-flow/src/main/java/com/vaadin/flow/component/crud/Crud.java#L218-L248

## Notes

- Fix archaeology: the field-clearing in `clear()` dates to the original binder-editor commit ("Provide a binder-based editor", 690c4efb6e); **fixing PR: none found** — the behavior is unchanged on main.
- The reporter's original example used `asRequired()`-style non-empty validation surfaced via an error `Dialog`; this reproduction uses a counter Div instead of a Dialog for a deterministic, headless assertion — the underlying trigger (clear → validator → status handler error) is identical.
- The save path clears the editor the same way; it was harder to script deterministically because a dirty editor routes through a confirm dialog, but it exercises the identical `clear()` call. The cancel path is the reporter's exact scenario and is shown here.
