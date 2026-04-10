# Use Case 6 — Omitting Views from the Trail End

The breadcrumb belongs to a parent view that displays a selected
child in a side panel. The child view exists but is conceptually
*inside* the parent view, so the breadcrumb stops at the parent.

## Example: Employees list with a side-panel detail

```java
@Route("org/:orgId/departments/:deptId/employees/:empId?")
public class EmployeesView extends HorizontalLayout
        implements BeforeEnterObserver {

    private final Breadcrumb breadcrumb = new Breadcrumb();
    private final Grid<Employee> grid = new Grid<>();
    private final EmployeeDetailPanel detailPanel = new EmployeeDetailPanel();

    public EmployeesView() {
        VerticalLayout main = new VerticalLayout(breadcrumb, grid);
        add(main, detailPanel);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        RouteParameters params = event.getRouteParameters();
        Organization organization = OrganizationService.find(
                params.get("orgId").orElseThrow());
        Department department = DepartmentService.find(
                params.get("deptId").orElseThrow());

        // The trail stops at "Employees" — the selected employee, even
        // if present in the URL, is never added to the breadcrumb.
        breadcrumb.setItems(
                new BreadcrumbItem(organization.name(),
                        OrganizationView.class,
                        new RouteParameters("orgId", organization.id())),
                new BreadcrumbItem(department.name(),
                        DepartmentView.class,
                        new RouteParameters(
                                "orgId", organization.id(),
                                "deptId", department.id())),
                new BreadcrumbItem("Employees"));

        // Separately update the side panel based on the optional empId.
        params.get("empId").ifPresentOrElse(
                id -> detailPanel.show(EmployeeService.find(id)),
                detailPanel::clear);
    }
}
```

## Notes

- Even when `empId` is present in the URL (the side panel is showing
  an employee), the breadcrumb still ends at `Employees`. The
  application simply never adds a `BreadcrumbItem` for the selected
  employee.
- The breadcrumb and the side panel are updated from the same
  `beforeEnter` call but from different sources of truth.
