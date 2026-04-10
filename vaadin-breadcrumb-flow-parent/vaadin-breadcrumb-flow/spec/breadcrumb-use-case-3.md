# Use Case 3 — No Canonical, Current-Only on Direct Entry

An employee page can be reached from two unrelated places — a
department view or a project view — and there is no single canonical
path. When opened directly by URL, the breadcrumb must show only the
current page, because the application has no navigation context to
draw from.

The application needs to know where the user came from. The simplest
way to carry that context is a query parameter set by the two entry
views when they link to the employee page.

## Example: Employee page with two entry routes

```java
// Link from the department view:
Anchor link = new Anchor(
        RouteConfiguration.forSessionScope()
                .getUrl(EmployeeView.class, new RouteParameters("id", employee.id()))
                + "?from=department&contextId=" + department.id(),
        employee.name());
```

```java
@Route("employees/:id")
public class EmployeeView extends VerticalLayout implements BeforeEnterObserver {

    private final Breadcrumb breadcrumb = new Breadcrumb();
    private final H1 title = new H1();

    public EmployeeView() {
        add(breadcrumb, title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String employeeId = event.getRouteParameters().get("id").orElseThrow();
        Employee employee = EmployeeService.find(employeeId);
        title.setText(employee.name());

        String from = event.getLocation().getQueryParameters()
                .getSingleParameter("from").orElse(null);
        String contextId = event.getLocation().getQueryParameters()
                .getSingleParameter("contextId").orElse(null);

        if ("department".equals(from) && contextId != null) {
            Department department = DepartmentService.find(contextId);
            breadcrumb.setItems(
                    new BreadcrumbItem(department.name(),
                            DepartmentView.class,
                            new RouteParameters("id", department.id())),
                    new BreadcrumbItem(employee.name()));
        } else if ("project".equals(from) && contextId != null) {
            Project project = ProjectService.find(contextId);
            breadcrumb.setItems(
                    new BreadcrumbItem(project.name(),
                            ProjectView.class,
                            new RouteParameters("id", project.id())),
                    new BreadcrumbItem(employee.name()));
        } else {
            // Direct URL entry — no context available.
            breadcrumb.setItems(new BreadcrumbItem(employee.name()));
        }
    }
}
```

## Notes

- The navigation context is carried explicitly in the URL (query
  parameters). It could also come from session state, depending on
  what the application considers acceptable for bookmarkability.
- On direct URL entry, the `from` parameter is absent, so the `else`
  branch runs and the breadcrumb shows only the current page.
- The breadcrumb never tries to reconstruct a "probable" trail; the
  application decides what to show.
