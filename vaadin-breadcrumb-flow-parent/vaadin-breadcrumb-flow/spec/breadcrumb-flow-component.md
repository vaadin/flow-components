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

### 2. Paths from Route View Classes

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.addItem(new BreadcrumbItem("Home", HomeView.class));
breadcrumb.addItem(new BreadcrumbItem("Products", ProductsView.class));
breadcrumb.addItem(new BreadcrumbItem("Sprocket"));
```

Items can use view classes instead of string paths for type-safe routing. The path is resolved from the `@Route` annotation at render time. Route parameters are also supported:

```java
breadcrumb.addItem(new BreadcrumbItem("Sprocket",
        ProductView.class, new RouteParameters("id", "42")));
```

### 3. Automatic Router Integration

When placed in a router layout (e.g. `AppLayout`), the breadcrumb can automatically update itself based on the current navigation target and its route hierarchy.

```java
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout { }

@Route(value = "products", layout = MainLayout.class)
@PageTitle("Products")
public class ProductsView extends VerticalLayout { }

@Route(value = "products/:productId", layout = MainLayout.class)
@PageTitle("Product Details")
public class ProductView extends VerticalLayout { }

// In the layout:
public class MainLayout extends AppLayout {
    public MainLayout() {
        Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.setAutoGeneration(true);
        addToNavbar(breadcrumb);
    }
}
```

When `autoGeneration` is enabled, the breadcrumb implements `AfterNavigationObserver` and automatically:
1. Reads the current route and its parent route chain
2. Creates `BreadcrumbItem`s from each route segment, using `@PageTitle` for labels (falling back to the route path segment)
3. Makes all items except the last one clickable (with their resolved paths)
4. The last item represents the current page (no path, gets `aria-current="page"`)

For custom control over automatically generated items, a callback can be provided:

```java
breadcrumb.setAutoGeneration(true);
breadcrumb.setItemCustomizer((item, routeData) -> {
    // Customize label, prefix icon, or skip items
    if (routeData.isRoot()) {
        item.setPrefixComponent(VaadinIcon.HOME.create());
    }
});
```

### 4. With Icons (Prefix)

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
5. **Automatic router integration** — `setAutoGeneration(true)` makes the breadcrumb implement `AfterNavigationObserver` and automatically build the breadcrumb trail from the current route hierarchy, using `@PageTitle` for labels. This is the zero-configuration option for common use cases. When auto-generation is enabled, manually added items are cleared and replaced on each navigation.
6. **`HasAriaLabel` on `Breadcrumb`** — allows setting the `aria-label` for the navigation landmark, consistent with how the web component uses `label` for `aria-label`.
7. **`HasPrefix` on `BreadcrumbItem`** — allows setting a prefix component (e.g., icon) on individual items, consistent with the web component's `prefix` slot.
8. **No default `label` value** — left undefined by default to avoid baked-in English text, consistent with the web component spec.

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
| `setAutoGeneration` | `boolean autoGeneration` | `void` | Enables/disables automatic breadcrumb generation from the route hierarchy on navigation |
| `isAutoGeneration` | — | `boolean` | Returns whether auto-generation is enabled |
| `setItemCustomizer` | `ItemCustomizer customizer` | `void` | Sets a callback for customizing auto-generated items; `null` to clear |

`HasAriaLabel` provides `setAriaLabel(String)` / `getAriaLabel()` for the navigation landmark label.

When `autoGeneration` is `true`, the `Breadcrumb` implements `AfterNavigationObserver`. On each navigation event it:
1. Resolves the active route chain (current view and parent layouts)
2. Creates a `BreadcrumbItem` for each route segment, using `@PageTitle` for the label (falling back to the capitalized path segment)
3. Sets the resolved path on all items except the last (current page)
4. Calls the `ItemCustomizer` (if set) for each item, allowing label/icon customization
5. Replaces all existing items with the generated ones

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

---

**`Breadcrumb.ItemCustomizer`** — Functional interface for customizing auto-generated items

```java
@FunctionalInterface
public interface ItemCustomizer extends Serializable {
    void customize(BreadcrumbItem item, RouteData routeData);
}
```

Called for each auto-generated item during navigation. Allows modifying the item's label, prefix component, enabled state, or other properties. `RouteData` provides information about the route segment (view class, path, route parameters, whether it is the root route).

### Theming Reference

The following CSS custom properties can be used for theming (applied via CSS, not Java API):

| CSS Custom Property | Default | Description |
|---|---|---|
| `--vaadin-breadcrumb-separator-symbol` | `/` (Lumo: angle-right icon) | Separator character/icon |
| `--vaadin-breadcrumb-separator-color` | secondary text color | Separator color |
| `--vaadin-breadcrumb-separator-size` | small font size | Separator font size |
| `--vaadin-breadcrumb-separator-gap` | extra-small space | Space around separator |
