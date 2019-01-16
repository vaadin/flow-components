package com.vaadin.flow.component.details.vaadincom;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-details")
public class DetailsView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        Details component = new Details();
        // end-source-example

        addCard("Basic Demo", component);
    }
}
