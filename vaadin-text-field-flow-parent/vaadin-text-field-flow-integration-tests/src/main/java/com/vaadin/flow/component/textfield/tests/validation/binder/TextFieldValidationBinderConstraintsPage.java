/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.textfield.tests.validation.binder;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/validation/binder/constraints")
public class TextFieldValidationBinderConstraintsPage
        extends AbstractValidationBinderConstraintsPage<TextField, String> {
    public static final String BINDER_ERROR_MESSAGE = "BINDER_ERROR_MESSAGE";

    private String validValue;

    public TextFieldValidationBinderConstraintsPage() {
        super();

        binder.forField(field).withValidator(value -> value.equals(validValue),
                BINDER_ERROR_MESSAGE).bind("property");

        addInputControl("valid-value", "Set valid value for binder", event -> {
            validValue = event.getValue();
        });

        addInputControl("pattern", "Set pattern", event -> {
            field.setPattern(event.getValue());
        });

        addInputControl("min-length", "Set min length", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setMinLength(value);
        });

        addInputControl("max-length", "Set max length", event -> {
            int value = Integer.parseInt(event.getValue());
            field.setMaxLength(value);
        });
    }

    protected TextField getField() {
        return new TextField();
    }
}
