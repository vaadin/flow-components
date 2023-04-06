/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.customfield.examples;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-custom-field/error")
public class Error extends Div {

    public Error() {
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
