# Flow Component Guidelines

These guidelines describe how Vaadin Flow components — the Java server-side
wrappers around the Vaadin web components — are designed and implemented in the
`flow-components` repository. They are detailed enough that an agent or a new
contributor can build a component without reading existing source first, while
still pointing back into the codebase for canonical references.

The underlying web component is the source of truth. For its design and
implementation, see the `guidelines/` folder in the `web-components`
repository; the Flow API must stay consistent with it.

## Pre-requisites

These guidelines assume basic familiarity with:

- Vaadin Flow — `Component`, `Element`, `UI`, the attach/detach lifecycle.
- Vaadin web components — each Flow component wraps one.
- Mixin-interface composition (`HasEnabled`, `HasSize`, `HasStyle`, …).
- Flow signals and `SignalBinding`.
- Maven and JUnit.

For repository-level build and test commands, see `CLAUDE.md`.

## Chapters

| #   | File                                             | Topic                                                        |
| --- | ------------------------------------------------ | ------------------------------------------------------------ |
| 00  | [Overview](00-overview.md)                       | This file.                                                   |
| 01  | [Repository](01-repository.md)                   | Tech stack, module layout, Maven, naming, copyright.         |
| 02  | [Design](02-design.md)                           | Shaping the API and behaviour before implementation.         |
| 03  | [Component Structure](03-component-structure.md) | The main class, annotations, constructors, Element API.      |
| 04  | [Mixin Interfaces](04-mixins.md)                 | Composing capabilities from `Has*` interfaces.               |
| 05  | [Signals](05-signals.md)                         | Reactive `bind*` APIs and `SignalPropertySupport`.           |
| 06  | [Events](06-events.md)                           | Defining and registering component events.                   |
| 07  | [Composition](07-composition.md)                 | Slots, prefix/suffix, containers, JS connectors.             |
| 08  | [Data & Validation](08-data-and-validation.md)   | Data providers, data views, validation.                      |
| 09  | [Theming](09-theming.md)                         | Theme-variant enums.                                         |
| 10  | [I18n & Accessibility](10-i18n-and-a11y.md)      | Localisable text and Flow-side accessibility wiring.         |
| 11  | [Documenting](11-documenting.md)                 | Javadoc and README.                                          |
| 12  | [Testing](12-testing.md)                         | Unit, signal, variant, serializable, TestBench, integration. |
| 13  | [Checklist](13-checklist.md)                     | Final scan before submitting a component.                    |
