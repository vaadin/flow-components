# Use Case 5: Scoped to a Section

Breadcrumb starts at a section root instead of the application root.

## What needs to work

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setAutoGeneration(true);
breadcrumb.setRootPath("departments/engineering");
```

Navigating to `/departments/engineering/frontend/alice` produces: `Engineering > Frontend > Alice`

## Prerequisites

Use case 3 (auto-generation) must be implemented first.

## Implementation needed

### `Breadcrumb` — rootPath and rootView fields

```java
private String rootPath;
private Class<? extends Component> rootView;

public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
    this.rootView = null; // mutually exclusive
}

public void setRootView(Class<? extends Component> rootView) {
    this.rootView = rootView;
    this.rootPath = null; // mutually exclusive
}
```

### `Breadcrumb.generateItems` — filter items above the root

After building the candidate items list (step 2 of auto-generation), filter based on the root:

```java
private List<BreadcrumbItem> generateItems(AfterNavigationEvent event) {
    // ... (BreadcrumbProvider check) ...
    // ... (URL-based item generation) ...

    // Filter based on root
    String effectiveRootPath = resolveRootPath(event);
    if (effectiveRootPath != null) {
        items = filterFromRoot(items, effectiveRootPath);
    }

    // ... (maxItems, customizer, last item path clearing) ...
    return items;
}

private String resolveRootPath(AfterNavigationEvent event) {
    if (rootPath != null) {
        return rootPath;
    }
    if (rootView != null) {
        // Resolve the view class to its route path
        Router router = event.getSource();
        RouteConfiguration config = RouteConfiguration
                .forRegistry(router.getRegistry());
        return config.getUrl(rootView);
    }
    return null;
}

private List<BreadcrumbItem> filterFromRoot(
        List<BreadcrumbItem> items, String rootPath) {
    // Find the item whose path matches the root path
    // Keep that item and all items after it
    int rootIndex = -1;
    for (int i = 0; i < items.size(); i++) {
        Optional<String> itemPath = items.get(i).getPath();
        if (itemPath.isPresent() && itemPath.get().equals(rootPath)) {
            rootIndex = i;
            break;
        }
    }
    if (rootIndex >= 0) {
        return items.subList(rootIndex, items.size());
    }
    // Root path not found in items — return all items unchanged
    return items;
}
```

Key details:
- `setRootPath` and `setRootView` are mutually exclusive — setting one clears the other.
- `setRootView` resolves the view class to a path string using `RouteConfiguration`, same as `BreadcrumbItem.setPath(Class)`.
- Filtering keeps the root item and everything below it. Items above the root (e.g., "Departments" when root is "departments/engineering") are excluded.
- If the root path doesn't match any generated item, all items are shown (fail-open behavior).

### Unit tests needed

- `setRootPath("a/b")` → items above `a/b` are excluded, root item and below remain
- `setRootView(SomeView.class)` → resolves to path, same filtering behavior
- Setting rootPath clears rootView and vice versa
- Root path not matching any item → all items shown
- Root path with auto-generation disabled → no effect (auto-generation not running)

### Integration test view

A layout with a breadcrumb scoped to a department. Multiple nested routes under the department. Navigating within the section shows breadcrumbs starting at the department root. The application-level prefix is not visible.
