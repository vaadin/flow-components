# Vaadin Breadcrumb Flow Component

## Usage Examples

### 1. Basic Navigation

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.addItem(new BreadcrumbItem("Home", "/"));
breadcrumb.addItem(new BreadcrumbItem("Products", "/products"));
breadcrumb.addItem(new BreadcrumbItem("Widgets", "/products/widgets"));
breadcrumb.addItem(new BreadcrumbItem("Sprocket"));
```

The last item has no path, indicating it is the current page.

### 2. Router Integration

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.addItem(new BreadcrumbItem("Home", HomeView.class));
breadcrumb.addItem(new BreadcrumbItem("Products", ProductsView.class));
breadcrumb.addItem(new BreadcrumbItem("Sprocket"));
```

Items can use view classes instead of string paths for type-safe routing. Route parameters are also supported:

```java
breadcrumb.addItem(new BreadcrumbItem("Sprocket",
        ProductView.class, new RouteParameters("id", "42")));
```

### 3. With Icons (Prefix)

```java
BreadcrumbItem homeItem = new BreadcrumbItem("Home", "/");
homeItem.setPrefixComponent(VaadinIcon.HOME.create());

Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.addItem(homeItem);
breadcrumb.addItem(new BreadcrumbItem("Products", "/products"));
breadcrumb.addItem(new BreadcrumbItem("Widgets"));
```

---
### Key Design Decisions

1. **Child component API** — `addItem(BreadcrumbItem...)` for adding items individually, following the `SideNav`/`HasSideNavItems` pattern.
2. **`BreadcrumbItem` as a dedicated class** — not a generic component. Provides type-safe API for `path`, `disabled`, label text, and prefix component. Follows the `SideNavItem` pattern.
3. **`path` property** on items instead of `href` — consistent with `SideNavItem`. Supports both `String` paths and type-safe `Class<? extends Component>` view class overloads with optional `RouteParameters`.
4. **Auto `aria-current="page"`** on the last item when it has no `path` — handled by the web component.
5. **`HasAriaLabel` on `Breadcrumb`** — allows setting the `aria-label` for the navigation landmark, consistent with how the web component uses `label` for `aria-label`.
6. **`HasPrefix` on `BreadcrumbItem`** — allows setting a prefix component (e.g., icon) on individual items, consistent with the web component's `prefix` slot.
7. **No default `label` value** — left undefined by default to avoid baked-in English text, consistent with the web component spec.

---

## Implementation

### Classes

**`Breadcrumb`** — Container component

Extends `Component`, implements `HasSize`, `HasAriaLabel`.

| Constructor | Parameters | Description |
|---|---|---|
| `Breadcrumb()` | — | Creates an empty breadcrumb |
| `Breadcrumb(BreadcrumbItem... items)` | `BreadcrumbItem... items` | Creates a breadcrumb with the given items |

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `addItem` | `BreadcrumbItem... items` | `void` | Appends items to the breadcrumb |
| `addItemAsFirst` | `BreadcrumbItem item` | `void` | Inserts an item at the beginning |
| `addItemAtIndex` | `int index, BreadcrumbItem item` | `void` | Inserts an item at the given position |
| `remove` | `BreadcrumbItem... items` | `void` | Removes the given items |
| `removeAll` | — | `void` | Removes all items |

`HasAriaLabel` provides `setAriaLabel(String)` / `getAriaLabel()` for the navigation landmark label.

---

**`BreadcrumbItem`** — Individual breadcrumb item

Extends `Component`, implements `HasText`, `HasEnabled`, `HasPrefix`.

| Constructor | Parameters | Description |
|---|---|---|
| `BreadcrumbItem()` | — | Creates an empty item |
| `BreadcrumbItem(String text)` | `String text` | Creates an item with the given label (current page, no path) |
| `BreadcrumbItem(String text, String path)` | `String text, String path` | Creates an item with a label and string path |
| `BreadcrumbItem(String text, Class<? extends Component> view)` | `String text, Class<? extends Component> view` | Creates an item with a label and router view class |
| `BreadcrumbItem(String text, Class<? extends Component> view, RouteParameters params)` | `String text, Class<? extends Component> view, RouteParameters params` | Creates an item with a label, view class, and route parameters |

| Method | Parameters | Returns | Description |
|---|---|---|---|
| `setPath` | `String path` | `void` | Sets the navigation path; `null` to clear (marks as current page) |
| `getPath` | — | `Optional<String>` | Gets the navigation path |
| `setPath` | `Class<? extends Component> view` | `void` | Sets the path from a router view class |
| `setPath` | `Class<? extends Component> view, RouteParameters params` | `void` | Sets the path from a router view class with parameters |

`HasText` provides `setText(String)` / `getText()` for the item label.
`HasEnabled` provides `setEnabled(boolean)` / `isEnabled()` for disabling navigation.
`HasPrefix` provides `setPrefixComponent(Component)` / `getPrefixComponent()` for content before the label (e.g., icon).

### Theming Reference

The following CSS custom properties can be used for theming (applied via CSS, not Java API):

| CSS Custom Property | Default | Description |
|---|---|---|
| `--vaadin-breadcrumb-separator-symbol` | `/` (Lumo: angle-right icon) | Separator character/icon |
| `--vaadin-breadcrumb-separator-color` | secondary text color | Separator color |
| `--vaadin-breadcrumb-separator-size` | small font size | Separator font size |
| `--vaadin-breadcrumb-separator-gap` | extra-small space | Space around separator |
