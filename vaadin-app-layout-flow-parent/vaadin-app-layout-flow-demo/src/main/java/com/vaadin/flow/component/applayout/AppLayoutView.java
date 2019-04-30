package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout")
public class AppLayoutView extends DemoView {

    @Override
    protected void initView() {
        addCard("App Layout usage in a demo application",
                new Div(new Label("Try out the demo which is using the `vaadin-app-layout-flow` component. "),
                        new Anchor("https://bakery-flow.demo.vaadin.com/login", "Open demo.")));
        appLayoutBasic();
        appLayoutDrawerPrimary();
        appLayoutInMobile();
    }

    private void appLayoutBasic() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic App Layout
        AppLayout appLayout = new AppLayout();
        Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
        img.setHeight("44px");
        appLayout.addToNavbar(new DrawerToggle(), img);
        Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        appLayout.addToDrawer(tabs);
        // end-source-example
        // @formatter:on

        addCard("Basic App Layout");
    }

    private void appLayoutDrawerPrimary() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: App layout with drawer as primary section
        AppLayout appLayout = new AppLayout();
        appLayout.setPrimarySection(AppLayout.Section.DRAWER);
        Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
        img.setHeight("44px");
        appLayout.addToNavbar(new DrawerToggle(), img);
        Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        appLayout.addToDrawer(tabs);
        // end-source-example
        // @formatter:on

        addCard("App layout with drawer as primary section");
    }

    private void appLayoutInMobile() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: App layout in mobile
        AppLayout appLayout = new AppLayout();
        Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
        img.setHeight("44px");
        final boolean touchOptimized = true;
        appLayout.addToNavbar(touchOptimized, new DrawerToggle(), img);
        Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        appLayout.addToDrawer(tabs);
        // end-source-example
        // @formatter:on

        addCard("App layout in mobile");
    }

}
