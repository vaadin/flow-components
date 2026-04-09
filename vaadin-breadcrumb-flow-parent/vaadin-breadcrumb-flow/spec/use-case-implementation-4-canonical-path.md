# Use Case 4: Canonical Path (View-Declared Breadcrumbs)

Views declare their own breadcrumb trail via `BreadcrumbProvider`, overriding URL-based generation.

## What needs to work

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

Regardless of entry path, breadcrumb shows: `Home > Electronics > Laptops > ThinkPad X1 Carbon`

## Prerequisites

Use case 3 (auto-generation) must be implemented first.

## Implementation needed

### `BreadcrumbProvider` interface

New interface in the `breadcrumb` package.

```java
package com.vaadin.flow.component.breadcrumb;

import java.util.List;

/**
 * Interface for views or router layouts to declare their breadcrumb trail.
 * <p>
 * When a {@link Breadcrumb} with auto-generation enabled navigates to a
 * view that implements this interface, the provider's items are used
 * instead of URL-based generation.
 * <p>
 * Returning an empty list means the view should be skipped in the
 * breadcrumb trail (useful for technical layout routes).
 */
public interface BreadcrumbProvider {
    List<BreadcrumbItem> getBreadcrumbs();
}
```

### `Breadcrumb.afterNavigation` — check for `BreadcrumbProvider`

Add a check at the start of `generateItems()`:

```java
private List<BreadcrumbItem> generateItems(AfterNavigationEvent event) {
    // 1. Check if the current view implements BreadcrumbProvider
    Component currentView = getCurrentView(event);
    if (currentView instanceof BreadcrumbProvider provider) {
        List<BreadcrumbItem> items = provider.getBreadcrumbs();
        if (items != null && !items.isEmpty()) {
            // Ensure last item has no path (current page)
            BreadcrumbItem last = items.get(items.size() - 1);
            if (last.getPath().isPresent()) {
                last.setPath((String) null);
            }
        }
        return items != null ? items : List.of();
    }

    // 2. Fall through to URL-based generation (existing logic)
    ...
}

private Component getCurrentView(AfterNavigationEvent event) {
    // Get the innermost (leaf) component from the active chain
    List<HasElement> chain = event.getActiveChain();
    if (!chain.isEmpty()) {
        HasElement leaf = chain.get(chain.size() - 1);
        return (Component) leaf;
    }
    return null;
}
```

Key details:
- `event.getActiveChain()` returns the list of active route components (layouts + view). The last element is the innermost view (the navigation target).
- `BreadcrumbProvider` takes full precedence — when a view implements it, URL-based generation is completely skipped for that navigation.
- The last item's path is cleared to mark it as the current page, even if the provider sets one, ensuring consistent `aria-current="page"` behavior.
- An empty list from the provider means "no breadcrumb trail" — `removeAll()` is called and no items are shown. This is intentional for technical layout routes (use case 7).

### Unit tests needed

- View implementing `BreadcrumbProvider` → its items are used
- Provider items override URL-based generation entirely
- Last item's path is cleared automatically
- Provider returning empty list → no items shown
- Provider returning null → treated as empty list
- View NOT implementing `BreadcrumbProvider` → falls through to URL-based generation

### Integration test view

Two routes pointing to the same view (via `@RouteAlias`). The view implements `BreadcrumbProvider` with a fixed trail. Navigating via either route shows the same breadcrumb. A button navigates between the two routes to verify consistency.
