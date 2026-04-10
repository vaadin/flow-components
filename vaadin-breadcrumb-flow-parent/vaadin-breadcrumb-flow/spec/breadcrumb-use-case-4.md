# Use Case 4 — Deep Navigation with Static Prefix

In an HR tool, the department is fixed structure, but the chain of
"reports to" employees below it is dynamic navigation. When a deep
page is opened directly by URL, only the fixed prefix and the current
page are shown — the intermediate managers are dropped because they
were navigation, not hierarchy.

The application carries the navigation chain in a query parameter that
is extended each time the user drills further down. On direct URL
entry the parameter is absent and only the prefix + current page is
shown.

## Example: Employee view with a fixed prefix and dynamic chain

```java
// When the user clicks "Reports" on Alice's page, the link to Bob carries
// the current chain in a query parameter:
String chain = "alice";
Anchor toBob = new Anchor(
        RouteConfiguration.forSessionScope()
                .getUrl(EmployeeView.class, new RouteParameters("id", "bob"))
                + "?chain=" + URLEncoder.encode(chain, StandardCharsets.UTF_8),
        "Bob");

// When Bob's page renders a link to Carol, it extends the chain:
String extended = chain + "," + "bob";
```

```java
@Route("departments/engineering/people/:id")
public class EmployeeView extends VerticalLayout implements BeforeEnterObserver {

    private static final BreadcrumbItem ENGINEERING = new BreadcrumbItem(
            "Engineering", EngineeringDepartmentView.class);

    private final Breadcrumb breadcrumb = new Breadcrumb();
    private final H1 title = new H1();

    public EmployeeView() {
        add(breadcrumb, title);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String employeeId = event.getRouteParameters().get("id").orElseThrow();
        Employee current = EmployeeService.find(employeeId);
        title.setText(current.name());

        List<BreadcrumbItem> items = new ArrayList<>();
        items.add(ENGINEERING);

        String chain = event.getLocation().getQueryParameters()
                .getSingleParameter("chain").orElse("");
        if (!chain.isBlank()) {
            String runningChain = "";
            for (String id : chain.split(",")) {
                Employee ancestor = EmployeeService.find(id);
                items.add(buildEmployeeItem(ancestor, runningChain));
                runningChain = runningChain.isEmpty()
                        ? ancestor.id()
                        : runningChain + "," + ancestor.id();
            }
        }

        items.add(new BreadcrumbItem(current.name()));
        breadcrumb.setItems(items);
    }

    private static BreadcrumbItem buildEmployeeItem(Employee employee,
            String chain) {
        String basePath = RouteConfiguration.forSessionScope().getUrl(
                EmployeeView.class,
                new RouteParameters("id", employee.id()));
        String path = chain.isBlank()
                ? basePath
                : basePath + "?chain="
                        + URLEncoder.encode(chain, StandardCharsets.UTF_8);
        return new BreadcrumbItem(employee.name(), path);
    }
}
```

## Notes

- `ENGINEERING` is a static fixed prefix, always present.
- On direct URL entry to Carol's page (`?chain` absent), the breadcrumb
  is just `Engineering > Carol` — exactly what the use case demands.
- The chain parameter carries the navigation history so that each
  ancestor's link also preserves the trail (clicking "Alice" from
  Carol's page shows `Engineering > Alice`, not `Engineering > Alice >
  Bob > Alice`).
- Storing navigation state in the URL rather than session makes the
  links bookmarkable.
