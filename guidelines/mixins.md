# Mixin Interfaces

Standard component capabilities are provided through mixin interfaces.
Capabilities are delivered through narrow `Has*` interfaces with default methods
over `getElement()`; a component implements the ones it needs, and its class
declaration reads as a list of capabilities.

```java
public class Button extends Component
        implements ClickNotifier<Button>, Focusable<Button>, HasAriaLabel,
        HasEnabled, HasPrefix, HasSize, HasStyle, HasSuffix, HasText,
        HasThemeVariant<ButtonVariant>, HasTooltip {
}
```

## Standard Flow interfaces (`flow-server`)

| Interface                 | Adds                                                   |
|---------------------------|--------------------------------------------------------|
| `HasText`                 | `setText` / `getText`                                  |
| `HasEnabled`              | `setEnabled` / `isEnabled`, maps to `disabled`         |
| `HasSize`                 | `setWidth`, `setHeight`, min/max                       |
| `HasStyle`                | class names, inline `getStyle()`                       |
| `HasLabel`                | `setLabel` / `getLabel`                                |
| `HasHelper`               | helper text + helper component slot                    |
| `HasPlaceholder`          | `setPlaceholder`                                       |
| `HasTheme`                | `addThemeName` / `removeThemeName`                     |
| `HasAriaLabel`            | `setAriaLabel`, `setAriaLabelledBy`                    |
| `Focusable<T>`            | `focus()`, `setTabIndex`, focus listeners/shortcuts    |
| `ClickNotifier<T>`        | `addClickListener`, `addClickShortcut`                 |
| `HasComponents`           | `add` / `addComponentAtIndex` / `remove` / `removeAll` |
| `HasComponentsOfType<T>`  | Typed version of `HasComponents`                       |
| `HasValue<E,V>`           | value + `ValueChangeEvent` + listener                  |
| `HasValueAndElement<E,V>` | `HasValue` + read-only + required indicator            |
| `HasValidation`           | `isInvalid`, `setErrorMessage`                         |
| `HasValidator<V>`         | `getDefaultValidator()` for `Binder`                   |

## Shared mixins (`com.vaadin.flow.component.shared.*`, in `vaadin-flow-components-base`)

| Interface                         | Adds                                                                 |
| --------------------------------- | -------------------------------------------------------------------- |
| `HasThemeVariant<V>`              | typed theme variants + `bindThemeVariant(s)`                         |
| `HasTooltip`                      | `setTooltipText` / `setTooltipMarkdown` via `<vaadin-tooltip>`       |
| `HasPrefix` / `HasSuffix`         | `set{Prefix,Suffix}Component` (prefix/suffix slots)                  |
| `HasClearButton`                  | clear button on input fields                                         |
| `HasAutoOpen`                     | `setAutoOpen` for auto-opening overlays                              |
| `HasAllowedCharPattern`           | input character filter                                               |
| `HasValidationProperties`         | `setErrorMessage` / `setInvalid` via element properties              |
| `InputField<E,V>`                 | aggregate: enabled + helper + label + size + style + tooltip + value |
| `SelectionPreservationHandler<T>` | preserve selection across data-provider refreshes                    |
