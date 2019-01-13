package com.vaadin.flow.component.accordion.vaadincom;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-accordion")
public class AccordionView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        Accordion component = new Accordion();
        // end-source-example

        addCard("Basic Demo", component);
    }
}
