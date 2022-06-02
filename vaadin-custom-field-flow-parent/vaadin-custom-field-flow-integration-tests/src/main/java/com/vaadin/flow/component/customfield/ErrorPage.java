package com.vaadin.flow.component.customfield;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-custom-field/error")
public class ErrorPage extends Div {

    public ErrorPage() {
        CustomField<Integer> customField = new CustomField<Integer>() {
            @Override
            protected Integer generateModelValue() {
                return 0;
            }

            @Override
            protected void setPresentationValue(Integer integer) {
            }
        };
        customField.setLabel("My custom field");
        customField.setErrorMessage("My error message");
        add(customField);
    }
}
