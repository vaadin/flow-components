# Use Case 3: Automatic Router Integration (Strict Hierarchy)

Zero-configuration breadcrumb generation from the route hierarchy.

## What needs to work

```java
Breadcrumb breadcrumb = new Breadcrumb();
breadcrumb.setAutoGeneration(true);
```

Navigating to `/electronics/laptops/thinkpad-x1` automatically produces: `Home > Electronics > Laptops > ThinkPad X1 Carbon`

## Prerequisites

Use cases 1 and 2 must be implemented first.

## Implementation needed

### `Breadcrumb` — auto-generation with `AfterNavigationObserver`

The `Breadcrumb` class implements `AfterNavigationObserver`. The `afterNavigation` method only acts when `autoGeneration` is `true`.

```java
public class Breadcrumb extends Component
        implements HasSize, HasAriaLabel, AfterNavigationObserver {

    private boolean autoGeneration;

    public void setAutoGeneration(boolean autoGeneration) {
        this.autoGeneration = autoGeneration;
    }

    public boolean isAutoGeneration() {
        return autoGeneration;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        if (!autoGeneration) {
            return;
        }
        List<BreadcrumbItem> items = generateItems(event);
        removeAll();
        addItem(items.toArray(new BreadcrumbItem[0]));
    }

    private List<BreadcrumbItem> generateItems(AfterNavigationEvent event) {
        // 1. Get the current URL path
        String path = event.getLocation().getPath();

        // 2. Get the router and route registry
        Router router = event.getSource();
        RouteRegistry registry = router.getRegistry();

        // 3. Build candidate path prefixes from the URL segments
        //    e.g. "/electronics/laptops/thinkpad-x1" → ["", "electronics",
        //         "electronics/laptops", "electronics/laptops/thinkpad-x1"]
        List<String> segments = event.getLocation().getSegments();
        List<String> prefixes = new ArrayList<>();
        prefixes.add(""); // root
        StringBuilder prefix = new StringBuilder();
        for (String segment : segments) {
            if (!segment.isEmpty()) {
                if (prefix.length() > 0) prefix.append("/");
                prefix.append(segment);
                prefixes.add(prefix.toString());
            }
        }

        // 4. For each prefix, check if a route is registered
        //    If yes, create a BreadcrumbItem with label from @PageTitle
        List<BreadcrumbItem> items = new ArrayList<>();
        for (String candidatePath : prefixes) {
            Optional<Class<? extends Component>> target =
                    registry.getNavigationTarget(candidatePath);
            if (target.isPresent()) {
                String label = resolveLabel(target.get(), candidatePath);
                BreadcrumbItem item = new BreadcrumbItem(label, candidatePath);
                items.add(item);
            }
        }

        // 5. Last item has no path (current page)
        if (!items.isEmpty()) {
            BreadcrumbItem last = items.get(items.size() - 1);
            last.setPath((String) null);
        }

        return items;
    }

    private String resolveLabel(Class<? extends Component> viewClass,
            String pathSegment) {
        // Check @PageTitle annotation
        PageTitle pageTitle = viewClass.getAnnotation(PageTitle.class);
        if (pageTitle != null && !pageTitle.value().isEmpty()) {
            return pageTitle.value();
        }
        // Fallback: capitalize the last path segment
        String lastSegment = pathSegment.contains("/")
                ? pathSegment.substring(pathSegment.lastIndexOf('/') + 1)
                : pathSegment;
        if (lastSegment.isEmpty()) {
            return "Home"; // root route fallback
        }
        // "electronics" → "Electronics", "black-friday" → "Black Friday"
        return Arrays.stream(lastSegment.split("-"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}
```

Key details:
- `AfterNavigationObserver` is always implemented (not conditional), but `afterNavigation` only acts when `autoGeneration` is `true`. This avoids dynamic interface changes.
- Route lookup: for each path prefix, check if a route is registered using the `RouteRegistry`. Routes with parameters (`:productId`) need special handling — the registry may store them as patterns, so lookup must account for parameterized segments.
- The root route (`""`) is included if registered.
- `@PageTitle` is read via reflection on the view class. This is a standard annotation already used by the Vaadin router.
- The fallback label capitalization converts `kebab-case` segments to `Title Case`.

### Handling parameterized routes

The path prefix approach needs refinement for parameterized routes like `electronics/laptops/:productId`. When the URL is `/electronics/laptops/thinkpad-x1`:
- Prefix `electronics/laptops/thinkpad-x1` won't match the route pattern `electronics/laptops/:productId` via simple string lookup
- Need to use the route registry's pattern matching, e.g., `registry.getNavigationTarget(path)` which handles parameter substitution
- Alternatively, use `event.getActiveChain()` (if available) to get the list of active layout/view components, which already resolves the parameterized routes

### Unit tests needed

- `setAutoGeneration(true)` → `isAutoGeneration()` returns true
- After navigation event with matching routes, breadcrumb items are created
- `@PageTitle` is used for labels
- Fallback to capitalized path segment when no `@PageTitle`
- Last item has no path
- Root route (`""`) is included
- When `autoGeneration` is false, `afterNavigation` does nothing
- Parameterized routes are resolved correctly

### Integration test view

Multiple routes in a hierarchy with `@PageTitle`. A `Breadcrumb` in the layout with `setAutoGeneration(true)`. Navigating between views updates the breadcrumb automatically. The test verifies correct items, labels, and clickability.
