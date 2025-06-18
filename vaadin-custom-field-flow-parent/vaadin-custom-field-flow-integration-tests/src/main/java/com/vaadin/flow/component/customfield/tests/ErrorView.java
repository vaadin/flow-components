/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.customfield.tests;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field/error")
public class ErrorView extends Div {
    public ErrorView() {
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
