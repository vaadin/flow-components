package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AbstractAppRouterLayout;
import com.vaadin.flow.component.applayout.ActionMenuItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.RoutingMenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.stream.IntStream;

@BodySize
@Theme(Lumo.class)
public class AppRouterLayout extends AbstractAppRouterLayout {

    @Override
    protected void configure(AppLayout appLayout) {
        appLayout.setBranding(new Span("Vaadin").getElement());

        IntStream.range(1, 3).forEach(i ->
                appLayout.addMenuItem(new ActionMenuItem(
                        VaadinIcon.SAFE_LOCK.create(), "Action " + i,
                        e -> Notification.show(e.getSource().getTitle() + " executed!"))));

        IntStream.range(1, 3).forEach(i ->
                appLayout.addMenuItem(new RoutingMenuItem(
                        VaadinIcon.LOCATION_ARROW.create(), "Page " + i, "Page" + i)));

        appLayout.addMenuItem(new RoutingMenuItem(
                        VaadinIcon.HOME.create(), "Home", ""));

        appLayout.addMenuItem(new ActionMenuItem(
                VaadinIcon.USER.create(), "Logout",
                e -> UI.getCurrent().navigate("LoggedOut")));
    }
}
