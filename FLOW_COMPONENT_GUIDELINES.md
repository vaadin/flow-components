# Flow Component Creation Guidelines

This document provides step-by-step guidelines for **implementing** Vaadin Flow components — the Java server-side wrappers around Vaadin web components. It covers module layout, class structure, mixin usage, signals, events, theme variants, i18n, connectors, testing, and packaging.

For high-level design principles, see [DESIGN_GUIDELINES.md](DESIGN_GUIDELINES.md).

For the underlying web component implementation, see the `WEB_COMPONENT_GUIDELINES.md` in the web-components repository — the Flow wrapper relies on and must stay consistent with that component.

---

## Table of Contents

1. [Overview & Prerequisites](#overview--prerequisites)
2. [Naming Conventions](#naming-conventions)
3. [Module & File Structure](#module--file-structure)
4. [Component Implementation](#component-implementation)
5. [Mixin Interfaces](#mixin-interfaces)
6. [Signals & Reactive APIs](#signals--reactive-apis)
7. [Events](#events)
8. [Slots, Prefix/Suffix & Content Composition](#slots-prefixsuffix--content-composition)
9. [Theme Variants](#theme-variants)
10. [I18n](#i18n)
11. [Data Providers & Data Views](#data-providers--data-views)
12. [Validation](#validation)
13. [Connectors (JavaScript Glue)](#connectors-javascript-glue)
14. [Feature Flags & Experimental Components](#feature-flags--experimental-components)
15. [TestBench Elements](#testbench-elements)
16. [Unit Testing](#unit-testing)
17. [Integration Testing](#integration-testing)
18. [Documentation (Javadoc)](#documentation-javadoc)
19. [Accessibility](#accessibility)
20. [Package Configuration (Maven)](#package-configuration-maven)
21. [Common Patterns](#common-patterns)
22. [Checklist](#checklist)

---

## Overview & Prerequisites

### Technology Stack

- **Java 21+**
- **Maven** (multi-module parent-child)
- **Vaadin Flow 25+**
- **JUnit 6 (Jupiter)** for unit tests
- **JUnit 4** for integration tests (driven by TestBench)
- **Mockito** for mocking in unit tests
- **Vaadin TestBench + Selenium** for browser-based integration tests
- **Jetty** for running integration-test servers
- **Jackson 3** (`tools.jackson.*` for core/databind classes; `com.fasterxml.jackson.annotation.*` for annotations, which remain in the old package) for JSON serialisation

### Required Knowledge

- Vaadin Flow basics (`Component`, `Element`, `UI`, attach/detach lifecycle)
- Vaadin web components (the component is a wrapper around one)
- Mixin-interface composition (`HasEnabled`, `HasSize`, `HasStyle`, `HasTheme`, …)
- Flow signals and `SignalBinding`
- JUnit Jupiter for unit tests; JUnit 4 + TestBench for integration tests
- Maven lifecycle basics

### Important Ground Rules

- **Never construct JSON manually via string concatenation.** Always use Jackson 3 (data classes from `tools.jackson.databind.*`, annotations from `com.fasterxml.jackson.annotation.*`) and the `JacksonUtils` helper.
- **Every public API class, data class, and event object MUST implement `Serializable`** — Vaadin sessions may be persisted to disk.
- **Connectors and `executeJs` calls run in the `attach` handler**, never in the constructor — Flow may create a new client-side element for the same server-side component instance.
- **Signal binding rules are enforced.** Read `.claude/skills/signal-rules/SKILL.md` before writing or modifying `bind*` methods.

---

## Naming Conventions

### Parent Module Name

- **Pattern:** `vaadin-{component}-flow-parent`
- **Examples:** `vaadin-button-flow-parent`, `vaadin-date-picker-flow-parent`

### Java Package

- **Component API:** `com.vaadin.flow.component.{component}` (lowercase, dots, no hyphens — e.g. `datepicker` not `date-picker`)
- **Variants / inner classes:** same package
- **Data views (for data-driven components):** `com.vaadin.flow.component.{component}.dataview`
- **TestBench elements:** `com.vaadin.flow.component.{component}.testbench`
- **Unit tests:** `com.vaadin.flow.component.{component}.tests`
- **Integration tests:** `com.vaadin.flow.component.{component}.tests` (dominant convention — 42 of 45 components use `.tests`; a few legacy ones use `.test`, avoid that in new code)

### Class Names

- **Component class:** PascalCase name, no `Vaadin` prefix, no `Flow` suffix
  - `Button`, `ComboBox`, `DatePicker`, `SideNav`
- **Theme variants:** `{Component}Variant` (enum)
- **I18n class:** `{Component}I18n`
- **TestBench element:** `{Component}Element`
- **Event classes:** `{Component}{EventName}Event` (usually nested)
- **Test classes:**
  - Unit: `{Component}Test`, `{Component}SignalTest`, `{Component}VariantTest`, `{Component}SerializableTest`, `{Component}I18nTest`
  - Integration: `{Component}IT` (test class), `{Component}Page` (test view; `{Component}View` is also seen but `Page` is the dominant convention — ~87% of IT views)

### Web Component Tag

- **Pattern:** `vaadin-{component}` — matches the tag in the web components repository.

---

## Module & File Structure

Each component lives in a parent Maven module that contains three child modules.

```
vaadin-{component}-flow-parent/
├── pom.xml                                              # Parent pom; aggregates children
├── README.md
├── LICENSE                                              # Apache 2.0 or Commercial
├── vaadin-{component}-flow/                             # Main Flow component module
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/vaadin/flow/component/{name}/
│       │   │   ├── {Component}.java                     # Main class
│       │   │   ├── {Component}Variant.java              # Theme variants enum (if any)
│       │   │   └── {Component}I18n.java                 # I18n POJO (if any)
│       │   └── resources/META-INF/resources/frontend/
│       │       └── {component}Connector.js              # Optional JS connector
│       └── test/
│           └── java/com/vaadin/flow/component/{name}/tests/
│               ├── {Component}Test.java                 # JUnit Jupiter
│               ├── {Component}SignalTest.java           # If component has bind* APIs
│               ├── {Component}VariantTest.java          # If component has theme variants
│               └── {Component}SerializableTest.java     # Always add this
├── vaadin-{component}-testbench/                        # TestBench Element module
│   ├── pom.xml
│   └── src/main/java/com/vaadin/flow/component/{name}/testbench/
│       └── {Component}Element.java
└── vaadin-{component}-flow-integration-tests/           # Integration tests
    ├── pom.xml
    ├── src/main/
    │   ├── java/com/vaadin/flow/component/{name}/tests/
    │   │   └── {Component}Page.java                     # Test views with @Route
    │   ├── resources/
    │   │   └── vaadin-featureflags.properties           # Enable experimental features
    │   └── webapp/                                       # Optional static resources
    └── src/test/
        └── java/com/vaadin/flow/component/{name}/tests/
            └── {Component}IT.java                       # JUnit 4 browser tests
```

Notes:

- The `vaadin-{component}-flow-integration-tests` module is included in the default Maven profile but excluded from releases — see the `profiles` block in the parent `pom.xml`.
- Copy the layout from an existing small component (Button is the reference) when creating a new one.

### Copyright Headers

Every `.java`, `.js`, `.xml`, and `.properties` file carries a copyright header. Two variants exist depending on the component's license.

**Apache 2.0** (most components):

```java
/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
```

**Commercial** (Grid Pro, Charts, Spreadsheet, Dashboard, Board, CRUD, Map, Rich Text Editor, etc.):

```java
/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
```

Always use `2000-2026` — the range starts at the founding year of Vaadin regardless of file age. Use `spotless:apply` to enforce formatting.

---

## Component Implementation

### The Main Class

A Flow component class:

1. Extends `com.vaadin.flow.component.Component` (or `AbstractSinglePropertyField<C, V>` for value-bearing fields, `AbstractField<C, T>` for richer fields).
2. Declares `@Tag`, `@NpmPackage`, `@JsModule` annotations.
3. Implements mixin interfaces for capabilities (`HasText`, `HasEnabled`, `HasSize`, …).
4. Provides overloaded constructors covering common use cases (empty / text / icon / value / signal / listener / combinations).
5. Delegates state to `getElement().setProperty(...)` or to helper objects like `SignalPropertySupport`.

**Minimal skeleton:**

```java
/*
 * Copyright 2000-2026 Vaadin Ltd. ...
 */
package com.vaadin.flow.component.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;

/**
 * Example is a short description of what the component does.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-example")
@NpmPackage(value = "@vaadin/example", version = "25.2.0-alpha7")
@JsModule("@vaadin/example/src/vaadin-example.js")
public class Example extends Component
        implements HasEnabled, HasSize, HasStyle,
        HasThemeVariant<ExampleVariant> {

    public Example() {
    }

    public Example(String value) {
        this();
        setValue(value);
    }

    public void setValue(String value) {
        getElement().setProperty("value", value == null ? "" : value);
    }

    public String getValue() {
        return getElement().getProperty("value", "");
    }
}
```

### Annotations

- `@Tag("vaadin-{name}")` — required. Maps the Java class to the web component's HTML tag.
- `@NpmPackage(value = "@vaadin/{name}", version = "25.x.y")` — required. Declares the npm dependency. The version must match the version of the published web component; updating one without the other produces silent runtime breakage.
- `@JsModule("@vaadin/{name}/src/vaadin-{name}.js")` — required. Loads the web component module.
- `@JsModule("./{name}Connector.js")` — optional. Loads a Flow-specific connector (see [Connectors](#connectors-javascript-glue)).
- `@CssImport(...)` — rare; component styling is handled by the web component's theme bundle, not by the Flow wrapper.

### Constructors — Progressive Disclosure

Provide overloaded constructors for the most common use cases. Do not replace them with a fluent/builder API.

**Example (Button — 11 overloads):**

```java
public Button() {}
public Button(String text) { this(); setText(text); }
public Button(Signal<String> textSignal) { this(); bindText(textSignal); }
public Button(Component icon) { this(); setIcon(icon); }
public Button(String text, Component icon) { this(); setIcon(icon); setText(text); }
public Button(Signal<String> textSignal, Component icon) { this(); setIcon(icon); bindText(textSignal); }
public Button(String text, ComponentEventListener<ClickEvent<Button>> clickListener) { this(); setText(text); addClickListener(clickListener); }
public Button(Signal<String> textSignal, ComponentEventListener<ClickEvent<Button>> clickListener) { ... }
public Button(Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) { ... }
public Button(String text, Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) { ... }
public Button(Signal<String> textSignal, Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) { ... }
```

Reach for constructor overloads — not setter chains — when adding a new common-case shortcut.

### Working with the Element API

Flow components work primarily through the `Element` object:

```java
getElement().setProperty("disabled", true);                  // Sync property
getElement().getProperty("value", "");                       // Read property
getElement().setAttribute("theme", "primary");               // HTML attribute
getElement().executeJs("this.focus()");                      // Run JS on client
getElement().callJsFunction("open");                         // Call a method
getElement().addEventListener("my-event", e -> ...);         // DOM event listener
getElement().appendChild(child.getElement());                // Add child
```

**Rules:**

- For simple scalar state, `setProperty` / `getProperty` is sufficient.
- For state that must throw when a signal binding is active, use `Element.bindProperty` (returns the corresponding `bind*` implementation) — or use `SignalPropertySupport` for custom logic (see next section).
- Never reach past `getElement()` to manipulate the DOM of child elements directly. Use child `Component` instances.

### Boolean Property Naming — Positive Form

Web component boolean attributes work by presence/absence (e.g. `disabled` present = disabled), which forces them to use the non-default state as the attribute name. The Flow wrapper is free from this constraint and SHOULD use the positive, natural-language form instead. See [DESIGN_GUIDELINES.md → Prefer positive-form boolean APIs](DESIGN_GUIDELINES.md#prefer-positive-form-boolean-apis) for the rationale.

**Implementation pattern — polarity flip:**

```java
// Flow API: positive form
public void setEnabled(boolean enabled) {
    getElement().setEnabled(enabled);
    // Internally, this maps to the web component's `disabled` property
}

public boolean isEnabled() {
    return getElement().isEnabled();
}
```

The `HasEnabled` interface from Flow already handles this mapping. When you implement a new boolean state that doesn't have a pre-built mixin:

```java
// Custom boolean state — flip the polarity at the element boundary
public void setEditable(boolean editable) {
    getElement().setProperty("readonly", !editable);
}

public boolean isEditable() {
    return !getElement().getProperty("readonly", false);
}
```

When the web component attribute already uses the natural form (`opened`, `required`), keep the same polarity — no flip:

```java
public void setOpened(boolean opened) {
    getElement().setProperty("opened", opened);
}
```

---

## Mixin Interfaces

Flow capabilities are delivered through `Has*` interfaces that provide default methods. A new component implements the interfaces it needs.

### Standard Flow Interfaces (from `flow-server`)

| Interface | What it adds |
| --- | --- |
| `HasText` | `setText(String)` / `getText()` |
| `HasEnabled` | `setEnabled(boolean)` / `isEnabled()`, propagates to disabled attribute |
| `HasSize` | `setWidth`, `setHeight`, `setMinWidth`, `setMaxWidth`, etc. |
| `HasStyle` | `addClassName`, `removeClassName`, `getStyle()` inline styles |
| `HasLabel` | `setLabel(String)` / `getLabel()` |
| `HasHelper` | Helper text + helper component slot |
| `HasPlaceholder` | `setPlaceholder(String)` |
| `HasTheme` | `addThemeName`, `removeThemeName`, `getThemeNames()` |
| `HasAriaLabel` | `setAriaLabel(String)`, `setAriaLabelledBy(String)` |
| `Focusable<T>` | `focus()`, `setTabIndex(int)`, focus listeners, focus shortcuts |
| `ClickNotifier<T>` | `addClickListener`, `addClickShortcut` |
| `HasComponents` | `add(Component...)`, `remove(Component...)`, `removeAll()` |
| `HasOrderedComponents` | Ordered add/remove/replace (layouts) |
| `HasValue<E, V>` | Value + `ValueChangeEvent` + `addValueChangeListener` |
| `HasValueAndElement<E, V>` | `HasValue` + `HasElement` + read-only + required indicator |
| `HasValidation` | Validation state (`isInvalid`, `setErrorMessage`) |
| `HasValidator<V>` | `getDefaultValidator()` for `Binder` integration |

### Shared Flow-Components Mixins (from `vaadin-flow-components-base`)

Located at `com.vaadin.flow.component.shared.*`:

| Interface | What it adds |
| --- | --- |
| `HasThemeVariant<TVariantEnum>` | Typed theme variants: `addThemeVariants`, `removeThemeVariants`, `setThemeVariants`, `bindThemeVariant`, `bindThemeVariants` |
| `HasTooltip` | `setTooltipText`, `setTooltipMarkdown`, `getTooltip()` — via a `<vaadin-tooltip slot="tooltip">` child |
| `HasPrefix` | `setPrefixComponent`, `getPrefixComponent` — slot "prefix" |
| `HasSuffix` | `setSuffixComponent`, `getSuffixComponent` — slot "suffix" |
| `HasClearButton` | Show/hide a clear button in input fields |
| `HasAutoOpen` | `setAutoOpen(boolean)` for overlays that auto-open on interaction |
| `HasAllowedCharPattern` | Character-filter pattern for input |
| `HasValidationProperties` | `setErrorMessage` / `setInvalid` via element properties |
| `InputField<E, V>` | Aggregate interface: `HasEnabled + HasHelper + HasLabel + HasSize + HasStyle + HasTooltip + HasValue` |
| `SelectionPreservationHandler<T>` | Preserve selection across data-provider refreshes |

### Example Composition

```java
public class Button extends Component
        implements ClickNotifier<Button>, Focusable<Button>, HasAriaLabel,
        HasEnabled, HasPrefix, HasSize, HasStyle, HasSuffix, HasText,
        HasThemeVariant<ButtonVariant>, HasTooltip {
    // ...
}
```

A new capability should go into a shared mixin interface in `vaadin-flow-components-base` when it applies to more than one component. Duplication across components is a bug, not a style choice.

---

## Signals & Reactive APIs

### Overview

Flow 25 introduces `Signal<T>` — a reactive value that can be bound to a component property so that changes to the signal automatically propagate.

Every mutable state on a component has two APIs:

- **Imperative:** `setXxx(value)` / `getXxx()` — traditional.
- **Reactive:** `bindXxx(Signal<T> signal)` returning `SignalBinding<T>` — the component tracks the signal.

### SignalPropertySupport Pattern

For properties that need both imperative AND reactive variants, use `SignalPropertySupport<T>`:

```java
private final SignalPropertySupport<String> textSupport = SignalPropertySupport
        .create(this, this::textChangeHandler);

@Override
public void setText(String text) {
    textSupport.set(text);                         // throws if a binding is active
}

@Override
public String getText() {
    return textNode.getText();
}

@Override
public SignalBinding<String> bindText(Signal<String> textSignal) {
    return textSupport.bind(textSignal);
}

private void textChangeHandler(String text) {
    // Any side-effect that should run for both imperative and reactive updates
    textNode.setText(text);
    updateIconSlot();
    updateThemeAttribute();
}
```

The `textChangeHandler` (the change handler passed to `SignalPropertySupport.create`) MUST run all side-effects so that imperative and reactive updates produce identical behaviour.

### Rules (See `.claude/skills/signal-rules/SKILL.md`)

1. **Imperative APIs must throw if a one-way signal binding is active.** `BindingActiveException` is thrown automatically by `Element.bindProperty` and `SignalPropertySupport`. Do not roll your own.
2. **One-way bindings are not allowed on state the component controls internally.** If the component itself updates `invalid` via value-change validation, there must NOT be a one-way `bindInvalid(Signal<Boolean>)` API. Stop and ask the user if you encounter this case.
3. **Signal bindings run the same side-effects as imperative APIs.** For `Element.bindProperty`, register an `onChange` callback on the returned `SignalBinding`. For `SignalPropertySupport`, put the side-effects in the change handler.

### Typical `bind*` Methods

Add reactive variants alongside imperative setters for properties developers will want to drive reactively:

- `bindText(Signal<String>)`
- `bindEnabled(Signal<Boolean>)` (on `HasEnabled`-implementing components)
- `bindVisible(Signal<Boolean>)`
- `bindValue(Signal<T>)` — for fields; typically two-way, requires a write callback
- `bindReadOnly(Signal<Boolean>)`
- `bindError(Signal<String>)`
- `bindThemeVariant(TVariantEnum, Signal<Boolean>)` / `bindThemeVariants(Signal<List<TVariantEnum>>)` — provided by `HasThemeVariant`

### Constructor Variants with Signals

If the component has a `bindText` API, add constructors that accept a `Signal<String>` in place of a `String`:

```java
public Example(Signal<String> valueSignal) { this(); bindValue(valueSignal); }
```

---

## Events

### Defining a Custom Event

Events are nested `public static class` that extend `ComponentEvent<T>` and are annotated `@DomEvent("<dom-event-name>")`:

```java
@DomEvent("opened-changed")
public static class OpenedChangeEvent extends ComponentEvent<Accordion> {
    private final Integer index;

    public OpenedChangeEvent(Accordion source, boolean fromClient,
            @EventData("event.detail.value") Integer index) {
        super(source, fromClient);
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }
}
```

- `@DomEvent(...)` names the client-side DOM event to subscribe to.
- `@EventData("event.detail.value")` extracts data from the client event; any value reachable via JavaScript expressions can be bound.
- The `fromClient` flag indicates whether the event originated on the client (as opposed to a server-side synthetic fire).

### Registering Listener Methods

```java
public Registration addOpenedChangeListener(
        ComponentEventListener<OpenedChangeEvent> listener) {
    return ComponentUtil.addListener(this, OpenedChangeEvent.class, listener);
}
```

**Always:**

- Return `Registration` so callers can unregister with `registration.remove()`.
- Do NOT add a `removeOpenedChangeListener` method. The `Registration` is the canonical removal mechanism.
- Name methods `add{EventName}Listener`.

### Built-in Listeners

Many events come for free through mixin interfaces:

- `ClickNotifier<T>` → `addClickListener(...)`
- `Focusable<T>` → `addFocusListener(...)`, `addBlurListener(...)`
- `HasValue<E, V>` → `addValueChangeListener(...)`

Prefer these over defining a parallel event on the component.

---

## Slots, Prefix/Suffix & Content Composition

### HasPrefix / HasSuffix

For form-style components that accept decoration in "prefix" and "suffix" slots:

```java
public class MyField extends Component implements HasPrefix, HasSuffix {
    // ...
}

// Usage:
MyField f = new MyField();
f.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
f.setSuffixComponent(new Div("USD"));
```

Slots are managed under the hood by `SlotUtils.setSlot(this, "prefix", component)` — do not manipulate `slot=` attributes manually when a shared utility exists.

### Manual Slot Management

When a slot is component-specific (e.g. Button's `icon` slot that shifts between `prefix` and `suffix` depending on `iconAfterText`), use the `Element` API:

```java
private void updateIconSlot() {
    if (iconComponent == null) {
        return;
    }
    if (hasIconOnly()) {
        iconComponent.getElement().removeAttribute("slot");
    } else {
        iconComponent.getElement().setAttribute(
                "slot", iconAfterText ? "suffix" : "prefix");
    }
}
```

### Container Components

Components that hold multiple children (Accordion, Tabs, SideNav, Select, MenuBar, Dialog…) implement `HasComponents` or a narrower `Has…Items` interface, and provide convenience `add(...)` overloads:

```java
public AccordionPanel add(String summary, Component content) {
    return add(new AccordionPanel(summary, content));
}

public AccordionPanel add(AccordionPanel panel) {
    getElement().appendChild(panel.getElement());
    return panel;
}
```

The returned child is typed so developers can chain further setup: `accordion.add("Details", body).setOpened(true)`.

### Data-Driven Content (`setItems`)

When children are typically computed at runtime, the component also exposes `setItems(...)` / `setItems(Collection<T>)` / `setItems(DataProvider<T>)` — see [Data Providers](#data-providers--data-views).

---

## Theme Variants

Theme variants are surfaced as typed enums implementing `ThemeVariant`. Applications add/remove them via `HasThemeVariant<V>`.

### Variant Enum Template

```java
package com.vaadin.flow.component.example;

import com.vaadin.flow.component.shared.ThemeVariant;

/**
 * Set of theme variants applicable to the {@code vaadin-example} component.
 */
public enum ExampleVariant implements ThemeVariant {
    LUMO_SMALL("small"),
    LUMO_LARGE("large"),
    LUMO_PRIMARY("primary"),
    /**
     * @deprecated Use {@link #PRIMARY} instead.
     */
    @Deprecated
    AURA_PRIMARY("primary"),
    PRIMARY("primary"),
    SMALL("small"),
    LARGE("large");

    private final String variant;

    ExampleVariant(String variant) {
        this.variant = variant;
    }

    @Override
    public String getVariantName() {
        return variant;
    }
}
```

**Conventions:**

- The `getVariantName()` value MUST match the exact `theme="..."` token the web component accepts.
- Historical entries with `LUMO_` or `AURA_` prefixes stay for backwards compatibility but are `@Deprecated`. New theme-agnostic entries (without prefixes) replace them.
- The enum name is `{Component}Variant` (singular), not `{Component}Variants`.

### Applying Variants

```java
button.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.LARGE);
button.setThemeVariant(ButtonVariant.ERROR, true);             // Add one
button.setThemeVariants(ButtonVariant.PRIMARY);                // Replace all
button.removeThemeVariants(ButtonVariant.LARGE);

// Reactive
button.bindThemeVariant(ButtonVariant.PRIMARY, isPrimarySignal);
button.bindThemeVariants(variantsListSignal);
```

All of these are provided for free by `HasThemeVariant<V>`; do not re-implement them on the component class.

### Variant Tests

For every variant enum, add a `{Component}VariantTest.java` that verifies each enum value maps to the expected theme token.

---

## I18n

### When to Add an I18n Class

Add one when the component renders any user-visible text of its own (labels, placeholders, error messages, tooltips, landmark names). Defaults must be sensible English fallbacks and every string must be customisable.

### Structure

```java
package com.vaadin.flow.component.example;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExampleI18n implements Serializable {
    private String emptyText;
    private String clear;
    private ErrorMessages errorMessages;

    public String getEmptyText() { return emptyText; }
    public ExampleI18n setEmptyText(String emptyText) {
        this.emptyText = emptyText;
        return this;
    }
    // ... repeat for each field

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorMessages implements Serializable {
        private String requiredMessage;
        // ...
    }
}
```

**Rules:**

- `implements Serializable` is required.
- `@JsonInclude(Include.NON_NULL)` so that unset strings are not sent to the client (avoiding confusing defaults and overwriting web-component defaults).
- Jackson annotations (`@JsonInclude`, `@JsonIgnore`, etc.) are imported from `com.fasterxml.jackson.annotation.*` — in Jackson 3, annotations remained in the legacy package even though databind moved to `tools.jackson.*`. Mixing annotations from a different package will silently fail to be recognised.
- Databind classes (`ObjectNode`, `ArrayNode`, `JsonNode`) are imported from `tools.jackson.databind.*`.
- Setters may return `this` for fluent chaining in configuration code — this is the one legitimate use of fluent-style in the Flow API.
- Nested structures use static inner classes that themselves implement `Serializable`.

### Component Integration

On the component:

```java
private ExampleI18n i18n;

public ExampleI18n getI18n() { return i18n; }

public void setI18n(ExampleI18n i18n) {
    this.i18n = Objects.requireNonNull(i18n, "The I18N properties must not be null");
    runBeforeClientResponse(ui -> {
        if (i18n == this.i18n) {
            setI18nJson();
        }
    });
}

private void setI18nJson() {
    JsonNode json = JacksonUtils.writeTree(this.i18n);
    getElement().setPropertyJson("i18n", json);
}
```

**Never** hand-serialise the object to JSON using string concatenation. Always use `JacksonUtils` (which wraps Jackson 3).

### I18n Tests

Add a `{Component}I18nTest.java` that:

- Verifies every getter returns the value set through its setter.
- Verifies that unset fields serialize to absent JSON keys (not `null`).
- Verifies that the component pushes i18n changes to the element as JSON when attached.

---

## Data Providers & Data Views

Components that render a list of items (ComboBox, Select, Grid, VirtualList, MultiSelectComboBox, CheckboxGroup, RadioButtonGroup, etc.) integrate with Vaadin's data provider system.

### Data View Interfaces

Implement the appropriate interface(s) depending on the data model:

- `HasListDataView<T, V>` — in-memory list data provider
- `HasLazyDataView<T, F, V>` — lazy/server-driven data provider with optional filter
- `HasDataView<T, F, V>` — general data view

### DataCommunicator

For lazy-loading components, the server side uses `DataCommunicator<T>` to track which items the client currently has cached and serves pages on demand. Example: `ComboBoxDataCommunicator`, `Grid`'s internal data communicator.

### `setItems(...)` Overloads

Every data-driven component exposes a family of `setItems` methods:

```java
public ExampleListDataView<T> setItems(T... items);
public ExampleListDataView<T> setItems(Collection<T> items);
public ExampleListDataView<T> setItems(ListDataProvider<T> dataProvider);
public ExampleLazyDataView<T> setItems(CallbackDataProvider.FetchCallback<T, F> fetchCallback);
public ExampleLazyDataView<T> setItems(CallbackDataProvider.FetchCallback<T, F> fetchCallback,
                                       CallbackDataProvider.CountCallback<T, F> countCallback);
```

The return type is the specific data view so the caller can chain item-level operations (refresh, remove, insert, etc.).

### Connector Integration

Data-driven components typically require a JS connector to bridge `DataCommunicator` with the web component's own data binding. See [Connectors](#connectors-javascript-glue).

---

## Validation

### Simple Validation (via `HasValidationProperties`)

Components that implement `HasValidationProperties` get `setInvalid(boolean)` / `isInvalid()` / `setErrorMessage(String)` for free, backed by element properties.

### Manual vs Automatic Validation

Field components (ComboBox, Select, DatePicker, TextField…) run validation automatically on value change. Developers can switch this off via `setManualValidation(true)` and take full control:

```java
field.setManualValidation(true);
// ... later:
field.setInvalid(!isCorrect(value));
field.setErrorMessage(isCorrect(value) ? null : "Must be a positive number");
```

Both modes MUST be supported. See `ComboBox` Javadoc for the canonical explanation of the two modes.

### ValidationController

Complex field components use `com.vaadin.flow.component.shared.internal.ValidationController` to coordinate built-in constraints, application-level validators, and the current validation mode. Follow the pattern used in `ComboBoxBase`, `Select`, `DatePicker`.

### Binder Integration

Implementing `HasValidator<V>` lets Vaadin `Binder` automatically pick up the component's default validator. Provide `getDefaultValidator()` or let the Flow base class handle it.

---

## Connectors (JavaScript Glue)

Not every component needs a connector. Add one only when the bridging between `DataCommunicator`, renderers, or dynamic DOM requires custom JavaScript.

### Location

```
src/main/resources/META-INF/resources/frontend/{name}Connector.js
```

### Loading

On the component class:

```java
@JsModule("@vaadin/combo-box/src/vaadin-combo-box.js")
@JsModule("./comboBoxConnector.js")
```

### Pattern: `initLazy`

```javascript
window.Vaadin.Flow.comboBoxConnector = {};
window.Vaadin.Flow.comboBoxConnector.initLazy = (comboBox) => {
  // Check whether the connector was already initialised for this element
  if (comboBox.$connector) {
    return;
  }
  comboBox.$connector = {};
  // Wire up data provider, event handlers, etc.
};
```

### Initialisation in Attach Handler

```java
@Override
protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    initConnector();
}

private void initConnector() {
    getElement().executeJs(
        "window.Vaadin.Flow.comboBoxConnector.initLazy(this)");
}
```

**Why `onAttach`?** Flow may create a fresh client-side element for the same server-side component instance after detach/re-attach. The connector must be re-initialised each time. Putting this in the constructor is a bug — it will run only once per server-side instance, and the second time the component attaches, the client element has lost its connector.

### What Typical Connectors Do

- **`comboBoxConnector.js`** — wires the web component's lazy `dataProvider` callback to the server's `DataCommunicator`, manages filter state, caches pages.
- **`contextMenuConnector.js`** — generates the nested menu structure from server-side items, binds to target elements.
- **`menuBarConnector.js`** — renders menu items and subitems, handles slot management.
- **`flow-component-renderer.js`** — renders arbitrary server-side components inside cells (Grid, ComboBox with custom renderer).

---

## Feature Flags & Experimental Components

### Runtime Check

```java
import com.vaadin.experimental.Feature;
import com.vaadin.experimental.FeatureFlags;

private boolean isFeatureFlagEnabled(Feature feature) {
    UI ui = UI.getCurrent();
    if (ui == null) {
        return false;
    }
    return FeatureFlags.get(ui.getSession().getService().getContext())
            .isEnabled(feature);
}

// Usage:
if (isFeatureFlagEnabled(FeatureFlags.MY_EXPERIMENTAL_FEATURE)) {
    // ... only active behaviour
}
```

### Enabling in Integration Tests

`src/main/resources/vaadin-featureflags.properties` in the integration-tests module:

```properties
com.vaadin.experimental.myExperimentalFeature=true
```

### Enabling in Unit Tests

Use the `EnableFeatureFlagExtension`:

```java
@RegisterExtension
EnableFeatureFlagExtension flagExtension =
    new EnableFeatureFlagExtension(FeatureFlags.MY_EXPERIMENTAL_FEATURE);
```

### Deprecation Cycle

Changes to experimental APIs do NOT require deprecation. Once the flag is removed and the component becomes stable, normal Vaadin deprecation rules apply.

---

## TestBench Elements

### File

`vaadin-{component}-testbench/src/main/java/com/vaadin/flow/component/{name}/testbench/{Component}Element.java`

### Structure

```java
/*
 * Copyright 2000-2026 Vaadin Ltd. ...
 */
package com.vaadin.flow.component.example.testbench;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.elementsbase.Element;

/**
 * A TestBench element representing a {@code <vaadin-example>} element.
 */
@Element("vaadin-example")
public class ExampleElement extends TestBenchElement {

    public String getValue() {
        return getPropertyString("value");
    }

    public void setValue(String value) {
        setProperty("value", value);
    }
}
```

**Conventions:**

- `@Element("vaadin-...")` — matches the web component tag.
- Extend `TestBenchElement`; implement helper interfaces as appropriate (`HasLabel`, `HasLabelAsText`, `HasValue`, `HasHelper`, `HasStringValueProperty`).
- Add convenience methods that cover common test interactions (`open()`, `close()`, `typeFilter(String)`, `selectByText(String)`).
- Keep the element lean — it is a thin wrapper around the DOM, not a fixture.

---

## Unit Testing

### Technology

- **JUnit 6 (Jupiter)** — `org.junit.jupiter.api.Test`, `Assertions.*`
- **Mockito** for mocks
- **AbstractSignalsTest** as a base class when testing signal bindings
- **MockUIExtension** for tests that need a UI/VaadinSession
- **EnableFeatureFlagExtension** for feature-flag-gated code

### Basic Test Template

```java
package com.vaadin.flow.component.example.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.example.Example;

class ExampleTest {

    private Example component;

    @BeforeEach
    void setup() {
        component = new Example();
    }

    @Test
    void emptyCtor() {
        Assertions.assertEquals("", component.getValue());
    }

    @Test
    void valueCtor() {
        component = new Example("foo");
        Assertions.assertEquals("foo", component.getValue());
    }
}
```

### Signal Test Template

```java
import com.vaadin.tests.AbstractSignalsTest;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;

class ExampleSignalTest extends AbstractSignalsTest {

    @Test
    void bindValue_throwsOnSetValue() {
        ValueSignal<String> signal = new ValueSignal<>("foo");
        Example component = new Example(signal);
        UI.getCurrent().add(component);

        Assertions.assertThrows(BindingActiveException.class,
                () -> component.setValue("bar"));
    }

    @Test
    void bindValue_runsSideEffects() {
        // Verify that the same side-effects run whether the value is set
        // imperatively or via the signal
    }
}
```

### Variant Test Template

```java
class ExampleVariantTest {
    @Test
    void variantNames() {
        Assertions.assertEquals("primary", ExampleVariant.PRIMARY.getVariantName());
        Assertions.assertEquals("small", ExampleVariant.SMALL.getVariantName());
    }

    @Test
    void addThemeVariant_setsThemeAttribute() {
        Example c = new Example();
        c.addThemeVariants(ExampleVariant.PRIMARY);
        Assertions.assertTrue(c.getThemeNames().contains("primary"));
    }
}
```

### Serializable Test Template

Every component MUST have one:

```java
class ExampleSerializableTest {
    @Test
    void isSerializable() throws Exception {
        Example component = new Example("value");
        byte[] bytes;
        try (var baos = new java.io.ByteArrayOutputStream();
             var oos = new java.io.ObjectOutputStream(baos)) {
            oos.writeObject(component);
            bytes = baos.toByteArray();
        }
        try (var bais = new java.io.ByteArrayInputStream(bytes);
             var ois = new java.io.ObjectInputStream(bais)) {
            Example deserialised = (Example) ois.readObject();
            Assertions.assertEquals("value", deserialised.getValue());
        }
    }
}
```

### Coverage Requirements

- Every constructor overload
- Every public setter/getter pair
- `setXxx(null)` behaviour for nullable setters
- Each theme variant value
- Every event listener registration + firing
- Every `bind*` method (binding works, imperative setter throws, side-effects run)
- Serialisation
- I18n round-trip and JSON shape

---

## Integration Testing

### Technology

- **JUnit 4** (`org.junit.Test`, not `org.junit.jupiter`)
- **Vaadin TestBench** driving a headless browser
- **Jetty** serving the test application
- **`AbstractComponentIT`** as the base class

### Test View (`src/main/java/.../tests/{Component}Page.java`)

```java
/* copyright header */
package com.vaadin.flow.component.example.tests;

import com.vaadin.flow.component.example.Example;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-example")
public class ExamplePage extends Div {

    public ExamplePage() {
        Example defaultExample = new Example();
        defaultExample.setId("default-example");
        add(defaultExample);

        Example withValue = new Example("Hello");
        withValue.setId("with-value");
        add(withValue);
    }
}
```

**Conventions:**

- Test view extends `Div` or another layout component.
- `@Route` value matches the `@TestPath` on the IT class.
- Every testable element has an `id` so the IT can locate it.
- `Page` is the dominant suffix for new test views (`View` exists in legacy code; prefer `Page`).

### Test Class (`src/test/java/.../tests/{Component}IT.java`)

```java
/* copyright header */
package com.vaadin.flow.component.example.tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.vaadin.flow.component.example.testbench.ExampleElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-example")
public class ExampleIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-example"));
    }

    @Test
    public void defaultValue_isEmpty() {
        ExampleElement element = $(ExampleElement.class).id("default-example");
        Assert.assertEquals("", element.getValue());
    }
}
```

### Running Integration Tests

```sh
mvn verify -am -pl vaadin-example-flow-parent/vaadin-example-flow-integration-tests \
    -DskipUnitTests
```

To run a single test **method**, append a `*` wildcard (TestBench mangles method names with browser info):

```sh
mvn verify -am -pl vaadin-example-flow-parent/vaadin-example-flow-integration-tests \
    -Dit.test='ExampleIT#defaultValue_isEmpty*' -DskipUnitTests
```

### Starting the IT Server for Manual Testing

```sh
mvn package jetty:run -Dvaadin.pnpm.enable -Dvaadin.frontend.hotdeploy=true -am -B -q \
    -DskipTests -pl vaadin-example-flow-parent/vaadin-example-flow-integration-tests
```

Port 8080. Wait for "Frontend compiled successfully" before hitting the page. Restart after code changes; frontend hot-deploy reloads JS but not Java.

When running tests against an already-running server, add `-DskipJetty` to the verify command.

---

## Documentation (Javadoc)

### Class-Level Javadoc

```java
/**
 * Brief description of what the component does.
 * <p>
 * Longer explanation covering key concepts, use cases, and important
 * limitations. Include a short code example in a {@code <pre>} block when
 * useful.
 *
 * <h2>Validation</h2>
 * <p>
 * If the component validates, describe built-in constraints, how to
 * disable them ({@link #setManualValidation(boolean)}), and how error
 * messages are surfaced.
 *
 * @param <T> description if the class is generic
 * @author Vaadin Ltd
 */
```

### Method Javadoc

- Describe WHAT the method does and WHY a caller would use it — not HOW it works internally.
- Mention side-effects (DOM updates, re-rendering).
- Document exceptions explicitly (`@throws IllegalStateException if ...`).
- Cross-link related methods with `{@link ...}`.
- For boolean parameters, say what `true` vs `false` does.

### `@since` and `@deprecated`

- `@since` is required on new public classes and methods.
- `@deprecated` with a `@see` or `{@link}` pointer to the replacement is required when deprecating an API. Deprecated APIs stay for at least one major version before removal.

### README.md

Each component parent module has a `README.md` with:

- Brief description
- Link to vaadin.com docs
- Minimal code example (`new Component(...)` usage)
- Installation snippet (`<dependency>` block)
- License notice

---

## Accessibility

This section covers the Flow-side implementation mechanics that realise the universal behavioural requirements listed in [DESIGN_GUIDELINES.md → Universal behavioural requirements](DESIGN_GUIDELINES.md#universal-behavioural-requirements).

**Most accessibility work lives in the web component.** The Flow wrapper's job is to surface the web component's accessibility APIs to Java so that applications can supply names, labels, and error messages — and to propagate state (disabled, invalid, required) through the usual Flow mechanisms.

### Expose an Accessible Name

If the web component exposes `aria-label` / `aria-labelledby`, the Flow component MUST implement `HasAriaLabel`. This is provided by `com.vaadin.flow.component.HasAriaLabel` as a default-method interface; all the Flow component has to do is list it in the `implements` clause.

### Localise Every Rendered String

Every user-visible label the component renders itself (not the application's content) MUST be reachable via the i18n class or a direct setter — never a hard-coded Java string the application cannot override. See [I18n](#i18n).

### Disabled State

Implement `HasEnabled`. The underlying web component is responsible for the visual "dimmed" treatment and for preventing keyboard/mouse interaction.

If the component needs "disabled but still focusable/hoverable" semantics (for tooltip explanations on disabled controls), follow the `FeatureFlags.ACCESSIBLE_DISABLED_BUTTONS` pattern in `Button`:

```java
@Override
public ShortcutRegistration addFocusShortcut(Key key, KeyModifier... modifiers) {
    ShortcutRegistration reg = Focusable.super.addFocusShortcut(key, modifiers);
    if (isFeatureFlagEnabled(FeatureFlags.ACCESSIBLE_DISABLED_BUTTONS)) {
        reg.setDisabledUpdateMode(DisabledUpdateMode.ALWAYS);
    }
    return reg;
}
```

### Keyboard Shortcuts

- `Focusable.addFocusShortcut(Key, KeyModifier...)` for keyboard focus.
- `ClickNotifier.addClickShortcut(Key, KeyModifier...)` for click activation.
- Manually defined shortcuts via `Shortcuts.addShortcutListener(...)`.

Keep the underlying keyboard semantics consistent with the web component's documented shortcuts. The Flow side usually does NOT override them.

### Testing Accessibility

- Unit tests: verify `aria-label` is set when `setAriaLabel("...")` is called.
- Integration tests: verify roles and ARIA attributes on the rendered DOM using TestBench element APIs.
- Manually verify with a screen reader and keyboard-only navigation before a new component graduates from experimental.

---

## Package Configuration (Maven)

### Parent `pom.xml` (`vaadin-{component}-flow-parent/pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-flow-components</artifactId>
        <version>25.2-SNAPSHOT</version>
    </parent>
    <artifactId>vaadin-example-flow-parent</artifactId>
    <packaging>pom</packaging>
    <name>Vaadin Example Parent</name>
    <description>Vaadin Example Parent</description>
    <modules>
        <module>vaadin-example-flow</module>
        <module>vaadin-example-testbench</module>
    </modules>
    <dependencies/>
    <profiles>
        <profile>
            <id>default</id>
            <activation>
                <property><name>!release</name></property>
            </activation>
            <modules>
                <module>vaadin-example-flow-integration-tests</module>
            </modules>
        </profile>
    </profiles>
</project>
```

The integration-tests module lives under a `default` profile so it is built during development but excluded during releases.

### Main Flow Module `pom.xml`

Minimal dependencies; the full baseline comes from the aggregator:

```xml
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-flow-components-base</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>flow-html-components</artifactId>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-flow-components-test-util</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
```

Copy from `vaadin-button-flow-parent/vaadin-button-flow/pom.xml` when creating a new component.

---

## Common Patterns

### Pattern 1 — Simple Interactive Component (Button-like)

**Use when:**

- No internal value state; component fires events in response to user interaction.
- Minimal child composition (icon slot, prefix/suffix).

**Template:** Button
- Implements: `ClickNotifier`, `Focusable`, `HasText`, `HasEnabled`, `HasSize`, `HasStyle`, `HasAriaLabel`, `HasPrefix`, `HasSuffix`, `HasThemeVariant<...>`, `HasTooltip`
- No I18n.
- No connector.

### Pattern 2 — Field Component (input-like)

**Use when:**

- Component accepts user input and has a value.
- Needs validation, labels, helpers, Binder integration.

**Template:** `TextField`, `DatePicker`, `ComboBox`, `Select`
- Extends `AbstractSinglePropertyField` or a shared base (e.g. `ComboBoxBase`).
- Implements: `InputField`, `HasValidationProperties`, `HasLabel`, `HasPlaceholder`, `HasPrefix`, possibly `HasSuffix`, `HasClearButton`, `HasAutoOpen`, `HasAllowedCharPattern`.
- Often has an I18n class for error/required messages.
- Uses `ValidationController` for manual-validation mode.

### Pattern 3 — Overlay Component (dialog/popup)

**Use when:**

- Component renders in an overlay layer above other content.
- Needs open/close state and escape-key handling.

**Template:** `Dialog`, `Notification`, `ConfirmDialog`, `Popover`
- Custom `open()`, `close()`, `isOpened()`, `@Synchronize("opened-changed")`.
- Often a nested `OpenedChangeEvent`.
- Rendered outside the parent hierarchy; may need `ModalRoot` helper for a11y focus trapping.

### Pattern 4 — Data-Driven List Component

**Use when:**

- Component renders a list of items with optional lazy loading, filtering, selection.

**Template:** `ComboBox`, `Select`, `Grid`, `VirtualList`, `MultiSelectComboBox`
- Implements the relevant `HasDataView` / `HasListDataView` / `HasLazyDataView` interfaces.
- Uses a `DataCommunicator` (custom or from Flow) internally.
- Requires a JS connector.
- Exposes `setItems(...)` overloads returning the specific data view.

### Pattern 5 — Navigational Component

**Use when:**

- Component renders links into the application's route space.

**Template:** `SideNav`, `AppLayout` (with `SideNav` inside), `MenuBar` when used for nav
- Accepts `Class<? extends Component>` for `@Route`-annotated views as the primary API (e.g. `new SideNavItem("Home", HomeView.class)`).
- Resolves URLs via `com.vaadin.flow.router.RouteConfiguration`.
- Supports `RouteParameters` / `QueryParameters` / `HasUrlParameter` for parameterised routes.
- Shell components (layouts that host routed views) implement `com.vaadin.flow.router.RouterLayout`.
- A string-path overload may exist for external/hand-managed URLs, but the typed overload is the primary form.
- The rendered DOM is still a plain `<a href>` (from the underlying web component); Flow Router intercepts clicks automatically.

### Pattern 6 — Container / Layout

**Use when:**

- Component arranges child components visually (layouts, cards, details, accordion).

**Template:** `HorizontalLayout`, `VerticalLayout`, `FormLayout`, `SplitLayout`, `Details`, `Accordion`
- Implements `HasComponents` or `HasOrderedComponents`.
- `add(...)` / `remove(...)` / `removeAll()`.
- Theme variants for layout style (spacing, padding, margin).

---

## Checklist

Use this checklist when creating a new Flow component.

### Module Structure

- [ ] Parent module `vaadin-{component}-flow-parent/` created with pom.xml
- [ ] `vaadin-{component}-flow/` submodule with pom.xml
- [ ] `vaadin-{component}-testbench/` submodule with pom.xml
- [ ] `vaadin-{component}-flow-integration-tests/` submodule (inside default profile)
- [ ] Component added to top-level aggregator POM
- [ ] Copyright headers on all files

### Component Class

- [ ] `@Tag("vaadin-{name}")` annotation
- [ ] `@NpmPackage(value = "@vaadin/{name}", version = "...")` matches web component version
- [ ] `@JsModule("@vaadin/{name}/src/vaadin-{name}.js")`
- [ ] Class extends `Component` (or appropriate base)
- [ ] Implements all relevant mixin interfaces
- [ ] Progressive-disclosure constructors (empty / common shortcuts / signal variants / listener variants)
- [ ] Every mutable property has `setXxx` / `getXxx` (or `isXxx`)
- [ ] No fluent-chain setters (unless it's the I18n object)

### Theme Variants

- [ ] `{Component}Variant` enum implements `ThemeVariant`
- [ ] Variant names exactly match the web component's `theme` tokens
- [ ] Deprecated legacy names (`AURA_*`, `LUMO_*`) left in place as `@Deprecated`
- [ ] Class implements `HasThemeVariant<{Component}Variant>`

### Signals

- [ ] For every mutable property worth binding reactively, `bind{Property}(Signal<...>)` is provided
- [ ] Signal bindings use `SignalPropertySupport` or `Element.bindProperty`
- [ ] Imperative setters throw `BindingActiveException` when a one-way binding is active
- [ ] Side-effects run identically for imperative and reactive updates
- [ ] Unit test covers the binding-throws-on-set case
- [ ] `.claude/skills/signal-rules/SKILL.md` has been followed

### Events

- [ ] Custom events use `@DomEvent("...")` and extend `ComponentEvent<T>`
- [ ] `@EventData` used for payload extraction
- [ ] `addXxxListener` methods return `Registration`
- [ ] No `removeXxxListener` methods

### Slots / Composition

- [ ] `HasPrefix` / `HasSuffix` implemented if the web component has those slots
- [ ] `HasComponents` / `HasOrderedComponents` for containers
- [ ] Slot management uses `SlotUtils` (or documented manual management)

### I18n

- [ ] `{Component}I18n` POJO created if component renders user-visible text
- [ ] Jackson 3 annotations from `com.fasterxml.jackson.annotation.*`; databind types from `tools.jackson.databind.*`
- [ ] `@JsonInclude(JsonInclude.Include.NON_NULL)` on every class
- [ ] `implements Serializable`
- [ ] JSON built with `JacksonUtils`, never string concatenation
- [ ] Unit test verifies round-trip and JSON shape

### Data

- [ ] For list-based components: implements `HasListDataView` / `HasLazyDataView` / `HasDataView`
- [ ] `setItems(...)` overloads return the specific data view

### Validation

- [ ] For fields: implements `HasValidationProperties`
- [ ] Supports both automatic and manual validation modes
- [ ] `Binder` integration works

### Connector (if needed)

- [ ] Connector file at `src/main/resources/META-INF/resources/frontend/{name}Connector.js`
- [ ] Loaded via `@JsModule("./{name}Connector.js")`
- [ ] `initLazy` pattern with `$connector` guard
- [ ] Initialised in `onAttach`, not constructor

### Accessibility

- [ ] `HasAriaLabel` implemented if the web component supports it
- [ ] `HasEnabled` implemented; disabled state respects the web component's semantics
- [ ] Focusable components implement `Focusable<T>` and support focus shortcuts
- [ ] No hard-coded user-visible strings; everything reachable via setters / i18n

### Feature Flags (Experimental Components)

- [ ] `FeatureFlags.get(...).isEnabled(FEATURE)` check at runtime
- [ ] Integration tests enable flag via `vaadin-featureflags.properties`
- [ ] Unit tests use `EnableFeatureFlagExtension`
- [ ] Javadoc notes the experimental status

### TestBench Element

- [ ] `{Component}Element.java` under `...testbench` package
- [ ] `@Element("vaadin-{name}")`
- [ ] Extends `TestBenchElement`; implements relevant `Has*` interfaces from TestBench
- [ ] Convenience methods for common test interactions

### Unit Tests

- [ ] `{Component}Test.java` covers constructors, setters, getters, events
- [ ] `{Component}VariantTest.java` covers each theme variant
- [ ] `{Component}SignalTest.java` extends `AbstractSignalsTest`; covers every `bind*` API
- [ ] `{Component}I18nTest.java` (if the component has i18n)
- [ ] `{Component}SerializableTest.java` — ALWAYS

### Integration Tests

- [ ] `{Component}Page.java` under `src/main/java/.../tests` with `@Route("vaadin-{name}")`
- [ ] `{Component}IT.java` under `src/test/java/.../tests` with `@TestPath("vaadin-{name}")`
- [ ] Extends `AbstractComponentIT`
- [ ] Covers user-visible behaviour in a real browser

### Documentation

- [ ] Class Javadoc includes description, code example, validation section (if applicable)
- [ ] Every public method documented; `@param`, `@return`, `@throws`
- [ ] `@since` on new APIs
- [ ] `README.md` in parent module

### Final Validation

- [ ] `mvn spotless:apply` has been run
- [ ] `mvn checkstyle:check` passes
- [ ] `mvn test -pl vaadin-{component}-flow-parent/vaadin-{component}-flow` passes
- [ ] `mvn verify -am -pl vaadin-{component}-flow-parent/vaadin-{component}-flow-integration-tests -DskipUnitTests` passes
- [ ] Serialisation test passes
- [ ] Component works in the integration-test Jetty server with both Lumo and Aura themes (if applicable)
- [ ] Matches the underlying web component's API surface — nothing missing, nothing invented
