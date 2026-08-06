<!-- Edit any field. This file is committed on the `repro/9842` branch and posted as the issue comment. -->

> [!WARNING]
> **Automated reproduction — produced by the Claude Code `repro` skill. Needs human verification.**
> The steps, verdict, and root-cause pointer below were generated automatically and must be confirmed by a human before being treated as authoritative.

- **Verdict:** reproduced — but the real trigger is the **locale**, not the attach order
- **Hypothesis tested:** The bug is that the dropdown item matching the value is not marked selected, triggered by setting the value before the component is initialized on the client, observable as a missing checkmark in the overlay.
- **Regression?:** worked in 24.x / broke in 25.0.0 (`vaadin/web-components#9354`, which replaced the internal combo-box with `_scroller.selectedItem` assignment)
- **Fixed by:** n/a — still broken
- **Duplicate of:** none found
- **Branch:** `repro/9842` — pushed to `vaadin/flow-components`
- **Reproduced on:** flow-components @ `main` (25.3-SNAPSHOT, `@vaadin/time-picker` 25.3.0-alpha8); same code path is unchanged on the `25.2` line
- **Present on main?:** yes (still broken)
- **Theme / Browser:** Lumo / Chromium 151
- **Screenshot:** ![TimePicker with de-DE locale: 10:00 is the value but no checkmark in the dropdown](https://raw.githubusercontent.com/vaadin/flow-components/2a9bb9722579590e4db34129c3151687e676e673/repro-9842.png) — embeds inline.

## Observed behavior

With a 24-hour locale (`setLocale(Locale.GERMANY)`) and a value set in the same server round trip as the initial rendering, the dropdown shows **no checkmark** and does not scroll to the selected time. Client state of the affected picker:

```
value:            "10:00"
_comboBoxValue:   "10:00"
_dropdownItems:   ["00:00", "01:00", …]
_scroller.selectedItem: undefined      <-- never assigned
vaadin-time-picker-item[selected]: none
```

The correctly working variants (value set from a later button click, or after an extra client round trip) show `selectedItem = {label: "10:00", value: "10:00"}` and one item carrying the `selected` attribute.

**The attach order is not what decides it — the locale is.** Variants in the repro view:

| Variant | Locale | When the value is set | Checkmark |
| --- | --- | --- | --- |
| A | default (en-US) | before `add()` | ✅ |
| B | default (en-US) | after `add()`, same round trip | ✅ |
| G | `de-DE` | before `add()` | ❌ |
| H | `de-DE` | before `add()`, value set before `setLocale` | ❌ |
| I | `de-DE` | after `add()`, same round trip | ❌ |
| J | `de-DE` | later, from a button click | ✅ |
| K | `de-DE` | after an extra JS round trip (the workaround in the issue) | ✅ |

Console is clean apart from the usual dev-server `favicon.ico` 404.

The same failure reproduces **without Flow at all**, with a plain web component and the default (ISO) i18n:

```js
host.innerHTML = '<vaadin-time-picker value="10:00"></vaadin-time-picker>';
// -> _scroller.selectedItem === undefined, no [selected] item
```

So this is a `vaadin/web-components` bug; Flow only exposes it for 24-hour locales.

## Expected behavior

The item matching the value is marked as selected (checkmark shown, overlay scrolled to it) regardless of when the value was set.

## Steps to reproduce

1. Create a `TimePicker`, call `setLocale(Locale.GERMANY)` (or any 24-hour locale) and `setValue(LocalTime.of(10, 0))`, and add it to the view.
2. Open the page and open the overlay.
3. No checkmark next to `10:00`, and the list is scrolled to the top instead of to the value.

## Reproduction

How to run: start the server (`mvn package jetty:run -Dvaadin.frontend.hotdeploy=true -am -B -q -DskipTests -pl vaadin-time-picker-flow-parent/vaadin-time-picker-flow-integration-tests`) and open the route below.

- **Route / page:** `http://localhost:8080/repro-9842`
- **Scaffold:** `vaadin-time-picker-flow-parent/vaadin-time-picker-flow-integration-tests/src/main/java/com/vaadin/flow/component/timepicker/tests/Repro9842View.java`

```java
TimePicker tp = new TimePicker();
tp.setLocale(Locale.GERMANY);
tp.setValue(LocalTime.of(10, 0));
add(tp);
```

## Root cause (suspected)

`_scroller.selectedItem` is assigned in a single place, guarded by "`_comboBoxValue` changed in *this* update **and** `_dropdownItems` already exists":

https://github.com/vaadin/web-components/blob/bb32a09b50b7ebb34658983baeafc3a9b8e6f0d0/packages/time-picker/src/vaadin-time-picker-mixin.js#L228-L236

`_comboBoxValue` is declared with `sync: true`, so assigning it runs an update immediately. During the very first update the order is:

1. the `value` observer sets `_comboBoxValue` → a nested update runs while `_dropdownItems` is still `undefined` → the guard fails, nothing is selected;
2. `__updateDropdownItems` then fills `_dropdownItems` and assigns `_comboBoxValue` again:

https://github.com/vaadin/web-components/blob/bb32a09b50b7ebb34658983baeafc3a9b8e6f0d0/packages/time-picker/src/vaadin-time-picker-mixin.js#L510-L529

If that second assignment produces a **different** string, one more update runs, `_comboBoxValue` is in `props`, `_dropdownItems` exists, and the item is selected. If it produces the **same** string, no update runs and `selectedItem` is never assigned. The `_dropdownItems`-only update that follows does not help, because the guard requires `_comboBoxValue` to be among the changed properties.

That is exactly the locale split:

- **en-US** — step 1 formats `"10:00"` (default ISO i18n), step 2 formats `"10:00 AM"` → value changed → works by accident.
- **de-DE and every other 24-hour locale** — both steps format `"10:00"` → no change → broken.

Instrumented `updated()` calls for the failing picker, in order: `[value, opened, __effectiveI18n, i18n]` (`_dropdownItems` still null) → `[_comboBoxValue]` (`_dropdownItems` still null) → `[_dropdownItems]` (no `_comboBoxValue`) → nothing further.

Assigning `selectedItem` when `_dropdownItems` changes as well, or computing it from `(_comboBoxValue, _dropdownItems)` rather than from a single changed property, would fix it.

## Notes

- The issue title points at the attach order, but variants A/B above show a pre-attached value works fine under en-US, and variant I shows a post-attach value fails under de-DE. The deciding factor is whether the connector's localized time string differs from the ISO string.
- The workaround in the issue comment (`executeJs("return").then(...)`) works because the extra round trip sets the value after `_dropdownItems` exists — variant K.
- No IT-module dependencies were added.
- The overlay also fails to scroll to the value in the broken case, which is the same missing `selectedItem`.
