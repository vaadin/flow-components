# Routing

Components that render links or navigate to URLs should integrate with the Flow
Router. While web components are router-agnostic (plain anchor tags), Flow
wrapper components make routing a first-class concept.

## Typed routes first

Navigational components take `Class<? extends Component>` for `@Route` views
as the primary API:

```java
new SideNavItem("Home", HomeView.class);

sideNavItem.setPath(HomeView.class);
```

Resolve URLs from classes via `RouteConfiguration` so path changes update
automatically and typos fail at compile time. A string-path overload may coexist
for external links, but the typed form is primary.

Use the router's own concepts rather than re-modelling them:
`RouteParameters` / `QueryParameters` / `HasUrlParameter` for parameterised
routes; shell layouts implement `RouterLayout`.

## Keep real link semantics

Still set a real `href` so middle-click, "open in new tab", and screen-reader
link semantics work — don't replace the anchor with a `UI.navigate(...)`
click handler. Flow Router intercepts clicks on known routes automatically.
