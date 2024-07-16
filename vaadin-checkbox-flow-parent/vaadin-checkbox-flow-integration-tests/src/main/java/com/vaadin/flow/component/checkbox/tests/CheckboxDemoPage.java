/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Checkbox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-test-demo")
public class CheckboxDemoPage extends DemoView {

    @Override
    public void initView() {

        addDefaultCheckbox();
        addDisabledCheckbox();
        addIndeterminateCheckbox();
        addValueChangeCheckbox();
        addAccessibleCheckbox();
        addCheckboxWithHtmlLabel();
    }

    @Override
    public void populateSources() {

    }

    private void addDefaultCheckbox() {
        // begin-source-example
        // source-example-heading: Default Checkbox
        Checkbox checkbox = new Checkbox();
        checkbox.setLabel("Default Checkbox");
        // end-source-example
        addCard("Default Checkbox", checkbox);
        checkbox.setId("default-checkbox");
    }

    private void addDisabledCheckbox() {
        Div message = new Div();
        message.setId("disabled-checkbox-message");
        // begin-source-example
        // source-example-heading: Disabled Checkbox
        Checkbox disabledCheckbox = new Checkbox("Disabled Checkbox");
        disabledCheckbox.setValue(true);
        disabledCheckbox.setEnabled(false);
        // end-source-example
        disabledCheckbox.addClickListener(evt -> message.setText("Checkbox "
                + evt.getSource().getLabel()
                + " was clicked, but the component is disabled and this shouldn't happen!"));
        addCard("Disabled Checkbox", disabledCheckbox, message);
        disabledCheckbox.setId("disabled-checkbox");
    }

    private void addIndeterminateCheckbox() {
        // begin-source-example
        // source-example-heading: Indeterminate Checkbox
        Checkbox indeterminateCheckbox = new Checkbox("Indeterminate Checkbox");
        indeterminateCheckbox.setIndeterminate(true);
        // end-source-example

        NativeButton button = new NativeButton("Reset", event -> {
            indeterminateCheckbox.setValue(false);
            indeterminateCheckbox.setIndeterminate(true);
        });
        button.setId("reset-indeterminate");

        addCard("Indeterminate Checkbox", indeterminateCheckbox, button);
        indeterminateCheckbox.setId("indeterminate-checkbox");
    }

    private void addValueChangeCheckbox() {
        // begin-source-example
        // source-example-heading: Checkbox with a ValueChangeListener
        Checkbox valueChangeCheckbox = new Checkbox(
                "Checkbox with a ValueChangeListener");
        Div message = new Div();
        valueChangeCheckbox.addValueChangeListener(event -> {
            message.setText(
                    String.format("Checkbox value changed from '%s' to '%s'",
                            event.getOldValue(), event.getValue()));
        });
        // end-source-example
        addCard("Checkbox with a ValueChangeListener", valueChangeCheckbox,
                message);
        valueChangeCheckbox.setId("value-change-checkbox");
        message.setId("value-change-checkbox-message");
    }

    private void addAccessibleCheckbox() {
        // begin-source-example
        // source-example-heading: Checkbox with Custom Accessible Label
        Checkbox accessibleCheckbox = new Checkbox("Accessible Checkbox");
        accessibleCheckbox.setAriaLabel("Click me");
        // end-source-example
        addCard("Checkbox with Custom Accessible Label", accessibleCheckbox);
        accessibleCheckbox.setId("accessible-checkbox");
    }

    private void addCheckboxWithHtmlLabel() {
        // begin-source-example
        // source-example-heading: Checkbox with simple html markup in the label
        Checkbox checkbox = new Checkbox();
        checkbox.setLabelAsHtml(
                "Accept the <a href='https://vaadin.com/privacy-policy'>privacy policy</a>");
        // end-source-example
        addCard("Checkbox with simple html markup in the label", checkbox);
        checkbox.setId("html-checkbox");
    }
}
