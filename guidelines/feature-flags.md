# Feature Flags

A Vaadin feature flag ships functionality as experimental: users opt in
explicitly, and the API or behavior can be validated — and still changed — in
practice before it becomes final. New components typically ship behind a
flag, matching the experimental status of the web component. New features and
behavior changes of existing components may ship behind one. While flagged,
breaking changes need no deprecation cycle.

A flag gates one of three things, with different mechanics:

- **A new component** — attaching the component without the flag fails fast
  with an exception. The flag matches the experimental status of the web
  component.
- **A new feature** — new API such as a class or method — using the API
  without the flag fails fast with an exception.
- **A behavior toggle** — the flag changes how existing API behaves. The flag
  silently toggles the behavior; nothing throws.

## Defining a flag

Define the `Feature` in the component module in a `FeatureFlagProvider`
implementation, and register it via the Java SPI. When implementing this,
duplicate the setup from an existing component that uses a flag — there is no
need to extract common helpers, the code is trivial.

```java
public class ExampleFeatureFlagProvider implements FeatureFlagProvider {

    public static final Feature EXAMPLE_COMPONENT = new Feature(
            "Example component", // title
            "exampleComponent", // id
            "https://vaadin.com/docs/latest/components/example", // moreInfoLink
            true, // requiresServerRestart
            "com.vaadin.flow.component.example.Example"); // componentClassName

    @Override
    public List<Feature> getFeatures() {
        return List.of(EXAMPLE_COMPONENT);
    }
}
```

Register the provider in
`src/main/resources/META-INF/services/com.vaadin.experimental.FeatureFlagProvider`:

```
com.vaadin.flow.component.example.ExampleFeatureFlagProvider
```

- The `id` is what users put in `vaadin-featureflags.properties`, prefixed
  with `com.vaadin.experimental.`.
- `componentClassName` is the qualified class name when the flag gates a
  component, `null` otherwise.
- Some older flags predate the SPI and live as constants in Flow core's
  `com.vaadin.experimental.FeatureFlags`. Define new flags in the component
  module instead — do not add constants to Flow.

## Gating a component or new feature

Gated new API fails fast: add a dedicated exception class and throw it when
the API is used without the flag enabled.

```java
public class ExampleExperimentalFeatureException extends RuntimeException {
    public ExampleExperimentalFeatureException() {
        super("""
                The Example component is currently an experimental feature \
                and needs to be explicitly enabled. The component can be \
                enabled using Copilot, in the experimental features tab, \
                or by adding a \
                `src/main/resources/vaadin-featureflags.properties` file \
                with the following content: \
                `com.vaadin.experimental.exampleComponent=true`""");
    }
}
```

For a component, check the flag in `onAttach` — the earliest point where the
component has a `UI` to resolve the flag from:

```java
@Override
protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    checkFeatureFlag(attachEvent.getUI());
}

private void checkFeatureFlag(UI ui) {
    FeatureFlags featureFlags = FeatureFlags
            .get(ui.getSession().getService().getContext());
    if (!featureFlags
            .isEnabled(ExampleFeatureFlagProvider.EXAMPLE_COMPONENT)) {
        throw new ExampleExperimentalFeatureException();
    }
}
```

For a new feature that is not a component (no attach lifecycle), check the
flag at the first point of use instead.

The Javadoc of the gated API states that it is experimental and names the
flag:

```java
/**
 * Example is a component that ...
 * <p>
 * This component is experimental and needs to be enabled with the
 * {@code com.vaadin.experimental.exampleComponent} feature flag.
 * ...
 */
```

## Toggling behavior

A behavior toggle switches the behavior silently instead of throwing:

- When the behavior lives in the web component, no server-side check is
  needed at all: Flow injects enabled flags into the page as
  `window.Vaadin.featureFlags.{id}`, which the web component reads directly.
  The Java side then only defines the flag (provider + SPI registration) and
  documents it.
- When the flag affects server-side behavior, check the flag where the
  behavior branches:

```java
if (isFeatureFlagEnabled(ExampleFeatureFlagProvider.EXAMPLE_FEATURE)) {
    // apply the flagged behavior
}

private boolean isFeatureFlagEnabled(Feature feature) {
    UI ui = UI.getCurrent();
    return ui != null && FeatureFlags
            .get(ui.getSession().getService().getContext()).isEnabled(feature);
}
```

Document the flag in the Javadoc of the methods it affects, including how to
enable it:

```java
/**
 * ...
 * This can be addressed by enabling the feature flag
 * {@code exampleFeature}, ... To enable this feature flag, add the
 * following line to
 * {@code src/main/resources/vaadin-featureflags.properties}:
 *
 * <pre>
 * com.vaadin.experimental.exampleFeature = true
 * </pre>
 */
```

## Flags in tests

- Unit tests use `EnableFeatureFlagExtension`; its `disableFeature()` covers
  the disabled path (e.g. the exception thrown by gated new API).
- ITs enable the flag via the IT module's
  `src/main/resources/vaadin-featureflags.properties`:
  `com.vaadin.experimental.exampleComponent=true`.

## Graduation

When the experimental phase ends, remove the flag infrastructure in one
change: the `Feature` constant and provider, the SPI registration, the
exception class and checks, the Javadoc notes, and the flag entries in test
resources.
