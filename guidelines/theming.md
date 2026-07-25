# Theming

All styling and theming is implemented in the web component and only exposed
through the Flow wrapper using theme variants.

Theme variants are not mutually exclusive and multiple can be applied.
Components do not validate invalid combinations, instead application developers
are responsible for not applying invalid combinations (`SMALL` and `LARGE`).

Theme variants are exposed as typed enums implementing `ThemeVariant`:

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

The component that supports these theme variants implements 
`HasThemeVariant<V>`:

```java
public class ExampleComponent extends Component implements HasThemeVariant<DetailsVariant> {
    ...
}
```

Vaadin components currently support two themes: Lumo and Aura. Not every variant
is supported by every theme: some are supported by Lumo, some by Aura, some by
both. The current approach to solving this is prefixing variants that are only
supported by one theme with `LUMO_` or `AURA_`.