# Flow Component Design Guidelines

This document captures high-level design principles for Vaadin Flow components — the Java server-side wrappers around the Vaadin web components. These guidelines inform feature design, API shape decisions, and spec authoring — before any implementation work begins.

For implementation details, see [FLOW_COMPONENT_GUIDELINES.md](FLOW_COMPONENT_GUIDELINES.md).

For the design principles of the underlying web components, see `DESIGN_GUIDELINES.md` in the web-components repository; the Flow component's API must stay consistent with that design.

---

## Wrap, don't reinvent

Every Flow component is a Java wrapper around an existing Vaadin web component. The wrapper's primary job is to surface the web component's capabilities to server-side Java code in a Flow-idiomatic way — NOT to redesign them.

**What this means in practice:**

- The web component is the source of truth for behaviour, DOM structure, theme variants, slots, shadow parts, and CSS custom properties. The Flow component must match the web component's semantics, not contradict them.
- If the web component exposes a property `disabled`, the Flow component's `setDisabled(boolean)` (usually via `HasEnabled`) must map to that property — not to something the Flow layer invents.
- If the web component exposes an event `opened-changed`, the Flow event listener must listen for that DOM event via `@DomEvent("opened-changed")` — not synthesise a separate Flow-only event.
- Theme variant enums (`ButtonVariant`, `SideNavVariant`, etc.) MUST use the same variant names as the web component's `theme` attribute accepts, so that both sides stay in lockstep.
- The `@NpmPackage` version in the Flow component class MUST match the web component version it is validated against; changing one without the other produces silently broken runtime behaviour.

**Why:**

- Every divergence between the Flow API and the web component API becomes an integration bug waiting to happen. Developers who read the web component docs expect the Flow API to reflect them.
- Flow components that invent parallel concepts (a Flow-only property with no web-component counterpart) force the Flow layer to keep more state than it needs to, duplicate logic, and drift over time.
- Two Vaadin layers disagreeing about the same concept is the worst kind of bug: each layer is internally consistent, so the error only surfaces at the seam.

---

## Progressive disclosure through constructors

The common case should be one line of Java. Complex configuration should be possible but opt-in.

Flow components achieve this with **heavily overloaded constructors** that cover the frequent combinations directly, rather than forcing a builder pattern or long setter chains.

**Example:**

```java
new Button();                                      // empty
new Button("Save");                                // text
new Button(icon);                                  // icon
new Button("Save", icon);                          // text + icon
new Button("Save", e -> save());                   // text + click listener
new Button("Save", icon, e -> save());             // text + icon + listener
new Button(textSignal);                            // reactive text
new Button(textSignal, icon, e -> save());         // reactive text + icon + listener
```

**Rules:**

- Each overload must correspond to a real use case seen in application code — do not add constructors just because the combinatorial matrix has a gap.
- Do NOT chain setters in a builder-style DSL. Flow components are setter-based, not fluent. Fluent chains read like domain-specific languages and do not compose with framework conventions like `Binder` and dependency injection.
- When a reactive (Signal-based) variant of a constructor parameter exists, provide it in the same position in a parallel constructor.

**Why:**

- The single-line-to-common-case principle keeps Flow application code readable: most buttons in a real application are created as `new Button("Save", e -> save())`, and that must not require boilerplate.
- Overloaded constructors make the component's intended uses discoverable through IDE autocompletion.
- Refusing fluent chains keeps Flow consistent with the rest of Vaadin Flow and Jakarta: everything is setter-based so that framework tooling (validation, binding, state restoration) sees the same shape.

---

## Imperative API first, reactive Signal API alongside

Every mutable state has two APIs: a traditional setter/getter AND a `bind*` variant that accepts a `Signal<T>`.

**Example:**

```java
// Imperative
button.setText("Save");
String text = button.getText();

// Reactive
SignalBinding<String> binding = button.bindText(textSignal);
```

**Rules:**

- Both APIs must produce the same effect on the underlying web component and fire the same events; a developer switching from one to the other must observe no difference except the source of the value.
- When a signal binding is active, the imperative setter MUST throw `BindingActiveException` (for one-way bindings). This is how developers discover that something else owns the state.
- Signal bindings are an extension, not a replacement: the imperative API is always the canonical form. Documentation and examples lead with the imperative form.
- For the detailed rules on binding implementation, see `.claude/skills/signal-rules/SKILL.md`.

