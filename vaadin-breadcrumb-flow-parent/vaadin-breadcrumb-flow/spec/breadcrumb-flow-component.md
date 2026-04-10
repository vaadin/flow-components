# Vaadin Breadcrumb Flow Component

A breadcrumb shows a sequence of navigation items that lead to the current
page. By default it populates itself automatically from the current route,
so a strict URL hierarchy needs no per-view code at all. For anything the
URL structure cannot express (canonical paths, navigation context,
omissions, fully custom trails), the application sets items explicitly.

## Usage Examples

### 1. Strict URL hierarchy (one-liner)

When the route hierarchy *is* the breadcrumb — each ancestor is a
registered `@Route`, each has a `@PageTitle` (or the current view
implements `HasDynamicTitle`) — there is nothing to configure. Drop
a breadcrumb into the layout and it tracks the current route.

```java
public class MainLayout extends AppLayout {
    public MainLayout() {
        addToNavbar(new Breadcrumb());
    }
}
```

The breadcrumb listens for navigation events, walks each URL prefix
that resolves to a registered route, reads `@PageTitle` from each
ancestor's route class for the label, and uses the resolved URL as
the path. The current view's label comes from its
`HasDynamicTitle.getPageTitle()` if implemented, otherwise from its
`@PageTitle` annotation. The last item has no path and is
automatically marked as the current page.

As soon as the application calls `setItems`, `add`, or
`addItemAtIndex`, auto-population switches off for that breadcrumb
instance (it becomes manual). Call `setAutoPopulateFromRoute(true)`
to turn it back on. Pass `new Breadcrumb(false)` to start in manual
mode from the beginning.

### 2. Static trail with route classes

For views that want explicit control — or that sit in a route
structure that isn't directly hierarchical — construct the items
yourself. Calling `add` disables auto-population.

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.add(
        new BreadcrumbItem("Home", HomeView.class),
        new BreadcrumbItem("Electronics", ElectronicsView.class),
        new BreadcrumbItem("Laptops", LaptopsView.class),
        new BreadcrumbItem("ThinkPad X1 Carbon"));
```

The last item has no path, so it is automatically rendered as the
current page (`aria-current="page"`) by the underlying web component.

### 3. Trail computed at runtime

Rebuild the trail from data you compute in the view (canonical path,
navigation state, dynamic chain — see UC2, UC3, UC4, UC8).

```java
Breadcrumb breadcrumb = new Breadcrumb();

// later, e.g. when the view changes or data loads:
List<BreadcrumbItem> trail = new ArrayList<>();
trail.add(new BreadcrumbItem("Home", HomeView.class));
trail.add(new BreadcrumbItem("Electronics", ElectronicsView.class));
trail.add(new BreadcrumbItem("Laptops", LaptopsView.class));
trail.add(new BreadcrumbItem(product.getName()));
breadcrumb.setItems(trail);
```

`setItems` replaces all current items. Passing an empty list or
`setItems()` (no-arg varargs) clears the trail.

### 4. Trail from a data model

When items come from a data model (e.g. a navigation service), map the
records to `BreadcrumbItem` instances and pass them to `setItems`.

```java
record Crumb(String label, String path) {}

List<Crumb> crumbs = navigationService.getCanonicalTrail(productId);

breadcrumb.setItems(crumbs.stream()
        .map(c -> c.path() == null
                ? new BreadcrumbItem(c.label())
                : new BreadcrumbItem(c.label(), c.path()))
        .toList());
```

### 5. Item with a prefix (icon)

Items can carry a prefix component (typically an icon) via `HasPrefix`.

```java
BreadcrumbItem home = new BreadcrumbItem("Home", HomeView.class);
home.setPrefixComponent(VaadinIcon.HOME.create());

Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.add(home,
        new BreadcrumbItem("Electronics", ElectronicsView.class),
        new BreadcrumbItem("ThinkPad X1 Carbon"));
```

### 6. Custom separator

Applications can replace the default chevron with any component. The
component clones the separator between each pair of items.

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setSeparator(new Span("/"));
breadcrumb.add(
        new BreadcrumbItem("Home", HomeView.class),
        new BreadcrumbItem("Electronics", ElectronicsView.class),
        new BreadcrumbItem("ThinkPad X1 Carbon"));
```

### 7. View-class path with route parameters

```java
BreadcrumbItem employee = new BreadcrumbItem("Jane Doe",
        EmployeeView.class,
        new RouteParameters("id", "123"));
```

### 8. Plain URL path (external or non-Vaadin)

```java
BreadcrumbItem docs = new BreadcrumbItem("Docs", "https://vaadin.com/docs");
docs.setOpenInNewBrowserTab(true);
```

---

### Key Design Decisions

