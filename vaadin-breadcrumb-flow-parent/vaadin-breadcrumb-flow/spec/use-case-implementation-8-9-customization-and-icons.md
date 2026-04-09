# Use Cases 8 & 9: Customizing Items and Icons (Prefix)

Adding icons or other customization to breadcrumb items.

## What needs to work

**Use case 8: Customizing auto-generated items**

```java
breadcrumb.setAutoGeneration(true);
breadcrumb.setItemCustomizer((item, routeData) -> {
    if (routeData.isRoot()) {
        item.setPrefixComponent(VaadinIcon.HOME.create());
    }
    return item;
});
```

**Use case 9: Manual icons**

```java
BreadcrumbItem homeItem = new BreadcrumbItem("Home", "/");
homeItem.setPrefixComponent(VaadinIcon.HOME.create());
```

## Prerequisites

Use case 1 (basic navigation with `HasPrefix`) covers the manual icon case. Use case 7 (ItemCustomizer/RouteData) covers the auto-generation customization infrastructure.

## Implementation needed

No additional implementation is needed beyond what use cases 1 and 7 provide:

- **Manual icons (use case 9):** `BreadcrumbItem` implements `HasPrefix`, which provides `setPrefixComponent(Component)` / `getPrefixComponent()`. This works out of the box once use case 1 is implemented. The `HasPrefix` interface uses `SlotUtils` to put the component into `slot="prefix"`, matching the web component's `prefix` slot.

- **Auto-generation customization (use case 8):** The `ItemCustomizer` receives each generated item and can call any method on it, including `setPrefixComponent()`. The `RouteData.isRoot()` flag identifies the root item. This works once use case 7's `ItemCustomizer` infrastructure is in place.

### Unit tests needed

- `setPrefixComponent(icon)` → icon is in the prefix slot
- `getPrefixComponent()` returns the set icon
- `setPrefixComponent(null)` removes the prefix
- `ItemCustomizer` can call `setPrefixComponent()` on items during auto-generation

### Integration test view

A breadcrumb with a home icon on the first item (both manual and auto-generated variants). Verify the icon renders before the label text.
