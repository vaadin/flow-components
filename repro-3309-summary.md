> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced
- **Hypothesis tested:** The bug is that `TimePicker` sends an ill-formed BCP 47 tag to its connector. It is triggered by a `Locale` whose language or country is not a valid subtag, such as `new Locale("en_GB")`. It is observable as `vaadin-time-picker: The locale <tag> is not supported.` in the console and the dev-mode error dialog.
- **Regression?:** unknown. The Java tag-building code is identical on `23.0` and `main`. V14 likely swallowed the same error silently, as a maintainer suggested in the thread.
- **Fixed by:** partially by vaadin/flow-components#9873. It fixed the misleading message only: the old `catch` block overwrote `locale` with `en-US` before building the text. The error itself still fires.
- **Duplicate of:** none found
- **Branch:** `repro/3309`, pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.4-SNAPSHOT, 5c881d1a0c)
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium (Playwright)
- **Screenshot** (static bug): ![Pickers with valid and ill-formed locales](https://raw.githubusercontent.com/vaadin/flow-components/<commit-sha>/repro-3309.png)

## Observed behavior

Each `DateTimePicker` below gets an explicit locale. The ill-formed ones throw from the time picker connector:

```
Error: vaadin-time-picker: The locale en_gb is not supported.
Error: vaadin-time-picker: The locale e is not supported.
Error: vaadin-time-picker: The locale en-GB_X is not supported.
Error: vaadin-time-picker: The locale en1 is not supported.
```

| Row | Java `Locale` | Tag sent | Error | Date shown | Time shown |
| --- | --- | --- | --- | --- | --- |
| default | none (UI `en_US`) | `en-US` | no | `2/1/2024` | `2:30 PM` |
| uk | `Locale.UK` | `en-GB` | no | `02/01/2024` | `14:30` |
| underscore | `new Locale("en_GB")` | `en_gb` | **yes** | `2/1/2024` | `14:30` |
| hyphen | `new Locale("en-GB")` | `en-gb` | no | n/a | n/a |
| iw | `new Locale("iw", "IL")` | `he-IL` | no | n/a | n/a |

The failing picker keeps working. The time part uses the web component default 24-hour format, because `setLocale` throws before it assigns `i18n`. The date part falls back silently to the browser locale. This matches all three points of the reporter:

- The component still works.
- The time shows in 24-hour format, not US format.
- Before #9873, the message always named `en-US`, so the real tag stayed hidden.

The date picker logs no warning for the same locale. `DatePicker` sends `Locale.toLanguageTag()`, which returns `und` for ill-formed locales, and the browser accepts `und`.

Valid but unknown tags such as `zz-ZZ` do not throw. Only syntactically ill-formed tags do.

## Expected behavior

No error. The time picker should fall back to a default locale, as the `TimePicker.setLocale` javadoc promises, and like `DatePicker` already does.

## Steps to reproduce

1. Create `new DateTimePicker()` or `new TimePicker()`.
2. Give it an ill-formed locale, for example `picker.setLocale(new Locale("en_GB"))`.
3. The same happens without an explicit locale, when the UI locale is ill-formed. It can come from `I18NProvider.getProvidedLocales()`, `UI.setLocale`, or the JVM default locale.
4. Open the page. The dev-mode error dialog shows `vaadin-time-picker: The locale en_gb is not supported.`

## Reproduction

How to run: start the server (`mvn … jetty:run`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-3309`
- **Scaffold:** `vaadin-date-time-picker-flow-parent/vaadin-date-time-picker-flow-integration-tests/src/main/java/com/vaadin/flow/component/datetimepicker/Repro3309View.java`
- **Query switch:** `?lang=<language>&country=<country>` renders `new Locale(lang, country)` next to the default picker.

```java
DateTimePicker picker = new DateTimePicker();
picker.setLocale(new Locale("en_GB")); // tag sent to the connector: "en_gb"
add(picker);
```

## Root cause (suspected)

`TimePicker.executeLocaleUpdate` builds the tag by joining `getLanguage()` and `getCountry()` with no validation. An ill-formed value such as `en_gb` reaches the connector unchanged:

https://github.com/vaadin/flow-components/blob/5c881d1a0c2e8b1ea8278c4da8b3bbd07a0b7549/vaadin-time-picker-flow-parent/vaadin-time-picker-flow/src/main/java/com/vaadin/flow/component/timepicker/TimePicker.java#L788-L800

The connector then throws instead of falling back:

https://github.com/vaadin/flow-components/blob/5c881d1a0c2e8b1ea8278c4da8b3bbd07a0b7549/vaadin-time-picker-flow-parent/vaadin-time-picker-flow/src/main/resources/META-INF/frontend/vaadin-time-picker/timepickerConnector.ts#L37-L44

`DatePicker` already handles this case with `toLanguageTag()` and an `lvariant` check:

https://github.com/vaadin/flow-components/blob/5c881d1a0c2e8b1ea8278c4da8b3bbd07a0b7549/vaadin-date-picker-flow-parent/vaadin-date-picker-flow/src/main/java/com/vaadin/flow/component/datepicker/DatePicker.java#L1022-L1038

The javadoc promises an `en-US` fallback that the code never does:

https://github.com/vaadin/flow-components/blob/5c881d1a0c2e8b1ea8278c4da8b3bbd07a0b7549/vaadin-time-picker-flow-parent/vaadin-time-picker-flow/src/main/java/com/vaadin/flow/component/timepicker/TimePicker.java#L733-L734

## Notes

- Flow sets the session locale only from `I18NProvider.getProvidedLocales()` or `Locale.getDefault()`, never from `Accept-Language` directly. Headers such as `en-GB-oxendict`, `zh-Hant-TW`, `en_GB` and `es-419` all left the UI at `en_US` and caused no error. The ill-formed locale in the report most likely came from application code, for example `Locale("en_GB")` in Kotlin.
- Possible fix: build the time picker tag like `DatePicker` does. The alternative is a real fallback in the connector `catch` block. #9873 left that fallback out on purpose, as a behavior change.
- No IT `pom.xml` edits were needed.
