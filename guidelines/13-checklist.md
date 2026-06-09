# Checklist

A quick scan before submitting a new component — not a substitute for reading
the chapters above.

## Structure & class

- [ ] Parent module + `-flow` / `-testbench` / `-integration-tests` children;
      IT module under the `default` profile; added to the aggregator pom.
- [ ] Copyright header on every file (`mvn spotless:apply`).
- [ ] `@Tag`, `@NpmPackage` (version matches the web component), `@JsModule`.
- [ ] Extends `Component` (or a field base); implements the right mixins.
- [ ] Progressive-disclosure constructors (empty / shortcuts / signal / listener).
- [ ] Positive-form boolean APIs; no fluent setters (except the i18n object).

## Behaviour

- [ ] Every mutable property worth binding has `bind{X}(Signal<…>)`; imperative
      setter throws under an active one-way binding; side-effects run for both.
- [ ] Custom events use `@DomEvent` + `@EventData`; `add…Listener` returns
      `Registration`; no `remove…Listener`.
- [ ] Theme variants match the web component's `theme` tokens.
- [ ] List components implement the right `HasDataView` and `setItems` overloads.
- [ ] Fields support automatic AND manual validation; `Binder` works.
- [ ] `HasAriaLabel` if the web component supports it.
- [ ] No hard-coded user text — everything via setters / `{Component}I18n`.

## Connectors & flags (if applicable)

- [ ] Connector under `frontend/`, loaded via `@JsModule`, `initLazy` +
      `$connector` guard, initialised in `onAttach`.
- [ ] Experimental component gated on a `FeatureFlags` check; flag enabled in
      tests; experimental status noted in Javadoc.

## Tests & docs

- [ ] `{Component}Test`, `{Component}VariantTest`, `{Component}SignalTest`,
      `{Component}I18nTest` (if i18n), and `{Component}SerializableTest` (ALWAYS).
- [ ] `{Component}Page` + `{Component}IT` covering real-browser behaviour.
- [ ] Class + method Javadoc with `@since`; `README.md` in the parent module.

## Final

- [ ] `mvn spotless:apply` run; `mvn checkstyle:check` passes.
- [ ] Unit + integration tests pass.
- [ ] API matches the web component — nothing missing, nothing invented.
