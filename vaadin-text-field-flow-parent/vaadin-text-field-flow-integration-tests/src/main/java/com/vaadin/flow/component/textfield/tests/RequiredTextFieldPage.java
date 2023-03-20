
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/required-field")
public class RequiredTextFieldPage extends Div {

    public static class StringWrapper {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public RequiredTextFieldPage() {
        Div message = new Div();
        message.setId("message");

        StringWrapper model = new StringWrapper();
        Binder<StringWrapper> binder = new Binder<>();

        TextField textField = new TextField();
        textField.addValueChangeListener(event -> message.setText(String.format(
                "Value changed from '%s' to '%s'", event.getOldValue(),
                event.getValue(), textField.getValue())));

        binder.forField(textField).asRequired().bind(StringWrapper::getValue,
                StringWrapper::setValue);
        binder.setBean(model);

        add(textField, message);
    }
}
