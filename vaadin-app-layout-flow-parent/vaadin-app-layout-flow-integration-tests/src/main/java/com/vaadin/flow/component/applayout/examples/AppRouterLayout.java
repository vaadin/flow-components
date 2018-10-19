package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AbstractAppRouterLayout;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.function.IntFunction;
import java.util.stream.IntStream;

@BodySize
@Theme(Lumo.class)
public class AppRouterLayout extends AbstractAppRouterLayout {

    private static final int NOTIFICATION_DURATION = 10000;

    @Override
    protected void configure(AppLayout appLayout, AppLayoutMenu appLayoutMenu) {
        appLayout.setBranding(new Span("Vaadin").getElement());

        appLayoutMenu.addMenuItems(generateMenuItems(
            i -> new AppLayoutMenuItem(VaadinIcon.SAFE_LOCK.create(),
                "Action " + i, e -> Notification
                .show(e.getSource().getTitle() + " executed!",
                        NOTIFICATION_DURATION, Notification.Position.BOTTOM_START))));

        appLayoutMenu.addMenuItems(generateMenuItems(
            i -> (new AppLayoutMenuItem(VaadinIcon.LOCATION_ARROW.create(),
                "Page " + i, "Page" + i))));

        appLayoutMenu.addMenuItems(
            new AppLayoutMenuItem(VaadinIcon.HOME.create(), "Home", ""),
            new AppLayoutMenuItem(VaadinIcon.USER.create(), "Logout",
                e -> UI.getCurrent().navigate("LoggedOut")));
    }

    private static AppLayoutMenuItem[] generateMenuItems(
        IntFunction<AppLayoutMenuItem> f) {
        return IntStream.range(1, 3).mapToObj(f)
            .toArray(AppLayoutMenuItem[]::new);
    }
}
