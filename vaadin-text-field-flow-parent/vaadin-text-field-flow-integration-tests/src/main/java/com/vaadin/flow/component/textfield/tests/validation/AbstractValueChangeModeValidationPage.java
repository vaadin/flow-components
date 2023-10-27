/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.tests.validation.AbstractValidationPage;

public abstract class AbstractValueChangeModeValidationPage<T extends TextFieldBase>
        extends AbstractValidationPage<T> {

    public static final String SET_EAGER_MODE_BUTTON = "set-eager-mode-button";
    public static final String SET_LAZY_MODE_BUTTON = "set-lazy-mode-button";
    public static final String SET_TIMEOUT_MODE_BUTTON = "set-timeout-mode-button";

    public static final String VALIDATION_LOG = "validation-log";
    public static final String RESET_VALIDATION_LOG_BUTTON = "reset-validation-log-button";

    public static final int VALUE_CHANGE_TIMEOUT = 600;

    private Div validationLog;

    public AbstractValueChangeModeValidationPage() {
        super();

        add(createButton(SET_EAGER_MODE_BUTTON, "Set eager mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.EAGER);
        }));

        add(createButton(SET_LAZY_MODE_BUTTON, "Set lazy mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.LAZY);
            testField.setValueChangeTimeout(VALUE_CHANGE_TIMEOUT);
        }));

        add(createButton(SET_TIMEOUT_MODE_BUTTON, "Set timeout mode", event -> {
            testField.setValueChangeMode(ValueChangeMode.TIMEOUT);
            testField.setValueChangeTimeout(VALUE_CHANGE_TIMEOUT);
        }));

        addValidationLog();
    }

    private void addValidationLog() {
        validationLog = new Div();
        validationLog.setId(VALIDATION_LOG);

        NativeButton resetValidationLogButton = createButton(
                RESET_VALIDATION_LOG_BUTTON, "Reset validation log", event -> {
                    resetValidationLog();
                });

        add(new Div(validationLog, resetValidationLogButton));
    }

    private void resetValidationLog() {
        validationLog.removeAll();
    }

    protected void logValidationResult(boolean isValid) {
        Div record = new Div();
        record.setText(isValid ? "valid" : "invalid");
        validationLog.add(record);
    }
}
