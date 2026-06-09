# Theming

Theme variants are typed enums implementing `ThemeVariant`; applications
add/remove them through `HasThemeVariant<V>`.

```java
public enum ExampleVariant implements ThemeVariant {
    PRIMARY("primary"),
    SMALL("small"),
    LARGE("large");

    private final String variant;
    ExampleVariant(String variant) { this.variant = variant; }

    @Override
    public String getVariantName() { return variant; }
}
```

- `getVariantName()` MUST return the exact `theme` token the web component
  accepts.
- The enum is `{Component}Variant` (singular).

Applying them — all provided by `HasThemeVariant<V>`, don't re-implement:

```java
button.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.LARGE);
button.setThemeVariants(ButtonVariant.PRIMARY);                  // replace all
button.removeThemeVariants(ButtonVariant.LARGE);
button.bindThemeVariant(ButtonVariant.PRIMARY, isPrimarySignal); // reactive
```

Add a `{Component}VariantTest` mapping each enum value to its expected token
(see [Testing](12-testing.md)).