1. **Auto-populate from the current route by default.** The Flow
   component integrates with the Vaadin router so that UC1 (strict URL
   hierarchy) needs literally no code beyond `new Breadcrumb()`. On
   attach, the component registers an `AfterNavigationListener` on the
   current `UI`. On each navigation it walks the URL path segments,
   resolves each prefix against the session's `RouteConfiguration`,
   and for each resolved ancestor route class reads the `@PageTitle`
   annotation to form a `BreadcrumbItem`. The current view's label is
   taken from `HasDynamicTitle.getPageTitle()` if the view implements
   it, otherwise from its `@PageTitle` annotation. Segments that do
   not resolve to a registered route are skipped. This behavior is
   purely a Flow-side convenience — the underlying web component
   remains router-agnostic. The web component spec's "no automatic
   trail computation" rule applies at the web component layer; the
   Flow wrapper deliberately adds this on top because Flow *does*
   know about the current route and hiding that knowledge would make
   the primary use case needlessly verbose.
   - *Why default on:* UC1 is the primary use case in `use-cases.md`
     and should be the easiest one to accomplish. Requiring an explicit
     opt-in method on every Breadcrumb would defeat that.
   - *How to opt out:* call `setAutoPopulateFromRoute(false)` (or
     use the `new Breadcrumb(false)` constructor). Any call to
     `add`, `addItemAtIndex`, or `setItems` automatically switches
     the breadcrumb to manual mode, because the two modes are
     mutually exclusive and the explicit items should win.

2. **`BreadcrumbItem` is both the declarative and the programmatic form.**
   In Flow, constructing Java objects *is* the programmatic form —
   unlike JavaScript there is no meaningful difference between building
   DOM and building a data array. A single `BreadcrumbItem` type covers
   both the web component's declarative markup and its `items` property.
   `setItems(List<BreadcrumbItem>)` and `setItems(BreadcrumbItem...)` are
   provided as "replace all" convenience on top of `add` / `remove`.

3. **Last item as current page is implicit.** The web component
   automatically treats the last item as the current page. The Flow API
   follows suit — there is no `setCurrent` flag. An item with no path
   renders as plain text; an item with a path but which happens to be
   last is still marked `aria-current="page"` by the web component.

4. **Path accepts both `String` and a view class**, mirroring
   `SideNavItem`. The view-class overload uses `RouteConfiguration` to
   resolve the URL, which is the idiomatic Flow way and survives
   refactoring. Route parameters and typed URL parameters are
   supported through additional overloads, same shape as `SideNavItem`.

5. **`target` / open-in-new-tab is carried over from `SideNavItem`**
   for consistency, even though no use case explicitly requires it.
   It is a zero-cost convenience for the common "external link in
   breadcrumb" situation.

6. **No `items-changed` listener.** The web component fires
   `items-changed` to support two-way binding in JS frameworks; in Flow
   the server is the source of truth, so no listener is needed.
   If a future use case requires intercepting clicks on ancestor items,
   a cancellable `BreadcrumbItemClickEvent` can be added without
   breaking the current API.

7. **No theme variants.** The web component spec defines no theme
   variants, so the Flow component does not implement `HasThemeVariant`.

8. **No `HasComponents`.** `Breadcrumb#add` is typed as
   `add(BreadcrumbItem...)` rather than coming from `HasComponents`,
   because a breadcrumb is a list of `BreadcrumbItem`s — arbitrary
   components are not a valid child. The separator is set via a
   dedicated slot method, not by adding children.

9. **Feature flag.** The breadcrumb web component is experimental and
   gated by `window.Vaadin.featureFlags.breadcrumbComponent = true`. The
   Flow component enables the flag automatically on attach (via
   `Element.executeJs` in the attach handler) so Flow users don't have
   to configure it manually. The experimental status is documented on
   the class Javadoc.

---

## Implementation

### Classes

**`Breadcrumb`** — Main component class

Extends `Component`, implements `HasSize`, `HasStyle`.
Annotated with `@Tag("vaadin-breadcrumb")` and the appropriate
`@NpmPackage` / `@JsModule`.

