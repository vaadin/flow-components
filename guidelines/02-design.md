# Design

High-level principles for shaping a component's API and behaviour, before any
implementation work begins. Implementation mechanics live in the chapters that
follow.

## Wrap, don't reinvent

Every Flow component is a Java wrapper around an existing web component. Its job
is to surface that component's capabilities to server-side Java in a
Flow-idiomatic way — not to redesign them.

- The web component is the source of truth for behaviour, DOM, theme variants,
  slots, and CSS. The Flow API must match its semantics, not contradict them.
- A web-component property maps to the matching Flow setter (`disabled` →
  `HasEnabled`); a web-component DOM event maps to a `@DomEvent` listener — e.g.
  `custom-value-set` → `@DomEvent("custom-value-set")` — never a Flow-only
  synthetic event.
- Theme-variant enums use the exact `theme` tokens the web component accepts.
- `@NpmPackage` version MUST match the validated web component version.

**Why:** every divergence between the two layers is an integration bug that
only surfaces at the seam, where each layer is internally consistent.

## Progressive disclosure through constructors

The common case is one line of Java; complex configuration is opt-in. Achieve
this with overloaded constructors, not builders or long setter chains.

```java
new Button("Save", e -> save());           // text + click listener
new Button(textSignal, icon, e -> save()); // reactive text + icon + listener
```

- Each overload must match a real use case — don't fill the combinatorial
  matrix for its own sake.
- No fluent/builder DSLs: components are setter-based, so framework tooling
  (`Binder`, dependency injection, state restoration) sees one predictable
  shape across the whole library.
- Provide a Signal-based variant in the same position as each value parameter.

## Imperative API first, reactive Signal API alongside

Every mutable state has a setter/getter AND a `bind*(Signal<T>)` variant.

- Both produce the same effect and fire the same events.
- While a one-way binding is active, the imperative setter throws
  `BindingActiveException` — that is how a developer learns something else owns
  the state.
- The imperative API is canonical; docs and examples lead with it. Signals are
  an extension, never a replacement.

See [Signals](05-signals.md) for the binding rules.

## Compose via mixin interfaces, not inheritance

Capabilities come from narrow `Has*` interfaces with default methods over
`getElement()`. A component's class declaration reads as a list of capabilities.

- Implement a standard interface rather than duplicating its methods.
- A capability shared by two components belongs in a shared interface in
  `vaadin-flow-components-base`, not copied into each.
- Avoid intermediate abstract base classes unless one already exists and is
  shared. Favour composition.

See [Mixin Interfaces](04-mixins.md).

## Differentiation drives scope

A component's docs must name the adjacent components it is NOT, and what each
handles that this one does not.

- `Select` is not `ComboBox` (fixed short list vs filterable/lazy).
- `Accordion` is not `Details` (single-open group vs standalone region).
- `Grid` is not `VirtualList` (tabular vs 1-D list).

The Flow wrapper shares the web component's boundary and MUST NOT add
responsibilities the web component lacks. When a request blurs the boundary, the
answer is usually "use the other component."

## Integrate with Flow Router

Web components are router-agnostic (plain `<a href>`); Flow has exactly one
router, and the wrappers make its concepts first-class.

- Navigational components take `Class<? extends Component>` for `@Route` views
  (`new SideNavItem("Home", HomeView.class)`) as the primary API; resolve URLs
  via `RouteConfiguration` so renames update automatically and typos fail at
  compile time.
- Use `RouteParameters` / `QueryParameters` / `HasUrlParameter`; shell layouts
  implement `RouterLayout`.
- A string-path overload may coexist for external links, but the typed form is
  primary.
- Still set a real `href` so middle-click, "open in new tab", and screen-reader
  link semantics work — don't replace the anchor with a `UI.navigate(...)`
  click handler. Flow Router intercepts clicks on known routes automatically.

## Prefer positive-form boolean APIs

Web-component boolean attributes name the non-default state (`disabled`,
`hidden`) because they work by presence/absence. Java has no such constraint, so
the Flow API uses the positive, natural-language form.

| Flow API            | Web component attribute |                        |
| ------------------- | ----------------------- | ---------------------- |
| `setEnabled(false)` | `disabled`              | flipped                |
| `setVisible(false)` | `hidden`                | flipped                |
| `setReadOnly(true)` | `readonly`              | same — already natural |
| `setOpened(true)`   | `opened`                | same                   |