**Why:**

- Imperative APIs are what existing Flow code uses; removing them would break every application ever written with Flow.
- Signal bindings compose with Flow's reactive side (signals, computed values, effects) and eliminate the manual listener plumbing that reactive code otherwise requires.
- Throwing on conflicts surfaces bugs where two authors of the same state fight each other — otherwise the race condition hides until production.

---

## Compose via mixin interfaces, not inheritance

Flow components use narrow interfaces (`HasText`, `HasEnabled`, `HasSize`, `HasStyle`, `HasLabel`, `HasTooltip`, `HasPrefix`, `HasSuffix`, `HasThemeVariant<E>`, `ClickNotifier<T>`, `Focusable<T>`, `HasValue<E, V>`, `HasValidationProperties`, `HasComponents`, `HasOrderedComponents`, etc.) to add capabilities. Each interface contributes default methods that operate on `getElement()` so that any component implementing it gets the behaviour for free.

**Rules:**

- Implement a standard interface if one matches the behaviour you need. Do not introduce parallel methods (`setAriaLabel` on a component that already has `HasAriaLabel`) that duplicate interface contracts.
- If the same capability appears in two components, the capability belongs in a shared `Has…` interface in `vaadin-flow-components-shared-parent/vaadin-flow-components-base` — not duplicated in each component.
- A new component's class declaration should read as a list of capabilities: `public class Button extends Component implements ClickNotifier<Button>, Focusable<Button>, HasAriaLabel, HasEnabled, HasPrefix, HasSize, HasStyle, HasSuffix, HasText, HasThemeVariant<ButtonVariant>, HasTooltip`.
- Do not extend intermediate abstract classes ("AbstractClickableComponent", "AbstractField") unless one already exists and is used by several components. Favour composition.

**Why:**

- Mixin interfaces let each capability evolve independently: a fix in `HasTooltip` reaches every tooltip-capable component for free.
- Inheritance hierarchies force premature commitment. Composition lets a component pick up a capability at any point in its lifetime without restructuring.
- Tests become simpler: when Button needs prefix/suffix support, it is enough that the tests verify the contract of `HasPrefix`/`HasSuffix` (which already has its own shared tests) rather than re-testing the behaviour inside Button.

---

## Differentiation drives scope

Every Flow component's documentation must explicitly name the adjacent Flow components and patterns that it is NOT, and state what each of those does that this component does not handle — and vice versa.

This is the same rule as in the web component guidelines: the web component and its Flow wrapper share the same scope, and so they share the same boundary. The Flow wrapper MUST NOT introduce responsibilities that the web component does not support; if it does, the divergence becomes a cross-layer bug.

**Examples of boundary differentiation:**

- `Button` is NOT an anchor — use `Anchor` for navigation that should render as a plain `<a href>`.
- `Dialog` is NOT a notification — use `Notification` for non-modal transient announcements.
- `Select` is NOT a `ComboBox` — Select is for a fixed, short list of options; ComboBox is for filterable, potentially lazy-loaded lists.
- `SideNav` is NOT a `Tabs` — SideNav is vertical hierarchical navigation; Tabs is flat, horizontal, section-switching.
- `Accordion` is NOT `Details` — Accordion is a group with single-open semantics; Details is a standalone expandable region.
- `Grid` is NOT a `VirtualList` — Grid is tabular with columns and cells; VirtualList is a 1-dimensional rendered list.

If a behaviour is requested that blurs the boundary, the correct answer is usually "use the other component" — not to grow this one.

---

## Integrate with Flow Router

The underlying web components are router-agnostic by design (they render plain `<a href>` and expect whatever SPA router is installed to intercept clicks). Flow, however, IS the opinionated layer: every Flow application has exactly one router — `com.vaadin.flow.router.Router` — and the Flow wrappers MUST make that router's concepts first-class.

**What this means in practice:**

- Navigational components accept `Class<? extends Component>` that refers to a `@Route`-annotated view, not just opaque path strings. Example: `new SideNavItem("Home", HomeView.class)`.
- They resolve URLs by reading `@Route` / `@RouteAlias` via `RouteConfiguration`, so renaming a route updates the navigation automatically and typos fail at compile time.
- Parameterised routes use Flow's `RouteParameters` / `QueryParameters` / `HasUrlParameter` APIs — the same types an application uses elsewhere.
- Shell components like `AppLayout` implement `RouterLayout` so that Flow's router can treat them as the target of route-view nesting.
- Do NOT accept paths as opaque strings *only*. A string-based overload can coexist for cases where the application really does not have a Flow class (external links, hand-managed paths), but the type-safe `Class<? extends Component>` overload is the primary form.

