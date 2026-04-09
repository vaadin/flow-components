# Use Case 2: Paths from Route View Classes

Type-safe path resolution from `@Route`-annotated view classes.

## What needs to work

```java
breadcrumb.addItem(new BreadcrumbItem("Home", HomeView.class));
breadcrumb.addItem(new BreadcrumbItem("Sprocket",
        ProductView.class, new RouteParameters("id", "42")));
```

## Prerequisites

Use case 1 (basic navigation) must be implemented first.

## Implementation needed

### `BreadcrumbItem` — additional constructors and setPath overloads

Follow the `SideNavItem` pattern for resolving view classes to paths.

```java
public BreadcrumbItem(String text, Class<? extends Component> view) {
    setText(text);
    setPath(view);
}

public BreadcrumbItem(String text, Class<? extends Component> view,
        RouteParameters params) {
    setText(text);
    setPath(view, params);
}

public void setPath(Class<? extends Component> view) {
    setPath(view, RouteParameters.empty());
}

public void setPath(Class<? extends Component> view,
        RouteParameters routeParameters) {
    // Resolve the view class to a URL path using the router registry.
    // Follow SideNavItem's pattern:
    //   RouteConfiguration.forRegistry(router.getRegistry())
    //       .getUrl(view, routeParameters)
    // The resolution must happen when the component is attached
    // (needs access to the Router via UI), not at construction time.
    this.navigationTarget = view;
    this.routeParameters = routeParameters;
    // If already attached, resolve immediately. Otherwise, resolve on attach.
    if (getElement().getNode().isAttached()) {
        resolveViewPath();
    }
}
```

Key details:
- Store the view class and route parameters as fields
- Resolve the actual path string lazily when the component is attached (needs access to the `Router` via `UI.getCurrent()`)
- Use `RouteConfiguration.forRegistry(router.getRegistry()).getUrl(view, routeParameters)` to resolve — same pattern as `SideNavItem`
- Add an attach listener that triggers resolution if view class is set but path isn't resolved yet
- When `setPath(String)` is called, clear the stored view class (explicit string path takes precedence)

### Reference: SideNavItem pattern

From `SideNavItem.java`, the path resolution works like:

```java
private void resolveViewPath() {
    Router router = ComponentUtil.getRouter(this);
    RouteConfiguration routeConfiguration = RouteConfiguration
            .forRegistry(router.getRegistry());
    String url = routeConfiguration.getUrl(navigationTarget, routeParameters);
    setPath(url); // Sets the string path
}
```

The component also needs to handle `@RouteAlias` annotations, matching the `SideNavItem` behavior.

### Unit tests needed

- `BreadcrumbItem(String, Class)` stores the view class
- `setPath(Class)` with attached component resolves to the correct URL
- `setPath(Class, RouteParameters)` resolves with parameters
- `setPath(String)` after `setPath(Class)` clears the view class
- `getPath()` returns the resolved string path after attach

### Integration test view

A page with breadcrumb items using view classes. Clicking an item navigates to the corresponding view. Route parameters are correctly embedded in the URL.
