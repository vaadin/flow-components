
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/required-field-custom-validator")
public class RequiredTextFieldPageWithCustomServerSideValidator extends Div {

    public static class StringWrapper {
        private String value = "invalid";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public RequiredTextFieldPageWithCustomServerSideValidator() {
        StringWrapper model = new StringWrapper();
        Binder<StringWrapper> binder = new Binder<>();

        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);

        binder.forField(textField)
                .withValidator(s -> s.equals("Valid"), "Text is not Valid")
                .asRequired()
                .bind(StringWrapper::getValue, StringWrapper::setValue);
        binder.setBean(model);

        binder.validate();

        add(textField);
    }
}
