<!-- Edit any field. This file is committed on the `repro/<issue>` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced (for `DateTimePickerElement.setDateTime`; the issue's `setTime` case was fixed in 2021 — see Notes)
- **Hypothesis tested:** The bug is `DateTimePickerElement.setDateTime()` having no effect, triggered by a `LocalDateTime` whose `toString()` carries more than 3 fractional-second digits (e.g. `22:32:52.999587`) being set into the web component's `value` property, observable as the `vaadin-date-time-picker` value not changing and no value-change event reaching the server — while the same value truncated to milliseconds works.
- **Regression?:** not a regression — `setDateTime` has passed the untruncated `LocalDateTime.toString()` to the web component since the element was introduced
- **Fixed by:** partially — flow-components#2099 (c3400aeca7) added millisecond truncation to `DateTimePickerElement.setTime` (incidentally; that PR targeted duplicate value-change events); `setDateTime` was left unfixed
- **Duplicate of:** none found
- **Branch:** `repro/1994` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components `main` @ b28844221d (Vaadin 25.3-SNAPSHOT); reported against Vaadin 20.0.6 — the `setDateTime` code path is unchanged
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (playwright-cli)
- **Screenshot** (static bug): ![Repro view after the test sequence](https://raw.githubusercontent.com/vaadin/flow-components/857f808fc9a095fe0b26684ff858de699b1763c2/repro-1994.png)

## Observed behavior

The browser test replicates `DateTimePickerElement.setValue` byte-for-byte (set the `value` property on `vaadin-date-time-picker`, then dispatch a bubbling `change` event — the exact mechanism of the TestBench element) with the issue's three-step sequence:

| Step | Value set | Component `value` after | Server value-change event |
| --- | --- | --- | --- |
| 1 | `2022-01-02T22:31:52` | `2022-01-02T22:31` ✔ | received, `fromClient: true` ✔ |
| 2 | `2022-01-03T22:32:52.999587` | **`2022-01-02T22:31` (unchanged)** ✘ | **none** ✘ |
| 3 | `2022-01-03T22:32:52.999` | `2022-01-03T22:32` ✔ | received, `fromClient: true` ✔ |

Step 2 is what `setDateTime(LocalDateTime.parse("2022-01-03T22:32:52.999587"))` sends: `LocalDateTime.toString()` keeps microsecond precision, the web component's ISO parser rejects the string, and the value silently stays at the previous state. No JS console error — the rejection is completely silent. Step 3 shows 3-digit (millisecond) precision is the boundary.

## Expected behavior

`setDateTime` should update the picker regardless of the sub-millisecond precision of the passed `LocalDateTime` — truncating to milliseconds like `DateTimePickerElement.setTime` already does (the web component's value cannot represent sub-millisecond precision anyway).

## Steps to reproduce

1. Open `http://localhost:8080/repro-1994` (a plain `DateTimePicker` with a value-change log span, id `log`).
2. In the browser, run the TestBench element's mechanism: set `document.querySelector('vaadin-date-time-picker').value = '2022-01-03T22:32:52.999587'` and dispatch a `change` event.
3. Observe the component value and the log span do not change.
4. Repeat with `'2022-01-03T22:32:52.999'` — both update.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -pl vaadin-date-time-picker-flow-parent/vaadin-date-time-picker-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-1994`
- **Scaffold:** `vaadin-date-time-picker-flow-parent/vaadin-date-time-picker-flow-integration-tests/src/main/java/com/vaadin/flow/component/datetimepicker/Repro1994View.java`

```java
DateTimePicker picker = new DateTimePicker("Date and time");
picker.setId("picker");

Span log = new Span("no value change");
log.setId("log");
picker.addValueChangeListener(event -> log.setText("value: "
        + event.getValue() + ", fromClient: " + event.isFromClient()));
```

## Root cause (suspected)

`DateTimePickerElement.setDateTime` passes `LocalDateTime.toString()` — up to 9 fractional digits — straight into the web component's `value` property:

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-date-time-picker-flow-parent/vaadin-date-time-picker-testbench/src/main/java/com/vaadin/flow/component/datetimepicker/testbench/DateTimePickerElement.java#L56-L62

The web component's ISO time parser accepts at most 3 fractional digits, so longer strings are rejected and the value is silently left unchanged:

https://github.com/vaadin/web-components/blob/80a9b89a1bd8a9b0628732df31a03d14ce7b960f/packages/time-picker/src/vaadin-time-picker-helper.js#L34-L44

The sibling `setTime` method in the same class already has the fix (with a comment explaining exactly this):

https://github.com/vaadin/flow-components/blob/b28844221de06fd387a363d8a0aa3579ce63cfc5/vaadin-date-time-picker-flow-parent/vaadin-date-time-picker-testbench/src/main/java/com/vaadin/flow/component/datetimepicker/testbench/DateTimePickerElement.java#L111-L119

Applying the same `truncatedTo(ChronoUnit.MILLIS)` in `setDateTime` (and in `setDate`/`clear` paths it is a no-op) would fix the reported case.

## Notes

- Of the issue's three claims: `DateTimePickerElement.setDateTime` — **still broken** (reproduced above); `DateTimePickerElement.setTime` — **fixed** since flow-components#2099 (c3400aeca7, Sep 2021) which added the truncation; `TimePickerElement.setTime()` — no such method exists (the element only has the string-based `setValue` from `HasStringValueProperty`, where the caller controls the string).
- The reporter's workaround (`.truncatedTo(ChronoUnit.MILLIS)` at every call site) remains valid.
- The browser verification mimics the TestBench element mechanism exactly (`setProperty("value", …)` + `dispatchEvent(new CustomEvent('change', {bubbles: true}))`), so the result transfers 1:1 to `DateTimePickerElement`.
- No pom edits needed.
