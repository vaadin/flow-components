# Signals & Reactive APIs

Flow 25 introduces `Signal<T>` — a reactive value bound to a component
property so changes propagate automatically. State that supports it exposes
`bindXxx(Signal<T>)` returning `SignalBinding<T>` alongside the imperative
`setXxx` / `getXxx` — but only *some* state supports it, by design:

- Every state has an imperative API. A reactive API is added selectively,
  never instead.
- A binding is worth adding when the state can be expected to change over
  the component's lifetime — typically state that changes often, such as a
  value, `min`/`max` bounds, or displayed content.
- State configured once at initialization (an ARIA role, autofocus) does
  not need a binding.

Where both APIs exist, they are two doors to the same state:

- Both update the same state and fire the same events.
- Both trigger the same side-effects on state changes.
- While a one-way binding is active, the imperative setter throws
  `BindingActiveException` — that is how a developer learns that the binding
  owns the state.
- Docs and examples lead with the imperative API.

> Read `.claude/skills/signal-rules/SKILL.md` before writing or changing any
> `bind*` method.

## Where bind* methods come from

**Inherited from mixins and base classes — the common case.** Most `bind*`
methods a component exposes come for free with an interface or base class it
already has: `bindVisible` (`Component`), `bindEnabled` (`HasEnabled`),
`bindText` (`HasText`), `bindValue` / `bindReadOnly` /
`bindRequiredIndicatorVisible` (`HasValue`), `bindWidth` / `bindHeight`
(`HasSize`), `bindClassName(s)` (`HasStyle`), `bindHelperText`
(`HasHelper`), `bindPlaceholder` (`HasPlaceholder`), and from this repo's
shared module `bindThemeVariant(s)` (`HasThemeVariant`) and
`bindClearButtonVisible` (`HasClearButton`). Never implement these in a
component — implementing the mixin provides both the imperative and the
reactive API as a matching pair.

**Overriding an inherited pair.** A component overrides an inherited
`bind*` method only when it also overrides the matching setter to add
side-effects — then both must be overridden together and routed through one
change handler (see below). `Button` overrides `setText` / `bindText`
because changing the text also updates the icon slot and theme attribute.

**Component-specific bindings.** A new `bind*` method is added directly to
a component for state the component itself defines, when that state meets
the "changes over the lifetime" bar: `bindMin` / `bindMax` / `bindStep` on
`Slider` and the number and date/time fields, `bindDrawerOpened` on
`AppLayout`, `bindContent` on `Markdown`.

## SignalPropertySupport

For a property needing both variants with shared side-effects, route both
through one change handler so imperative and reactive updates behave
identically:

```java
private final SignalPropertySupport<String> textSupport =
        SignalPropertySupport.create(this, this::textChangeHandler);

@Override public void setText(String text) { textSupport.set(text); } // throws if bound
@Override public SignalBinding<String> bindText(Signal<String> s) { return textSupport.bind(s); }

private void textChangeHandler(String text) {
    // ALL side-effects, for both the imperative and reactive paths
    textNode.setText(text);
    updateIconSlot();
}
```
