<!-- Edit any field. This file is committed on the `repro/6764` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that the connector derives the date pattern from `Date.prototype.toLocaleDateString(locale)`, which uses the ECMA-402 *numeric* skeleton (unpadded day/month) instead of the locale's conventional short/medium pattern, triggered by any locale whose CLDR pattern is zero-padded (`de-*`: `dd.MM.yyyy`), observable as a DatePicker input showing `1.3.2024` where German convention and `java.time` produce `01.03.2024`.
- **Regression?:** not a regression (broken since introduction) — the pre-`date-fns` connector called `rawDate.toLocaleDateString(locale)` directly and produced the same unpadded output, so #4016 (24.0.0.alpha3) preserved the behavior rather than causing it.
- **Fixed by:** n/a — still present on `main`
- **Duplicate of:** none found
- **Branch:** `repro/6764` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright). Not browser-specific — Node and Chromium ICU agree, matching the reporter's note.
- **Screenshot** (static bug): ![DatePicker input value vs. java.time format, per locale](https://raw.githubusercontent.com/vaadin/flow-components/7e1b6fb0f77f37c45076965893b8e4840298a47d/repro-6764.png) — embeds inline.

## Observed behavior

With `setLocale(Locale.GERMANY)` and `setValue(LocalDate.of(2024, 3, 1))`, the input renders `1.3.2024`. The same view, rendering `java.time` output for each locale next to the picker, shows the divergence directly (read from the live DOM):

| Locale | DatePicker input | `java.time` SHORT | `java.time` MEDIUM |
| --- | --- | --- | --- |
| `de-DE` | **`1.3.2024`** | `01.03.24` | `01.03.2024` |
| `de` | **`1.3.2024`** | `01.03.24` | `01.03.2024` |
| `de-AT` | **`1.3.2024`** | `01.03.24` | `01.03.2024` |
| `de-CH` | **`1.3.2024`** | `01.03.24` | `01.03.2024` |
| `fr-FR` | `01/03/2024` | `01/03/2024` | `1 mars 2024` |
| `pl-PL` | `1.03.2024` | `1.03.2024` | `1 mar 2024` |
| `en-GB` | `01/03/2024` | `01/03/2024` | `1 Mar 2024` |
| `en-US` | `3/1/2024` | `3/1/24` | `Mar 1, 2024` |
| `es-ES` | `1/3/2024` | `1/3/24` | `1 mar 2024` |
| `fi-FI` | `1.3.2024` | `1.3.2024` | `1.3.2024` |

The control rows matter: `fi-FI` is *correctly* unpadded (Finnish convention really is `d.M.yyyy`) and `fr-FR` / `en-GB` are correctly padded, so the connector is not simply "always unpadded" — it follows whatever `toLocaleDateString` returns, and for `de-*` that is the numeric skeleton `d.M.y` rather than the CLDR short pattern `dd.MM.yy`.

The documented workaround works: the same `de-DE` picker with `new DatePickerI18n().setDateFormat("dd.MM.yyyy")` renders `01.03.2024`.

Console is clean (only the dev-mode Lit warning and a favicon 404).

## Expected behavior

For German locales the value should display as `dd.MM.yyyy` — `01.03.2024` — matching the CLDR pattern for `de` and what `DateTimeFormatter.ofLocalizedDate(...).withLocale(Locale.GERMANY)` produces on the server.

## Steps to reproduce

1. Create a `DatePicker`, call `setLocale(Locale.GERMANY)`, and set a value with a single-digit day and month, e.g. `LocalDate.of(2024, 3, 1)`.
2. Open the view — no `DatePickerI18n` is set.
3. The input shows `1.3.2024` instead of `01.03.2024`.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -DskipTests -pl vaadin-date-picker-flow-parent/vaadin-date-picker-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-6764`
- **Scaffold:** `vaadin-date-picker-flow-parent/vaadin-date-picker-flow-integration-tests/src/main/java/com/vaadin/flow/component/datepicker/Repro6764View.java`

```java
DatePicker picker = new DatePicker();
picker.setLocale(Locale.GERMANY);
picker.setValue(LocalDate.of(2024, 3, 1));
// input renders "1.3.2024"; java.time renders "01.03.2024"
```

The committed view puts ten locales side by side with the corresponding `java.time` SHORT/MEDIUM output, plus a `setDateFormat("dd.MM.yyyy")` control.

## Root cause (suspected)

The locale-based pattern is reverse-engineered from a formatted test date. `toLocaleDateString(locale)` with no options resolves to `{year:'numeric', month:'numeric', day:'numeric'}`, which for `de-*` yields `6.5.1234` — so the `'06' → 'dd'` / `'05' → 'MM'` substitutions never match and only the unpadded `'6' → 'd'` / `'5' → 'M'` ones apply, producing `d.M.yyyy`:

https://github.com/vaadin/flow-components/blob/3110712634ddd4c0a3c1002e2d45923a45c265c1/vaadin-date-picker-flow-parent/vaadin-date-picker-flow/src/main/resources/META-INF/frontend/datepickerConnector.js#L19-L49

The padded branches are reached only for locales where ICU's numeric skeleton happens to pad (`fr-FR`, `en-GB`). A fix would need the locale's actual short/medium date pattern rather than the numeric skeleton — e.g. deriving the pattern from `Intl.DateTimeFormat(locale, { dateStyle: 'short' }).formatToParts(...)` (which for `de-DE` returns 2-digit day and month), or sending the server-side `DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)` pattern down with the locale.

## Notes

- The current behavior is codified by an existing integration test — `DatePickerLocaleIT#datePickerWithLocale_setInputValue_blur_assertDisplayedValue` asserts `3.5.2018` for `Locale.GERMAN` — so changing it is a deliberate behavior change that also updates that test, and it changes the displayed format for existing German (and any similarly affected locale) applications.
- `DateTimePicker` reuses the same `datepickerConnector.js`, so its date part shows the same unpadded German format.
- Parsing is unaffected: typing the padded `05.06.2023` into the `de-DE` picker parses correctly (value becomes `2023-06-05`, not invalid), but the input is then rewritten to `5.6.2023` — so a German user who types the conventional form sees it silently unpadded. Only the display side diverges.
- No dependencies were added to the IT module pom.
