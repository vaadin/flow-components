package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

public class AppRouterLayoutWithRouterLinks extends Div
    implements RouterLayout {

    private final AppLayout appLayout = new AppLayout();

    public AppRouterLayoutWithRouterLinks() {
        appLayout.setBranding(VaadinIcon.VAADIN_H.create());
        RouterLink link1 = new RouterLink("Page 3", Page3.class);
        RouterLink link2 = new RouterLink("Page 4", Page4.class);
        RouterLink link3 = new RouterLink("LoggedOut", LoggedOut.class);
        appLayout.setMenu(new HorizontalLayout(link1,link2,link3));
        add(appLayout);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        appLayout.setContent(content.getElement());
    }

}
