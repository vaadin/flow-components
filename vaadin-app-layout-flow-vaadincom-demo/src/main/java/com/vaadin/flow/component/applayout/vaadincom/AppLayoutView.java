package com.vaadin.flow.component.applayout.vaadincom;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.ActionMenuItem;
import com.vaadin.flow.component.applayout.RoutingMenuItem;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout")
public class AppLayoutView extends DemoView {

    @Override
    protected void initView() {
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
        // @formatter:off
        // begin-source-example
        // source-example-heading: Simple App Layout with brand logo
        AppLayout appLayout = new AppLayout();
        Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
        img.setHeight("44px");
        appLayout.setBranding(img);

        appLayout.addMenuItem(new RoutingMenuItem("Page 1", "page1"));
        appLayout.addMenuItem(new RoutingMenuItem("Page 2", "page2"));
        appLayout.addMenuItem(new RoutingMenuItem("Page 3", "page3"));
        appLayout.addMenuItem(new RoutingMenuItem("Page 4", "page4"));

        Component content = new Span(new H3("Page title"), new Span("Page content"));
        appLayout.setContent(content);
        // end-source-example
        // @formatter:on

        addCard("Simple App Layout with brand logo");
    }

    private void appLayoutWithMenus() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: App Layout with Action Menu Item
        AppLayout appLayout = new AppLayout();

        appLayout.addMenuItem(new RoutingMenuItem(
                VaadinIcon.USER.create(), "My Profile", "profile"));

        appLayout.addMenuItem(new RoutingMenuItem(
                VaadinIcon.TRENDING_UP.create(), "Trending Topics", "trends"));

        appLayout.addMenuItem(new ActionMenuItem(
                VaadinIcon.SIGN_OUT.create(), "Sign Out", e -> logout()));
        // end-source-example
        // @formatter:on

        addCard("App Layout with Action Menu Item");
    }

    private void logout() {
    }

}
