package com.vaadin.flow.component.gridpro.vaadincom;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-pro")
public class GridProView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        GridPro component = new GridPro();
        // end-source-example

        addCard("Basic Demo", component);
    }
}
