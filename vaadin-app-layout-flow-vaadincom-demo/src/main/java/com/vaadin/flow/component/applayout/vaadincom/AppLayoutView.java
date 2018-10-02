package com.vaadin.flow.component.applayout.vaadincom;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
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

        appLayout.addMenuItems(new AppLayoutMenuItem("Page 1", "page1"),
            new AppLayoutMenuItem("Page 2", "page2"),
            new AppLayoutMenuItem("Page 3", "page3"),
            new AppLayoutMenuItem("Page 4", "page4"));

        Component content = new Span(new H3("Page title"),
            new Span("Page content"));
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

        appLayout.addMenuItems(
            new AppLayoutMenuItem(VaadinIcon.USER.create(),"My Profile","profile"),
            new AppLayoutMenuItem(VaadinIcon.TRENDING_UP.create(),"Trending Topics","trends"),
            new AppLayoutMenuItem(VaadinIcon.SIGN_OUT.create(),"Sign Out", e -> logout()));
        // end-source-example
        // @formatter:on

        addCard("App Layout with Action Menu Item");
    }

    private void logout() {
    }

}
