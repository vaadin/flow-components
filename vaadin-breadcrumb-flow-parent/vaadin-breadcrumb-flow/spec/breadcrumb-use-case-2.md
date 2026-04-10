# Use Case 2 — Multiple Paths, Canonical Path Always Shown

The same detail view can be reached from more than one route (canonical
catalog path and a promotional path), but the breadcrumb must always
show the canonical category trail. The breadcrumb therefore does not
depend on how the page was reached — it is computed from the product
itself.

## Example: Product detail view reachable via two routes

```java
@Route("electronics/laptops/:productId")
@RouteAlias("deals/black-friday/:productId")
public class ProductDetailView extends VerticalLayout
        implements BeforeEnterObserver {

    private final Breadcrumb breadcrumb = new Breadcrumb();
    private final H1 title = new H1();

    public ProductDetailView() {
        add(breadcrumb, title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String productId = event.getRouteParameters().get("productId").orElseThrow();
        Product product = ProductService.find(productId);

        title.setText(product.name());

        // Always the canonical trail — ignores the route used to enter.
        breadcrumb.setItems(
                new BreadcrumbItem("Home", HomeView.class),
                new BreadcrumbItem("Electronics", ElectronicsView.class),
                new BreadcrumbItem("Laptops", LaptopsView.class),
                new BreadcrumbItem(product.name()));
    }
}
```

## Notes

- `setItems` is called unconditionally from `beforeEnter`, so the trail
  is rebuilt on every navigation. This covers navigation from the
  catalog, from the promotional page, and direct URL entry — all three
  produce the same breadcrumb.
- If the canonical category for a product were data-driven (e.g. the
  product belongs to a computed category), the computation would happen
  in the `ProductService`, not in the breadcrumb. The Flow component
  only renders what it is given.
