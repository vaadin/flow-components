package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout")
public class AppLayoutView extends DemoView {

    @Override
    protected void initView() {
        addCard("App Layout usage in a demo application",
                new Div(new Label("Try out the demo which is using the `vaadin-app-layout-flow` component. "),
                        new Anchor("https://bakery-flow.demo.vaadin.com/login", "Open demo.")));
        appLayoutWithBrandingLogo();
        appLayoutWithBrandingText();
        appLayoutWithMenus();
    }

    private void appLayoutWithBrandingText() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic App Layout with text branding
        AppLayout appLayout = new AppLayout();
        appLayout.setBranding(new H3("App Company"));

        // end-source-example
        // @formatter:on

        addCard("Basic App Layout with text branding");
    }

    private void appLayoutWithBrandingLogo() {
        try {
            // @formatter:off
            // begin-source-example
            // source-example-heading: Simple App Layout with brand logo
            AppLayout appLayout = new AppLayout();
            AppLayoutMenu menu = appLayout.createMenu();
            Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
            img.setHeight("44px");
            appLayout.setBranding(img);

            menu.addMenuItems(new AppLayoutMenuItem("Page 1", "page1"),
                    new AppLayoutMenuItem("Page 2", "page2"),
                    new AppLayoutMenuItem("Page 3", "page3"),
                    new AppLayoutMenuItem("Page 4", "page4"));

            Component content = new Span(new H3("Page title"),
                    new Span("Page content"));
            appLayout.setContent(content);
            // end-source-example
            // @formatter:on
        } catch (IllegalArgumentException e) {
            // Workaround is needed if the vaadin tabs version < 1.0.5 is used
        }

        addCard("Simple App Layout with brand logo");
    }

    private void appLayoutWithMenus() {
        try {
            // @formatter:off
            // begin-source-example
            // source-example-heading: App Layout with Action Menu Item
            AppLayout appLayout = new AppLayout();
            AppLayoutMenu menu = appLayout.createMenu();

            menu.addMenuItems(
                    new AppLayoutMenuItem(VaadinIcon.USER.create(), "My Profile", "profile"),
                    new AppLayoutMenuItem(VaadinIcon.TRENDING_UP.create(), "Trending Topics", "trends"),
                    new AppLayoutMenuItem(VaadinIcon.SIGN_OUT.create(), "Sign Out", e -> logout()));
            // end-source-example
            // @formatter:on
        } catch (IllegalArgumentException e) {
            // Workaround is needed if the vaadin tabs version < 1.0.5 is used
        }

        addCard("App Layout with Action Menu Item");
    }

    private void logout() {
    }

}
