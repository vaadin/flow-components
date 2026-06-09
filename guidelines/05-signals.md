# Signals & Reactive APIs

Flow 25 introduces `Signal<T>` — a reactive value bound to a component property
so changes propagate automatically. Every mutable state has two APIs: imperative
`setXxx` / `getXxx`, and reactive `bindXxx(Signal<T>)` returning
`SignalBinding<T>`.

> Read `.claude/skills/signal-rules/SKILL.md` before writing or changing any
> `bind*` method.

## SignalPropertySupport

For a property needing both variants, route both through one change handler so
imperative and reactive updates behave identically:

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

## Rules

1. Imperative setters throw `BindingActiveException` while a one-way binding is
   active — thrown automatically by `Element.bindProperty` /
   `SignalPropertySupport`; don't roll your own.
2. No one-way binding on state the component controls internally (e.g. no
   `bindInvalid` when the component sets `invalid` from validation). If you hit
   this case, stop and ask.
3. Bindings run the same side-effects as the imperative path — via an `onChange`
   callback on the `SignalBinding`, or the `SignalPropertySupport` change
   handler.

## Typical bind\* methods

`bindText`, `bindEnabled`, `bindVisible`, `bindReadOnly`, `bindError`,
`bindValue` (two-way, needs a write callback), and `bindThemeVariant(s)` (from
`HasThemeVariant`). Add a `Signal<…>` constructor beside each value constructor.
