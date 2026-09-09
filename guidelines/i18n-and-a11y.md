# I18n & Accessibility

Most internationalization and accessibility behavior lives in the web component.
The Flow wrapper only exposes its i18n and labelling APIs to Java and
propagates state (disabled, invalid, required) through mixins.

## I18n

Add a `{Component}I18n` class when the component renders user-visible text of
its own — a Jackson-serialisable POJO mapping the web component's `i18n`
property.

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExampleI18n implements Serializable {
    private String clear;

    public String getClear() { return clear; }
    public ExampleI18n setClear(String clear) { this.clear = clear; return this; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorMessages implements Serializable { /* ... */ }
}
```

- `implements Serializable` (including nested classes).
- `@JsonInclude(NON_NULL)` so unset strings aren't sent and don't overwrite
  web-component defaults.
- Exclude properties that are not used by the web component (e.g. server-side
  error message), using Jackson annotations
- Jackson annotations and databind types come from different packages (see
  [Repository](repository.md#technology-stack)) — mixing them silently fails.
- Setters may return `this` — the one sanctioned fluent style in the Flow API.

Push it to the element via `JacksonUtils`:

```java
public void setI18n(ExampleI18n i18n) {
    this.i18n = Objects.requireNonNull(i18n,
            "The i18n properties object should not be null");
    getElement().setPropertyJson("i18n", JacksonUtils.beanToJson(i18n));
}
```

## Accessibility

- If the web component exposes `aria-label` / `aria-labelledby`, implement
  `HasAriaLabel`.
- For field components with `accessibleName` / `accessibleNameRef` properties,
  override `setAriaLabel` / `setAriaLabelledBy` to map to them.
- Implement `HasEnabled`; the web component owns the dimmed visuals and
  interaction blocking.
- Keyboard shortcuts: `Focusable.addFocusShortcut`,
  `ClickNotifier.addClickShortcut`, or `Shortcuts.addShortcutListener`. Keep
  them consistent with the web component; the Flow side usually doesn't
  override keyboard semantics.