| Constructor | Parameters | Description |
|---|---|---|
| `Breadcrumb()` | — | Creates an empty breadcrumb in auto-populate-from-route mode. On attach, the breadcrumb registers an `AfterNavigationListener` on the current `UI` and rebuilds its items on each navigation. |
| `Breadcrumb(boolean autoPopulateFromRoute)` | `boolean autoPopulateFromRoute` | Creates an empty breadcrumb. When `false`, auto-population is disabled — the breadcrumb stays empty until the application calls `add` or `setItems`. |
| `Breadcrumb(BreadcrumbItem... items)` | `BreadcrumbItem... items` | Creates a breadcrumb containing the given items in order. Auto-population is disabled, because the explicit items are the trail. |

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `add` | `BreadcrumbItem... items` | `void` | Appends items to the trail. Null entries throw `NullPointerException`. **Side effect:** disables auto-populate-from-route if it was enabled. |
| `addItemAtIndex` | `int index, BreadcrumbItem item` | `void` | Inserts an item at a specific position in the trail. **Side effect:** disables auto-populate-from-route. |
| `remove` | `BreadcrumbItem... items` | `void` | Removes the given items from the trail. Items not currently in this breadcrumb are ignored. |
| `removeAll` | — | `void` | Removes all items from the trail. Does not affect the separator. Does not re-enable auto-populate. |
| `setItems` | `BreadcrumbItem... items` | `void` | Replaces all current items with the given items, in order. Equivalent to `removeAll()` followed by `add(items)`. **Side effect:** disables auto-populate-from-route. |
| `setItems` | `List<BreadcrumbItem> items` | `void` | As above, taking a `List`. A `null` list is treated as an empty list. |
| `getItems` | — | `List<BreadcrumbItem>` | Returns an unmodifiable list of the items currently in the trail, in order. Does not include the separator. |
| `setAutoPopulateFromRoute` | `boolean autoPopulate` | `void` | Turns auto-population on or off. Turning on immediately populates the trail from the current route. Turning off leaves the current items in place. |
| `isAutoPopulateFromRoute` | — | `boolean` | Returns whether auto-population is currently enabled. |
| `setSeparator` | `Component separator` | `void` | Sets a custom separator component. The web component clones this between each pair of items. Passing `null` restores the default chevron. |
| `getSeparator` | — | `Component` | Returns the current custom separator component, or `null` if the default is in use. |

---

**`BreadcrumbItem`** — A single entry in the trail

Extends `Component`, implements `HasPrefix`, `HasEnabled`, `HasStyle`.
Annotated with `@Tag("vaadin-breadcrumb-item")` and the appropriate
`@NpmPackage` / `@JsModule`.

| Constructor | Parameters | Description |
|---|---|---|
| `BreadcrumbItem()` | — | Creates an empty item with no label and no path. |
| `BreadcrumbItem(String label)` | `String label` | Creates an item with the given label and no path. Use this for the current-page (last) item. |
| `BreadcrumbItem(String label, String path)` | `String label, String path` | Creates an item with the given label that links to the given URL path. |
| `BreadcrumbItem(String label, Class<? extends Component> view)` | `String label, Class<? extends Component> view` | Creates an item with the given label that links to the given view (resolved via `RouteConfiguration`). |
| `BreadcrumbItem(String label, Class<? extends Component> view, RouteParameters routeParameters)` | `String label, Class<? extends Component> view, RouteParameters routeParameters` | Creates an item linking to a view with route parameters. |
| `<T, C extends Component & HasUrlParameter<T>> BreadcrumbItem(String label, Class<? extends C> view, T parameter)` | `String label, Class<? extends C> view, T parameter` | Creates an item linking to a view with a typed URL parameter. |

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `getLabel` | — | `String` | Returns the visible label. |
| `setLabel` | `String label` | `void` | Sets the visible label. |
| `getPath` | — | `String` | Returns the URL path this item links to, or `null` if the item has no path. |
| `setPath` | `String path` | `void` | Sets the URL path this item links to. Passing `null` removes the path, making this item render as plain text. |
| `setPath` | `Class<? extends Component> view` | `void` | Resolves the view's `@Route` via `RouteConfiguration` and sets that as the path. |
| `setPath` | `Class<? extends Component> view, RouteParameters routeParameters` | `void` | Same as above, with route parameters. |
| `<T, C extends Component & HasUrlParameter<T>> setPath` | `Class<? extends C> view, T parameter` | `void` | Same as above, with a typed URL parameter. |
| `getTarget` | — | `String` | Returns the anchor `target` attribute of the link, or `null` if not set. |
| `setTarget` | `String target` | `void` | Sets the anchor `target` attribute of the link. Only meaningful when a path is set. |
| `isOpenInNewBrowserTab` | — | `boolean` | Convenience: returns `"_blank".equals(getTarget())`. |
| `setOpenInNewBrowserTab` | `boolean openInNewBrowserTab` | `void` | Convenience shortcut for `setTarget("_blank")` / `setTarget(null)`. |

Inherited from `HasPrefix`:

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `setPrefixComponent` | `Component component` | `void` | Sets the component displayed before the label (typically an icon). |
| `getPrefixComponent` | — | `Component` | Returns the current prefix component, or `null`. |

---

### Behavior notes

- `add(BreadcrumbItem...)` and friends only accept `BreadcrumbItem`
  instances. The separator is configured through `setSeparator` and is
  not part of the items list.
- The last item in the trail is always the "current page" regardless of
  whether it has a path. This is done by the web component; no server
  state needs to track it.
- Calling `setItems(...)` on a Breadcrumb that already has items
  removes the existing items from the DOM before adding the new ones
  (i.e. `getItems()` returns only the most recently set list).
