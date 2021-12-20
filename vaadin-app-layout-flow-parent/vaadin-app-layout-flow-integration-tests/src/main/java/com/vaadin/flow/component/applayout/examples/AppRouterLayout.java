package com.vaadin.flow.component.applayout.examples;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

public class AppRouterLayout extends AppLayout {

    public static final String CUSTOM_TOGGLE_ID = "toggle-with-icon";
    public static final String CUSTOM_ICON_ID = "custom-icon";

    public AppRouterLayout() {
        RouterLink home = new RouterLink("Home", Home.class);
        RouterLink page1 = new RouterLink("Page 1", Page1.class);
        RouterLink page2 = new RouterLink("Page 2", Page2.class);
        VerticalLayout layout = new VerticalLayout(home, page1, page2);
        addToDrawer(layout);
        addToNavbar(new DrawerToggle());
        DrawerToggle customToggle = new DrawerToggle();
        customToggle.setId(CUSTOM_TOGGLE_ID);
        Icon icon = VaadinIcon.FLIP_H.create();
        icon.setId(CUSTOM_ICON_ID);
        customToggle.setIcon(icon);
        addToNavbar(customToggle);
        addI18nControls();
    }

    public void addI18nControls() {
        NativeButton setI18nButton = new NativeButton("set i18n", e -> {
            AppLayoutI18n i18n = new AppLayout.AppLayoutI18n()
                    .setDrawer("Custom drawer");
            setI18n(i18n);
        });
        setI18nButton.setId("set-i18n");
        addToNavbar(setI18nButton);
    }
}