**Why:**

- The whole point of Flow is compile-time-safe, type-driven server-side code. A navigation API that forces developers to write `"users/42/settings"` instead of `UserSettingsView.class` throws away Flow's key benefit.
- Real applications rename, move, and refactor views. Router-integrated APIs update with the refactor; string-based APIs silently break.
- Flow Router handles history, scroll restoration, before-enter checks, and `@RouteAlias` redirects correctly; replicating any of that at the component level duplicates logic that already works.

**Preserving the underlying web component's accessibility:**

- The web component renders a plain `<a href>` under the hood. The Flow wrapper should still set a correct `href` (resolved via `RouteConfiguration`) so that middle-click, "open in new tab", "copy link address", and screen-reader link semantics all work. Do NOT replace the anchor with a click handler that calls `UI.navigate(...)`.
- Flow Router automatically intercepts clicks on anchors pointing to known routes, so the UX is "SPA navigation when possible, full-page load as a correct fallback" — without the component having to know the difference.

---

## Prefer positive-form boolean APIs

Web component boolean attributes work by presence/absence: `disabled` present means disabled, `disabled` absent means enabled. This forces web components to name every boolean attribute for the NON-DEFAULT state — you cannot have an `enabled` attribute because the default (enabled) would require setting the attribute on every element.

Flow has no such constraint. Java methods take `boolean` parameters, so the API can — and should — use the positive, natural-language form that reads most clearly in application code.

**What this means in practice:**

- Name the Flow API for the concept, not the web component attribute. Use `setEnabled(true/false)`, not `setDisabled(true/false)`. Use `setVisible(true/false)`, not `setHidden(true/false)`.
- The mapping from the positive-form Java API to the negative-form HTML attribute is the wrapper's job and is invisible to the developer. `setEnabled(false)` internally sets the `disabled` property on the element; the developer never needs to think about that.
- When the web component attribute already uses the natural-language form (`readonly`, `required`, `opened`), keep the same polarity in Java: `setReadOnly(true)` maps to `readonly`, `setOpened(true)` maps to `opened`. No flip needed.
- The getter follows the same polarity as the setter: `isEnabled()`, `isVisible()`, `isReadOnly()`, `isOpened()`.

**Existing examples in the codebase:**

| Flow API | Web component attribute | Polarity |
| --- | --- | --- |
| `setEnabled(false)` | `disabled` added | Flipped — Java uses the positive form |
| `setVisible(false)` | `hidden` added | Flipped — Java uses the positive form |
| `setReadOnly(true)` | `readonly` added | Same — attribute is already natural |
| `setOpened(true)` | `opened` added | Same — attribute is already natural |
| `setRequiredIndicatorVisible(true)` | `required` added | Same — attribute is already natural |

**Why:**

- `button.setEnabled(false)` is immediately clear. `button.setDisabled(true)` requires a mental double-negative when asking "is this button active?" — especially inside conditionals: `if (!button.isDisabled())` vs `if (button.isEnabled())`.
- Consistency with standard Java and Swing conventions, where `setEnabled`, `setVisible`, and `setEditable` are the established names.
- The web component naming constraint is an HTML platform limitation, not a design choice. The Flow layer exists precisely to provide a better developer experience on top of the platform, and using natural-language naming is one of the ways it does that.

---

## Consistency over novelty

Look first at how the closest existing Flow component solves the problem. Match its conventions before inventing a new one.

**Concrete conventions to match:**

- Getters: `get{Property}()` for objects, `is{Property}()` for booleans.
- Setters: `set{Property}(value)`. If the property is nullable, say so in Javadoc and accept `null`.
- Lists: `add{Item}(item)` / `remove{Item}(item)` / `add{Item}(item...)` varargs / `setItems(...)`.
- Listeners: `add{Event}Listener(ComponentEventListener<E> listener)` returning `Registration`. No `removeXxxListener` — developers unregister via the returned `Registration`.
- Signal bindings: `bind{Property}(Signal<T> signal)` returning `SignalBinding<T>`.
- Theme variants: `addThemeVariants(V...)` / `removeThemeVariants(V...)` / `setThemeVariants(V...)` / `bindThemeVariant(V, Signal<Boolean>)` — all provided by `HasThemeVariant<V>`.
- Slots: `set{Slot}Component(Component)` / `get{Slot}Component()` (e.g. `setPrefixComponent`).
- Events: nested `public static class XxxEvent extends ComponentEvent<ThisComponent>`, annotated `@DomEvent("...")`.
- I18n: nested or companion class named `{Component}I18n`, Jackson-serialisable POJO, `getI18n()` / `setI18n({Component}I18n)`.

