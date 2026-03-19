---
name: signal-rules
description: Read these rules whenever you add, modify, or review `bind*` methods (e.g. `bindError`, `bindEnabled`, `bindVisible`), or work with `Signal` or `SignalPropertySupport` types in component code.
---

## Preface

Developers can update component state in two ways:
- Imperatively, through setters. For example, `button.setEnabled(boolean enabled)` updates a button's enabled state imperatively.
- Reactively, through signal bindings. For example, `button.bindEnabled(Signal<Boolean> signal)` sets up a signal binding that updates a button's enabled state reactively when the signal changes.

There are two types of signal bindings:
- One-way bindings: the component state is updated when the signal changes, but not the other way around. For example, `bindEnabled(Signal<Boolean> signal)` is a one-way binding.
- Two-way bindings: the component state is updated when the signal changes, and the signal is updated when the component state changes. For example, `bindValue(Signal<String> valueSignal, SerializableConsumer<String> writeCallback)` is a two-way binding.

When working on component APIs related to signal bindings, ensure that you follow the rules below to ensure the component works properly and behaves consistently.

## Rule 1: Imperative APIs must throw if a one-way signal binding is active

Once a state is bound to a signal through a one-way binding, updating the state imperatively must throw a `BindingActiveException`. This ensures that the component state never goes out of sync with the signal, and prevents developers from changing the state imperatively, only to have that overridden by the next signal change. This does not apply to two-way bindings, as state changes can be synced back to the signal. 

For example:
- A component has the imperative `setText(String text)` and `setContent(Component content)` methods to either set a text node or a component as its contents.
- A reactive `bindText(Signal<String> textSignal)` API is added.
- Both `setText` and `setContent` should throw a `BindingActiveException` if a signal binding has been set up before.

How to accomplish this:
- Use Flow component API pairs such as `Element.setProperty` / `Element.bindProperty`. These throw automatically when the setter is used after the bind method.
- When there is no appropriate set / bind method pair available, `SignalPropertySupport` can be used for custom cases. It throws automatically if its `set` method is used after its `bind` method.

The component must have unit tests to verify that state can not be updated imperatively when a signal binding is active.

## Rule 2: One-way bindings not allowed if the component controls the state

This relates to Rule 1, however in this case, instead of developers calling a public API to imperatively modify state, the component itself modifies the state imperatively.

For example:
- A component has built-in validation that automatically triggers on value change events.
- The component effectively calls `getElement().setProperty("invalid", invalid)` to update the invalid state.
- A `bindInvalid` API would call `getElement().bindProperty("invalid", invalidSignal, null)`.
- When the component tries to update the invalid state on value changes it would now throw a `BindingActiveException`, because `setProperty` checks that no signal binding is active.

In this case having an API that establishes a one-way binding is conceptually wrong. This is a blocker. Do NOT attempt to work around it (e.g. by using a two-way binding or write callback instead). Do NOT write any code. Ask the user on how to proceed.

There are multiple ways to violate this rule, for example:
- The component uses imperative Element APIs, such as `setProperty` or `setAttribute`, internally to modify state, and a `bind*` API uses the corresponding reactive API, such as `bindProperty` or `bindAttribute`.
- The component uses methods inherited from a base class to modify state imperatively, and a `bind*` API uses the corresponding reactive API.
- The component uses `@Synchronize` to sync a property from the client, and a `bind*` API uses `bindProperty` for that property.

Always explore the component implementation to verify that it never modifies state that can be bound through a one-way binding.

## Rule 3: Signal bindings need to run the same side effects as imperative APIs

If a component executes additional logic (a side effect) when setting a state imperatively, then same logic needs to run when the state is updated reactively through a signal.

For example:
- A component has a custom `setReadOnly` implementation that also updates the read-only state of nested components that it manages.
- A `bindReadOnly` implementation also needs to update the read-only state of nested components.

How to accomplish this:
- Any `bind*` API provided by Flow should return a `SignalBinding`. The binding provides an `onChange` API to register callbacks that are called when the signal changes. The callback allows to run the same side effects as the imperative API.
- When using `SignalPropertySupport`, `SignalPropertySupport.create` requires providing a callback that is run whenever the state is changed imperatively or reactively. Use that to run the same side effects.

The component must have unit tests to verify that the same side effects are run, regardless whether state is changed imperatively or reactively.
