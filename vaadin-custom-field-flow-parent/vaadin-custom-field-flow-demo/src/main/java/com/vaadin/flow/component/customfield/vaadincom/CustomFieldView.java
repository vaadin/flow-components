package com.vaadin.flow.component.customfield.vaadincom;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field")
public class CustomFieldView extends DemoView {

    @Override
    protected void initView() {
        basicDemo();
    }

    private void basicDemo() {
        // begin-source-example
        // source-example-heading: Basic Demo
        CustomField component = new CustomField();
        // end-source-example

        addCard("Basic Demo", component);
    }
}
