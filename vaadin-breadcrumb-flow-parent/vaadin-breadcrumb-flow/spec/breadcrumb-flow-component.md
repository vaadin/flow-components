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

### 3. Automatic Router Integration (Strict Hierarchy)

When placed in a router layout (e.g. `AppLayout`), the breadcrumb can automatically update itself based on the current URL path and route configuration.

```java
@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout { }

@Route(value = "electronics", layout = MainLayout.class)
@PageTitle("Electronics")
public class ElectronicsView extends VerticalLayout { }

@Route(value = "electronics/laptops", layout = MainLayout.class)
@PageTitle("Laptops")
public class LaptopsView extends VerticalLayout { }

@Route(value = "electronics/laptops/:productId", layout = MainLayout.class)
@PageTitle("ThinkPad X1 Carbon")
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

Navigating to `/electronics/laptops/thinkpad-x1` produces: `Home > Electronics > Laptops > ThinkPad X1 Carbon`

The breadcrumb is fully determined by the URL structure. Each URL path prefix that matches a registered route becomes an item. `@PageTitle` is used for labels (falling back to the capitalized path segment). All items except the last are clickable.

### 4. Canonical Path (View-Declared Breadcrumbs)

When a page can be reached from multiple paths but should always show the same breadcrumb trail, the view declares its canonical breadcrumb trail using the `BreadcrumbProvider` interface:

```java
@Route(value = "electronics/laptops/:productId", layout = MainLayout.class)
@RouteAlias(value = "deals/black-friday/:productId", layout = MainLayout.class)
public class ProductView extends VerticalLayout implements BreadcrumbProvider {

    @Override
    public List<BreadcrumbItem> getBreadcrumbs() {
        return List.of(
            new BreadcrumbItem("Home", HomeView.class),
            new BreadcrumbItem("Electronics", ElectronicsView.class),
            new BreadcrumbItem("Laptops", LaptopsView.class),
            new BreadcrumbItem("ThinkPad X1 Carbon")
        );
    }
}
```

Whether the user navigates via `/electronics/laptops/thinkpad-x1` or `/deals/black-friday/thinkpad-x1`, the breadcrumb always shows: `Home > Electronics > Laptops > ThinkPad X1 Carbon`

When auto-generation is enabled and the current view implements `BreadcrumbProvider`, the provider's items take precedence over URL-based generation.

### 5. Scoped to a Section

The breadcrumb can be scoped to start at a section root instead of the application root:

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setAutoGeneration(true);
breadcrumb.setRootPath("departments/engineering");
// or
breadcrumb.setRootView(EngineeringView.class);
```

Navigating to `/departments/engineering/frontend/alice` produces: `Engineering > Frontend > Alice`

The `departments` prefix is not included in the breadcrumb.

### 6. Limiting Depth