- The underlying web component remains router-agnostic: items with a
  path render as `<a href>`. The Vaadin client router, if present,
  intercepts the click like any other anchor. The Flow-level
  auto-populate feature adds router *read* integration (listening for
  navigation events and resolving routes) but does not change how
  activation happens.

### Auto-populate-from-route mechanics

When a `Breadcrumb` is in auto-populate mode (the default) and is
attached:

1. It registers an `AfterNavigationListener` on the current `UI`.
   The registration is removed in the detach handler.
2. On each navigation event, the breadcrumb clears its existing
   items and rebuilds them from `event.getLocation()`:
    - Splits the location's path into segments.
    - For each non-empty prefix of the segments (including the empty
      prefix for the root route), calls
      `RouteConfiguration.forSessionScope().getRoute(prefix)` to
      resolve it to a route class.
    - For each resolved class, reads its `@PageTitle` annotation. If
      absent, the prefix is skipped. (A future I18n hook may allow
      overriding this fallback.)
    - Creates a `BreadcrumbItem` with that label and the resolved
      URL as the path.
3. The final item is the current view itself. If the current view
   implements `HasDynamicTitle`, its `getPageTitle()` is used as the
   label. Otherwise the class's `@PageTitle` is used. The last item
   has no path set, so the web component marks it as the current
   page.
4. Rebuilding uses the same `removeAll` + internal-`add` path as
   manual mode, but does *not* flip the mode flag to manual — only
   application-level calls to `add` / `setItems` do.

This behavior is a best-effort convenience tailored to UC1. Apps
whose trail does not map one-to-one to URL prefixes (UC2–UC8) should
turn auto-populate off and set items explicitly.

---

### Accessibility

- The web component wraps its items in `<nav aria-label="Breadcrumb">`
  and an ordered list. No Flow API is needed to produce this markup.
- `aria-current="page"` is automatically set on the last item.
- The breadcrumb does not implement `HasAriaLabel` because the label
  is fixed by the WAI-ARIA Breadcrumb pattern. If a future use case
  requires a localized label, an `I18n` object (following the
  `SideNavI18n` pattern) can be added without breaking the current API.

---

### CSS custom properties (reference only)

Theming is done via CSS custom properties defined by the web component
(see `breadcrumb-web-component.md`):

- `--vaadin-breadcrumb-gap`
- `--vaadin-breadcrumb-padding`
- `--vaadin-breadcrumb-font-family`
- `--vaadin-breadcrumb-font-size`
- `--vaadin-breadcrumb-text-color`
- `--vaadin-breadcrumb-current-text-color`
- `--vaadin-breadcrumb-separator-color`
- `--vaadin-breadcrumb-separator-symbol`
- `--vaadin-breadcrumb-item-padding`
- `--vaadin-breadcrumb-item-text-decoration`
- `--vaadin-breadcrumb-item-hover-text-decoration`

These are not exposed as Java methods.

---

### Features considered but omitted

- **Theme variants.** Not defined by the web component spec.
- **`items-changed` / click listeners.** No use case requires
  intercepting clicks; plain anchors handle navigation via the
  client-side router. Can be added later as a cancellable event
  without breaking the current API.
- **Data provider / `setItems(Stream<T>, ItemLabelGenerator)`.**
  A breadcrumb trail is short by definition (typically 2–6 items),
  built fresh per navigation, and mixes links with a final
  non-link item. The existing `setItems(List<BreadcrumbItem>)` is
  sufficient; a generic data-provider overload would add ceremony
  without covering a use case from `use-cases.md`.
- **`HasAriaLabel`.** The WAI-ARIA pattern fixes the label; exposing
  a setter would encourage breaking the pattern.

---

### Coverage check: use cases → API

| Use case | How the Flow API covers it |
|---|---|
| UC1 Strict hierarchy | Drop `new Breadcrumb()` into the layout. Auto-populate-from-route walks each URL prefix, resolves each to a route class, reads its `@PageTitle`, and builds the trail. The current view's label comes from `HasDynamicTitle.getPageTitle()` (or its `@PageTitle`). No per-view code is needed. |
| UC2 Canonical path | Application builds the canonical trail (independent of how the page was reached) and calls `setItems`. |
| UC3 No canonical, current-only on direct entry | Application either passes a multi-item trail built from navigation context or a single-item trail containing just the current page. |
| UC4 Deep navigation with static prefix | Application concatenates the fixed prefix items with the dynamic chain and calls `setItems`. |
| UC5 Omit from trail start | Application omits the parent entries when building the item list. |
| UC6 Omit from trail end | Application omits the trailing entry when building the item list. |
| UC7 Omit intermediate views | Application omits the wrapper entry when building the item list. |
| UC8 Programmatic customization per route | Direct usage of `setItems` with any content the application wants. |
