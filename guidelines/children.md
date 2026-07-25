# Children

The web component's content model determines the Flow API for children. For
every place the web component accepts child components — its default slot or
a named slot — the Flow component exposes exactly one Java API. The shape of
that API follows from two questions: how many children does the place hold,
and what does it accept?

| Holds | Accepts       | API shape                                                                         | Examples                                 |
| ----- | ------------- | --------------------------------------------------------------------------------- | ---------------------------------------- |
| one   | any component | `setX(Component)` / `getX()`, `null` clears                                       | `Card.setTitle`, `Scroller.setContent`   |
| many  | any component | `HasComponents` (default slot); `addToX(...)` + `getXComponents()` (named slot)   | `VerticalLayout`; `Card.addToFooter`     |
| many  | one type      | `HasComponentsOfType<T>` or typed `add` / `remove`, plus factory methods          | `Breadcrumbs`, `Tabs`, `Accordion`       |

Children computed from data (items, options) are not managed through these
APIs at all — expose `setItems(...)` and renderers instead; see
[Data & Validation](data-and-validation.md).

## Named slots

Manage named-slot children with `SlotUtils`
(`com.vaadin.flow.component.shared`). It owns the two invariants of slotted
children: the `slot` attribute is set before the child is appended, and it is
removed again when the child leaves the slot.

- Single-occupant slot: `setSlot` (replaces the previous occupant) +
  `getChildInSlot`; passing `null` clears the slot.
- Multi-occupant slot: `addToSlot` (additive) + `getElementsInSlot`;
  `clearSlot` empties the slot.

For the standard `prefix` / `suffix` slots, implement the shared `HasPrefix` /
`HasSuffix` mixins instead of writing the setters by hand.

Drop to the `Element` API only when the slot assignment depends on component
state and a plain setter cannot model it. Button, for example, moves its icon
between the `prefix` and `suffix` slots and removes the attribute entirely
for icon-only buttons. Hand-rolled slot code must still uphold the
`SlotUtils` invariants above.

## Default-slot containers

A component whose default slot accepts any number of arbitrary children
implements `HasComponents` and inherits complete container behaviour —
`add` / `remove` / `removeAll` / `addComponentAtIndex`, re-parenting of
already-attached children, rejection of foreign children on remove.
(`HasOrderedComponents` is deprecated; `HasComponents` covers ordering.)

Do not implement `HasComponents` when the content model is narrower — it
would advertise capability the web component does not have. Instead:

- Single content child: `setContent(Component)` / `getContent()`
  (`Scroller`, `AppLayout`).
- A fixed set of regions: one `addToX` method per region
  (`SplitLayout.addToPrimary` / `addToSecondary`).
- A specific child type: see [Typed children](#typed-children).

When the DOM is more complex than "children of my element", the component
must override `HasComponents` methods so the interface stays consistent:

- **Forwarded content** — children live in an inner element: forward the
  mutation and accessor methods to it (`Details` forwards to its content
  `Div`).
- **Mixed slots** — named-slot children coexist with default-slot children:
  filter children carrying a `slot` attribute out of `getChildren()`, and
  keep `removeAll()` and `addComponentAtIndex(...)` consistent with that
  filtered view (`Card`, `Dialog`).

## Typed children

When the web component only works with a specific child component
(`Tabs` / `Tab`, `Accordion` / `AccordionPanel`), the Java API enforces that
type in its signatures: implement `HasComponentsOfType<T>` (`Breadcrumbs`),
or provide typed `add` / `remove` methods when mutations need extra
bookkeeping (`Tabs` maintains its selection across mutations).

Consider adding factory overloads that create, add, and return the child:

```java
public AccordionPanel add(String summary, Component content) {
    return add(new AccordionPanel(summary, content));
}
// accordion.add("Details", body).setOpened(true);
```

The same convention: `TabSheet.add(String, Component)` returns the `Tab`,
`MenuBar.addItem(String)` returns the `MenuItem`, `Grid.addColumn(...)`
returns the `Column`.

When a typed child owns associated components, removing the child removes
them too — `TabSheet.remove(Tab)` also removes the tab's content.
