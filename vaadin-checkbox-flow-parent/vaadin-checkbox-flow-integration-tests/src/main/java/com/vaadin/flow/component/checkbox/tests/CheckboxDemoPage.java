package com.vaadin.flow.component.checkbox.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * View for {@link Checkbox} integration tests.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-checkbox-test-demo")
public class CheckboxDemoPage extends Div {

    public CheckboxDemoPage() {
        addDefaultCheckbox();
        addDisabledCheckbox();
        addIndeterminateCheckbox();
        addValueChangeCheckbox();
        addAccessibleCheckbox();
        addCheckboxHtmlLabel();
        addCheckboxLazyHtmlLabel();
    }

    private void addDefaultCheckbox() {
        // begin-source-example
        // source-example-heading: Default Checkbox
        Checkbox checkbox = new Checkbox();
        checkbox.setLabel("Default Checkbox");
        // end-source-example
        checkbox.setId("default-checkbox");

        NativeButton button = new NativeButton("Change label", event -> {
            checkbox.setLabel("New Label");
        });
        button.setId("change-default-label");

        addCard("Default Checkbox", checkbox, button);
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
        valueChangeCheckbox.addValueChangeListener(event -> message.setText(
                String.format("Checkbox value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
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

    private void addCheckboxHtmlLabel() {
        // begin-source-example
        // source-example-heading: Checkbox with the HTML-content based label
        Checkbox checkbox = new Checkbox();
        checkbox.setLabelAsHtml(
                "Accept the <a href='https://vaadin.com/privacy-policy'>privacy policy</a>");
        // end-source-example
        checkbox.setId("html-label-checkbox");

        NativeButton button = new NativeButton("Change label", event -> {
            checkbox.setLabelAsHtml(
                    "Accept the <a href='https://vaadin.com/community-terms'>community terms</a>");
        });
        button.setId("change-html-label");

        addCard("Checkbox with the label that contains HTML markup", checkbox,
                button);
    }

    private void addCheckboxLazyHtmlLabel() {
        Checkbox checkbox = new Checkbox();
        checkbox.setId("lazy-html-label-checkbox");

        NativeButton button = new NativeButton("Set label", event -> {
            checkbox.setLabelAsHtml(
                    "Accept the <a href='https://vaadin.com/privacy-policy'>privacy policy</a>");
        });
        button.setId("set-html-label");

        addCard("Checkbox with the lazy label that contains HTML markup",
                checkbox, button);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