The wrapper hides the mapping; the getter follows the setter's polarity. See
[Component Structure](03-component-structure.md#boolean-properties--positive-form)
for the implementation.

## Consistency over novelty

Match the closest existing component before inventing. Standard shapes:
`get{X}()` / `is{X}()`, `set{X}(value)`, `add{Item}` / `remove{Item}` /
`setItems`, `add{Event}Listener` returning `Registration` (no `remove…`),
`bind{X}(Signal<T>)`, theme-variant methods from `HasThemeVariant<V>`,
`set{Slot}Component`, nested `@DomEvent` event classes, `{Component}I18n`.
Deviate only when a real requirement makes the standard shape impossible, and
document it.

## Completeness over minimalism

Every feature the web component supports MUST be reachable from Flow — a missing
API is a defect, not a design choice. Do the infrastructure work (a new mixin, a
shared controller) rather than leaving a feature inaccessible.

**Counter-balance:** every method, constructor, and field must trace back to a
real requirement or an established web-component API. No speculative API.

## Ship new components as experimental

A new component enters behind a Vaadin feature flag and graduates once its API
is validated in practice.

- Gate the wrapper on the matching `com.vaadin.experimental.FeatureFlags` flag,
  checked at runtime, matching the experimental status of the web component.
- ITs enable the flag via `vaadin-featureflags.properties`; unit tests use
  `EnableFeatureFlagExtension`.
- Javadoc states the component is experimental.
- Breaking changes to an experimental API need no deprecation cycle; once
  graduated, normal Vaadin deprecation rules apply.

## Server/client split is an implementation detail

State flows between the JVM and the browser, but that split MUST NOT leak into
the public API.

- Developers never reason about `@Synchronize` vs `setProperty` vs `executeJs`
  — the API looks like ordinary Java object state.
- Connectors live in `frontend/{name}Connector.js`, load via `@JsModule`, and
  initialise in the attach handler — invisible in Javadoc.
- Client-side errors translate into meaningful server-side state or events, not
  raw JavaScript stack traces.

## Sensible defaults, zero-config rendering

A component created with its zero-argument constructor and no further calls MUST
render correctly on its own.

- An unconfigured component renders without errors or blank space.
- Client dependencies are declared with `@NpmPackage` / `@JsModule`, so
  `new Component()` triggers the right bundle with no extra setup.
- No startup step beyond adding the component to the UI.

## Universal behavioural requirements

These apply to EVERY component and are enforced at the web-component, mixin, or
framework level. **A component's own docs MUST NOT restate them** — only note
where the component adds something specific (a concrete default, an override, a
specific interaction).

Accessibility — accessible names, focus order, keyboard support, RTL — is owned
by the web component and its guidelines; the Flow wrapper neither restates nor
re-implements it. Its only accessibility job is exposing the web component's
i18n and labelling APIs to Java.

- **Localisable text.** Any text the component renders itself (internal button
  labels, error messages, tooltip defaults) is customisable from Java, never
  hard-coded — see [I18n & Accessibility](10-i18n-and-a11y.md).
- **Property order independence.** Setting properties before vs after attach
  produces the same result. Don't introduce ordering dependencies (e.g. by
  deferring writes to `onAttach` in a way that discards earlier values).
- **Safe before attachment.** Never throw on a detached instance. Flow queues
  client-bound calls (`executeJs`, `callJsFunction`) until attach; properties
  sync on connect; server-side methods like `Button.click()` work regardless.
- **No thrown errors for bad input.** Clamp or ignore out-of-range values and
  log a warning. The sole exception is `BindingActiveException`.
- **Serialisation.** Every component and every data object it exposes is
  `Serializable`; always add a `{Component}SerializableTest`.
- **Signals don't replace imperative state.** Imperative setters always work,
  even on components with `bind*` APIs.
- **JSON via Jackson.** Use `JacksonUtils` / `ObjectNode` / `@JsonInclude`,
  never string concatenation.

## Relationship to specs

A Flow component's spec MUST: show both imperative and Signal-based examples
where both exist; reference the web component and note Flow-specific additions;
state which universal requirements are relevant and where the component adds
something; list the theme variants and confirm they match the web component's
`theme` attribute.
