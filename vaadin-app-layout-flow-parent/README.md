# App Layout Component for Vaadin Flow

### Overview
Vaadin App Layout is a component providing a quick and easy way to get a common application layout structure done.

### Installing
Add App Layout to your project:
```
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-app-layout-flow</artifactId>
    <version>2.0.0.alpha2</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes, viewport-fit=cover")
@BodySize
@Theme(Lumo.class)
public class AppRouterLayout extends AppLayout {
    
     {
         final DrawerToggle drawerToggle = new DrawerToggle();
         final RouterLink home = new RouterLink("Home", HomeView.class);
         final RouterLink about = new RouterLink("About Company", AboutView.class);
         final VerticalLayout layout = new VerticalLayout(home, about);
         addToDrawer(layout);
         addToNavbar(drawerToggle);
     }

}

@Route(value = "", layout = AppRouterLayout.class)
public class HomeView extends Div {
}

@Route(value = "about", layout = AppRouterLayout.class)
public class AboutView extends Div {
}

```

### License

This program is available under Vaadin Commercial License and Service Terms.
