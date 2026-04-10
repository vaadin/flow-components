# Use Case 8 — Programmatic Customization per Route

The application directly decides what appears in the breadcrumb. The
trail can be anything — a data-driven path, a static list, or a mix —
and can be rebuilt at any time in response to application state
changes.

## Example: Fully application-controlled trail in a layout

Here a shared `MainLayout` owns the breadcrumb, and each view that
wants a breadcrumb pushes its own trail into the layout via a
well-known method. This centralizes the breadcrumb instance and lets
the views decide the content freely.

```java
public class MainLayout extends AppLayout {

    private final Breadcrumb breadcrumb = new Breadcrumb();

    public MainLayout() {
        addToNavbar(breadcrumb);
    }

    public void setTrail(BreadcrumbItem... items) {
        breadcrumb.setItems(items);
    }

    public void clearTrail() {
        breadcrumb.removeAll();
    }
}

@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout
        implements AfterNavigationObserver {

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // Any shape the view wants — here, a flat single-item trail.
        ((MainLayout) getParent().orElseThrow()).setTrail(
                new BreadcrumbItem("Dashboard"));
    }
}

@Route(value = "reports/:reportId", layout = MainLayout.class)
public class ReportView extends VerticalLayout
        implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String reportId = event.getRouteParameters().get("reportId").orElseThrow();
        Report report = ReportService.find(reportId);

        MainLayout layout = (MainLayout) getParent().orElseThrow();
        layout.setTrail(
                new BreadcrumbItem("Home", DashboardView.class),
                new BreadcrumbItem("Reports", ReportsView.class),
                new BreadcrumbItem(report.category().name(),
                        ReportsView.class,
                        new RouteParameters("category", report.category().id())),
                new BreadcrumbItem(report.title()));
    }
}
```

## Notes

- The breadcrumb lives in the layout, so there is exactly one
  instance per session. Views push their trail when they enter.
- Because `setItems` fully replaces the trail on each call, there is
  no bookkeeping needed between navigations.
- The trail content is entirely up to the view: it can skip
  intermediate routes (UC5/UC7), end early (UC6), include items that
  are not routes at all, or pull labels from any data source.