**Why:**

- A developer who has used any Flow component must be able to pick up a new one without re-learning naming conventions.
- Consistency makes automated tooling (Binder, DevTools, copilots, AI coding agents) work the same everywhere.
- Novelty in naming or shape creates cost for every reader forever. The only justification for deviating is that a genuine requirement makes the standard shape impossible; document the deviation when that happens.

---

## Completeness over minimalism

Every requirement the web component supports must be reachable from Flow. Missing API in the wrapper is a defect, not a design choice.

**Rules:**

- If the web component supports a feature (a property, a slot, an event, a theme variant), the Flow wrapper MUST expose it — even if the team believes "no one will use it in Java."
- When a feature cannot be surfaced cleanly without infrastructure work (a new mixin interface, a new shared controller), do that work rather than leaving the feature inaccessible.
- Missing coverage is particularly bad for accessibility: if the web component exposes `aria-label`, the Flow component must implement `HasAriaLabel`. Otherwise screen-reader-dependent users cannot use the component.

**Counter-balance — no bloat:** every method, constructor, annotation, and field must trace back to at least one requirement or an API already established in the web component. Speculative API is removed.

The tension between "completeness" and "no bloat" produces an API that covers the real surface of the web component without inventing anything that isn't already there.

---

## Ship new components as experimental

A new Flow component enters the library behind a Vaadin feature flag and graduates to stable only after its API has been validated in practice.

**What this means in practice:**

- The web component declares `static experimental = true`; the Flow wrapper MUST be gated by the corresponding Vaadin feature flag (see `com.vaadin.experimental.FeatureFlags`).
- The flag is checked at runtime in the component's attach handler (or constructor) so that applications that have not opted in cannot accidentally use an unstable API.
- Integration tests for an experimental component enable the flag via `src/main/resources/vaadin-featureflags.properties` in the integration-tests module, or via the `EnableFeatureFlagExtension` JUnit extension in unit tests.
- Javadoc on the public class states that the component is experimental and that its API may change before it becomes stable.
- Breaking changes to an experimental component's API do NOT require a deprecation cycle. Once the component graduates, changes follow the normal Vaadin deprecation rules.

**Why:**

- The ideal API is hard to know from specification alone. Feedback from real usage — and from the surrounding Flow ecosystem — surfaces issues that no amount of up-front design catches.
- The feature flag makes it safe to publish early; applications that do not opt in are never affected by churn in an unstable API.
- Once released without a flag, an API is effectively frozen: the cost of changing it after real applications depend on it is prohibitive.

---

## Server/client split is an implementation detail

Flow components wrap web components, which means state flows between the Java VM and the browser. The split is a mechanical reality, but it MUST NOT leak into the public API shape.

**Concrete rules:**

- Developers MUST NOT have to understand when a property is synced from the client via `@Synchronize`, when it is pushed via `setProperty`, or when it requires `executeJs` — the public API looks like ordinary Java object state.
- If a property requires a JavaScript connector to work (e.g. data-heavy components), the connector lives in `src/main/resources/META-INF/resources/frontend/{name}Connector.js`, is loaded via `@JsModule`, and is initialised in the component's attach handler. None of this is visible in the public Javadoc.
- Re-attachment: connectors and `Element.executeJs` calls MUST run in the `attach` handler (not the constructor or `onEnter`) because Flow may create a fresh client-side element for the same server-side component instance.
- Errors that originate at the client (network issues, JavaScript exceptions) MUST be translated into meaningful server-side state or events, not leaked as raw JavaScript stack traces.

**Why:**

- Developers using Flow expect the "one language" promise. Forcing them to reason about the two-side architecture defeats the entire value proposition.
- Re-attachment bugs are among the hardest to diagnose because they only appear when a component is removed and re-added. Baking the attach-handler rule into the architecture prevents a whole class of bugs.

