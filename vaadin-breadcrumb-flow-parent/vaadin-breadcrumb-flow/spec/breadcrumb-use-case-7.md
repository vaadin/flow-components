# Use Case 7 — Omitting Intermediate Views from the Trail

A view exists only as a layout wrapper (it has no standalone content
and is never displayed on its own). That wrapper should not appear in
the breadcrumb even though it is part of the route hierarchy.

In this example `EmployeesLayout` is a Vaadin layout that only hosts
employee sub-views. It has a matching route but no meaningful page of
its own, so the breadcrumb skips it.

## Example: Layout wrapper skipped

```java
@ParentLayout(MainLayout.class)
@Route("org/:orgId/departments/:deptId/employees")
public class EmployeesLayout extends Div implements RouterLayout {
    // Layout only — no content, no breadcrumb. Never shown on its own.
}

@Route(value = ":empId", layout = EmployeesLayout.class)
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
        Organization organization = OrganizationService.find(
                params.get("orgId").orElseThrow());
        Department department = DepartmentService.find(
                params.get("deptId").orElseThrow());
        Employee employee = EmployeeService.find(
                params.get("empId").orElseThrow());
        title.setText(employee.name());

        // EmployeesLayout is a wrapper and never appears in the trail.
        breadcrumb.setItems(
                new BreadcrumbItem(organization.name(),
                        OrganizationView.class,
                        new RouteParameters("orgId", organization.id())),
                new BreadcrumbItem(department.name(),
                        DepartmentView.class,
                        new RouteParameters(
                                "orgId", organization.id(),
                                "deptId", department.id())),
                new BreadcrumbItem(employee.name()));
    }
}
```

## Notes

- `EmployeesLayout` is a parent layout, not a destination view.
  Omitting it from the breadcrumb is entirely a matter of the
  application not adding an item for it.
- There is no need to tell the Flow component which route is a
  wrapper. Wrapper detection is an application concern.
