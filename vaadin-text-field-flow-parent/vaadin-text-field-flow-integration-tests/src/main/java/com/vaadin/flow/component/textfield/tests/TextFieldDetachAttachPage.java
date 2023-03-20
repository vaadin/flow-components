
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link TextField}.
 */
@Route("vaadin-text-field/text-field-detach-attach")
public class TextFieldDetachAttachPage extends Div {

    /**
     * Constructs a basic layout with a text field.
     */
    public TextFieldDetachAttachPage() {
        TextField textField = new TextField();
        textField.setRequiredIndicatorVisible(true);
        textField.setId("text-field");
        add(textField);

        NativeButton toggleAttached = new NativeButton("toggle attached", e -> {
            if (textField.getParent().isPresent()) {
                remove(textField);
            } else {
                add(textField);
            }
        });
        toggleAttached.setId("toggle-attached");
        add(toggleAttached);
    }
}