---

## Declarative (XML/Lumo) compatibility remains

Flow components are sometimes consumed through Lit templates, Hilla, or server-side templating. The component's default state (zero-argument constructor + defaults) MUST produce something meaningful that renders correctly with no further calls.

**Rules:**

- A new component without any configuration renders without errors or blank space — it either shows a placeholder, or is visually empty but semantically correct.
- Required dependencies (web component npm package, connector JS, etc.) are declared with `@NpmPackage` and `@JsModule` on the class so that `new Component()` in any context triggers the right client-side bundle.
- Components must not require an application startup step beyond adding them to the UI (e.g. no "register this component with a factory at startup" or "call `Component.init()` before use").

**Why:**

- Real applications compose many components. If one of them requires a non-trivial setup step, every application must remember to call it — and eventually some application forgets.
- Frameworks (Spring, CDI) often construct components lazily or in contexts without a live UI. Components that need only their constructor + the standard Flow lifecycle work in all of these.

---

## Universal behavioural requirements

Certain behaviours apply to EVERY Flow component. These are enforced at the web component, mixin, or framework level — they are not optional per component.

**Individual components' `requirements.md` files MUST NOT restate these as per-component requirements.** Repeated requirements drift: if "must support RTL" appears in fifty specs, some will get the phrasing wrong, some will add subtle contradictions, and the canonical list becomes impossible to evolve.

A Flow component's documentation may mention a universal concern only when the component genuinely adds something on top of the universal rule:

- A concrete default it introduces (e.g. `SideNav` defaulting to `aria-label="Main navigation"`).
- A component-specific extension or override.
- A specific interaction pattern not pinned down by the universal rule.

### Every interactive element has an accessible name

Every focusable element, button, link, control, and landmark region the component contributes has an accessible name. When the name comes from visible text it is automatic; when it does not (icon-only buttons, landmarks, dismiss controls, overflow controls), the Flow component MUST expose a way for the application to supply one — typically by implementing `HasAriaLabel` or by exposing a dedicated `set{Label}(String)` method.

### Localisable labels and error messages

Any text the component renders itself — landmark labels, placeholder strings, internal button labels ("More", "Clear selection", "Open menu"), error messages, tooltip defaults — MUST be customisable from Java. This is typically done through the component's `I18n` class (`ComboBoxI18n`, `UploadI18n`, etc.). Defaults are sensible English fallbacks, never hard-coded strings the application cannot change.

### Focus order follows visual order

Flow components inherit their DOM structure from the underlying web component. If the web component has focus-order issues, fix them there — do NOT compensate on the Flow side (e.g. by manipulating `tabindex` from Java). The focus order rule is owned by the web component.

### Right-to-left layout support

The web component is responsible for RTL layout. The Flow wrapper MUST NOT introduce per-direction branches, CSS, or behaviour of its own. If the Flow component sends icons or directional text to the web component, it relies on the component to render them correctly under RTL.

### Serialisation

Every Flow component, and every data object the component exposes (i18n objects, items in `setItems`, event objects, etc.) MUST be `Serializable`. Vaadin sessions may be persisted to disk, and a non-serialisable component breaks that invariant. Always add a `{Component}SerializableTest` for this.

### Signals do not replace imperative state

Even when a component exposes `bind*` APIs, its imperative setters MUST continue to work. A developer who has never heard of signals must still be able to use every component using only `setXxx`/`getXxx`.

### JSON construction uses Jackson, never string concatenation

When a component serialises data to push to the client (i18n JSON, item JSON, configuration JSON), it MUST use the Jackson 3 (`tools.jackson`) APIs provided by Flow (`JacksonUtils`, `ObjectNode`, `@JsonInclude`). Hand-written JSON via string concatenation or `String.format` is banned: it breaks on edge cases (nested quotes, null, Unicode, dates) in ways that are easy to miss in tests and catastrophic at runtime.

---

## Relationship to specs

Spec files for a Flow component (under `specs/` or alongside the corresponding web component spec) MUST:

- Show both an imperative usage example AND a reactive (Signal-based) example whenever both APIs exist.
- Reference the underlying web component and note any Flow-specific additions.
- Explicitly state which universal requirements are relevant and where the component adds something specific on top.
- List the theme variants the component exposes and confirm they match the web component's `theme` attribute.
