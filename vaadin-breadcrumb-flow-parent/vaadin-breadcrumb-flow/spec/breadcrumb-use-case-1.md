# Use Case 1 — Strict Hierarchy

The route hierarchy *is* the breadcrumb: every ancestor of the
current view is a registered `@Route`, each with a `@PageTitle` for
its display name. Because this maps one-to-one to URL prefixes, the
default auto-populate-from-route behavior handles the entire use case
with no per-view code.

## Example: Laptop catalog hierarchy

Drop a `Breadcrumb` into the shared layout:

```java
public class MainLayout extends AppLayout {
    public MainLayout() {
        addToNavbar(new Breadcrumb());
    }
}
```

Annotate each ancestor route with `@PageTitle`:

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
```

For the leaf view whose label depends on a route parameter, implement
`HasDynamicTitle`:

```java
@Route(value = "electronics/laptops/:productId", layout = MainLayout.class)
public class LaptopDetailView extends VerticalLayout
        implements BeforeEnterObserver, HasDynamicTitle {

    private Product product;

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String productId = event.getRouteParameters().get("productId").orElseThrow();
        product = ProductService.find(productId);
    }

    @Override
    public String getPageTitle() {
        return product.name();
    }
}
```

That's all. Navigating to `/electronics/laptops/thinkpad-x1` produces
`Home > Electronics > Laptops > ThinkPad X1 Carbon` automatically.

## How it works

On each navigation the breadcrumb:

1. Takes the current location (e.g. `electronics/laptops/thinkpad-x1`).
2. Walks each URL prefix (`""`, `electronics`, `electronics/laptops`,
   `electronics/laptops/thinkpad-x1`).
3. Resolves each prefix to a route class via `RouteConfiguration`.
4. Reads `@PageTitle` from each ancestor route class.
5. Takes the current view's label from `HasDynamicTitle.getPageTitle()`
   (or its static `@PageTitle`).
6. Builds a `BreadcrumbItem` per resolved prefix, using the resolved
   URL as the path. The last item has no path and is rendered as the
   current page.

No application code beyond the route annotations is required.
