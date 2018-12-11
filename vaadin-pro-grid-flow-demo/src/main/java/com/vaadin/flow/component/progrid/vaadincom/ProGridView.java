package com.vaadin.flow.component.progrid.vaadincom;

import com.vaadin.flow.component.progrid.ProGrid;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-pro-grid")
public class ProGridView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        ProGrid component = new ProGrid();
        // end-source-example

        addCard("Basic Demo", component);
    }
}
