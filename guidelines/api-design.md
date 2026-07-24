# API Design

How the public Java API of a component is shaped. The API
is the product developers see — it must be predictable across the whole
library, so consistency beats local optimisation.

## Consistency over novelty

Match the closest existing component before inventing. Standard shapes:
`get{X}()` / `is{X}()`, `set{X}(value)`, `add{Item}` / `remove{Item}` /
`setItems`, `add{Event}Listener`, `bind{X}(Signal<T>)`,
`set{Slot}Component`, nested `@DomEvent` event classes, `{Component}I18n`.
Deviate only when a real requirement makes the standard shape impossible, and
document it.

## Progressive disclosure through constructors

The common case is one line of Java; complex configuration is opt-in. Achieve
this with overloaded constructors, not builders or long setter chains.

```java
public Button() {}
public Button(String text) { this(); setText(text); }
public Button(Signal<String> textSignal) { this(); bindText(textSignal); }
public Button(String text, Component icon) { this(); setIcon(icon); setText(text); }
public Button(String text, ComponentEventListener<ClickEvent<Button>> l) { this(); setText(text); addClickListener(l); }
```

- A component created with its zero-argument constructor and no further calls
  must render correctly on its own.
- Each overload must match a real use case — don't fill the combinatorial
  matrix for its own sake.
- Reach for an overload — not a setter chain — for new common-case shortcuts.
- No fluent/builder DSLs: components are setter-based, so framework tooling
  (`Binder`, dependency injection, state restoration) sees one predictable
  shape across the whole library.

## Prefer positive-form boolean APIs

Web-component boolean attributes name the non-default state (`disabled`,
`hidden`) because they work by presence/absence. Java has no such constraint,
so the Flow API uses the positive, natural-language form.

| Flow API            | Web component attribute |                        |
| ------------------- | ----------------------- | ---------------------- |
| `setEnabled(false)` | `disabled`              | flipped                |
| `setVisible(false)` | `hidden`                | flipped                |
| `setReadOnly(true)` | `readonly`              | same — already natural |
| `setOpened(true)`   | `opened`                | same                   |

The wrapper hides the mapping; the getter follows the setter's polarity. Flip
the polarity at the element boundary when the attribute is negative:

```java
// custom boolean state with no pre-built mixin
public boolean isCloseOnEscape() {
  return !getElement().getProperty("noCloseOnEscape", false);
}

public void setCloseOnEscape(boolean closeOnEscape) {
    getElement().setProperty("noCloseOnEscape", !closeOnEscape);
}
```

`HasEnabled` and `setVisible` already handle their flips. When the attribute
is already natural (`opened`, `required`), keep the same polarity.
