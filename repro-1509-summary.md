<!-- Edit any field. This file is committed on the `repro/1509` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** A `DatePicker` with a German locale (`de_DE`/`de_CH`) formats day/month without leading zeros, triggered by `setLocale(...)`, observable as `6.7.2015` instead of the zero-padded `06.07.2015` that `tr_TR` produces.
- **Branch:** `repro/1509` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `25.3-SNAPSHOT` (main line)
- **Present on main?:** yes (still present)
- **Theme / Browser:** default / Chromium (playwright-cli)
- **Screenshot:** `repro-1509.png` (on the branch; embedded below)

## Observed behavior

Three date pickers set to the same value `2015-07-06`:

| Locale | Displayed value |
| --- | --- |
| `de-CH` | `6.7.2015` |
| `de-DE` | `6.7.2015` |
| `tr-TR` | `06.07.2015` |

The German/Swiss locales render the day and month **without leading zeros**, while Turkish renders them **zero-padded** — the inconsistency from the report.

## Expected behavior

Consistent zero-padded day/month for locales whose convention is `dd.MM.yyyy` — e.g. `de_DE`/`de_CH` should display `06.07.2015`.

## Steps to reproduce

1. Add date pickers with `setLocale(new Locale("de","DE"))`, `new Locale("de","CH")`, and `new Locale("tr","TR"))`.
2. Set each to the same single-digit date, e.g. `LocalDate.of(2015, 7, 6)`.
3. Compare the displayed input values.

## Reproduction

How to run: start jetty for the date-picker IT module and open the route.

- **Route:** `http://localhost:8080/repro-1509`
- **Scaffold:** `vaadin-date-picker-flow-parent/.../Repro1509View.java` (committed on this branch)

```java
DatePicker picker = new DatePicker();
picker.setLocale(new Locale("de", "DE")); // or de_CH, tr_TR
picker.setValue(LocalDate.of(2015, 7, 6));
```

## Root cause (suspected)

The Flow date-picker connector derives the date-fns pattern from the browser's `toLocaleDateString` of a test date (`Date.UTC(1234, 4, 6)` → day 6, month 5). It maps `06`→`dd` else `6`→`d`, and `05`→`MM` else `5`→`M`, so the pattern **inherits whatever padding the locale's `toLocaleDateString` used**: `de-*` formats the test date unpadded (`6.5.1234` → `d.M.yyyy`) while `tr-TR` formats it padded (`06.05.1234` → `dd.MM.yyyy`). There is no normalization to a consistent padded pattern:

https://github.com/vaadin/flow-components/blob/bccb70fdefa9209d9052676903cccf29a3832aad/vaadin-date-picker-flow-parent/vaadin-date-picker-flow/src/main/resources/META-INF/resources/frontend/datepickerConnector.js#L25-L37

## Notes

- The padding difference is the browser `Intl`/`toLocaleDateString` behavior per locale (German numeric default is unpadded), which the connector copies verbatim.
- The "provide an API to set the format manually" part of the report is already addressed: `DatePickerI18n.setDateFormat(String)` exists. This reproduction covers the remaining locale-padding inconsistency the maintainer kept the issue open for.
