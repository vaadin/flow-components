# Flow Component Guidelines

These guidelines describe how Vaadin Flow components — the Java server-side
wrappers around the Vaadin web components — should be designed and implemented
in the `flow-components` repository. Chapers can be read selectively for the
topics your work touches.

Treat these as guidelines, not hard rules. They are best practices that should
be followed by default, but can be deviated from when necessary to make
something work.

## Chapters

| Chapter                                                 | Topic                                                                     |
|---------------------------------------------------------|---------------------------------------------------------------------------|
| [Repository](repository.md)                             | Tech stack, module layout, naming, Maven, dependencies.                   |
| [Component Implementation](component-implementation.md) | Component class structure, integrating Flow wrappers with web components. |
| [API Design](api-design.md)                             | Shape of the public Java API.                                             |
| [Managing children](children.md)                        | Managing child components, slots, container components.                   |
| [Events](events.md)                                     | Defining and registering component events.                                |
| [Mixin Interfaces](mixins.md)                           | Reusable mixin interfaces providing common capabilities to components.    |
| [Signals](signals.md)                                   | Reactive `bind*` APIs and `SignalPropertySupport`.                        |
| [Routing](routing.md)                                   | Flow Router integration in navigational APIs.                             |
| [Data & Validation](data-and-validation.md)             | Data providers, data views, validation.                                   |
| [Theming](theming.md)                                   | Theme-variant enums.                                                      |
| [I18n & Accessibility](i18n-and-a11y.md)                | Localisable text and accessibility wiring.                                |
| [Documenting](documenting.md)                           | Javadoc and README.                                                       |
| [Testing](testing.md)                                   | Unit tests, TestBench elements, integration tests, WTR.                   |
| [Feature Flags](feature-flags.md)                       | Using feature flags for experimental components and features.             |
