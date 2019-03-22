package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-app-layout")
public class AppLayoutView extends DemoView {

    @Override
    protected void initView() {
        addCard("App Layout usage in a demo application",
                new Div(new Label("Try out the demo which is using the `vaadin-app-layout-flow` component. "),
                        new Anchor("https://bakery-flow.demo.vaadin.com/login", "Open demo.")));

        basicAppLayout();
    }

    @SuppressWarnings("unused")
    private void basicAppLayout() {
        // @formatter:off
        // begin-source-example
        // source-example-heading: Basic App Layout
        AppLayout appLayout = new AppLayout();

        // end-source-example
        // @formatter:on

        addCard("Basic App Layout");
    }


}