The breadcrumb can be limited to a maximum number of levels:

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setAutoGeneration(true);
breadcrumb.setMaxItems(2);
```

Navigating to `/departments/engineering/frontend` produces: `Departments > Engineering`

Only the first two levels are shown. Deeper views are not reflected in the breadcrumb.

### 7. Skipping Technical Route Segments

Some routes are layout wrappers that don't represent navigable pages. These can be excluded from the breadcrumb via the `ItemCustomizer`:

```java
breadcrumb.setAutoGeneration(true);
breadcrumb.setItemCustomizer((item, routeData) -> {
    // Return null to skip this item
    if (SettingsLayout.class.equals(routeData.getViewClass())) {
        return null;
    }
    return item;
});
```

Or by having the layout implement `BreadcrumbProvider` and returning an empty list:

```java
@Route(value = "settings", layout = MainLayout.class)
public class SettingsLayout extends VerticalLayout
        implements RouterLayout, BreadcrumbProvider {

    @Override
    public List<BreadcrumbItem> getBreadcrumbs() {
        return List.of(); // This route is not a page — skip it
    }
}
```

Navigating to `/settings/general` produces: `Home > General Settings` (skipping "Settings").

### 8. Customizing Auto-Generated Items

For custom control over automatically generated items, a callback can be provided:

```java
breadcrumb.setAutoGeneration(true);
breadcrumb.setItemCustomizer((item, routeData) -> {
    // Add an icon to the root item
    if (routeData.isRoot()) {
        item.setPrefixComponent(VaadinIcon.HOME.create());
    }
    // Return the item (or null to skip it)
    return item;
});
```

### 9. With Icons (Prefix)

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
5. **Automatic router integration** — `setAutoGeneration(true)` makes the breadcrumb implement `AfterNavigationObserver` and automatically build the breadcrumb trail from URL path prefixes that match registered routes, using `@PageTitle` for labels. This is the zero-configuration option for strict hierarchies (use case 1).
6. **`BreadcrumbProvider` interface** — views can implement this to declare their canonical breadcrumb trail, overriding URL-based auto-generation. This handles use cases where the breadcrumb isn't derivable from the URL (canonical paths, multiple entry points). Returning an empty list skips the view in the breadcrumb trail (for technical layout routes).
7. **Scoping and depth control** — `setRootPath()`/`setRootView()` limits auto-generation to a section of the app. `setMaxItems()` limits the number of breadcrumb levels shown.
8. **`ItemCustomizer` with skip support** — returning `null` from the customizer skips that item, enabling filtering of technical route segments without modifying the view.
9. **`HasAriaLabel` on `Breadcrumb`** — allows setting the `aria-label` for the navigation landmark, consistent with how the web component uses `label` for `aria-label`.
10. **`HasPrefix` on `BreadcrumbItem`** — allows setting a prefix component (e.g., icon) on individual items, consistent with the web component's `prefix` slot.
11. **No default `label` value** — left undefined by default to avoid baked-in English text, consistent with the web component spec.

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
| `setAutoGeneration` | `boolean autoGeneration` | `void` | Enables/disables automatic breadcrumb generation from routes on navigation |
| `isAutoGeneration` | — | `boolean` | Returns whether auto-generation is enabled |
| `setItemCustomizer` | `ItemCustomizer customizer` | `void` | Sets a callback for customizing auto-generated items; `null` to clear |
| `setRootPath` | `String rootPath` | `void` | Sets the root path for auto-generation; items above this path are excluded |
| `setRootView` | `Class<? extends Component> rootView` | `void` | Sets the root view class for auto-generation; items above this view's route are excluded |
| `setMaxItems` | `int maxItems` | `void` | Limits the number of breadcrumb items shown; 0 for unlimited (default) |
| `getMaxItems` | — | `int` | Returns the maximum number of items |

`HasAriaLabel` provides `setAriaLabel(String)` / `getAriaLabel()` for the navigation landmark label.

When `autoGeneration` is `true`, the `Breadcrumb` implements `AfterNavigationObserver`. On each navigation event it:
1. Checks if the current view implements `BreadcrumbProvider` — if so, uses its items directly
2. Otherwise, resolves breadcrumb items from URL path prefixes: for each prefix of the current URL path that matches a registered route, creates a `BreadcrumbItem` using `@PageTitle` for the label (falling back to the capitalized path segment)
3. Filters items based on `rootPath`/`rootView` (excludes items above the root)
4. Limits items to `maxItems` (if set), keeping the first N levels
5. Calls the `ItemCustomizer` (if set) for each item — if it returns `null`, the item is skipped
6. Sets the path on all items except the last (current page gets no path → `aria-current="page"`)
7. Replaces all existing items with the generated ones

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

**`BreadcrumbProvider`** — Interface for views to declare their breadcrumb trail

```java
public interface BreadcrumbProvider {
    List<BreadcrumbItem> getBreadcrumbs();
}
```

Views or router layouts that implement this interface provide their own breadcrumb items, overriding URL-based auto-generation. Returning an empty list means the view should be skipped in the breadcrumb trail (useful for technical layout routes).

---

**`Breadcrumb.ItemCustomizer`** — Functional interface for customizing auto-generated items

```java
@FunctionalInterface
public interface ItemCustomizer extends Serializable {
    BreadcrumbItem customize(BreadcrumbItem item, RouteData routeData);
}
```

Called for each auto-generated item during navigation. Can modify and return the item, or return `null` to skip it. `RouteData` provides information about the route segment (view class, path, route parameters, whether it is the root route).

### Theming Reference

The following CSS custom properties can be used for theming (applied via CSS, not Java API):

| CSS Custom Property | Default | Description |
|---|---|---|
| `--vaadin-breadcrumb-separator-symbol` | `/` (Lumo: angle-right icon) | Separator character/icon |
| `--vaadin-breadcrumb-separator-color` | secondary text color | Separator color |
| `--vaadin-breadcrumb-separator-size` | small font size | Separator font size |
| `--vaadin-breadcrumb-separator-gap` | extra-small space | Space around separator |
