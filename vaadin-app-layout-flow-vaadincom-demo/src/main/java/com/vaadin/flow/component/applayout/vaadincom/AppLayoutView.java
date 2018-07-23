package com.vaadin.flow.component.applayout.vaadincom;

import com.vaadin.flow.component.applayout.ActionMenuItem;
import com.vaadin.flow.component.applayout.RoutingMenuItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout")
public class AppLayoutView extends DemoView {

    @Override
    protected void initView() {
        emptyAppLayout();
        appLayoutWithBrandingText();
        appLayoutWithBrandingLogo();
        appLayoutWithMenus();
        appLayoutAsARouterLayout();
    }

    private void emptyAppLayout() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic App Layout Example
        AppLayout appLayout = new AppLayout();
        // end-source-example
        // @formatter:on

        addCard("Basic App Layout Example", appLayout);
    }

    private void appLayoutWithBrandingText() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic App Layout with text branding
        AppLayout appLayout = new AppLayout();
        appLayout.setBranding(new Span("App Company").getElement());
        // end-source-example
        // @formatter:on

        addCard("Using text branding", appLayout);
    }

    private void appLayoutWithBrandingLogo() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic App Layout with text branding
        AppLayout appLayout = new AppLayout();
        appLayout.setBranding(
                new Image("https://imgur.com/a/hkhePn5", "Vaadin Logo")
                        .getElement());
        // end-source-example
        // @formatter:on

        addCard("Using logo branding", appLayout);
    }

    private void appLayoutWithMenus() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: App Layout with Menus
        AppLayout appLayout = new AppLayout();

        appLayout.addMenuItem(new RoutingMenuItem(
                VaadinIcon.USER.create(), "My Profile", "profile"));

        appLayout.addMenuItem(new RoutingMenuItem(
                VaadinIcon.TRENDING_UP.create(), "Trending Topics", "trends"));

        appLayout.addMenuItem(new ActionMenuItem(
                VaadinIcon.SIGN_OUT.create(), "Sign Out", e -> logout()));
        // end-source-example
        // @formatter:on

        addCard("Adding menus", appLayout);
    }

    private void logout() {
    }

    private void appLayoutAsARouterLayout() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: App Layout as a router layout
        AppLayout appLayout = new AppLayout();
        // end-source-example
        // @formatter:on

        addCard("AppLayout as a router layout", appLayout);
    }
}
