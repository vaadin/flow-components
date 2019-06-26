package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

@Route("vaadin-app-layout")
public class AppLayoutView extends DemoView {

    @Override
    protected void initView() {
        addCard("App Layout usage in a demo application",
                new Div(new Label("Try out the demo which is using the `vaadin-app-layout-flow` component. "),
                        new Anchor("https://bakery-flow.demo.vaadin.com/login", "Open demo.")));
        addCard("Basic App Layout");
        addCard("App layout with drawer as primary section");
        addCard("App layout in mobile");
        addCard("App layout as main view for PWA");
    }

    // @formatter:off
    // begin-source-example
    // source-example-heading: Basic App Layout
    public class BasicAppLayoutView extends AppLayout {
        public BasicAppLayoutView() {
            Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
            img.setHeight("44px");
            addToNavbar(new DrawerToggle(), img);
            Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
            tabs.setOrientation(Tabs.Orientation.VERTICAL);
            addToDrawer(tabs);
        }
    }
    // end-source-example
    // @formatter:on

    // @formatter:off
    // begin-source-example
    // source-example-heading: App layout with drawer as primary section
    public class AppLayoutWithDrawerView extends AppLayout {
        public AppLayoutWithDrawerView() {
            setPrimarySection(AppLayout.Section.DRAWER);
            Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
            img.setHeight("44px");
            addToNavbar(new DrawerToggle(), img);
            Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
            tabs.setOrientation(Tabs.Orientation.VERTICAL);
            addToDrawer(tabs);
        }
    }
    // end-source-example
    // @formatter:on

    // @formatter:off
    // begin-source-example
    // source-example-heading: App layout in mobile
    public class AppLayoutInMobile extends AppLayout {
        public AppLayoutInMobile() {
            Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
            img.setHeight("44px");
            final boolean touchOptimized = true;
            addToNavbar(touchOptimized, new DrawerToggle(), img);
            Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
            tabs.setOrientation(Tabs.Orientation.VERTICAL);
            addToDrawer(tabs);
        }
    }
     // end-source-example
    // @formatter:on

    // @formatter:off
    // begin-source-example
    // source-example-heading: App layout as main view for PWA
    @Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes, viewport-fit=cover")
    @PWA(name = "My Application", shortName = "My App")
    class MainAppView extends AppLayout {

        public MainAppView() {
            Image img = new Image("https://i.imgur.com/GPpnszs.png", "Vaadin Logo");
            img.setHeight("44px");
            addToNavbar(new DrawerToggle(), img);
            Tabs tabs = new Tabs(new Tab("Home"), new Tab("About"));
            tabs.setOrientation(Tabs.Orientation.VERTICAL);
            addToDrawer(tabs);
        }
    }
    // end-source-example
    // @formatter:on
}
