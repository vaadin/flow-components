/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.testutil.ValidationTestView;

/**
 * View for testing validation with {@link TextField}.
 */
@Route("vaadin-text-field/text-field-validation")
public class TextFieldValidationPage extends ValidationTestView {

    @Override
    protected HasValidation getValidationComponent() {
        return new TextField();
    }

}
