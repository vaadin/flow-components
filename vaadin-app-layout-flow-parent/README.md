# App Layout Component for Vaadin Flow

### Overview
Vaadin App Layout is a component providing a quick and easy way to get a common application layout structure done.

### License & Author

This add-on is distributed under [Commercial Vaadin Add-on License version 3](http://vaadin.com/license/cval-3) (CVALv3).

To purchase a license, visit http://vaadin.com/pricing

### Installing
Add App Layout to your project:
```
<dependencies>
  <dependency>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-app-layout-flow</artifactId>
    <version>1.0.0.alpha1</version>
  </dependency>
</dependencies>
```

### Basic Use

```java
public class AppLayoutView extends AbstractAppRouterLayout {
    @Override
    protected void configure(AppLayout appLayout) {
        appLayout.setBranding(new Span("App Name").getElement());
        appLayout.addMenuItem(new AppLayoutMenuItem("About Company", "about"));
    }
}
/* Annotate AboutPage with @ParentLayout(AppLayoutView.class) */
```
