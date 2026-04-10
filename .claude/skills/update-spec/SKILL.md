---
description: Creates or updates a Flow component spec based on use-cases and the web component spec
argument-hint: <ComponentName or spec-folder-path-or-url>
---

You are a senior Vaadin developer who translates web component specifications into Flow (Java) component specifications. Your task is to read a use cases document with framework agonistc use cases and a web component spec. Based on this input you create or update the corresponding Flow component spec.

Arguments: [source]

The argument can be:
- **A component name** (e.g. `Breadcrumb`, `Card`) — uses the spec folder from the vaadin/web-components main branch on GitHub at `packages/{kebab-name}/spec`
- **A file path** (e.g. `web-components/packages/breadcrumb/spec`) — reads from the local folder
- **A URL** (e.g. a raw GitHub URL) — fetches from that URL

The folder contains two documents we need:
- {kebab-name}-web-component.md - the web component specification
- use-cases.md - framework agnostic use cases

Derive from the component name:
- **kebab-name**: PascalCase → kebab-case (e.g. `DatePicker` → `date-picker`)
- **Human Name**: PascalCase → space-separated (e.g. `DatePicker` → `Date Picker`)
- **package-name**: kebab-name with hyphens removed (e.g. `breadcrumb`, `datepicker`)

## Steps

### 1. Read the web component spec

Read the web components specification with the name `{kebab-name}-web-component.md` from the folder defined by the argument.

If the spec cannot be found, stop and tell the user.

### 2. Read the use cases document

Read the use case specification with the name `use-cases.md` from the folder defined by the argument.

If the spec cannot be found, stop and tell the user.

### 3. Read existing Flow component code (if any)

Check if the Flow component module exists at `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/src/main/java/com/vaadin/flow/component/{package-name}/`. If it does, read the existing Java source files to understand what's already implemented. This informs the spec — existing API should be reflected accurately.

### 4. Read an existing Flow spec for reference

Read an existing Flow component spec to understand the format. Check `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/spec/` or similar paths. If no existing Flow spec exists yet, the format is defined in step 4.

### 5. Create or update the Flow component spec

Write the spec in the end to: `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/spec/{kebab-name}-flow-component.md`

The goal is to design a good and easy to use API to cover implementation of all use cases. Do not limit yourself to API in this component, it is fine to suggest additional API in other components or in Flow itself (sources in flow/), if needed.

Naming and patterns for Java/Flow specific features should be made so that they feel natural to Java developers.

If a feature is not needed for any use case, it is probably unnecessary. You can report that these features were considered as useful ones but were omitted as they were not needed.

Note that while web components are generic and agnostic to certain technologies, the Flow component should not be. The Flow components should be as convenient to use as possible and offer convenient integration API to other components or features as needed.

The Flow spec translates the web component spec into Java API terms. Follow this structure:

```markdown
# Vaadin {Human Name} Flow Component

## Usage Examples

### 1. {Example Name}

​```java
// Java code showing Flow API usage
​```

### 2. {Example Name}
...

---
### Key Design Decisions

1. **Decision** — rationale.
...

---

## Implementation

### Classes

**`{ComponentName}`** — Main component class

Extends `Component`, implements relevant interfaces.

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `methodName` | `Type param` | `ReturnType` | Description |

| Constructor | Parameters | Description |
|---|---|---|
| `{ComponentName}()` | — | Creates an empty component |

**`{ComponentName}Item`** — Item class (if applicable)
...
```

#### Translation rules

Apply these rules when translating from web component spec to Flow spec:

**Properties → Java methods:**
- Each web component property `propertyName` becomes `getPropertyName()` / `setPropertyName(Type value)` methods
- Boolean properties: `isPropertyName()` / `setPropertyName(boolean value)`
- `items` property (Array): becomes `setItems(List<{ComponentName}Item> items)` or child component API using `add()`/`remove()` methods, depending on what makes sense. If the web component supports both slotted children AND an `items` property, the Flow component should support both a child component API AND a data-driven `setItems()` API.
- Reflected properties with string type may also get convenience enum support

**Slots → Java methods:**
- Named slots become setter/getter methods: `setSlotName(Component component)` / `getSlotName()`
- Default slot: component implements `HasComponents` or provides `add()`/`remove()` methods
- Multiple items in a slot: `addToSlotName(Component... components)` / `getSlotNameComponents()`

**Events → Java events:**
- Web component events `event-name` become `addEventNameListener(ComponentEventListener<EventNameEvent> listener)` methods
- The event class `EventNameEvent` extends `ComponentEvent<{ComponentName}>`
- Property change events (`property-changed`) may not need explicit Flow events if the property is only set from server side

**CSS Custom Properties:**
- Generally not exposed as Java API — they are CSS-level customization
- May be mentioned in the spec documentation but not as methods

**ARIA / Accessibility:**
- `label` property: implement `HasAriaLabel` interface
- `role` attribute: set in constructor or via dedicated method
- Other ARIA attributes: handled automatically by the web component or via specific methods

**Theme variants:**
- If the web component has theme variants, create a `{ComponentName}Variant` enum implementing `ThemeVariant`
- Component implements `HasThemeVariant<{ComponentName}Variant>`

**General principles:**
- Usage examples must be idiomatic Java, showing how a Vaadin Flow developer would use the component
- Translate HTML examples into Java component tree construction
- Where the web component uses string properties for content (like `text`), the Flow API should offer both `String` and `Component` overloads where appropriate (see Card's `setTitle(String)` and `setTitle(Component)`)
- Data-driven examples should use Java collections (`List`, `Stream`)
- Preserve all key design decisions from the web component spec, adding Flow-specific decisions where needed
- If the web component spec mentions responsive/overflow behavior, note that this is handled by the web component — the Flow API doesn't need to expose it

### 6. Verify consistency

After writing the spec, verify:
- Every web component property/slot/event is accounted for in the Flow spec (either as a method or noted as handled by the web component)
- Method naming follows existing Vaadin Flow conventions (check other components for patterns)
- No web component implementation details leak into the Flow API (e.g., shadow DOM structure is not relevant)

Be thorough.

### 7. Create or update the Flow component use case implementation example files

For each use case defined in `use-cases.md`, create a `vaadin-{kebab-name}-flow-parent/vaadin-{kebab-name}-flow/spec/{kebab-name}-use-case-N.md` file where N is the use case number. 

Analyze the defined use case and write out the implementation needed for each use case so it works with the current spec.

The use case implementation should focus solely on the code written in the application for the use case, not about what needs to be implemented in a component. Ensure you check the Vaadin MCP for best practices.

Ensure that use case #1, the primary use case, is the easiest one to accomplish.

While implementing the use cases, if you notice anything that could be improved in the Flow component spec to make the use case clearer, better or easier to implement then go back and update the spec and update all use cases again. Repeat until there is nothing more to improve.

## Important guidelines

- The Flow spec describes the **Java API**, not the web component internals
- All example code must be **Java code**, not HTML
- Keep the spec focused on what a Flow developer needs to know
- Do NOT include web component shadow DOM structure details
- Do NOT include CSS custom properties as Java methods
- DO mention CSS custom properties in a separate section for theming reference
- If the web component spec is missing, stop and inform the user
- If updating an existing spec, preserve any Flow-specific decisions or additions that don't conflict with the web component spec
