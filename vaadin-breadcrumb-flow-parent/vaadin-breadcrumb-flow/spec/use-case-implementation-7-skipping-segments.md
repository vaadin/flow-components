# Use Case 7: Skipping Technical Route Segments

Technical layout routes (no user-visible page) are excluded from the breadcrumb.

## What needs to work

**Option A: Via ItemCustomizer**

```java
breadcrumb.setItemCustomizer((item, routeData) -> {
    if (SettingsLayout.class.equals(routeData.getViewClass())) {
        return null;
    }
    return item;
});
```

**Option B: Via BreadcrumbProvider on the layout**

```java
@Route(value = "settings", layout = MainLayout.class)
public class SettingsLayout extends VerticalLayout
        implements RouterLayout, BreadcrumbProvider {

    @Override
    public List<BreadcrumbItem> getBreadcrumbs() {
        return List.of(); // skip this route
    }
}
```

Navigating to `/settings/general` produces: `Home > General Settings`

## Prerequisites

Use case 3 (auto-generation) and use case 4 (`BreadcrumbProvider`) must be implemented first.

## Implementation needed

### `Breadcrumb.ItemCustomizer` interface

Inner functional interface of `Breadcrumb`:

```java
@FunctionalInterface
public interface ItemCustomizer extends Serializable {
    /**
     * Customizes an auto-generated breadcrumb item.
     *
     * @param item the generated item (can be modified)
     * @param routeData information about the route segment
     * @return the item to use (possibly modified), or {@code null} to
     *         skip this item
     */
    BreadcrumbItem customize(BreadcrumbItem item, RouteData routeData);
}
```

### `RouteData` class

A simple data holder passed to the customizer:

```java
public class RouteData implements Serializable {
    private final Class<? extends Component> viewClass;
    private final String path;
    private final RouteParameters routeParameters;
    private final boolean root;

    // Constructor, getters

    public Class<? extends Component> getViewClass() { return viewClass; }
    public String getPath() { return path; }
    public RouteParameters getRouteParameters() { return routeParameters; }
    public boolean isRoot() { return root; }
}
```

### `Breadcrumb` — customizer field and application

```java
private ItemCustomizer itemCustomizer;

public void setItemCustomizer(ItemCustomizer customizer) {
    this.itemCustomizer = customizer;
}
```

In `generateItems()`, after building items and applying root/maxItems filters:

```java
if (itemCustomizer != null) {
    items = items.stream()
        .map(item -> itemCustomizer.customize(item,
            new RouteData(viewClassForItem, pathForItem, paramsForItem,
                isFirstItem)))
        .filter(Objects::nonNull) // null means skip
        .collect(Collectors.toList());
}
```

### Option B: BreadcrumbProvider on intermediate layouts

During URL-based generation, when building items for each path prefix, also check if the matched route's view class implements `BreadcrumbProvider`:

```java
for (String candidatePath : prefixes) {
    Optional<Class<? extends Component>> target =
            registry.getNavigationTarget(candidatePath);
    if (target.isPresent()) {
        // Check if this intermediate view wants to be skipped
        if (BreadcrumbProvider.class.isAssignableFrom(target.get())) {
            // Instantiation not needed — just check if the class
            // has a static/annotation-based way to signal "skip me".
            // Alternative: check for a marker annotation instead.
        }
        ...
    }
}
```

**Note:** Checking `BreadcrumbProvider` on intermediate views (not the leaf view) during URL-based generation is tricky because the intermediate views are not instantiated — they're just classes. The `getBreadcrumbs()` method requires an instance.

**Practical approach:** The `ItemCustomizer` (Option A) is the recommended way to skip segments during auto-generation. Option B (`BreadcrumbProvider` returning empty list) only works when the layout is the actual navigation target (leaf view), not when it's an intermediate layout in the URL hierarchy.

### Unit tests needed

- `ItemCustomizer` returning the item unchanged → item included
- `ItemCustomizer` returning null → item skipped
- `ItemCustomizer` modifying the item → modified item used
- `RouteData` provides correct view class, path, and root flag
- `setItemCustomizer(null)` → no customization applied
- After skipping items, the last remaining item becomes current page (no path)

### Integration test view

A settings section with a layout wrapper route (`/settings`) and child routes (`/settings/general`, `/settings/notifications`). An `ItemCustomizer` skips the settings layout. Breadcrumb shows `Home > General Settings` when on `/settings/general`.
