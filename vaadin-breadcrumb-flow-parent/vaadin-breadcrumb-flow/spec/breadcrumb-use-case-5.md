# Use Case 5 — Omitting Views from the Trail Start

The breadcrumb is scoped to a part of the application. Views *above*
that scope should not appear in the trail, even though they exist in
the route hierarchy.

In this example the application has a `Organization > Department >
Employee` structure, but the breadcrumb is only meaningful within the
Department section. The `Organization` level is therefore omitted.

## Example: Department-scoped breadcrumb

```java
@Route("org/:orgId/departments/:deptId")
public class DepartmentView extends VerticalLayout
        implements BeforeEnterObserver {

    private final Breadcrumb breadcrumb = new Breadcrumb();
    private final H1 title = new H1();

    public DepartmentView() {
        add(breadcrumb, title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Department department = DepartmentService.find(
                event.getRouteParameters().get("deptId").orElseThrow());
        title.setText(department.name());

        // Only the Department level — Organization is intentionally omitted.
        breadcrumb.setItems(new BreadcrumbItem(department.name()));
    }
}

@Route("org/:orgId/departments/:deptId/employees/:empId")
public class EmployeeView extends VerticalLayout
        implements BeforeEnterObserver {

    private final Breadcrumb breadcrumb = new Breadcrumb();
    private final H1 title = new H1();

    public EmployeeView() {
        add(breadcrumb, title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        Department department = DepartmentService.find(
                params.get("deptId").orElseThrow());
        Employee employee = EmployeeService.find(
                params.get("empId").orElseThrow());
        title.setText(employee.name());

        breadcrumb.setItems(
                new BreadcrumbItem(department.name(),
                        DepartmentView.class,
                        new RouteParameters(
                                "orgId", params.get("orgId").orElseThrow(),
                                "deptId", department.id())),
                new BreadcrumbItem(employee.name()));
    }
}
```

## Notes

- The `Organization` view exists in the URL hierarchy
  (`/org/:orgId/...`) but never appears in the breadcrumb. The
  application simply does not add an item for it.
- The Flow component does not know or care that it is "missing" a
  level. It renders what it is given.
